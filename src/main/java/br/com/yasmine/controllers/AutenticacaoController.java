package br.com.yasmine.controllers;

import br.com.yasmine.dto.autenticacao.AutenticacaoDTO;
import br.com.yasmine.repositories.AutenticacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class AutenticacaoController {

    @Autowired
    private AutenticacaoRepository autRepository;

    @GetMapping
    public ResponseEntity<List<AutenticacaoDTO>> listarUsuarios() {
        String role = getUsuarioRole();
        if (role == null) return ResponseEntity.status(403).build();

        List<AutenticacaoDTO> usuarios = autRepository.findAll(role);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutenticacaoDTO> buscarPorId(@PathVariable long id) {
        String role = getUsuarioRole();
        if (role == null) return ResponseEntity.status(403).build();

        AutenticacaoDTO usuario = autRepository.findId(id, role);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<AutenticacaoDTO> criarUsuario(@RequestBody AutenticacaoDTO autDTO) {
        long id = autRepository.saveAndReturnId(autDTO);
        autDTO.setId(id);
        return ResponseEntity.status(201).body(autDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarUsuario(@PathVariable long id, @RequestBody AutenticacaoDTO autDTO) {
        autDTO.setId(id);

        AutenticacaoDTO existeUsuario = autRepository.findId(id, getUsuarioRole());
        if (existeUsuario == null) {
            return ResponseEntity.notFound().build();
        }

        autRepository.update(autDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable long id) {
        boolean deletado = autRepository.delete(id);
        return deletado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private String getUsuarioRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                return authority.getAuthority().replace("ROLE_", "");
            }
        }
        return null;
    }
}
