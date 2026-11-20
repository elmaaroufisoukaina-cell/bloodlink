package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demande_sang")
@Getter @Setter @NoArgsConstructor
public class DemandeSang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(length = 255)
    private String commentaire;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hopital_demandeur_id", nullable = false)
    private Hopital hopitalDemandeur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hopital_fournisseur_id", nullable = false)
    private Hopital hopitalFournisseur;

    // admin d'hopital qui a envoye la demande
    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneDemande> lignes = new ArrayList<>();
}