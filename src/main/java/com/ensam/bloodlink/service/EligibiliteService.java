package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.Donneur;
import com.ensam.bloodlink.entity.Sexe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EligibiliteService {

    private final ParametreService parametres;

    public record Resultat(boolean eligible, String motif) {
    }

    // R1 : age, poids, delai depuis le dernier don
    public Resultat verifier(Donneur d, LocalDate date) {
        int ageMin = parametres.getInt("AGE_MIN", 18);
        int ageMax = parametres.getInt("AGE_MAX", 65);
        int poidsMin = parametres.getInt("POIDS_MIN_KG", 50);

        int age = Period.between(d.getDateNaissance(), date).getYears();
        if (age < ageMin || age > ageMax) {
            return new Resultat(false, "l'age doit etre compris entre " + ageMin + " et " + ageMax + " ans.");
        }
        if (d.getPoidsKg().compareTo(BigDecimal.valueOf(poidsMin)) < 0) {
            return new Resultat(false, "le poids minimum est de " + poidsMin + " kg.");
        }
        if (d.getDateProchainDon() != null && date.isBefore(d.getDateProchainDon())) {
            return new Resultat(false, "dernier don trop recent. Prochain don possible le "
                    + d.getDateProchainDon().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
        }
        return new Resultat(true, null);
    }

    // methode convenue avec Wiam pour les rendez-vous
    public boolean isEligible(Donneur d, LocalDate date) {
        return verifier(d, date).eligible();
    }

    // R2 : date du don + delai minimum selon le sexe
    public LocalDate calculerProchainDon(LocalDate dateDon, Sexe sexe) {
        int jours = (sexe == Sexe.HOMME)
                ? parametres.getInt("DELAI_DON_HOMME_JOURS", 90)
                : parametres.getInt("DELAI_DON_FEMME_JOURS", 120);
        return dateDon.plusDays(jours);
    }
}