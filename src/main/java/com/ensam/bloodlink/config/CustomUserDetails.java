package com.ensam.bloodlink.config;

import com.ensam.bloodlink.entity.Role;
import com.ensam.bloodlink.entity.Utilisateur;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String motDePasse;
    private final Role role;
    private final boolean actif;
    private final Long hopitalId;   // null pour un donneur ou l'admin systeme
    private final Long donneurId;   // null pour le personnel

    public CustomUserDetails(Utilisateur u) {
        this.id = u.getId();
        this.email = u.getEmail();
        this.motDePasse = u.getMotDePasse();
        this.role = u.getRole();
        this.actif = u.isActif();
        this.hopitalId = (u.getHopital() != null) ? u.getHopital().getId() : null;
        this.donneurId = (u.getDonneur() != null) ? u.getDonneur().getId() : null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security attend le prefixe ROLE_
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }
}