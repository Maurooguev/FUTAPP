import "./StandingsTable.css";

function StandingsTable({ tabla }) {
  return (
    <div className="standings-block" id="tabla">
      <div className="standings-header">
        <h2>Tabla de posiciones</h2>
      </div>

      <div className="standings-wrap">
        <table className="standings-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Equipo</th>
              <th>Pts</th>
              <th>PJ</th>
              <th>PG</th>
              <th>PE</th>
              <th>PP</th>
              <th>GF</th>
              <th>GC</th>
              <th>DG</th>
            </tr>
          </thead>

          <tbody>
            {tabla.map((fila, index) => (
              <tr key={fila.idEquipo} className={index === 0 ? "top-row" : ""}>
                <td>{index + 1}</td>
                <td className="team-cell">{fila.nombreEquipo}</td>
                <td>{fila.puntos}</td>
                <td>{fila.partidosJugados}</td>
                <td>{fila.ganados}</td>
                <td>{fila.empatados}</td>
                <td>{fila.perdidos}</td>
                <td>{fila.golesFavor}</td>
                <td>{fila.golesContra}</td>
                <td>{fila.diferenciaGol}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default StandingsTable;
