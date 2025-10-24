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
        Client saved = clientRepository.save(client);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }


    @GetMapping("/solde/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Double> getSolde(@PathVariable Long id) {
        return compteService.getCompteById(id)
                .map(compte -> ResponseEntity.ok(compte.getSolde()))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Compte> createCompte(@RequestBody Compte compte) {
        Compte saved = compteService.createCompte(compte);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/transfere")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> transfere(@RequestParam Long fromId,
                                            @RequestParam Long toId,
                                            @RequestParam double montant) {
        boolean success = compteService.transfer(fromId, toId, montant);
        if (success) return ResponseEntity.ok("Transfert effectué avec succès");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Erreur: comptes introuvables ou solde insuffisant");
    }
}