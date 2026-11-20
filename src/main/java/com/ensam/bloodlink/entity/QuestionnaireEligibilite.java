package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "questionnaire_eligibilite")
@Getter @Setter @NoArgsConstructor
public class QuestionnaireEligibilite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poids_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal poidsKg;

    @Column(precision = 4, scale = 1)
    private BigDecimal hemoglobine;

    @Column(name = "tension_systolique")
    private Integer tensionSystolique;

    @Column(name = "voyage_recent", nullable = false)
    private boolean voyageRecent;

    @Column(name = "tatouage_recent", nullable = false)
    private boolean tatouageRecent;

    @Column(name = "sous_traitement", nullable = false)
    private boolean sousTraitement;

    @Column(nullable = false)
    private boolean eligible;

    // obligatoire si eligible = false
    @Column(name = "motif_refus", length = 255)
    private String motifRefus;

    @OneToOne(optional = false)
    @JoinColumn(name = "don_id", nullable = false, unique = true)
    private Don don;
}