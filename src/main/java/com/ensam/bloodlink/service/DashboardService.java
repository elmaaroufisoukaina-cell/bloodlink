package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.StatutDon;
import com.ensam.bloodlink.entity.StatutPoche;
import com.ensam.bloodlink.repository.DonRepository;
import com.ensam.bloodlink.repository.PocheSanguineRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StockService stockService;
    private final PocheSanguineRepository pocheRepository;
    private final DonRepository donRepository;

    // donnees du tableau de bord
    @Getter
    @AllArgsConstructor
    public static class Dashboard {
        private List<StockService.GroupeStock> jauges;
        private long expirantCetteSemaine;
        private long totalServies;
        private long totalPerimees;
        private int tauxGaspillage;
        private List<String> moisLabels;
        private List<Long> moisValeurs;
    }
    // tableau de bord de l'admin systeme
    public Dashboard construire() {
        LocalDate aujourdhui = LocalDate.now();
        // poches disponibles qui expirent dans les 7 jours
        long expirant = pocheRepository.countByStatutAndDatePeremptionBetween(StatutPoche.DISPONIBLE,
                aujourdhui, aujourdhui.plusDays(7));
        // taux de gaspillage = perimees / (servies + perimees)
        long servies = pocheRepository.countByStatut(StatutPoche.SERVIE);
        long perimees = pocheRepository.countByStatut(StatutPoche.PERIMEE);
        int taux = 0;
        if (servies + perimees > 0) {
            taux = (int) Math.round(perimees * 100.0 / (servies + perimees));
        }

        // dons realises par mois
        List<String> labels = new ArrayList<>();
        List<Long> valeurs = new ArrayList<>();
        for (Object[] ligne : donRepository.compterParMois(StatutDon.REALISE)) {
            int annee = ((Number) ligne[0]).intValue();
            int mois = ((Number) ligne[1]).intValue();
            labels.add(String.format("%02d/%d", mois, annee));
            valeurs.add(((Number) ligne[2]).longValue());
        }

        // null = jauges de tout le reseau
        return new Dashboard(stockService.jauges(null), expirant, servies, perimees, taux, labels, valeurs);
    }
}