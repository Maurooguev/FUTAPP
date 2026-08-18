# Futapp

Aplicacion para administrar torneos de futbol amateur, equipos, jugadores,
partidos, resultados y estadisticas.

## Estructura

- `futapp-back`: API REST con Java 21, Spring Boot, JPA, PostgreSQL y Flyway.
- `futapp-front`: aplicacion web con React y Vite.

## Requisitos

- Java 21
- Node.js y npm
- Docker Desktop

## Iniciar todo

Desde la carpeta raiz:

```powershell
.\dev.ps1
```

El script inicia PostgreSQL, abre el backend en otra terminal y ejecuta el
frontend en la terminal actual.

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Estado del backend: http://localhost:8080/status

Para detener cada servidor, presionar `Ctrl + C` en su terminal.

PostgreSQL queda ejecutandose en Docker para conservar los datos. Se puede
detener sin borrarlos con:

```powershell
docker compose stop postgres
```

## Iniciar por separado

Backend:

```powershell
docker compose up -d --wait postgres
.\futapp-back\mvnw.cmd -f .\futapp-back\pom.xml spring-boot:run
```

Frontend:

```powershell
npm --prefix .\futapp-front run dev
```

## Configuracion del frontend

La API utiliza `http://localhost:8080` por defecto. Para cambiarla, copiar
`futapp-front/.env.example` como `futapp-front/.env.local` y modificar
`VITE_API_URL`.

## Base de datos

La estructura se administra con migraciones versionadas de Flyway. Hibernate
solo valida que las entidades coincidan con el esquema y no modifica tablas al
arrancar.

La configuracion local predeterminada esta en `compose.yaml`. Para cambiar las
credenciales o conectar una base externa se pueden definir estas variables:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `CORS_ALLOWED_ORIGINS` (uno o varios origenes web separados por coma)
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` y `POSTGRES_PORT`

`.env.example` contiene un ejemplo para desarrollo. No se deben subir
credenciales reales al repositorio.

### Migrar los datos anteriores de H2

Esta operacion se ejecuta una sola vez y exige que PostgreSQL todavia no tenga
datos de Futapp. Conserva IDs, usuarios, contrasenas cifradas, tokens, torneos,
equipos, jugadores, fechas, partidos y estadisticas.

1. Detener el backend anterior con `Ctrl + C` para liberar el archivo H2.
2. Abrir Docker Desktop.
3. Desde la carpeta raiz ejecutar:

```powershell
.\migrate-h2-to-postgres.ps1
```

Si el destino contiene datos o ocurre un error, la importacion se cancela y la
transaccion se revierte para evitar una migracion parcial.

### Respaldos

Crear un respaldo en la carpeta local ignorada `backups`:

```powershell
.\backup-postgres.ps1
```

Restaurarlo (reemplaza los datos actuales y solicita confirmacion):

```powershell
.\restore-postgres.ps1 -BackupPath .\backups\futapp-AAAAMMDD-HHMMSS.dump
```

## Usuarios y permisos

El registro crea una cuenta de organizador con contraseña cifrada mediante
BCrypt. Al iniciar sesión, el backend entrega un token Bearer revocable con una
vigencia de siete días. Las consultas son públicas; crear, editar o eliminar
datos requiere una sesión con rol `ORGANIZADOR` o `ADMIN`.

Los clientes pueden validar y recuperar la sesion guardada mediante
`GET /auth/me` enviando `Authorization: Bearer <token>`. Esto permite compartir
el mismo flujo de acceso entre la web y futuras aplicaciones Android/iOS.

El DNI de los jugadores es opcional. Cuando se informa, debe ser único. El
número de camiseta también debe ser único dentro de cada equipo.

## Participaciones e historial

Los perfiles permanentes de clubes y jugadores se complementan con
`InscripcionEquipo` e `InscripcionJugador`. Estas relaciones conservan el
torneo, plantel, dorsal, posición, estado y fechas de alta o baja de cada
participación. Los datos existentes se migran automáticamente y las nuevas
altas generan su inscripción correspondiente.

Esta base permite que un club participe en varios torneos y que un jugador
forme parte de distintos planteles sin duplicar su perfil público.

Desde la administración del torneo se puede crear un club o inscribir uno ya
existente. Dentro de cada plantel también se puede crear un jugador o elegir un
perfil preexistente, asignándole dorsal y posición específicos para ese torneo.

Cada torneo nuevo queda asociado al organizador que lo crea y recibe un codigo
publico unico con formato `FUT-XXXXXX`. El buscador acepta ese codigo o el
nombre. La seccion `Mis torneos` muestra las competencias administrables por la
sesion actual; `ADMIN` puede gestionar todas. Los torneos historicos sin
propietario permanecen publicos en modo consulta.

## Verificacion

```powershell
.\futapp-back\mvnw.cmd -f .\futapp-back\pom.xml test
.\futapp-back\mvnw.cmd -f .\futapp-back\pom.xml -Ph2-migration -DskipTests compile
npm --prefix .\futapp-front run lint
npm --prefix .\futapp-front run build
```
