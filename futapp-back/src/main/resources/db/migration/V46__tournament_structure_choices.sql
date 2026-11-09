ALTER TABLE torneo ADD COLUMN cantidad_zonas INTEGER;
ALTER TABLE torneo ADD COLUMN clasificados_por_zona INTEGER;
ALTER TABLE torneo ADD COLUMN clasificados_playoff INTEGER;

UPDATE torneo t SET cantidad_zonas = datos.cantidad
FROM (
    SELECT d.id_torneo, COUNT(z.id_zona)::INTEGER AS cantidad
    FROM competicion_division d
    JOIN competicion_fase f ON f.id_division = d.id_division AND f.tipo = 'ZONAS'
    JOIN competicion_zona z ON z.id_fase = f.id_fase
    GROUP BY d.id_torneo
) datos
WHERE t.id_torneo = datos.id_torneo;

UPDATE torneo t SET clasificados_por_zona = datos.por_zona
FROM (
    SELECT d.id_torneo, (playoff.clasificados / NULLIF(COUNT(z.id_zona), 0))::INTEGER AS por_zona
    FROM competicion_division d
    JOIN competicion_fase grupos ON grupos.id_division = d.id_division AND grupos.tipo = 'ZONAS'
    JOIN competicion_zona z ON z.id_fase = grupos.id_fase
    JOIN competicion_fase playoff ON playoff.id_division = d.id_division AND playoff.tipo = 'ELIMINACION_DIRECTA'
    GROUP BY d.id_torneo, playoff.clasificados
) datos
WHERE t.id_torneo = datos.id_torneo;

UPDATE torneo t SET clasificados_playoff = datos.clasificados
FROM (
    SELECT d.id_torneo, f.clasificados
    FROM competicion_division d
    JOIN competicion_fase f ON f.id_division = d.id_division
    WHERE f.tipo = 'ELIMINACION_DIRECTA'
) datos
WHERE t.id_torneo = datos.id_torneo AND UPPER(t.formato) LIKE 'LIGA + PLAYOFF%';
