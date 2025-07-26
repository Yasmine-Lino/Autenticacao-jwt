package br.com.yasmine.security;

import br.com.yasmine.dto.autenticacao.RoleEnum;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    public RoleEnum validaRole(String role) {
        if (role == null) return null;

        try {
            return RoleEnum.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
