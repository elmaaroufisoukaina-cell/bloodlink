package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.*;
import com.ensam.bloodlink.exception.AccesInterditException;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.DemandeSangRepository;
import com.ensam.bloodlink.repository.GroupeSanguinRepository;
import com.ensam.bloodlink.repository.PocheSanguineRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DemandeTraitementService {

    private final DemandeSangRepository demandes;
    private final PocheSanguineRepository poches;
    private final GroupeSanguinRepository groupes;
    private final ExpirationService expirationService;

    // ce que l'ecran propose pour une ligne : les poches candidates et celles pre-cochees
    @Getter
    @AllArgsConstructor
    public static class Proposition {
        private LigneDemande ligne;
        private long restant;                 // quantite encore a servir
        private List<PocheSanguine> candidates; // compatibles, la plus proche de la peremption d'abord
        private Set<Long> preselection;       // les poches cochees par defaut

        // quantite que l'hopital ne peut pas fournir
        public long getManquant() {
            return Math.max(0, restant - preselection.size());
        }
    }

    // F8 : demandes recues, filtrees par urgence, paginees
    public Page<DemandeSang> recues(Long hopitalId, Urgence urgence, int page) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("dateDemande").descending());
        if (urgence == null) {
            return demandes.findByHopitalFournisseurId(hopitalId, pageable);
        }
        return demandes.findRecuesParUrgence(hopitalId, urgence, pageable);
    }

    // R11 : seul l'hopital fournisseur voit et traite la demande
    public DemandeSang trouverRecue(Long id, Long hopitalId) {
        DemandeSang d = demandes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));
        if (!d.getHopitalFournisseur().getId().equals(hopitalId)) {
            throw new AccesInterditException("Cette demande ne concerne pas votre hopital.");
        }
        return d;
    }

    // R3 + R4 : pour chaque ligne, les poches compatibles les plus proches de la peremption
    public List<Proposition> propositions(DemandeSang demande) {
        LocalDate aujourdhui = LocalDate.now();
        Set<Long> dejaProposees = new HashSet<>();   // une poche n'est pre-cochee que pour une seule ligne
        List<Proposition> resultat = new ArrayList<>();

        for (LigneDemande ligne : demande.getLignes()) {
            long restant = ligne.getQuantite() - poches.countByLigneDemandeId(ligne.getId());

            List<Long> idsCompatibles = groupes.findDonneursCompatibles(ligne.getGroupeSanguin().getId())
                    .stream().map(GroupeSanguin::getId).toList();

            // deja triees par date de peremption croissante (R4) et sans poche perimee (R5)
            List<PocheSanguine> candidates = poches.findCompatibles(
                    demande.getHopitalFournisseur().getId(), StatutPoche.DISPONIBLE,
                    ligne.getTypeProduit(), aujourdhui, idsCompatibles);

            Set<Long> preselection = new LinkedHashSet<>();
            for (PocheSanguine p : candidates) {
                if (preselection.size() >= restant) {
                    break;
                }
                if (dejaProposees.add(p.getId())) {
                    preselection.add(p.getId());
                }
            }
            resultat.add(new Proposition(ligne, Math.max(restant, 0), candidates, preselection));
        }
        return resultat;
    }

    // BF18 : "choix" = liste de "idLigne:idPoche" cochees par l'admin
    @Transactional
    public DemandeSang accepter(Long demandeId, Long hopitalId, List<String> choix) {
        // R5 : on perime d'abord les poches expirees (avant de charger la demande)
        expirationService.perimerPochesExpirees();

        DemandeSang demande = trouverRecue(demandeId, hopitalId);
        if (demande.getStatut() == StatutDemande.SERVIE || demande.getStatut() == StatutDemande.REFUSEE) {
            throw new BusinessException("Cette demande est deja cloturee.");
        }
        if (choix == null || choix.isEmpty()) {
            throw new BusinessException("Selectionnez au moins une poche a envoyer.");
        }

        Set<Long> dejaUtilisees = new HashSet<>();
        for (String c : choix) {
            String[] morceaux = c.split(":");
            if (morceaux.length != 2) {
                throw new BusinessException("Selection invalide.");
            }
            Long ligneId = Long.valueOf(morceaux[0]);
            Long pocheId = Long.valueOf(morceaux[1]);

            LigneDemande ligne = demande.getLignes().stream()
                    .filter(l -> l.getId().equals(ligneId)).findFirst()
                    .orElseThrow(() -> new BusinessException("Ligne de demande invalide."));
            PocheSanguine poche = poches.findById(pocheId)
                    .orElseThrow(() -> new BusinessException("Poche introuvable."));

            if (!dejaUtilisees.add(pocheId)) {
                throw new BusinessException("Une poche ne peut etre envoyee qu'une seule fois.");
            }
            if (!poche.getHopital().getId().equals(hopitalId)) {
                throw new AccesInterditException("Cette poche appartient a un autre hopital.");
            }
            // R5 : une poche perimee ou deja servie n'est jamais servie
            if (poche.getStatut() != StatutPoche.DISPONIBLE
                    || poche.getDatePeremption().isBefore(LocalDate.now())) {
                throw new BusinessException("La poche " + poche.getNumeroSerie() + " n'est plus disponible.");
            }
            if (poche.getTypeProduit() != ligne.getTypeProduit()) {
                throw new BusinessException("La poche " + poche.getNumeroSerie() + " n'est pas du produit demande.");
            }
            // R3 : le groupe de la poche doit pouvoir donner au groupe demande
            Long groupePoche = poche.getDon().getDonneur().getGroupeSanguin().getId();
            boolean compatible = groupes.findDonneursCompatibles(ligne.getGroupeSanguin().getId())
                    .stream().anyMatch(g -> g.getId().equals(groupePoche));
            if (!compatible) {
                throw new BusinessException("La poche " + poche.getNumeroSerie() + " n'est pas compatible.");
            }

            // decision 3 : la poche reste dans le stock du fournisseur, statut SERVIE + ligne renseignee
            poche.setStatut(StatutPoche.SERVIE);
            poche.setLigneDemande(ligne);

            // on ne peut pas servir plus que la quantite demandee (le count voit la modification : flush auto)
            if (poches.countByLigneDemandeId(ligne.getId()) > ligne.getQuantite()) {
                throw new BusinessException("Trop de poches selectionnees pour la ligne " + ligne.getId() + ".");
            }
        }

        recalculerStatut(demande);
        return demande;
    }

    // R6 : EN_ATTENTE si rien servi, PARTIELLEMENT_SERVIE si une partie, SERVIE si tout est complet
    private void recalculerStatut(DemandeSang demande) {
        boolean toutServi = true;
        boolean auMoinsUne = false;
        for (LigneDemande ligne : demande.getLignes()) {
            long servi = poches.countByLigneDemandeId(ligne.getId());
            if (servi > 0) {
                auMoinsUne = true;
            }
            if (servi < ligne.getQuantite()) {
                toutServi = false;
            }
        }
        if (toutServi) {
            demande.setStatut(StatutDemande.SERVIE);
        } else if (auMoinsUne) {
            demande.setStatut(StatutDemande.PARTIELLEMENT_SERVIE);
        } else {
            demande.setStatut(StatutDemande.EN_ATTENTE);
        }
    }

    // BF19 + R11 : un refus cloture la demande, le demandeur en creera une nouvelle
    @Transactional
    public void refuser(Long demandeId, Long hopitalId) {
        DemandeSang demande = trouverRecue(demandeId, hopitalId);
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BusinessException("Seule une demande en attente peut etre refusee.");
        }
        demande.setStatut(StatutDemande.REFUSEE);
    }
}