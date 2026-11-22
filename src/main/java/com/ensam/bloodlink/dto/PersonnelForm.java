package com.ensam.bloodlink.dto;

import com.ensam.bloodlink.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// creation d'un compte du personnel par l'admin systeme
@Getter
@Setter
public class PersonnelForm {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit avoir au moins 6 caracteres")
    private String motDePasse;

    @NotNull(message = "Le role est obligatoire")
    private Role role;

    @NotNull(message = "L'hopital est obligatoire")
    private Long hopitalId;
}