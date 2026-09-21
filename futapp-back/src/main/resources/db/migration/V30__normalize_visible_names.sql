UPDATE usuario SET nombre = INITCAP(LOWER(BTRIM(nombre))) WHERE nombre IS NOT NULL;

UPDATE torneo SET
    nombre = INITCAP(LOWER(BTRIM(nombre))),
    ubicacion = INITCAP(LOWER(BTRIM(ubicacion)))
WHERE nombre IS NOT NULL OR ubicacion IS NOT NULL;

UPDATE equipo SET
    nombre = INITCAP(LOWER(BTRIM(nombre))),
    color_camiseta = INITCAP(LOWER(BTRIM(color_camiseta))),
    barrio = INITCAP(LOWER(BTRIM(barrio))),
    responsable = INITCAP(LOWER(BTRIM(responsable)))
WHERE nombre IS NOT NULL OR color_camiseta IS NOT NULL OR barrio IS NOT NULL OR responsable IS NOT NULL;

UPDATE jugador SET
    nombre = INITCAP(LOWER(BTRIM(nombre))),
    apellido = INITCAP(LOWER(BTRIM(apellido))),
    apodo = INITCAP(LOWER(BTRIM(apodo))),
    posicion = INITCAP(LOWER(BTRIM(posicion))),
    equipo_favorito = INITCAP(LOWER(BTRIM(equipo_favorito))),
    barrio = INITCAP(LOWER(BTRIM(barrio))),
    nacionalidad = INITCAP(LOWER(BTRIM(nacionalidad)))
WHERE nombre IS NOT NULL OR apellido IS NOT NULL OR apodo IS NOT NULL OR posicion IS NOT NULL
   OR equipo_favorito IS NOT NULL OR barrio IS NOT NULL OR nacionalidad IS NOT NULL;

UPDATE perfil_organizador SET nombre_publico = INITCAP(LOWER(BTRIM(nombre_publico)))
WHERE nombre_publico IS NOT NULL;

UPDATE inscripcion_equipo SET categoria = INITCAP(LOWER(BTRIM(categoria))) WHERE categoria IS NOT NULL;
UPDATE inscripcion_jugador SET posicion = INITCAP(LOWER(BTRIM(posicion))) WHERE posicion IS NOT NULL;
UPDATE competicion_division SET nombre = INITCAP(LOWER(BTRIM(nombre))) WHERE nombre IS NOT NULL;
UPDATE competicion_fase SET nombre = INITCAP(LOWER(BTRIM(nombre))) WHERE nombre IS NOT NULL;
UPDATE competicion_zona SET nombre = INITCAP(LOWER(BTRIM(nombre))) WHERE nombre IS NOT NULL;
UPDATE solicitud_reclamo SET categoria = INITCAP(LOWER(BTRIM(categoria))) WHERE categoria IS NOT NULL;
