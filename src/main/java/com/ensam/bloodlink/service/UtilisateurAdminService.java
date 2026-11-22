package com.ensam.bloodlink.service;

import com.ensam.bloodlink.dto.PersonnelForm;
import com.ensam.bloodlink.entity.Role;
import com.ensam.bloodlink.entity.Utilisateur;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurAdminService {

    // roles du personnel d'hopital
    private static final List<Role> ROLES_PERSONNEL = List.of(Role.ADMIN_HOPITAL, Role.MEDECIN, Role.GESTIONNAIRE_STOCK);

    private final UtilisateurRepository utilisateurRepository;
    private final HopitalService hopitalService;
    private final PasswordEncoder passwordEncoder;

    // liste des comptes, filtre par role
    public Page<Utilisateur> lister(Role role, int page) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("email"));
        if (role == null) {
            return utilisateurRepository.findAll(pageable);
        }
        return utilisateurRepository.findByRole(role, pageable);
    }

    // seul l'admin systeme cree le personnel
    @Transactional
    public Utilisateur creerPersonnel(PersonnelForm f) {
        if (!ROLES_PERSONNEL.contains(f.getRole())) {
            throw new BusinessException("Ce role ne peut pas etre cree ici.");
        }
        String email = f.getEmail().trim().toLowerCase();
        if (utilisateurRepository.existsByEmail(email)) {
            throw new BusinessException("Cet email est deja utilise.");
        }
        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setMotDePasse(passwordEncoder.encode(f.getMotDePasse()));
        u.setRole(f.getRole());
        u.setActif(true);
        u.setHopital(hopitalService.trouver(f.getHopitalId()));
        return utilisateurRepository.save(u);
    }

    // activer ou desactiver un compte
    @Transactional
    public void basculerActif(Long id, Long idConnecte) {
        if (id.equals(idConnecte)) {
            throw new BusinessException("Vous ne pouvez pas desactiver votre propre compte.");
        }
        Utilisateur u = trouver(id);
        u.setActif(!u.isActif());
    }

    // changer le role d'un membre du personnel
    @Transactional
    public void changerRole(Long id, Role nouveauRole) {
        Utilisateur u = trouver(id);
        if (!ROLES_PERSONNEL.contains(u.getRole()) || !ROLES_PERSONNEL.contains(nouveauRole)) {
            throw new BusinessException("Seuls les roles du personnel peuvent etre modifies.");
        }
        u.setRole(nouveauRole);
    }

    private Utilisateur trouver(Long id) {
        return utilisateurRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}