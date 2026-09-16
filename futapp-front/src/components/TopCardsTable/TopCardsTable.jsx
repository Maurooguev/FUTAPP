import "./TopCardsTable.css";
import { Link } from "react-router-dom";

function TopCardsTable({ jugadores }) {
  return (
    <div className="cards-table-card">
      <div className="cards-table-header">
        <h2>Amarillas</h2>
      </div>

      <table className="cards-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Jugador</th>
            <th>Equipo</th>
            <th>Amarillas</th>
          </tr>
        </thead>

        <tbody>
          {jugadores.map((jugador, index) => (
            <tr key={jugador.idJugador}>
              <td>{index + 1}</td>
              <td><Link to={`/jugador/${jugador.idJugador}`}>{jugador.nombreJugador}</Link></td>
              <td><Link to={`/equipo/${jugador.idEquipo}`}>{jugador.nombreEquipo}</Link></td>
              <td>{jugador.cantidad}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default TopCardsTable;
