package com.ensam.bloodlink.dto;

import com.ensam.bloodlink.entity.Sexe;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

// champs du formulaire d'inscription (donneur + compte)
@Getter
@Setter
public class InscriptionForm {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit avoir au moins 6 caracteres")
    private String motDePasse;

    @NotBlank(message = "Le CIN est obligatoire")
    private String cin;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit etre dans le passe")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateNaissance;

    @NotNull(message = "Le sexe est obligatoire")
    private Sexe sexe;

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "1.0", message = "Le poids doit etre positif")
    private BigDecimal poidsKg;
    private String telephone;
    private String ville;

    @NotNull(message = "Le groupe sanguin est obligatoire")
    private Long groupeId;
}