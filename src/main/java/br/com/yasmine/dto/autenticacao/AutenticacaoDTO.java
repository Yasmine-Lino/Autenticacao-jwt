package br.com.yasmine.dto.autenticacao;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class AutenticacaoDTO {

    private long id;
    private String login;
    private String senha;
    private RoleEnum role;
    private String habilitado;

  /*  public boolean verificar(String login, String senha) {
        return  ("usuario".equals(login) || "admin".equals(login)) && "senha123".equals(senha);
    }*/

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("login", login);
        values.put("senha", senha);
        values.put("role",  role.name());
        values.put("habilitado", habilitado);
        return values;
    }
}
