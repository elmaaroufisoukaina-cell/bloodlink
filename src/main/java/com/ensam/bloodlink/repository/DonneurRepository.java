package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.Donneur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DonneurRepository extends JpaRepository<Donneur, Long> {

    Optional<Donneur> findByCin(String cin);

    boolean existsByCin(String cin);

    // F8 : recherche par groupe (0 = tous) et par ville (vide = toutes)
    @Query("select d from Donneur d where (:groupeId = 0 or d.groupeSanguin.id = :groupeId) "
            + "and lower(coalesce(d.ville, '')) like lower(concat('%', :ville, '%'))")
    Page<Donneur> rechercher(@Param("groupeId") long groupeId, @Param("ville") String ville, Pageable pageable);

    // R9 : donneurs des groupes compatibles dont le prochain don est possible a cette date
    @Query("select d from Donneur d where d.groupeSanguin.id in :groupeIds "
            + "and (d.dateProchainDon is null or d.dateProchainDon <= :date) "
            + "order by d.ville, d.nom")
    List<Donneur> findPotentiels(@Param("groupeIds") Collection<Long> groupeIds, @Param("date") LocalDate date);
}