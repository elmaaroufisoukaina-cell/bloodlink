package com.ensam.bloodlink.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DemandeForm {

    @NotNull(message = "Choisissez l'hopital fournisseur")
    private Long fournisseurId;

    @Size(max = 255, message = "Commentaire trop long")
    private String commentaire;

    // 3 lignes proposees, les lignes vides sont ignorees
    private List<LigneForm> lignes = new ArrayList<>(List.of(new LigneForm(), new LigneForm(), new LigneForm()));
}