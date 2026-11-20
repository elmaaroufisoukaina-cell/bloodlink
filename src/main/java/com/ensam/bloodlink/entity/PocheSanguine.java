package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "poche_sanguine")
@Getter @Setter @NoArgsConstructor
public class PocheSanguine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 50)
    private String numeroSerie;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_produit", nullable = false)
    private TypeProduit typeProduit;

    @Column(name = "volume_ml", nullable = false)
    private Integer volumeMl;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "date_prelevement", nullable = false)
    private LocalDate datePrelevement;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "date_peremption", nullable = false)
    private LocalDate datePeremption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPoche statut = StatutPoche.DISPONIBLE;

    // le groupe sanguin de la poche = groupe du donneur du don (non duplique)
    @ManyToOne(optional = false)
    @JoinColumn(name = "don_id", nullable = false)
    private Don don;

    // hopital qui detient la poche (ne change pas quand elle est servie)
    @ManyToOne(optional = false)
    @JoinColumn(name = "hopital_id", nullable = false)
    private Hopital hopital;

    // renseigne quand la poche est servie (tracabilite, R10)
    @ManyToOne
    @JoinColumn(name = "ligne_demande_id")
    private LigneDemande ligneDemande;
}