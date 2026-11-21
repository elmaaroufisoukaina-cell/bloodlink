package com.ensam.bloodlink.config;

import com.ensam.bloodlink.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// charge l'utilisateur depuis la base au moment de la connexion
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmail(email.trim().toLowerCase()).map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
    }
}