package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "don")
@Getter @Setter @NoArgsConstructor
public class Don {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_don", nullable = false)
    private LocalDate dateDon;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_don", nullable = false)
    private TypeDon typeDon;

    @Column(name = "volume_ml", nullable = false)
    private Integer volumeMl;

    // REALISE : des poches sont generees. REFUSE : conserve avec son motif, sans poches (R8)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDon statut;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donneur_id", nullable = false)
    private Donneur donneur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hopital_id", nullable = false)
    private Hopital hopital;

    // utilisateur de role MEDECIN
    @ManyToOne(optional = false)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Utilisateur medecin;

    // composition 1 pour 1
    @OneToOne(mappedBy = "don", cascade = CascadeType.ALL)
    private QuestionnaireEligibilite questionnaire;

    @OneToMany(mappedBy = "don")
    private List<PocheSanguine> poches = new ArrayList<>();
}