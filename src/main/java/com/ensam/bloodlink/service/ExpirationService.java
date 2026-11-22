package com.ensam.bloodlink.service;
import com.ensam.bloodlink.entity.StatutPoche;
import com.ensam.bloodlink.repository.PocheSanguineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
@Service
@RequiredArgsConstructor
public class ExpirationService {
    private final PocheSanguineRepository poches;
    // BF24 : chaque jour a 00h05, les poches dont la date est depassee deviennent PERIMEE
    @Scheduled(cron =
            "0 5 0 * * *")
    @Transactional
    public void tacheQuotidienne() {
        perimerPochesExpirees();
    }
    // R5 : aussi appele au moment de servir une demande et par le bouton de l'admin systeme
    @Transactional
    public int perimerPochesExpirees() {
        return poches.perimerPochesExpirees(StatutPoche.PERIMEE, StatutPoche.DISPONIBLE, LocalDate.now());
    }
}