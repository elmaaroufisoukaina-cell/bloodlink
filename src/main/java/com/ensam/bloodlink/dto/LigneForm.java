package com.ensam.bloodlink.dto;

import com.ensam.bloodlink.entity.TypeProduit;
import com.ensam.bloodlink.entity.Urgence;
import lombok.Getter;
import lombok.Setter;

// une ligne de la demande
@Getter
@Setter
public class LigneForm {
    private Long groupeId;
    private TypeProduit typeProduit;
    private Integer quantite;
    private Urgence urgence = Urgence.NORMALE;
}