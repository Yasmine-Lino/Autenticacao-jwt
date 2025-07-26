package br.com.yasmine.repositories;

import br.com.yasmine.dto.autenticacao.AutenticacaoDTO;
import br.com.yasmine.dto.autenticacao.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AutenticacaoRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AutenticacaoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByLogin(String login) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE login = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, login);
        return count != null && count > 0;
    }

    public boolean existsByLoginExceptId(String login, long id) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE login = ? AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, login, id);
        return count != null && count > 0;
    }

    public long saveAndReturnId(AutenticacaoDTO autDTO) {

        if (existsByLogin(autDTO.getLogin())) {
            throw new IllegalArgumentException("Login '" + autDTO.getLogin() + "' já existe.");
        }

        String sqlQuery = "insert into usuarios(login, senha, role, habilitado) " +
                "values (?, ?, ?, ?)";

        if (autDTO.getHabilitado() == null) {
            autDTO.setHabilitado("S");
        }
        validarHabilitado(autDTO.getHabilitado());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, autDTO.getLogin());
            stmt.setString(2, autDTO.getSenha());
            stmt.setString(3, autDTO.getRole().name());
            stmt.setString(4, autDTO.getHabilitado());
            return stmt;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        } else {
            throw new RuntimeException("Erro ao obter ID gerado.");
        }
    }

    public long simpleSave(AutenticacaoDTO autDTO) {

        if (existsByLogin(autDTO.getLogin())) {
            throw new IllegalArgumentException("Login '" + autDTO.getLogin() + "' já existe.");
        }

        if (autDTO.getHabilitado() == null) {
            autDTO.setHabilitado("S");
        }
        validarHabilitado(autDTO.getHabilitado());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("usuarios")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(autDTO.toMap()).longValue();
    }

    public AutenticacaoDTO findId(long id, String role) {
        String sqlQuery;
        if ("ADMIN".equalsIgnoreCase(role)) {
            sqlQuery = "select id, login, senha, role, habilitado from usuarios where id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToAutenticacao, id);
        } else {
            sqlQuery = "select id, login, senha, role, habilitado from usuarios where id = ? and habilitado = 'S'";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToAutenticacao, id);
        }
    }

    private AutenticacaoDTO mapRowToAutenticacao(ResultSet resultSet, int rowNum) throws SQLException {
        return AutenticacaoDTO.builder()
                .id(resultSet.getLong("id"))
                .login(resultSet.getString("login"))
                .senha(resultSet.getString("senha"))
                .role(RoleEnum.valueOf(resultSet.getString("role").toUpperCase()))
                .habilitado(resultSet.getString("habilitado"))
                .build();
    }

    public List<AutenticacaoDTO> findAll(String role) {
        String sqlQuery;
        if ("ADMIN".equalsIgnoreCase(role)) {
            sqlQuery = "select id, login, senha, role, habilitado from usuarios";
        } else {
            sqlQuery = "select id, login, senha, role, habilitado from usuarios where habilitado = 'S'";
        }
        return jdbcTemplate.query(sqlQuery, this::mapRowToAutenticacao);
    }

    public void update(AutenticacaoDTO autDTO) {

        String sqlQuery = "update usuarios set " +
                "login = ?, senha = ?, role = ?, habilitado = ? " +
                "where id = ?";

        if (autDTO.getHabilitado() == null) {
            autDTO.setHabilitado("S");
        }
        validarHabilitado(autDTO.getHabilitado());

        jdbcTemplate.update(sqlQuery
                , autDTO.getLogin()
                , autDTO.getSenha()
                , autDTO.getRole().name()
                , autDTO.getHabilitado()
                , autDTO.getId());
    }

    public boolean delete(long id) {
        String sqlQuery = "update usuarios set habilitado = 'N' where id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }


    private void validarHabilitado(String habilitado) {
        if (!"S".equals(habilitado) && !"N".equals(habilitado)) {
            throw new IllegalArgumentException("Campo 'habilitado' deve ser 'S' ou 'N'");
        }
    }

    public String obterLoginPorId(Long id) {
        String sql = "SELECT login FROM usuarios WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }
}