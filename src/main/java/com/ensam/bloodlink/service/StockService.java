package com.ensam.bloodlink.service;

import com.ensam.bloodlink.dto.PocheForm;
import com.ensam.bloodlink.entity.*;
import com.ensam.bloodlink.exception.AccesInterditException;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.DonRepository;
import com.ensam.bloodlink.repository.PocheSanguineRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockService {

    private final PocheSanguineRepository poches;
    private final DonRepository dons;
    private final GroupeSanguinService groupeService;

    // une jauge = le stock disponible d'un groupe compare a son seuil d'alerte
    @Getter
    @AllArgsConstructor
    public static class GroupeStock {
        private String code;
        private long quantite;
        private int seuil;

        public boolean isEnAlerte() {
            return quantite < seuil;
        }

        public int getPourcentage() {
            return seuil == 0 ? 100 : (int) Math.min(100, quantite * 100 / seuil);
        }
    }

    // stock disponible (non perime) par groupe ; hopitalId = null pour tout le reseau
    public Map<Long, Long> compterParGroupe(Long hopitalId) {
        LocalDate aujourdhui = LocalDate.now();
        List<Object[]> lignes = (hopitalId == null)
                ? poches.compterParGroupeReseau(StatutPoche.DISPONIBLE, aujourdhui)
                : poches.compterParGroupe(hopitalId, StatutPoche.DISPONIBLE, aujourdhui);
        Map<Long, Long> resultat = new HashMap<>();
        for (Object[] l : lignes) {
            resultat.put((Long) l[0], (Long) l[1]);
        }
        return resultat;
    }

    public List<GroupeStock> jauges(Long hopitalId) {
        Map<Long, Long> compte = compterParGroupe(hopitalId);
        List<GroupeStock> resultat = new ArrayList<>();
        for (GroupeSanguin g : groupeService.lister()) {
            resultat.add(new GroupeStock(g.getCode(), compte.getOrDefault(g.getId(), 0L), g.getSeuilAlerte()));
        }
        return resultat;
    }

    // F8 : poches d'un hopital, filtrees par statut, paginees, les plus proches de la peremption d'abord
    public Page<PocheSanguine> lister(Long hopitalId, StatutPoche statut, int page) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("datePeremption").ascending());
        if (statut == null) {
            return poches.findByHopitalId(hopitalId, pageable);
        }
        return poches.findByHopitalIdAndStatut(hopitalId, statut, pageable);
    }

    // R12 : on ne peut toucher qu'aux poches de son propre hopital
    public PocheSanguine trouverPourHopital(Long id, Long hopitalId) {
        PocheSanguine p = poches.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poche introuvable"));
        if (!p.getHopital().getId().equals(hopitalId)) {
            throw new AccesInterditException("Cette poche appartient a un autre hopital.");
        }
        return p;
    }

    public List<Don> donsRealises(Long hopitalId) {
        return dons.findByHopitalIdAndStatutOrderByDateDonDesc(hopitalId, StatutDon.REALISE);
    }

    @Transactional
    public PocheSanguine creer(PocheForm f, Long hopitalId) {
        Don don = dons.findById(f.getDonId())
                .orElseThrow(() -> new BusinessException("Don introuvable."));
        if (!don.getHopital().getId().equals(hopitalId) || don.getStatut() != StatutDon.REALISE) {
            throw new BusinessException("Ce don n'est pas valide pour votre hopital.");
        }
        verifierDates(f);
        String numero = f.getNumeroSerie().trim();
        if (poches.findByNumeroSerie(numero).isPresent()) {
            throw new BusinessException("Ce numero de serie existe deja.");
        }
        PocheSanguine p = new PocheSanguine();
        p.setNumeroSerie(numero);
        p.setTypeProduit(f.getTypeProduit());
        p.setVolumeMl(f.getVolumeMl());
        p.setDatePrelevement(f.getDatePrelevement());
        p.setDatePeremption(f.getDatePeremption());
        p.setStatut(StatutPoche.DISPONIBLE);
        p.setDon(don);
        p.setHopital(don.getHopital());
        return poches.save(p);
    }

    @Transactional
    public void modifier(Long id, PocheForm f, Long hopitalId) {
        PocheSanguine p = trouverPourHopital(id, hopitalId);
        if (p.getStatut() == StatutPoche.SERVIE) {
            throw new BusinessException("Une poche servie ne peut plus etre modifiee.");
        }
        verifierDates(f);
        String numero = f.getNumeroSerie().trim();
        poches.findByNumeroSerie(numero).filter(autre -> !autre.getId().equals(id)).ifPresent(autre -> {
            throw new BusinessException("Ce numero de serie existe deja.");
        });
        p.setNumeroSerie(numero);
        p.setTypeProduit(f.getTypeProduit());
        p.setVolumeMl(f.getVolumeMl());
        p.setDatePrelevement(f.getDatePrelevement());
        p.setDatePeremption(f.getDatePeremption());
    }

    @Transactional
    public void supprimer(Long id, Long hopitalId) {
        PocheSanguine p = trouverPourHopital(id, hopitalId);
        if (p.getStatut() == StatutPoche.SERVIE) {
            throw new BusinessException("Une poche servie ne peut pas etre supprimee (tracabilite).");
        }
        poches.delete(p);
    }

    @Transactional
    public void marquerPerimee(Long id, Long hopitalId) {
        PocheSanguine p = trouverPourHopital(id, hopitalId);
        if (p.getStatut() == StatutPoche.SERVIE) {
            throw new BusinessException("Une poche servie ne peut pas etre marquee perimee.");
        }
        p.setStatut(StatutPoche.PERIMEE);
    }

    private void verifierDates(PocheForm f) {
        if (!f.getDatePeremption().isAfter(f.getDatePrelevement())) {
            throw new BusinessException("La date de peremption doit etre apres la date de prelevement.");
        }
    }
}