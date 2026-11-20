package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parametre_systeme")
@Getter @Setter @NoArgsConstructor
public class ParametreSysteme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String cle;

    @Column(nullable = false, length = 100)
    private String valeur;

    @Column(length = 255)
    private String description;
}