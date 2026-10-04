import "./TopCardsTable.css";
import { Link } from "react-router-dom";
import EntityImage from "../EntityImage/EntityImage";

function TopCardsTable({ jugadores }) {
  return (
    <section className="yellow-cards-card" aria-labelledby="amarillas-title">
      <header className="yellow-cards-header">
        <div>
          <span>Disciplina</span>
          <h2 id="amarillas-title">Tarjetas amarillas</h2>
        </div>
        <span className="yellow-card-icon" aria-hidden="true" />
      </header>

      {jugadores.length === 0
        ? <div className="yellow-cards-empty"><span className="yellow-card-symbol" /><strong>Sin amonestados</strong><small>Todavía no se registraron tarjetas amarillas.</small></div>
        : <ol className="yellow-cards-ranking">
          {jugadores.map((jugador, index) => <li key={`${jugador.idJugador}-${jugador.idEquipo}`}>
            <span className="yellow-card-position">{index + 1}</span>
            <span className="yellow-card-avatar"><EntityImage type="player" value={jugador.fotoJugador} alt={`Foto de ${jugador.nombreJugador}`} /></span>
            <div className="yellow-card-player">
              <Link to={`/jugador/${jugador.idJugador}`}>{jugador.nombreJugador}</Link>
              <Link to={`/equipo/${jugador.idEquipo}`}>{jugador.nombreEquipo}</Link>
            </div>
            <div className="yellow-card-count"><span /><strong>{jugador.cantidad}</strong><small>{jugador.cantidad === 1 ? "tarjeta" : "tarjetas"}</small></div>
          </li>)}
        </ol>}
    </section>
  );
}

export default TopCardsTable;
