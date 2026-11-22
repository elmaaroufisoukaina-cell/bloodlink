package com.ensam.bloodlink.service;

import com.ensam.bloodlink.dto.InscriptionForm;
import com.ensam.bloodlink.entity.Donneur;
import com.ensam.bloodlink.entity.GroupeSanguin;
import com.ensam.bloodlink.entity.Role;
import com.ensam.bloodlink.entity.Utilisateur;
import com.ensam.bloodlink.exception.BusinessException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.DonneurRepository;
import com.ensam.bloodlink.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DonneurService {
    private final DonneurRepository donneurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final GroupeSanguinService groupeService;
    private final PasswordEncoder passwordEncoder;

    // inscription libre du donneur (R13)
    @Transactional
    public Donneur inscrire(InscriptionForm f) {
        String email = f.getEmail().trim().toLowerCase();
        String cin = f.getCin().trim().toUpperCase();

        if (donneurRepository.existsByCin(cin)) {
            throw new BusinessException("Ce CIN est deja enregistre.");
        }
        if (utilisateurRepository.existsByEmail(email)) {
            throw new BusinessException("Cet email est deja utilise.");
        }
        GroupeSanguin groupe = groupeService.trouver(f.getGroupeId());

        Donneur d = new Donneur();
        d.setCin(cin);
        d.setNom(f.getNom().trim());
        d.setPrenom(f.getPrenom().trim());
        d.setDateNaissance(f.getDateNaissance());
        d.setSexe(f.getSexe());
        d.setPoidsKg(f.getPoidsKg());
        d.setTelephone(f.getTelephone());
        d.setVille(f.getVille());
        d.setGroupeSanguin(groupe);
        donneurRepository.save(d);

        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setMotDePasse(passwordEncoder.encode(f.getMotDePasse()));
        u.setRole(Role.DONNEUR);
        u.setActif(true);
        u.setDonneur(d);
        utilisateurRepository.save(u);
        return d;
    }
    public Donneur trouver(Long id) {
        return donneurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donneur introuvable"));
    }
    // recherche par groupe (0 = tous) et par ville, pour l'admin
    public Page<Donneur> rechercher(long groupeId, String ville, int page) {
        String v = (ville == null) ? "" : ville.trim();
        return donneurRepository.rechercher(groupeId, v, PageRequest.of(page, 10, Sort.by("nom")));
    }
}