package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.Hopital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HopitalRepository extends JpaRepository<Hopital, Long> {

    List<Hopital> findByValideTrueOrderByNom();

    List<Hopital> findByValideTrueAndVilleContainingIgnoreCaseOrderByNom(String ville);
}