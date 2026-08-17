import "./TournamentCard.css";

function TournamentCard({ torneo, variant = "default", onClick }) {
  const estadoClass = torneo.estado?.toLowerCase();

  if (variant === "featured") {
    return (
      <div className="featured-tournament-card" onClick={onClick}>
        <div className="tournament-card-top">
          <span className={`status-badge ${estadoClass}`}>{torneo.estado}</span>
          <span className="tournament-location">{torneo.ubicacion}</span>
        </div>

        <h3>{torneo.nombre}</h3>
        <p>{torneo.descripcion}</p>

        <div className="tournament-meta">
          <span>{torneo.formato}</span>
          <span>{torneo.cantidadEquipos} equipos</span>
        </div>

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

      <p>{torneo.descripcion}</p>

      <div className="tournament-card-meta">
        <span>📍 {torneo.ubicacion}</span>
        <span>🏆 {torneo.formato}</span>
        <span>👥 {torneo.cantidadEquipos} equipos</span>
      </div>
    </div>
  );
}

export default TournamentCard;