package com.isep.dbe.service;

import com.isep.dbe.entity.Compte;
import com.isep.dbe.repository.CompteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompteService {

    private final CompteRepository compteRepository;

    public Optional<Compte> getCompteById(Long id) {
        return compteRepository.findById(id);
    }

    public Compte createCompte(Compte compte) {
        return compteRepository.save(compte);
    }

    public boolean transfer(Long fromId, Long toId, double montant) {
        Optional<Compte> fromOpt = compteRepository.findById(fromId);
        Optional<Compte> toOpt = compteRepository.findById(toId);

        if (fromOpt.isEmpty() || toOpt.isEmpty()) return false;

        Compte from = fromOpt.get();
        Compte to = toOpt.get();

        if (from.getSolde() < montant) return false;

        from.setSolde(from.getSolde() - montant);
        to.setSolde(to.getSolde() + montant);

        compteRepository.save(from);
        compteRepository.save(to);
        return true;
    }
}