package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.LigneDemande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneDemandeRepository extends JpaRepository<LigneDemande, Long> {
}