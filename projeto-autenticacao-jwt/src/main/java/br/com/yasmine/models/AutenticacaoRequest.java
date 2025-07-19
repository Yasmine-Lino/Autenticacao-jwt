package br.com.yasmine.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//@Data
@Getter
@Setter
public class AutenticacaoRequest {
    private String usuario;
    private String senha;


    public boolean verificar(String usuario, String senha) {
        return  ("usuario".equals(usuario) || "admin".equals(usuario)) && "senha123".equals(senha);
    }
}
