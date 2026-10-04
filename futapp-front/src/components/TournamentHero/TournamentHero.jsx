import { Link } from "react-router-dom";
import "./TournamentHero.css";
import FavoriteButton from "../FavoriteButton/FavoriteButton";
import CopyableCode from "../CopyableCode/CopyableCode";
import TournamentIcon from "../TournamentIcon/TournamentIcon";

const TABS = [["resumen", "Resumen"], ["info", "Info"], ["tabla", "Tabla"], ["equipos", "Equipos"], ["stats", "Estadísticas"], ["fixture", "Fixture"]];

function TournamentHero({ torneo, fechaActual, activeTab = "resumen", onTabChange }) {
  const tabs = torneo.formato?.toLowerCase() === "eliminación directa" ? TABS.filter(([id]) => id !== "tabla") : TABS;

  return (
    <>
    <section className="tournament-hero">
      <div className="tournament-shell">
        <Link to="/" className="tournament-back">
          ← Todos los torneos
        </Link>

        <div className="tournament-summary">
          <TournamentIcon className="tournament-emblem" value={torneo.logoOrganizador} aria-hidden="true" />
          <div className="tournament-heading">
            <div className="tournament-kicker">
              <span className="tournament-chip">{torneo.formato}</span>
              <CopyableCode className="tournament-public-code" value={torneo.codigoPublico} />
            </div>
            <h1>{torneo.nombre}</h1>
            <p>{torneo.descripcion || "Competición de fútbol amateur"}</p>
          </div>
          <FavoriteButton type="torneos" id={torneo.idTorneo} />
        </div>

        <div className="tournament-meta">
          <span><small>Estado</small><strong>{torneo.estado}</strong></span>
          <span><small>Ubicación</small><strong>{torneo.ubicacion}</strong></span>
          <span><small>Equipos</small><strong>{torneo.cantidadEquipos}</strong></span>
          <span><small>Jornada</small><strong>{fechaActual ? `Fecha ${fechaActual.numeroFecha}` : "Sin fechas"}</strong></span>
          <span><small>Visitas</small><strong>👁 {torneo.visitas || 0}</strong></span>
        </div>
      </div>

    </section>
    <nav className="tournament-tabs" aria-label="Secciones del torneo">
      <div className="tournament-shell">
        {tabs.map(([id, label]) => <button key={id} type="button" className={activeTab === id ? "active" : ""} aria-current={activeTab === id ? "page" : undefined} onClick={() => onTabChange?.(id)}>{label}</button>)}
      </div>
    </nav>
    </>
  );
}

export default TournamentHero;
