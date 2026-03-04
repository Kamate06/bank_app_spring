package com.isep.dbe.service;

import com.isep.dbe.entity.Compte;
import com.isep.dbe.repository.CompteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompteService {

    private final CompteRepository compteRepository;

    public Optional<Compte> getCompteById(Long id) {
        return compteRepository.findById(id);
    }

    public Compte createCompte(Compte compte) {
        if (compte.getSolde() == null || compte.getSolde().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le solde ne peut pas être négatif");
        }
        return compteRepository.save(compte);
    }

    @Transactional
    public boolean transfer(Long fromId, Long toId, double montant) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Les comptes source et destination doivent être différents");
        }

        Optional<Compte> fromOpt = compteRepository.findById(fromId);
        Optional<Compte> toOpt = compteRepository.findById(toId);

        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            return false;
        }

        Compte from = fromOpt.get();
        Compte to = toOpt.get();

        BigDecimal transferAmount = BigDecimal.valueOf(montant);
        
        if (from.getSolde().compareTo(transferAmount) < 0) {
            return false;
        }

        from.setSolde(from.getSolde().subtract(transferAmount));
        to.setSolde(to.getSolde().add(transferAmount));

        compteRepository.save(from);
        compteRepository.save(to);
        return true;
    }
}