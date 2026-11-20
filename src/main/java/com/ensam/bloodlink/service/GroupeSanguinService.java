package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.GroupeSanguin;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.GroupeSanguinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
}