package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.Hopital;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.HopitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}