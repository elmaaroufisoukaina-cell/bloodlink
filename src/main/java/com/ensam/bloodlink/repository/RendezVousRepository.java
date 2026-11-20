package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.RendezVous;
import com.ensam.bloodlink.entity.StatutRendezVous;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    // R7 : capacite d'un creneau = nombre de rendez-vous PLANIFIE sur ce creneau
    long countByHopitalIdAndDateHeureAndStatut(Long hopitalId, LocalDateTime dateHeure, StatutRendezVous statut);

    boolean existsByDonneurIdAndDateHeureAndStatut(Long donneurId, LocalDateTime dateHeure, StatutRendezVous statut);

    List<RendezVous> findByDonneurIdOrderByDateHeureDesc(Long donneurId);

    Page<RendezVous> findByHopitalId(Long hopitalId, Pageable pageable);

    Page<RendezVous> findByHopitalIdAndStatut(Long hopitalId, StatutRendezVous statut, Pageable pageable);

    // pour marquer le rendez-vous HONORE quand le medecin enregistre le don
    List<RendezVous> findByDonneurIdAndHopitalIdAndStatutAndDateHeureBetween(
            Long donneurId, Long hopitalId, StatutRendezVous statut, LocalDateTime debut, LocalDateTime fin);

    // un seul appel pour connaitre le nombre de rendez-vous de chaque creneau d'une journee
    @Query("select r.dateHeure, count(r) from RendezVous r "
            + "where r.hopital.id = :hopitalId and r.statut = :statut "
            + "and r.dateHeure >= :debut and r.dateHeure < :fin group by r.dateHeure")
    List<Object[]> compterParCreneau(@Param("hopitalId") Long hopitalId,
                                     @Param("statut") StatutRendezVous statut,
                                     @Param("debut") LocalDateTime debut,
                                     @Param("fin") LocalDateTime fin);
}