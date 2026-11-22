package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.*;
import com.ensam.bloodlink.exception.AccesInterditException;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.RendezVousRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final HopitalService hopitalService;
    private final DonneurService donneurService;
    private final EligibiliteService eligibiliteService;
    // un creneau avec le nombre de places restantes
    @Getter
    @AllArgsConstructor
    public static class Creneau {
        private LocalDateTime dateHeure;
        private long placesRestantes;
    }
    // creneaux d'une journee, de l'ouverture a la fermeture
    public List<Creneau> creneauxDisponibles(Long hopitalId, LocalDate jour) {
        Hopital h = hopitalService.trouverValide(hopitalId);
        // nombre de rdv deja pris pour chaque creneau du jour
        LocalDateTime debutJour = jour.atStartOfDay();
        Map<LocalDateTime, Long> pris = new HashMap<>();
        List<Object[]> lignes = rendezVousRepository.compterParCreneau(hopitalId, StatutRendezVous.PLANIFIE,
                debutJour, debutJour.plusDays(1));
        for (Object[] ligne : lignes) {
            pris.put((LocalDateTime) ligne[0], (Long) ligne[1]);
        }

        List<Creneau> creneaux = new ArrayList<>();
        LocalDateTime maintenant = LocalDateTime.now();
        LocalTime heure = h.getHeureOuverture();
        while (true) {
            LocalTime fin = heure.plusMinutes(h.getDureeCreneauMin());
            if (!fin.isAfter(heure) || fin.isAfter(h.getHeureFermeture())) {
                break;
            }
            LocalDateTime dateHeure = jour.atTime(heure);
            if (dateHeure.isAfter(maintenant)) {
                long restantes = h.getCapaciteParCreneau() - pris.getOrDefault(dateHeure, 0L);
                creneaux.add(new Creneau(dateHeure, Math.max(restantes, 0)));
            }
            heure = fin;
        }
        return creneaux;
    }

    // reservation d'un rdv (BF4, R7, R1)
    @Transactional
    public RendezVous reserver(Long donneurId, Long hopitalId, LocalDateTime dateHeure) {
        Donneur donneur = donneurService.trouver(donneurId);
        Hopital h = hopitalService.trouverValide(hopitalId);

        if (!dateHeure.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Le rendez-vous doit etre dans le futur.");
        }

        // l'heure doit correspondre a un creneau pendant les horaires d'ouverture
        LocalTime heure = dateHeure.toLocalTime();
        long minutes = Duration.between(h.getHeureOuverture(), heure).toMinutes();
        if (minutes < 0 || minutes % h.getDureeCreneauMin() != 0
                || heure.plusMinutes(h.getDureeCreneauMin()).isAfter(h.getHeureFermeture())) {
            throw new BusinessException("Cette heure est en dehors des horaires de l'hopital.");
        }
        long pris = rendezVousRepository.countByHopitalIdAndDateHeureAndStatut(hopitalId, dateHeure,
                StatutRendezVous.PLANIFIE);
        if (pris >= h.getCapaciteParCreneau()) {
            throw new BusinessException("Ce creneau est complet.");
        }
        EligibiliteService.Resultat r = eligibiliteService.verifier(donneur, dateHeure.toLocalDate());
        if (!r.eligible()) {
            throw new BusinessException("Vous n'etes pas eligible a cette date : " + r.motif());
        }
        if (rendezVousRepository.existsByDonneurIdAndDateHeureAndStatut(donneurId, dateHeure,
                StatutRendezVous.PLANIFIE)) {
            throw new BusinessException("Vous avez deja un rendez-vous a cette heure.");
        }
        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(dateHeure);
        rdv.setStatut(StatutRendezVous.PLANIFIE);
        rdv.setDonneur(donneur);
        rdv.setHopital(h);
        return rendezVousRepository.save(rdv);
    }
    // annulation par le donneur (BF5)
    @Transactional
    public void annuler(Long rendezVousId, Long donneurId) {
        RendezVous rdv = rendezVousRepository.findById(rendezVousId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable"));
        if (!rdv.getDonneur().getId().equals(donneurId)) {
            throw new AccesInterditException("Ce rendez-vous ne vous appartient pas.");
        }
        if (rdv.getStatut() != StatutRendezVous.PLANIFIE || !rdv.getDateHeure().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Seul un rendez-vous planifie et a venir peut etre annule.");
        }
        rdv.setStatut(StatutRendezVous.ANNULE);
    }
    public List<RendezVous> historique(Long donneurId) {
        return rendezVousRepository.findByDonneurIdOrderByDateHeureDesc(donneurId);
    }
    // rdv d'un hopital pour le medecin, filtre par statut (BF10)
    public Page<RendezVous> pourHopital(Long hopitalId, StatutRendezVous statut, int page) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("dateHeure").descending());
        if (statut == null) {
            return rendezVousRepository.findByHopitalId(hopitalId, pageable);
        }
        return rendezVousRepository.findByHopitalIdAndStatut(hopitalId, statut, pageable);
    }
}