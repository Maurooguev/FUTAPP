package com.futapp.service;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FusionJugadorService {
    private final JdbcTemplate jdbc;
    private final EntityManager entityManager;

    public FusionJugadorService(JdbcTemplate jdbc, EntityManager entityManager) {
        this.jdbc = jdbc;
        this.entityManager = entityManager;
    }

    @Transactional
    public void fusionar(Long idPrincipal, Long idDuplicado) {
        if (idPrincipal.equals(idDuplicado)) return;

        entityManager.flush();

        jdbc.update("""
                insert into jugador_alias (codigo, id_jugador)
                select duplicado.codigo_publico, ? from jugador duplicado
                where duplicado.id_jugador = ?
                  and not exists (select 1 from jugador_alias alias where upper(alias.codigo) = upper(duplicado.codigo_publico))
                """, idPrincipal, idDuplicado);
        jdbc.update("update jugador_alias set id_jugador = ? where id_jugador = ?", idPrincipal, idDuplicado);

        jdbc.update("""
                delete from inscripcion_jugador duplicada
                where duplicada.id_jugador = ?
                  and exists (
                    select 1 from inscripcion_jugador principal
                    where principal.id_jugador = ?
                      and principal.id_inscripcion_equipo = duplicada.id_inscripcion_equipo
                  )
                """, idDuplicado, idPrincipal);
        jdbc.update("update inscripcion_jugador set id_jugador = ? where id_jugador = ?", idPrincipal, idDuplicado);

        jdbc.update("""
                delete from usuario_jugador_favorito duplicado
                where duplicado.id_jugador = ?
                  and exists (
                    select 1 from usuario_jugador_favorito principal
                    where principal.id_jugador = ?
                      and principal.id_usuario = duplicado.id_usuario
                  )
                """, idDuplicado, idPrincipal);
        jdbc.update("update usuario_jugador_favorito set id_jugador = ? where id_jugador = ?", idPrincipal, idDuplicado);

        jdbc.update("update gol set id_jugador = ? where id_jugador = ?", idPrincipal, idDuplicado);
        jdbc.update("update tarjeta set id_jugador = ? where id_jugador = ?", idPrincipal, idDuplicado);
        jdbc.update("update partido set id_mvp = ? where id_mvp = ?", idPrincipal, idDuplicado);
        jdbc.update("update solicitud_reclamo set id_jugador = ? where id_jugador = ?", idPrincipal, idDuplicado);
        jdbc.update("delete from jugador where id_jugador = ?", idDuplicado);
        entityManager.clear();
    }
}
