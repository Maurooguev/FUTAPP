import { Link } from "react-router-dom";
import "./TournamentHero.css";

function TournamentHero({ torneo, fechaActual }) {
  return (
    <section className="tournament-hero">
      <div className="tournament-shell">
        <Link to="/" className="tournament-back">
          ← Volver
        </Link>

        <span className="tournament-chip">{torneo.formato}</span>

        <h1>{torneo.nombre}</h1>
        <p>{torneo.descripcion}</p>

        <div className="tournament-info-grid">
          <div className="tournament-info-box">
            <span>Estado</span>
            <strong>{torneo.estado}</strong>
          </div>

          <div className="tournament-info-box">
            <span>Ubicación</span>
            <strong>{torneo.ubicacion}</strong>
          </div>

          <div className="tournament-info-box">
            <span>Equipos</span>
            <strong>{torneo.cantidadEquipos}</strong>
          </div>

          <div className="tournament-info-box">
            <span>Fecha actual</span>
            <strong>
              {fechaActual ? `Fecha ${fechaActual.numeroFecha}` : "Sin fechas"}
            </strong>
          </div>
        </div>
      </div>
    </section>
  );
}

export default TournamentHero;