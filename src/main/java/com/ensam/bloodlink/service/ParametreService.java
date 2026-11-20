package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.ParametreSysteme;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.ParametreSystemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParametreService {

    private final ParametreSystemeRepository parametres;

    // lit un parametre entier ; si absent ou invalide, renvoie la valeur par defaut
    public int getInt(String cle, int defaut) {
        return parametres.findByCle(cle).map(p -> {
            try {
                return Integer.parseInt(p.getValeur().trim());
            } catch (NumberFormatException e) {
                return defaut;
            }
        }).orElse(defaut);
    }

    public List<ParametreSysteme> findAll() {
        return parametres.findAll(Sort.by("cle"));
    }

    @Transactional
    public void modifier(Long id, String valeur) {
        ParametreSysteme p = parametres.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parametre introuvable"));
        try {
            if (Integer.parseInt(valeur.trim()) <= 0) {
                throw new BusinessException("La valeur doit etre un entier positif.");
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("La valeur doit etre un entier positif.");
        }
        p.setValeur(valeur.trim());
    }
}