-- Datos de demostración para desarrollo local de FUTAPP.
-- Es idempotente: los torneos ZO-DEMO-01 a ZO-DEMO-10 se crean una sola vez.
BEGIN;

DO $$
DECLARE
    tournament_names TEXT[] := ARRAY[
        'Liga Barrial de Morón 2026',
        'Copa Merlo Norte',
        'Campeonato Oeste de Ituzaingó',
        'Liga Municipal de Hurlingham',
        'Copa Moreno Unido',
        'Torneo Clausura de Castelar',
        'Liga Amateur de Ramos Mejía',
        'Copa La Matanza 2026',
        'Torneo Apertura de Ciudadela',
        'Superliga de Paso del Rey'
    ];
    locations TEXT[] := ARRAY[
        'Morón', 'Merlo', 'Ituzaingó', 'Hurlingham', 'Moreno',
        'Castelar', 'Ramos Mejía', 'La Matanza', 'Ciudadela', 'Paso del Rey'
    ];
    tournament_states TEXT[] := ARRAY[
        'ACTIVO', 'ACTIVO', 'FINALIZADO', 'ACTIVO', 'FINALIZADO',
        'ACTIVO', 'FINALIZADO', 'ACTIVO', 'ACTIVO', 'FINALIZADO'
    ];
    played_rounds INTEGER[] := ARRAY[4, 3, 7, 5, 7, 2, 7, 4, 1, 7];
    team_prefixes TEXT[] := ARRAY[
        'Defensores de', 'Atlético', 'Deportivo', 'Unión',
        'Juventud', 'Estrella del Oeste', 'Los Pibes de', 'Barrio Unido'
    ];
    first_names TEXT[] := ARRAY['Mateo', 'Thiago', 'Lucas', 'Joaquín', 'Tomás', 'Franco'];
    last_names TEXT[] := ARRAY['Acosta', 'Benítez', 'Cabrera', 'Domínguez', 'Ferreyra', 'Gómez'];
    positions TEXT[] := ARRAY['Arquero', 'Defensor', 'Defensor', 'Volante', 'Volante', 'Delantero'];
    shirt_numbers INTEGER[] := ARRAY[1, 2, 4, 5, 8, 9];
    local_positions INTEGER[] := ARRAY[
        1,2,3,4, 1,8,2,3, 1,7,8,2, 1,6,7,8,
        1,5,6,7, 1,4,5,6, 1,3,4,5
    ];
    away_positions INTEGER[] := ARRAY[
        8,7,6,5, 7,6,5,4, 6,5,4,3, 5,4,3,2,
        4,3,2,8, 3,2,8,7, 2,8,7,6
    ];
    organizer_id BIGINT;
    tournament_id BIGINT;
    team_id BIGINT;
    team_ids BIGINT[];
    date_id BIGINT;
    match_id BIGINT;
    enrollment_id BIGINT;
    division_id BIGINT;
    phase_id BIGINT;
    local_team_id BIGINT;
    away_team_id BIGINT;
    scorer_id BIGINT;
    card_player_id BIGINT;
    mvp_id BIGINT;
    local_position INTEGER;
    away_position INTEGER;
    local_goals INTEGER;
    away_goals INTEGER;
    tournament_index INTEGER;
    team_index INTEGER;
    player_index INTEGER;
    round_index INTEGER;
    match_index INTEGER;
    goal_index INTEGER;
    public_code TEXT;
    team_name TEXT;
    match_state TEXT;
    match_time TIMESTAMP;
BEGIN
    INSERT INTO usuario (nombre, email, password_hash, rol)
    VALUES (
        'Liga Zona Oeste',
        'demo.zona.oeste@futapp.local',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ORGANIZADOR'
    )
    ON CONFLICT (email) DO UPDATE SET nombre = EXCLUDED.nombre
    RETURNING id_usuario INTO organizer_id;

    INSERT INTO usuario_rol (id_usuario, rol)
    VALUES (organizer_id, 'ORGANIZADOR')
    ON CONFLICT DO NOTHING;

    INSERT INTO perfil_organizador (id_usuario, nombre_publico, descripcion)
    VALUES (
        organizer_id,
        'Torneos Zona Oeste',
        'Organización de ligas y copas amateur en distintos barrios de Zona Oeste.'
    )
    ON CONFLICT (id_usuario) DO UPDATE SET
        nombre_publico = EXCLUDED.nombre_publico,
        descripcion = EXCLUDED.descripcion;

    FOR tournament_index IN 1..array_length(tournament_names, 1) LOOP
        public_code := format('ZO-DEMO-%s', lpad(tournament_index::TEXT, 2, '0'));
        IF EXISTS (SELECT 1 FROM torneo WHERE codigo_publico = public_code) THEN
            CONTINUE;
        END IF;

        INSERT INTO torneo (
            nombre, descripcion, ubicacion, estado, formato, cantidad_equipos,
            codigo_publico, id_propietario, ida_vuelta, puntos_victoria,
            puntos_empate, puntos_derrota, criterio_desempate, visitas
        ) VALUES (
            tournament_names[tournament_index],
            format('Competencia amateur de fútbol en %s con ocho equipos, fixture completo y estadísticas actualizadas.', locations[tournament_index]),
            locations[tournament_index], tournament_states[tournament_index], 'Liga', 8,
            public_code, organizer_id, FALSE, 3, 1, 0, 'DIFERENCIA_GOLES',
            120 + tournament_index * 37
        ) RETURNING id_torneo INTO tournament_id;

        INSERT INTO competicion_division (id_torneo, nombre, orden)
        VALUES (tournament_id, 'Primera División', 1)
        RETURNING id_division INTO division_id;

        INSERT INTO competicion_fase (id_division, nombre, tipo, orden, clasificados, estado)
        VALUES (
            division_id, 'Liga', 'LIGA', 1, NULL,
            CASE WHEN tournament_states[tournament_index] = 'FINALIZADO' THEN 'FINALIZADA' ELSE 'ACTIVA' END
        ) RETURNING id_fase INTO phase_id;

        team_ids := ARRAY[]::BIGINT[];
        FOR team_index IN 1..8 LOOP
            team_name := format('%s %s', team_prefixes[team_index], locations[tournament_index]);
            INSERT INTO equipo (
                nombre, color_camiseta, barrio, responsable, telefono_contacto,
                estado, id_torneo, codigo_publico, descripcion, email_contacto
            ) VALUES (
                team_name,
                (ARRAY['Azul y blanco','Rojo y negro','Verde y blanco','Celeste','Negro y dorado','Rojo','Azul','Blanco'])[team_index],
                locations[tournament_index],
                format('Delegado %s %s', first_names[((team_index - 1) % 6) + 1], last_names[((team_index + 1) % 6) + 1]),
                format('11%08s', tournament_index * 100 + team_index),
                'ACTIVO', tournament_id,
                format('EQ-Z%s%s', lpad(tournament_index::TEXT, 2, '0'), lpad(team_index::TEXT, 2, '0')),
                format('Club amateur de %s participante habitual de competencias barriales.', locations[tournament_index]),
                format('equipo%s%s@demo.futapp.local', tournament_index, team_index)
            ) RETURNING id_equipo INTO team_id;
            team_ids := array_append(team_ids, team_id);

            INSERT INTO inscripcion_equipo (id_equipo, id_torneo, categoria, estado, fecha_alta)
            VALUES (team_id, tournament_id, 'Primera', 'ACTIVA', CURRENT_DATE - 120)
            RETURNING id_inscripcion INTO enrollment_id;

            INSERT INTO cupo_equipo (id_division, id_equipo, numero, etiqueta)
            VALUES (division_id, team_id, team_index, format('Equipo %s', team_index));

            FOR player_index IN 1..6 LOOP
                INSERT INTO jugador (
                    nombre, apellido, dni, fecha_nacimiento, numero_camiseta,
                    posicion, estado, id_equipo, codigo_publico, apodo,
                    pierna_habil, barrio, nacionalidad, visitas
                ) VALUES (
                    first_names[player_index],
                    format('%s %s', last_names[player_index], substring(locations[tournament_index] from 1 for 1)),
                    format('98%s%s%s', lpad(tournament_index::TEXT, 2, '0'), lpad(team_index::TEXT, 2, '0'), lpad(player_index::TEXT, 2, '0')),
                    format('%s-%s-%s', 1993 + player_index, lpad((((team_index + player_index) % 12) + 1)::TEXT, 2, '0'), lpad((((team_index * 2 + player_index) % 27) + 1)::TEXT, 2, '0')),
                    shirt_numbers[player_index], positions[player_index], 'ACTIVO', team_id,
                    format('JUG-Z%s%s%s', lpad(tournament_index::TEXT, 2, '0'), team_index, player_index),
                    CASE WHEN player_index = 6 THEN 'El Tanque' WHEN player_index = 5 THEN 'El Mago' ELSE NULL END,
                    CASE WHEN player_index IN (3,5) THEN 'Izquierda' ELSE 'Derecha' END,
                    locations[tournament_index], 'Argentina', 8 + player_index * 3
                ) RETURNING id_jugador INTO scorer_id;

                INSERT INTO inscripcion_jugador (
                    id_inscripcion_equipo, id_jugador, numero_camiseta, posicion, estado, fecha_alta
                ) VALUES (
                    enrollment_id, scorer_id, shirt_numbers[player_index], positions[player_index], 'ACTIVA', CURRENT_DATE - 120
                );
            END LOOP;
        END LOOP;

        FOR round_index IN 1..7 LOOP
            INSERT INTO fecha (numero_fecha, descripcion, estado, id_torneo)
            VALUES (
                round_index, format('Fecha %s', round_index),
                CASE WHEN round_index <= played_rounds[tournament_index] THEN 'FINALIZADA' ELSE 'ACTIVA' END,
                tournament_id
            ) RETURNING id_fecha INTO date_id;

            FOR match_index IN 1..4 LOOP
                local_position := local_positions[(round_index - 1) * 4 + match_index];
                away_position := away_positions[(round_index - 1) * 4 + match_index];
                local_team_id := team_ids[local_position];
                away_team_id := team_ids[away_position];
                match_state := CASE WHEN round_index <= played_rounds[tournament_index] THEN 'FINALIZADO' ELSE 'PROGRAMADO' END;
                match_time := (CURRENT_DATE + ((round_index - played_rounds[tournament_index]) * 7))::TIMESTAMP
                    + TIME '18:00' + ((match_index - 1) * INTERVAL '90 minutes');

                IF match_state = 'FINALIZADO' THEN
                    IF local_position = 1 THEN
                        local_goals := 3; away_goals := 0;
                    ELSIF away_position = 1 THEN
                        local_goals := 0; away_goals := 2;
                    ELSE
                        local_goals := (local_position + round_index + match_index) % 4;
                        away_goals := (away_position + round_index) % 3;
                    END IF;
                    SELECT id_jugador INTO mvp_id
                    FROM jugador
                    WHERE id_equipo = CASE WHEN local_goals >= away_goals THEN local_team_id ELSE away_team_id END
                      AND numero_camiseta = 9;
                ELSE
                    local_goals := 0; away_goals := 0; mvp_id := NULL;
                END IF;

                INSERT INTO partido (
                    fecha_hora, estado, goles_local, goles_visitante, observaciones,
                    id_torneo, id_fecha, id_equipo_local, id_equipo_visitante, cancha, id_mvp
                ) VALUES (
                    match_time, match_state, local_goals, away_goals,
                    CASE WHEN match_state = 'FINALIZADO' THEN 'Partido disputado con normalidad.' ELSE 'Partido programado.' END,
                    tournament_id, date_id, local_team_id, away_team_id,
                    format('Complejo Deportivo %s', locations[tournament_index]), mvp_id
                ) RETURNING id_partido INTO match_id;

                IF match_state = 'FINALIZADO' THEN
                    FOR goal_index IN 1..local_goals LOOP
                        SELECT id_jugador INTO scorer_id FROM jugador
                        WHERE id_equipo = local_team_id AND numero_camiseta = CASE WHEN goal_index % 3 = 0 THEN 8 ELSE 9 END;
                        INSERT INTO gol (minuto, tipo_gol, observacion, id_partido, id_jugador, id_equipo)
                        VALUES (8 + goal_index * 13, 'Jugada', 'Gol del equipo local', match_id, scorer_id, local_team_id);
                    END LOOP;
                    FOR goal_index IN 1..away_goals LOOP
                        SELECT id_jugador INTO scorer_id FROM jugador
                        WHERE id_equipo = away_team_id AND numero_camiseta = CASE WHEN goal_index % 3 = 0 THEN 8 ELSE 9 END;
                        INSERT INTO gol (minuto, tipo_gol, observacion, id_partido, id_jugador, id_equipo)
                        VALUES (12 + goal_index * 14, 'Jugada', 'Gol del equipo visitante', match_id, scorer_id, away_team_id);
                    END LOOP;

                    SELECT id_jugador INTO card_player_id FROM jugador WHERE id_equipo = local_team_id AND numero_camiseta = 4;
                    INSERT INTO tarjeta (tipo_tarjeta, minuto, motivo, id_partido, id_jugador, id_equipo)
                    VALUES ('AMARILLA', 36 + match_index, 'Infracción táctica', match_id, card_player_id, local_team_id);
                    SELECT id_jugador INTO card_player_id FROM jugador WHERE id_equipo = away_team_id AND numero_camiseta = 5;
                    INSERT INTO tarjeta (tipo_tarjeta, minuto, motivo, id_partido, id_jugador, id_equipo)
                    VALUES ('AMARILLA', 52 + round_index, 'Juego brusco', match_id, card_player_id, away_team_id);

                    IF (tournament_index + round_index + match_index) % 13 = 0 THEN
                        INSERT INTO tarjeta (tipo_tarjeta, minuto, motivo, id_partido, id_jugador, id_equipo)
                        VALUES ('ROJA', 78, 'Doble amonestación', match_id, card_player_id, away_team_id);
                    END IF;
                END IF;
            END LOOP;
        END LOOP;

        IF tournament_states[tournament_index] = 'FINALIZADO' THEN
            UPDATE torneo SET id_campeon = team_ids[1] WHERE id_torneo = tournament_id;
        END IF;
    END LOOP;
END $$;

-- Imágenes públicas y determinísticas para visualizar perfiles completos.
-- Solo se aplican a los registros demo y no reemplazan cargas manuales.
UPDATE equipo
SET escudo = CASE
    WHEN id_equipo % 2 = 0 THEN '/uploads/escudo-demo-azul.png'
    ELSE '/uploads/escudo-demo-rojo.png'
END
WHERE codigo_publico LIKE 'EQ-Z%';

UPDATE jugador
SET foto = 'https://randomuser.me/api/portraits/men/' || ((id_jugador % 99) + 1)::TEXT || '.jpg'
WHERE codigo_publico LIKE 'JUG-Z%'
  AND (foto IS NULL OR btrim(foto) = '');

COMMIT;

SELECT estado, COUNT(*) AS torneos
FROM torneo
WHERE codigo_publico LIKE 'ZO-DEMO-%'
GROUP BY estado
ORDER BY estado;
