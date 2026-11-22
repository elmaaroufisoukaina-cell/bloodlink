package com.ensam.bloodlink.dto;

import com.ensam.bloodlink.entity.TypeDon;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// formulaire du medecin : identification du donneur + questionnaire d'eligibilite
@Getter @Setter
public class DonForm {

    @NotBlank(message = "Le CIN du donneur est obligatoire")
    private String cin;

    @NotNull(message = "Le type de don est obligatoire")
    private TypeDon typeDon = TypeDon.SANG_TOTAL;

    @NotNull(message = "Le volume est obligatoire")
    @Min(value = 1, message = "Le volume doit etre positif")
    private Integer volumeMl = 450;

    @NotNull(message = "Le poids mesure est obligatoire")
    @DecimalMin(value = "1.0", message = "Le poids doit etre positif")
    private BigDecimal poidsKg;

    private BigDecimal hemoglobine;

    private Integer tensionSystolique;

    private boolean voyageRecent;

    private boolean tatouageRecent;

    private boolean sousTraitement;
}