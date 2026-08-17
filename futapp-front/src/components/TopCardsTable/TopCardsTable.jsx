import "./TopCardsTable.css";

function TopCardsTable({ jugadores }) {
  return (
    <div className="cards-table-card">
      <div className="cards-table-header">
        <h2>Top 5 amarillas</h2>
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
              <td>{jugador.nombreJugador}</td>
              <td>{jugador.nombreEquipo}</td>
              <td>{jugador.cantidad}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default TopCardsTable;