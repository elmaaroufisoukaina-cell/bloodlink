package com.ensam.bloodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ligne_demande")
@Getter @Setter @NoArgsConstructor
public class LigneDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_produit", nullable = false)
    private TypeProduit typeProduit;

    @Column(nullable = false)
    private Integer quantite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgence urgence;

    @ManyToOne(optional = false)
    @JoinColumn(name = "groupe_sanguin_id", nullable = false)
    private GroupeSanguin groupeSanguin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private DemandeSang demande;

    // la quantite servie = nombre de poches liees a cette ligne (R6, non stockee)
    @OneToMany(mappedBy = "ligneDemande")
    private List<PocheSanguine> poches = new ArrayList<>();
}