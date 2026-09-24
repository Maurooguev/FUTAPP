UPDATE torneo
SET puntos_victoria = 0,
    puntos_empate = 0,
    puntos_derrota = 0,
    criterio_desempate = 'PENALES',
    ida_vuelta = FALSE
WHERE UPPER(formato) IN (
    'ELIMINACIÓN DIRECTA',
    'ELIMINACION DIRECTA',
    'ELIMINACION_DIRECTA'
);
