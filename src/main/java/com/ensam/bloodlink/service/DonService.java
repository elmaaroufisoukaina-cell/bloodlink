package com.ensam.bloodlink.service;

import com.ensam.bloodlink.dto.DonForm;
import com.ensam.bloodlink.entity.*;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonService {

    private final DonneurRepository donneurs;
    private final UtilisateurRepository utilisateurs;
    private final DonRepository dons;
    private final PocheSanguineRepository poches;
    private final RendezVousRepository rendezVous;
    private final EligibiliteService eligibilite;
    private final ParametreService parametres;

    // BF8 + BF9 : un don exige toujours un questionnaire (R8)
    @Transactional
    public Don enregistrer(DonForm f, Long medecinId) {
        Utilisateur medecin = utilisateurs.findById(medecinId)
                .orElseThrow(() -> new ResourceNotFoundException("Medecin introuvable"));
        Donneur donneur = donneurs.findByCin(f.getCin().trim().toUpperCase())
                .orElseThrow(() -> new BusinessException("Aucun donneur avec ce CIN."));
        Hopital hopital = medecin.getHopital();
        LocalDate aujourdhui = LocalDate.now();

        // le poids mesure ce jour-la remplace celui du profil
        donneur.setPoidsKg(f.getPoidsKg());

        List<String> motifs = evaluer(f, donneur, aujourdhui);
        boolean eligible = motifs.isEmpty();

        Don don = new Don();
        don.setDateDon(aujourdhui);
        don.setTypeDon(f.getTypeDon());
        don.setVolumeMl(f.getVolumeMl());
        don.setStatut(eligible ? StatutDon.REALISE : StatutDon.REFUSE);
        don.setDonneur(donneur);
        don.setHopital(hopital);
        don.setMedecin(medecin);

        QuestionnaireEligibilite q = new QuestionnaireEligibilite();
        q.setPoidsKg(f.getPoidsKg());
        q.setHemoglobine(f.getHemoglobine());
        q.setTensionSystolique(f.getTensionSystolique());
        q.setVoyageRecent(f.isVoyageRecent());
        q.setTatouageRecent(f.isTatouageRecent());
        q.setSousTraitement(f.isSousTraitement());
        q.setEligible(eligible);
        if (!eligible) {
            String motif = String.join(" ; ", motifs);
            motif = Character.toUpperCase(motif.charAt(0)) + motif.substring(1);
            q.setMotifRefus(motif.length() > 255 ? motif.substring(0, 255) : motif);
        }
        q.setDon(don);
        don.setQuestionnaire(q);   // cascade : le questionnaire est enregistre avec le don

        dons.save(don);

        if (eligible) {
            genererPoches(don);
            // R2 : prochaine date de don = aujourd'hui + delai selon le sexe
            donneur.setDateProchainDon(eligibilite.calculerProchainDon(aujourdhui, donneur.getSexe()));
        }

        marquerRendezVousHonore(donneur, hopital, aujourdhui);
        return don;
    }

    // retourne la liste des motifs de refus (liste vide = eligible)
    private List<String> evaluer(DonForm f, Donneur donneur, LocalDate date) {
        List<String> motifs = new ArrayList<>();

        // R1 : age, poids, delai depuis le dernier don
        EligibiliteService.Resultat base = eligibilite.verifier(donneur, date);
        if (!base.eligible()) {
            motifs.add(base.motif());
        }
        BigDecimal hbMin = (donneur.getSexe() == Sexe.HOMME) ? new BigDecimal("13.0") : new BigDecimal("12.0");
        if (f.getHemoglobine() != null && f.getHemoglobine().compareTo(hbMin) < 0) {
            motifs.add("hemoglobine trop basse (" + f.getHemoglobine() + " g/dl)");
        }
        Integer tension = f.getTensionSystolique();
        if (tension != null && (tension < 100 || tension > 180)) {
            motifs.add("tension systolique hors limites (" + tension + ")");
        }
        if (f.isVoyageRecent()) {
            motifs.add("voyage recent en zone a risque");
        }
        if (f.isTatouageRecent()) {
            motifs.add("tatouage recent");
        }
        if (f.isSousTraitement()) {
            motifs.add("donneur sous traitement");
        }
        return motifs;
    }

    // chaque produit a sa propre date de peremption (durees lues dans parametre_systeme)
    private void genererPoches(Don don) {
        LocalDate jour = don.getDateDon();
        if (don.getTypeDon() == TypeDon.SANG_TOTAL) {
            creerPoche(don, TypeProduit.GLOBULES_ROUGES, "GR", 280,
                    jour.plusDays(parametres.getInt("DUREE_GLOBULES_JOURS", 42)));
        }
        creerPoche(don, TypeProduit.PLASMA, "PL", 250,
                jour.plusDays(parametres.getInt("DUREE_PLASMA_JOURS", 365)));
        creerPoche(don, TypeProduit.PLAQUETTES, "PQ", 200,
                jour.plusDays(parametres.getInt("DUREE_PLAQUETTES_JOURS", 5)));
    }

    private void creerPoche(Don don, TypeProduit type, String code, int volume, LocalDate peremption) {
        PocheSanguine p = new PocheSanguine();
        p.setNumeroSerie("PS-" + don.getId() + "-" + code);
        p.setTypeProduit(type);
        p.setVolumeMl(volume);
        p.setDatePrelevement(don.getDateDon());
        p.setDatePeremption(peremption);
        p.setStatut(StatutPoche.DISPONIBLE);
        p.setDon(don);
        p.setHopital(don.getHopital());
        poches.save(p);
    }

    // decision 2 : le rendez-vous PLANIFIE du meme donneur, meme hopital, meme jour devient HONORE
    private void marquerRendezVousHonore(Donneur donneur, Hopital hopital, LocalDate jour) {
        LocalDateTime debut = jour.atStartOfDay();
        LocalDateTime fin = debut.plusDays(1).minusSeconds(1);
        for (RendezVous rdv : rendezVous.findByDonneurIdAndHopitalIdAndStatutAndDateHeureBetween(
                donneur.getId(), hopital.getId(), StatutRendezVous.PLANIFIE, debut, fin)) {
            rdv.setStatut(StatutRendezVous.HONORE);
        }
    }

    // dons enregistres dans un hopital, du plus recent au plus ancien
    public Page<Don> listerPourHopital(Long hopitalId, int page) {
        return dons.findByHopitalId(hopitalId, PageRequest.of(page, 10, Sort.by("dateDon").descending()));
    }
}