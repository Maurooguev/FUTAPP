UPDATE torneo SET descripcion = INITCAP(LOWER(BTRIM(descripcion))) WHERE descripcion IS NOT NULL;
UPDATE equipo SET descripcion = INITCAP(LOWER(BTRIM(descripcion))) WHERE descripcion IS NOT NULL;
UPDATE perfil_organizador SET descripcion = INITCAP(LOWER(BTRIM(descripcion))) WHERE descripcion IS NOT NULL;
UPDATE jugador SET biografia = INITCAP(LOWER(BTRIM(biografia))) WHERE biografia IS NOT NULL;
UPDATE fecha SET descripcion = INITCAP(LOWER(BTRIM(descripcion))) WHERE descripcion IS NOT NULL;
