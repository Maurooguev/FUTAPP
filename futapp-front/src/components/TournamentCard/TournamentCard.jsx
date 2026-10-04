import "./TournamentCard.css";
import EntityImage from "../EntityImage/EntityImage";
import CopyableCode from "../CopyableCode/CopyableCode";
import TournamentIcon from "../TournamentIcon/TournamentIcon";

function TournamentCard({ torneo, variant = "default", onClick }) {
  const estadoClass = torneo.estado?.toLowerCase();
  const estadoLabel = ({ activo: "ACT", finalizado: "FIN", publicado: "PUB", borrador: "BOR" })[estadoClass]
    || torneo.estado?.slice(0, 3).toUpperCase();
  const nextMatch = torneo.proximoPartido;
  const nextMatchLabel = nextMatch?.fechaHora
    ? new Intl.DateTimeFormat("es-AR", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(nextMatch.fechaHora))
    : null;
  const podium = torneo.podio || [];

  const compactInfo = (
    <div className="tournament-card-compact-meta">
      <span>📍 {torneo.ubicacion || "Ubicación a confirmar"}</span>
      <span aria-hidden="true">·</span>
      <span>{torneo.formato}</span>
      <span aria-hidden="true">·</span>
      <span>{torneo.cantidadEquipos} equipos</span>
    </div>
  );

  const podiumTable = podium.length > 0 && (
    <div className="tournament-card-podium" aria-label="Primeros puestos">
      <div className="tournament-card-podium-heading"><span>Posiciones</span><b>Pts</b></div>
      {podium.map((team, index) => (
        <div className="tournament-card-podium-row" key={team.idEquipo}>
          <span className="podium-position">{index + 1}</span>
          <EntityImage type="team" value={team.escudoEquipo} alt="" />
          <strong>{team.nombreEquipo}</strong>
          <b>{team.puntos}</b>
        </div>
      ))}
    </div>
  );

  if (variant === "featured") {
    return (
      <div className="featured-tournament-card" onClick={onClick}>
        <div className="tournament-card-top">
          <span className={`status-badge ${estadoClass}`} title={torneo.estado} aria-label={`Estado: ${torneo.estado}`}>{estadoLabel}</span>
          <span className="tournament-location">{torneo.ubicacion}</span>
        </div>
        <div className="tournament-card-title"><TournamentIcon value={torneo.logoOrganizador} aria-hidden="true" /><h3 title={torneo.nombre}>{torneo.nombre}</h3></div>
        <CopyableCode className="tournament-code" value={torneo.codigoPublico} />
        {compactInfo}

        {podiumTable}
        {torneo.nombreCampeon && <div className="tournament-card-highlight tournament-card-champion"><EntityImage type="team" value={torneo.escudoCampeon} alt="" /><span>Campeón · {torneo.nombreCampeon}</span></div>}
        {!torneo.nombreCampeon && nextMatchLabel && <div className="tournament-card-highlight">📅 Próximo · {nextMatchLabel}</div>}
        <button 
        type="button"
        className="tournament-link"
        onClick={(e) => {
            e.stopPropagation();
            onClick();
        }}
    >
        Ver torneo →
        </button>
      </div>
    );
  }

  return (
    <div className="tournament-card" onClick={onClick}>
      <div className="tournament-card-header">
        <div className="tournament-card-title"><TournamentIcon value={torneo.logoOrganizador} aria-hidden="true" /><h3 title={torneo.nombre}>{torneo.nombre}</h3></div>
        <span className={`status-badge small ${estadoClass}`} title={torneo.estado} aria-label={`Estado: ${torneo.estado}`}>{estadoLabel}</span>
      </div>
      <CopyableCode className="tournament-code" value={torneo.codigoPublico} />
      {compactInfo}
      {podiumTable}
      {torneo.nombreCampeon && <div className="tournament-card-highlight tournament-card-champion"><EntityImage type="team" value={torneo.escudoCampeon} alt="" /><span>Campeón · {torneo.nombreCampeon}</span></div>}
      {!torneo.nombreCampeon && nextMatchLabel && <div className="tournament-card-highlight">📅 Próximo partido · {nextMatchLabel}</div>}
    </div>
  );
}

export default TournamentCard;
