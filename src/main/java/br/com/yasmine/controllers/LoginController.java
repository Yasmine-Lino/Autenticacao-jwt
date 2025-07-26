package br.com.yasmine.controllers;

import br.com.yasmine.dto.autenticacao.AutenticacaoDTO;
import br.com.yasmine.dto.autenticacao.RoleEnum;
import br.com.yasmine.security.AutenticacaoService;
import br.com.yasmine.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LoginController {

    private final JwtService jwtService;
    private final AutenticacaoService autService;

    public LoginController(JwtService jwtService,  AutenticacaoService autService) {
        this.jwtService = jwtService;
        this.autService = autService;

    }

    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody AutenticacaoDTO autenticacaoDTO) {

        RoleEnum verificacao = autService.validaRole(
                autenticacaoDTO.getRole().name());

        if(verificacao == null) {
            return ResponseEntity.status(401).body("Credenciais inválidas!" );
        }

        String token = jwtService.generateToken(autenticacaoDTO.getLogin(), autenticacaoDTO.getRole().toString());


        return ResponseEntity.ok(Map.of("accessToken", token));

    }

    @GetMapping("/liberado")
    public ResponseEntity<String> liberado() {
        return ResponseEntity.ok("Você está autenticado!");
    }

    @GetMapping("/restrito")
    public ResponseEntity<String> restrito() {
        return ResponseEntity.ok("Acesso restrito autorizado!");
    }

    @GetMapping("/proibido")
    public ResponseEntity<String> proibido() {
        return ResponseEntity.ok("Acesso proibido!");
    }
}
