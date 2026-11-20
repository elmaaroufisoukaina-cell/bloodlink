package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.Don;
import com.ensam.bloodlink.entity.StatutDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonRepository extends JpaRepository<Don, Long> {

    List<Don> findByDonneurIdOrderByDateDonDesc(Long donneurId);

    Page<Don> findByHopitalId(Long hopitalId, Pageable pageable);

    List<Don> findByHopitalIdAndStatutOrderByDateDonDesc(Long hopitalId, StatutDon statut);

    // tableau de bord : nombre de dons realises par mois
    @Query("select year(d.dateDon), month(d.dateDon), count(d) from Don d where d.statut = :statut "
            + "group by year(d.dateDon), month(d.dateDon) order by year(d.dateDon), month(d.dateDon)")
    List<Object[]> compterParMois(@Param("statut") StatutDon statut);
}