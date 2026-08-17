import "./TopScorersTable.css";

function TopScorersTable({ goleadores }) {
  return (
    <div className="top-table-card">
      <div className="top-table-header">
        <h2>Top 5 goleadores</h2>
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
              <td>{goleador.nombreJugador}</td>
              <td>{goleador.nombreEquipo}</td>
              <td>{goleador.cantidadGoles}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default TopScorersTable;