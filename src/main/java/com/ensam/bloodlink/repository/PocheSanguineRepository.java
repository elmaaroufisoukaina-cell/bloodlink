package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.PocheSanguine;
import com.ensam.bloodlink.entity.StatutPoche;
import com.ensam.bloodlink.entity.TypeProduit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PocheSanguineRepository extends JpaRepository<PocheSanguine, Long> {

    Optional<PocheSanguine> findByNumeroSerie(String numeroSerie);

    Page<PocheSanguine> findByHopitalId(Long hopitalId, Pageable pageable);

    Page<PocheSanguine> findByHopitalIdAndStatut(Long hopitalId, StatutPoche statut, Pageable pageable);

    // quantite servie d'une ligne de demande (R6)
    long countByLigneDemandeId(Long ligneDemandeId);

    long countByStatut(StatutPoche statut);

    long countByStatutAndDatePeremptionBetween(StatutPoche statut, LocalDate debut, LocalDate fin);

    // BF7 : poches servies issues des dons d'un donneur
    List<PocheSanguine> findByDon_Donneur_IdAndStatut(Long donneurId, StatutPoche statut);

    // stock disponible d'un hopital, par groupe sanguin (R9, jauges)
    @Query("select g.id, count(p) from PocheSanguine p join p.don d join d.donneur dn join dn.groupeSanguin g "
            + "where p.hopital.id = :hopitalId and p.statut = :statut and p.datePeremption >= :today "
            + "group by g.id")
    List<Object[]> compterParGroupe(@Param("hopitalId") Long hopitalId,
                                    @Param("statut") StatutPoche statut,
                                    @Param("today") LocalDate today);

    // meme chose pour tout le reseau (tableau de bord admin systeme)
    @Query("select g.id, count(p) from PocheSanguine p join p.don d join d.donneur dn join dn.groupeSanguin g "
            + "where p.statut = :statut and p.datePeremption >= :today "
            + "group by g.id")
    List<Object[]> compterParGroupeReseau(@Param("statut") StatutPoche statut,
                                          @Param("today") LocalDate today);

    // R3 + R4 : poches compatibles disponibles, la plus proche de la peremption en premier
    @Query("select p from PocheSanguine p join p.don d join d.donneur dn "
            + "where p.hopital.id = :hopitalId and p.statut = :statut and p.typeProduit = :type "
            + "and p.datePeremption >= :today and dn.groupeSanguin.id in :groupeIds "
            + "order by p.datePeremption asc")
    List<PocheSanguine> findCompatibles(@Param("hopitalId") Long hopitalId,
                                        @Param("statut") StatutPoche statut,
                                        @Param("type") TypeProduit type,
                                        @Param("today") LocalDate today,
                                        @Param("groupeIds") Collection<Long> groupeIds);

    // BF15 : stock disponible des autres hopitaux valides (lecture seule)
    @Query("select p from PocheSanguine p where p.hopital.valide = true and p.hopital.id <> :hopitalId "
            + "and p.statut = :statut and p.datePeremption >= :today")
    List<PocheSanguine> findStockAutresHopitaux(@Param("hopitalId") Long hopitalId,
                                                @Param("statut") StatutPoche statut,
                                                @Param("today") LocalDate today);

    // R5 / BF24 : les poches dont la date de peremption est depassee deviennent PERIMEE
    @Modifying(clearAutomatically = true)
    @Query("update PocheSanguine p set p.statut = :perimee where p.statut = :disponible and p.datePeremption < :today")
    int perimerPochesExpirees(@Param("perimee") StatutPoche perimee,
                              @Param("disponible") StatutPoche disponible,
                              @Param("today") LocalDate today);
}