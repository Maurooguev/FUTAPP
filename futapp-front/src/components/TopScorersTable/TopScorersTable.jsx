import "./TopScorersTable.css";
import { Link } from "react-router-dom";
import EntityImage from "../EntityImage/EntityImage";

const podiumLabels = ["1°", "2°", "3°"];

function PlayerAvatar({ goleador }) {
  return (
    <span className="scorer-avatar">
      <EntityImage type="player" value={goleador.fotoJugador} alt={`Foto de ${goleador.nombreJugador}`} />
    </span>
  );
}

function TopScorersTable({ goleadores }) {
  const [lider, ...resto] = goleadores;

  return (
    <section className="top-scorers-card" aria-labelledby="goleadores-title">
      <header className="top-scorers-header">
        <div>
          <span>Estadísticas</span>
          <h2 id="goleadores-title">Goleadores</h2>
        </div>
        <span className="top-scorers-icon" aria-hidden="true">⚽</span>
      </header>

      {!lider && <div className="top-scorers-empty"><span>⚽</span><strong>Todavía no hay goleadores</strong><small>Los jugadores aparecerán cuando se carguen goles.</small></div>}

      {lider && <>
        <article className="leading-scorer">
          <span className="leading-scorer-rank">1</span>
          <PlayerAvatar goleador={lider} />
          <div className="leading-scorer-info">
            <small>Máximo goleador</small>
            <Link to={`/jugador/${lider.idJugador}`}>{lider.nombreJugador}</Link>
            <Link className="leading-scorer-team" to={`/equipo/${lider.idEquipo}`}>{lider.nombreEquipo}</Link>
          </div>
          <div className="leading-scorer-goals"><strong>{lider.cantidadGoles}</strong><span>{lider.cantidadGoles === 1 ? "gol" : "goles"}</span></div>
        </article>

        {resto.length > 0 && <ol className="scorers-ranking" start="2">
          {resto.map((goleador, index) => {
            const position = index + 2;
            return <li key={`${goleador.idJugador}-${goleador.idEquipo}`}>
              <span className={`scorer-position position-${position}`}>{podiumLabels[position - 1] || position}</span>
              <PlayerAvatar goleador={goleador} />
              <div className="scorer-info">
                <Link to={`/jugador/${goleador.idJugador}`}>{goleador.nombreJugador}</Link>
                <Link to={`/equipo/${goleador.idEquipo}`}>{goleador.nombreEquipo}</Link>
              </div>
              <strong className="scorer-goals">{goleador.cantidadGoles}<small>{goleador.cantidadGoles === 1 ? " gol" : " goles"}</small></strong>
            </li>;
          })}
        </ol>}
      </>}
    </section>
  );
}

export default TopScorersTable;
