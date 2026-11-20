package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.DemandeSang;
import com.ensam.bloodlink.entity.Urgence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandeSangRepository extends JpaRepository<DemandeSang, Long> {

    // demandes envoyees par un hopital
    Page<DemandeSang> findByHopitalDemandeurId(Long hopitalId, Pageable pageable);

    @Query(value = "select distinct d from DemandeSang d join d.lignes l "
            + "where d.hopitalDemandeur.id = :hopitalId and l.urgence = :urgence",
            countQuery = "select count(distinct d) from DemandeSang d join d.lignes l "
                    + "where d.hopitalDemandeur.id = :hopitalId and l.urgence = :urgence")
    Page<DemandeSang> findEnvoyeesParUrgence(@Param("hopitalId") Long hopitalId,
                                             @Param("urgence") Urgence urgence, Pageable pageable);

    // demandes recues par un hopital fournisseur
    Page<DemandeSang> findByHopitalFournisseurId(Long hopitalId, Pageable pageable);

    @Query(value = "select distinct d from DemandeSang d join d.lignes l "
            + "where d.hopitalFournisseur.id = :hopitalId and l.urgence = :urgence",
            countQuery = "select count(distinct d) from DemandeSang d join d.lignes l "
                    + "where d.hopitalFournisseur.id = :hopitalId and l.urgence = :urgence")
    Page<DemandeSang> findRecuesParUrgence(@Param("hopitalId") Long hopitalId,
                                           @Param("urgence") Urgence urgence, Pageable pageable);
}
