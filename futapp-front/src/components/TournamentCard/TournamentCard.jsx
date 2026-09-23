import "./TournamentCard.css";

function TournamentCard({ torneo, variant = "default", onClick }) {
  const estadoClass = torneo.estado?.toLowerCase();
  const nextMatch = torneo.proximoPartido;
  const nextMatchLabel = nextMatch?.fechaHora
    ? new Intl.DateTimeFormat("es-AR", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(nextMatch.fechaHora))
    : null;

  if (variant === "featured") {
    return (
      <div className="featured-tournament-card" onClick={onClick}>
        <div className="tournament-card-top">
          <span className={`status-badge ${estadoClass}`}>{torneo.estado}</span>
          <span className="tournament-location">{torneo.ubicacion}</span>
        </div>
        <span className="tournament-code">{torneo.codigoPublico}</span>

        <h3>{torneo.nombre}</h3>
        <div className="tournament-meta">
          <span>{torneo.formato}</span>
          <span>{torneo.cantidadEquipos} equipos</span>
        </div>

        {torneo.nombreCampeon && <div className="tournament-card-highlight">🏆 {torneo.nombreCampeon}</div>}
        {!torneo.nombreCampeon && nextMatchLabel && <div className="tournament-card-highlight">📅 Próximo · {nextMatchLabel}</div>}
        {torneo.nombrePropietario && <small className="tournament-organizer">✓ {torneo.nombrePropietario}</small>}

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
        <h3>{torneo.nombre}</h3>
        <span className={`status-badge small ${estadoClass}`}>{torneo.estado}</span>
      </div>
      <span className="tournament-code">{torneo.codigoPublico}</span>

      <div className="tournament-card-meta">
        <span>📍 {torneo.ubicacion}</span>
        <span>🏆 {torneo.formato}</span>
        <span>👥 {torneo.cantidadEquipos} equipos</span>
      </div>
      {torneo.nombreCampeon && <div className="tournament-card-highlight">🏆 Campeón: {torneo.nombreCampeon}</div>}
      {!torneo.nombreCampeon && nextMatchLabel && <div className="tournament-card-highlight">📅 Próximo partido · {nextMatchLabel}</div>}
      {torneo.nombrePropietario && <small className="tournament-organizer">✓ Organiza {torneo.nombrePropietario}</small>}
    </div>
  );
}

export default TournamentCard;
