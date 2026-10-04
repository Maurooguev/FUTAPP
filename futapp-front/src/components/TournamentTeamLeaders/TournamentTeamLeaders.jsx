import { Link } from "react-router-dom";
import EntityImage from "../EntityImage/EntityImage";
import "./TournamentTeamLeaders.css";

function Leader({ label, value, team, tone }) {
  return <div className={`team-leader-row ${tone}`}>
    <span className="team-leader-image"><EntityImage type="team" value={team?.escudoEquipo} alt="" /></span>
    <div><small>{label}</small>{team ? <Link to={`/equipo/${team.idEquipo}`}>{team.nombreEquipo}</Link> : <strong>Sin datos</strong>}</div>
    {team && <b>{value}</b>}
  </div>;
}

export default function TournamentTeamLeaders({ tabla }) {
  const conPartidos = tabla.filter((equipo) => equipo.partidosJugados > 0);
  const masGoleador = [...conPartidos].sort((a, b) => b.golesFavor - a.golesFavor || a.nombreEquipo.localeCompare(b.nombreEquipo))[0];
  const menosVencida = [...conPartidos].sort((a, b) => a.golesContra - b.golesContra || b.partidosJugados - a.partidosJugados || a.nombreEquipo.localeCompare(b.nombreEquipo))[0];

  return <section className="team-leaders-card">
    <header><div><span>Rendimiento</span><h2>Equipos destacados</h2></div><b aria-hidden="true">◆</b></header>
    <div className="team-leaders-list">
      <Leader label="Equipo más goleador" team={masGoleador} value={`${masGoleador?.golesFavor ?? 0} GF`} tone="attack" />
      <Leader label="Valla menos vencida" team={menosVencida} value={`${menosVencida?.golesContra ?? 0} GC`} tone="defense" />
    </div>
  </section>;
}

