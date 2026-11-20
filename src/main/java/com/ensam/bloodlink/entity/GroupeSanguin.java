package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groupe_sanguin")
@Getter @Setter @NoArgsConstructor
public class GroupeSanguin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 3)
    @Column(nullable = false, unique = true, length = 3)
    private String code;

    // seuil d'alerte global au reseau (R9)
    @NotNull
    @Min(0)
    @Column(name = "seuil_alerte", nullable = false)
    private Integer seuilAlerte;

    // groupes auxquels CE groupe peut donner (table compatibilite_groupe, R3)
    @ManyToMany
    @JoinTable(name = "compatibilite_groupe",
            joinColumns = @JoinColumn(name = "groupe_donneur_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_receveur_id"))
    private Set<GroupeSanguin> peutDonnerA = new HashSet<>();
}