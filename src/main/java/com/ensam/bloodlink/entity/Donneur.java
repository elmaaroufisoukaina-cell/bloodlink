package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "donneur")
@Getter @Setter @NoArgsConstructor
public class Donneur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le CIN est obligatoire")
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String cin;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nom;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String prenom;

    @NotNull
    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexe sexe;

    @NotNull
    @DecimalMin(value = "1.0", message = "Le poids doit etre positif")
    @Column(name = "poids_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal poidsKg;

    @Size(max = 20)
    private String telephone;

    @Size(max = 100)
    private String ville;

    // R2 : date du dernier don + delai selon le sexe, recalculee apres chaque don
    @Column(name = "date_prochain_don")
    private LocalDate dateProchainDon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "groupe_sanguin_id", nullable = false)
    private GroupeSanguin groupeSanguin;
}