package com.ensam.bloodlink.dto;

import com.ensam.bloodlink.entity.TypeProduit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class PocheForm {

    @NotBlank(message = "Le numero de serie est obligatoire")
    @Size(max = 50)
    private String numeroSerie;

    @NotNull(message = "Le type de produit est obligatoire")
    private TypeProduit typeProduit;

    @NotNull(message = "Le volume est obligatoire")
    @Min(value = 1, message = "Le volume doit etre positif")
    private Integer volumeMl;

    @NotNull(message = "La date de prelevement est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate datePrelevement;

    @NotNull(message = "La date de peremption est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate datePeremption;

    @NotNull(message = "Le don d'origine est obligatoire")
    private Long donId;
}