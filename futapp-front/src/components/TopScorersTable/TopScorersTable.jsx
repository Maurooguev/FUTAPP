import "./TopScorersTable.css";
import { Link } from "react-router-dom";

function TopScorersTable({ goleadores }) {
  return (
    <div className="top-table-card">
      <div className="top-table-header">
        <h2>Goleadores</h2>
      </div>

      <table className="top-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Jugador</th>
            <th>Equipo</th>
            <th>Goles</th>
          </tr>
        </thead>

        <tbody>
          {goleadores.map((goleador, index) => (
            <tr key={goleador.idJugador}>
              <td>{index + 1}</td>
              <td><Link to={`/jugador/${goleador.idJugador}`}>{goleador.nombreJugador}</Link></td>
              <td><Link to={`/equipo/${goleador.idEquipo}`}>{goleador.nombreEquipo}</Link></td>
              <td>{goleador.cantidadGoles}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default TopScorersTable;
