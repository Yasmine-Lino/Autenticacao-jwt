package br.com.yasmine.controllers;

import br.com.yasmine.models.AutenticacaoRequest;
import br.com.yasmine.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LoginController {

    private final JwtService jwtService;

    public LoginController(JwtService jwtService) {
        this.jwtService = jwtService;

    }

    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody AutenticacaoRequest autenticacaoRequest) {

        boolean verificacao = autenticacaoRequest.verificar(
                autenticacaoRequest.getUsuario(), autenticacaoRequest.getSenha());

        if(!verificacao) {
            return ResponseEntity.status(401).body("Credenciais inválidas!" );
        }
        String token = jwtService.generateToken(autenticacaoRequest.getUsuario());

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
