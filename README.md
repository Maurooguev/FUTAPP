# Futapp

Aplicacion para administrar torneos de futbol amateur, equipos, jugadores,
partidos, resultados y estadisticas.

## Estructura

- `futapp-back`: API REST con Java 21, Spring Boot, JPA y H2.
- `futapp-front`: aplicacion web con React y Vite.

## Requisitos

- Java 21
- Node.js y npm

## Iniciar todo

Desde la carpeta raiz:

```powershell
.\dev.ps1
```

El script abre el backend en otra terminal y ejecuta el frontend en la terminal
actual.

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Estado del backend: http://localhost:8080/status
- Consola H2: http://localhost:8080/h2-console

Para detener cada servidor, presionar `Ctrl + C` en su terminal.

## Iniciar por separado

Backend:

```powershell
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

## Usuarios y permisos

El registro crea una cuenta de organizador con contraseña cifrada mediante
BCrypt. Al iniciar sesión, el backend entrega un token Bearer revocable con una
vigencia de siete días. Las consultas son públicas; crear, editar o eliminar
datos requiere una sesión con rol `ORGANIZADOR` o `ADMIN`.

El DNI de los jugadores es opcional. Cuando se informa, debe ser único. El
número de camiseta también debe ser único dentro de cada equipo.

## Verificacion

```powershell
.\futapp-back\mvnw.cmd -f .\futapp-back\pom.xml test
npm --prefix .\futapp-front run lint
npm --prefix .\futapp-front run build
```
