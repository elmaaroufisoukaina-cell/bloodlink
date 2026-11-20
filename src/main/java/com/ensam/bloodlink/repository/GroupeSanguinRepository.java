package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.GroupeSanguin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupeSanguinRepository extends JpaRepository<GroupeSanguin, Long> {

    Optional<GroupeSanguin> findByCode(String code);

    // R3 : tous les groupes qui peuvent donner au groupe receveur (lui-meme inclus)
    @Query("select g from GroupeSanguin g join g.peutDonnerA r where r.id = :receveurId")
    List<GroupeSanguin> findDonneursCompatibles(@Param("receveurId") Long receveurId);
}