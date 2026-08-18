import { Link } from "react-router-dom";
import "./TournamentHero.css";

function TournamentHero({ torneo, fechaActual }) {
  const inicial = torneo.nombre?.trim().charAt(0).toUpperCase() || "T";

  return (
    <section className="tournament-hero">
      <div className="tournament-shell">
        <Link to="/" className="tournament-back">
          ← Todos los torneos
        </Link>

        <div className="tournament-summary">
          <div className="tournament-emblem" aria-hidden="true">{inicial}</div>
          <div className="tournament-heading">
            <div className="tournament-kicker">
              <span className="tournament-chip">{torneo.formato}</span>
              <span className="tournament-public-code">{torneo.codigoPublico}</span>
            </div>
            <h1>{torneo.nombre}</h1>
            <p>{torneo.descripcion || "Competición de fútbol amateur"}</p>
          </div>
        </div>

        <div className="tournament-meta">
          <span><small>Estado</small><strong>{torneo.estado}</strong></span>
          <span><small>Ubicación</small><strong>{torneo.ubicacion}</strong></span>
          <span><small>Equipos</small><strong>{torneo.cantidadEquipos}</strong></span>
          <span><small>Jornada</small><strong>{fechaActual ? `Fecha ${fechaActual.numeroFecha}` : "Sin fechas"}</strong></span>
        </div>
      </div>

      <nav className="tournament-tabs" aria-label="Secciones del torneo">
        <div className="tournament-shell">
          <a href="#resumen">Resumen</a><a href="#tabla">Tabla</a><a href="#equipos">Equipos</a><a href="#stats">Estadísticas</a><a href="#fixture">Fixture</a>
        </div>
      </nav>
    </section>
  );
}

export default TournamentHero;
