package com.isep.dbe.controller;

import com.isep.dbe.entity.Client;
import com.isep.dbe.entity.Compte;
import com.isep.dbe.repository.ClientRepository;
import com.isep.dbe.service.CompteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BanqueController {

    private final ClientRepository clientRepository;
    private final CompteService compteService;

    @GetMapping("/clients")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<Client> getClients() {
        return clientRepository.findAll();
    }
    
    @PostMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        if (client.getNom() == null || client.getNom().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (client.getEmail() == null || !client.getEmail().contains("@")) {
            return ResponseEntity.badRequest().build();
        }
        Client saved = clientRepository.save(client);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/solde/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Double> getSolde(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return compteService.getCompteById(id)
                .map(compte -> ResponseEntity.ok(compte.getSolde().doubleValue()))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Compte> createCompte(@RequestBody Compte compte) {
        try {
            Compte saved = compteService.createCompte(compte);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/transfere")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> transfere(@RequestParam Long fromId,
                                            @RequestParam Long toId,
                                            @RequestParam double montant) {
        try {
            boolean success = compteService.transfer(fromId, toId, montant);
            if (success) {
                return ResponseEntity.ok("Transfert effectué avec succès");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur: comptes introuvables ou solde insuffisant");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}