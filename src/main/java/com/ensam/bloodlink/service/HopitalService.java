package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.Hopital;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.HopitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HopitalService {

    private final HopitalRepository hopitalRepository;
    // le donneur voit seulement les hopitaux valides
    public List<Hopital> listerValides(String ville) {
        if (ville == null || ville.isBlank()) {
            return hopitalRepository.findByValideTrueOrderByNom();
        }
        return hopitalRepository.findByValideTrueAndVilleContainingIgnoreCaseOrderByNom(ville.trim());
    }

    public Hopital trouver(Long id) {
        return hopitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hopital introuvable"));
    }

    public Hopital trouverValide(Long id) {
        Hopital h = trouver(id);
        if (!h.isValide()) {
            throw new BusinessException("Cet hopital n'est pas encore valide.");
        }
        return h;
    }

    // tous les hopitaux (valides ou non) pour l'admin systeme (BF20)
    public Page<Hopital> lister(int page) {
        return hopitalRepository.findAll(PageRequest.of(page, 10, Sort.by("nom")));
    }

    public List<Hopital> listerTous() {
        return hopitalRepository.findAll(Sort.by("nom"));
    }

    // creation ou modification d'un hopital
    @Transactional
    public Hopital enregistrer(Hopital h) {
        if (!h.getHeureFermeture().isAfter(h.getHeureOuverture())) {
            throw new BusinessException("L'heure de fermeture doit etre apres l'heure d'ouverture.");
        }
        return hopitalRepository.save(h);
    }

    // valider ou invalider un hopital
    @Transactional
    public void basculerValidation(Long id) {
        Hopital h = trouver(id);
        h.setValide(!h.isValide());
    }
}