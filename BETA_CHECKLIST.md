# FUTAPP — beta privada

## Antes de compartir el enlace

- Usar una base PostgreSQL exclusiva para la beta; no reutilizar datos importantes.
- Configurar HTTPS, `PUBLIC_URL` y `CORS_ALLOWED_ORIGINS` con el dominio definitivo de la beta.
- Generar claves distintas y aleatorias para `DATABASE_PASSWORD` y `BETA_INVITE_CODE`.
- Configurar el correo SMTP y comprobar “Olvidé mi contraseña” desde un correo real.
- Ejecutar `./preflight.ps1` y realizar una copia de seguridad inicial.
- Crear una cuenta de prueba para cada rol: organizador, director y jugador.

## Recorrido que debe probar cada persona

1. Registrarse con el código de invitación, cerrar sesión y volver a ingresar.
2. Recargar la página y confirmar que la sesión continúa.
3. Solicitar recuperación de contraseña, usar el enlace recibido y volver a ingresar.
4. Completar exclusivamente las acciones correspondientes a su rol.
5. Intentar abrir una acción de otro rol y confirmar que FUTAPP la rechaza claramente.
6. Probar desde teléfono y computadora, anotando navegador y modelo del dispositivo.

## Recorridos por rol

- **Organizador:** crear torneo, invitar equipos, preparar fixture, cargar resultado y revisar solicitudes permitidas.
- **Director:** reclamar un equipo, administrar plantel, responder reclamos y verificar que no pueda crear torneos.
- **Jugador:** reclamar su ficha, editar solamente sus datos permitidos y verificar que no pueda administrar equipos.

## Cómo informar un problema

Pedir siempre: cuenta/rol, URL, hora aproximada, qué intentó hacer, qué esperaba,
qué ocurrió y captura de pantalla. No solicitar contraseñas, tokens, claves SMTP ni
el código privado de la beta. Clasificar como `bloqueante`, `importante` o `visual`.

## Ampliación de la beta

Empezar con 3–5 personas. Corregir primero accesos indebidos, pérdida de datos,
duplicados y bloqueos. Luego ampliar a 10–20 personas. Cambiar
`BETA_INVITE_CODE` si el código se comparte fuera del grupo previsto.
