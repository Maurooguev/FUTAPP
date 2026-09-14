CREATE INDEX IF NOT EXISTS ix_partido_torneo_estado ON partido (id_torneo, estado);
CREATE INDEX IF NOT EXISTS ix_partido_local_torneo ON partido (id_equipo_local, id_torneo);
CREATE INDEX IF NOT EXISTS ix_partido_visitante_torneo ON partido (id_equipo_visitante, id_torneo);
CREATE INDEX IF NOT EXISTS ix_gol_jugador ON gol (id_jugador);
CREATE INDEX IF NOT EXISTS ix_tarjeta_jugador ON tarjeta (id_jugador);
CREATE INDEX IF NOT EXISTS ix_torneo_campeon ON torneo (id_campeon) WHERE id_campeon IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_reclamo_solicitante_fecha ON solicitud_reclamo (id_solicitante, fecha_solicitud DESC);
CREATE INDEX IF NOT EXISTS ix_auth_token_expiracion ON auth_token (expires_at);
CREATE INDEX IF NOT EXISTS ix_reset_token_expiracion ON password_reset_token (expires_at);
