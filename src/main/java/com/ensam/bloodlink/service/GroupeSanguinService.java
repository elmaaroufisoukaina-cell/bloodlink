package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.GroupeSanguin;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.GroupeSanguinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupeSanguinService {

    private final GroupeSanguinRepository groupes;

    public List<GroupeSanguin> lister() {
        return groupes.findAll(Sort.by("code"));
    }

    public GroupeSanguin trouver(Long id) {
        return groupes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Groupe sanguin introuvable"));
    }

    // l'admin modifie le seuil d'alerte d'un groupe sans toucher au code
    @Transactional
    public void modifierSeuil(Long id, Integer seuil) {
        if (seuil == null || seuil < 0) {
            throw new BusinessException("Le seuil doit etre un entier positif ou nul.");
        }
        trouver(id).setSeuilAlerte(seuil);
    }
}