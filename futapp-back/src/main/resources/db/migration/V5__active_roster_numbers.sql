ALTER TABLE inscripcion_jugador DROP CONSTRAINT uk_inscripcion_camiseta;

CREATE UNIQUE INDEX uk_inscripcion_camiseta_activa
    ON inscripcion_jugador (id_inscripcion_equipo, numero_camiseta)
    WHERE estado = 'ACTIVA' AND numero_camiseta IS NOT NULL;
