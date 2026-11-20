package com.ensam.bloodlink.repository;

import com.ensam.bloodlink.entity.ParametreSysteme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametreSystemeRepository extends JpaRepository<ParametreSysteme, Long> {

    Optional<ParametreSysteme> findByCle(String cle);
}