package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.Donneur;
import com.ensam.bloodlink.entity.GroupeSanguin;
import com.ensam.bloodlink.repository.DonneurRepository;
import com.ensam.bloodlink.repository.GroupeSanguinRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlerteService {

    private final StockService stockService;
    private final GroupeSanguinService groupeService;
    private final GroupeSanguinRepository groupes;
    private final DonneurRepository donneurs;
    private final EligibiliteService eligibilite;

    // un groupe en penurie + les donneurs a contacter
    @Getter
    @AllArgsConstructor
    public static class AlerteGroupe {
        private GroupeSanguin groupe;
        private long stock;
        private List<Donneur> donneurs;
    }

    // BF14 + R9
    public List<AlerteGroupe> verifier(Long hopitalId) {
        LocalDate aujourdhui = LocalDate.now();
        Map<Long, Long> stock = stockService.compterParGroupe(hopitalId);
        List<AlerteGroupe> alertes = new ArrayList<>();

        for (GroupeSanguin g : groupeService.lister()) {
            long quantite = stock.getOrDefault(g.getId(), 0L);
            if (quantite >= g.getSeuilAlerte()) {
                continue; // stock suffisant
            }
            // les groupes qui peuvent donner a ce groupe (R3)
            List<Long> idsCompatibles = groupes.findDonneursCompatibles(g.getId())
                    .stream().map(GroupeSanguin::getId).toList();
            // donneurs de ces groupes, dont le delai est passe ET qui respectent age et poids (R1)
            List<Donneur> aContacter = donneurs.findPotentiels(idsCompatibles, aujourdhui).stream()
                    .filter(d -> eligibilite.isEligible(d, aujourdhui))
                    .toList();
            alertes.add(new AlerteGroupe(g, quantite, aContacter));
        }
        return alertes;
    }
}