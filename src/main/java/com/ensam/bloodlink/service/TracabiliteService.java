package com.ensam.bloodlink.service;

import com.ensam.bloodlink.entity.PocheSanguine;
import com.ensam.bloodlink.entity.StatutPoche;
import com.ensam.bloodlink.entity.TypeProduit;
import com.ensam.bloodlink.exception.AccesInterditException;
import com.ensam.bloodlink.exception.ResourceNotFoundException;
import com.ensam.bloodlink.repository.PocheSanguineRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TracabiliteService {

    private final PocheSanguineRepository poches;

    // ce que voit un donneur : aucune donnee patient, seulement l'hopital destinataire
    @Getter
    @AllArgsConstructor
    public static class Utilisation {
        private LocalDate dateDon;
        private TypeProduit typeProduit;
        private String numeroSerie;
        private String hopital;
    }

    // BF13 : la chaine complete donneur > don > poche > demande > hopital (R10)
    public PocheSanguine tracer(Long pocheId, Long hopitalId) {
        PocheSanguine p = poches.findById(pocheId)
                .orElseThrow(() -> new ResourceNotFoundException("Poche introuvable"));
        if (!p.getHopital().getId().equals(hopitalId)) {
            throw new AccesInterditException("Cette poche appartient a un autre hopital.");
        }
        return p;
    }

    // BF7 : "Votre don a ete utilise a l'hopital X"
    public List<Utilisation> utilisationsPourDonneur(Long donneurId) {
        return poches.findByDon_Donneur_IdAndStatut(donneurId, StatutPoche.SERVIE).stream()
                .filter(p -> p.getLigneDemande() != null)
                .map(p -> new Utilisation(p.getDon().getDateDon(), p.getTypeProduit(), p.getNumeroSerie(),
                        p.getLigneDemande().getDemande().getHopitalDemandeur().getNom()))
                .toList();
    }
}