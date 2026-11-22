package com.ensam.bloodlink.service;

import com.ensam.bloodlink.dto.DemandeForm;
import com.ensam.bloodlink.dto.LigneForm;
import com.ensam.bloodlink.entity.*;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.DemandeSangRepository;
import com.ensam.bloodlink.repository.GroupeSanguinRepository;
import com.ensam.bloodlink.repository.PocheSanguineRepository;
import com.ensam.bloodlink.repository.UtilisateurRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DemandeEnvoiService {

    private final PocheSanguineRepository pocheRepository;
    private final DemandeSangRepository demandeRepository;
    private final GroupeSanguinRepository groupeRepository;
    private final GroupeSanguinService groupeService;
    private final HopitalService hopitalService;
    private final UtilisateurRepository utilisateurRepository;

    //une ligne du resultat:hopital+groupe+produit+nombre de poches
    @Getter
    @AllArgsConstructor
    public static class StockLigne {
        private Hopital hopital;
        private String groupe;
        private TypeProduit type;
        private long quantite;
    }

    // recherche dans le stock des autres hopitaux (BF15, R3)
    // groupeId = 0 :tous les groupes,type = null:tous les produits
    public List<StockLigne> rechercherStock(Long monHopitalId, long groupeId, TypeProduit type, String ville) {
        Set<Long> compatibles = null;
        if (groupeId != 0) {
            compatibles = new HashSet<>();
            for (GroupeSanguin g : groupeRepository.findDonneursCompatibles(groupeId)) {
                compatibles.add(g.getId());
            }
        }
        String villeCherchee = (ville == null) ? "" : ville.trim().toLowerCase();

        List<PocheSanguine> poches = pocheRepository.findStockAutresHopitaux(monHopitalId,
                StatutPoche.DISPONIBLE, LocalDate.now());

        // on regroupe les poches par hopital, groupe et produit
        Map<String, StockLigne> resultat = new LinkedHashMap<>();
        for (PocheSanguine p : poches) {
            GroupeSanguin g = p.getDon().getDonneur().getGroupeSanguin();
            if (compatibles != null && !compatibles.contains(g.getId())) {
                continue;
            }
            if (type != null && p.getTypeProduit() != type) {
                continue;
            }
            if (!villeCherchee.isEmpty() && !p.getHopital().getVille().toLowerCase().contains(villeCherchee)) {
                continue;
            }
            String cle = p.getHopital().getId() + "-" + g.getCode() + "-" + p.getTypeProduit();
            StockLigne ligne = resultat.get(cle);
            if (ligne == null) {
                resultat.put(cle, new StockLigne(p.getHopital(), g.getCode(), p.getTypeProduit(), 1));
            } else {
                ligne.quantite++;
            }
        }
        List<StockLigne> liste = new ArrayList<>(resultat.values());
        liste.sort(Comparator.comparing((StockLigne l) -> l.getHopital().getNom()).thenComparing(StockLigne::getGroupe));
        return liste;
    }
    // envoi d'une demande
    @Transactional
    public DemandeSang envoyer(DemandeForm f, Long utilisateurId, Long monHopitalId) {
        if (f.getFournisseurId().equals(monHopitalId)) {
            throw new BusinessException("L'hopital fournisseur doit etre different de votre hopital.");
        }
        Hopital fournisseur = hopitalService.trouverValide(f.getFournisseurId());
        Hopital demandeur = hopitalService.trouver(monHopitalId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        DemandeSang demande = new DemandeSang();
        demande.setDateDemande(LocalDateTime.now());
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setCommentaire(f.getCommentaire());
        demande.setHopitalDemandeur(demandeur);
        demande.setHopitalFournisseur(fournisseur);
        demande.setUtilisateur(utilisateur);

        for (LigneForm lf : f.getLignes()) {
            if (lf.getQuantite() == null || lf.getQuantite() <= 0) {
                continue;
            }
            if (lf.getGroupeId() == null || lf.getTypeProduit() == null || lf.getUrgence() == null) {
                throw new BusinessException("Chaque ligne doit avoir un groupe, un produit et une urgence.");
            }
            LigneDemande ligne = new LigneDemande();
            ligne.setTypeProduit(lf.getTypeProduit());
            ligne.setQuantite(lf.getQuantite());
            ligne.setUrgence(lf.getUrgence());
            ligne.setGroupeSanguin(groupeService.trouver(lf.getGroupeId()));
            ligne.setDemande(demande);
            demande.getLignes().add(ligne);
        }
        if (demande.getLignes().isEmpty()) {
            throw new BusinessException("Ajoutez au moins une ligne avec une quantite.");
        }
        return demandeRepository.save(demande);
    }
    // demandes envoyees par mon hopital
    public Page<DemandeSang> envoyees(Long hopitalId, Urgence urgence, int page) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("dateDemande").descending());
        if (urgence == null) {
            return demandeRepository.findByHopitalDemandeurId(hopitalId, pageable);
        }
        return demandeRepository.findEnvoyeesParUrgence(hopitalId, urgence, pageable);
    }
}