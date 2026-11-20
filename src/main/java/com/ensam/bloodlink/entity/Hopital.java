package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Entity
@Table(name = "hopital")
@Getter @Setter @NoArgsConstructor
public class Hopital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nom;

    @Size(max = 255)
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String ville;

    @Size(max = 20)
    private String telephone;

    @Email(message = "E-mail invalide")
    @Size(max = 150)
    private String email;

    // R4 de la section "decisions" : seuls les hopitaux valides apparaissent dans les recherches
    @Column(nullable = false)
    private boolean valide = false;

    @NotNull(message = "L'heure d'ouverture est obligatoire")
    @DateTimeFormat(pattern = "HH:mm")
    @Column(name = "heure_ouverture", nullable = false)
    private LocalTime heureOuverture;

    @NotNull(message = "L'heure de fermeture est obligatoire")
    @DateTimeFormat(pattern = "HH:mm")
    @Column(name = "heure_fermeture", nullable = false)
    private LocalTime heureFermeture;

    @NotNull
    @Min(value = 1, message = "La duree du creneau doit etre positive")
    @Column(name = "duree_creneau_min", nullable = false)
    private Integer dureeCreneauMin;

    @NotNull
    @Min(value = 1, message = "La capacite doit etre positive")
    @Column(name = "capacite_par_creneau", nullable = false)
    private Integer capaciteParCreneau;
}