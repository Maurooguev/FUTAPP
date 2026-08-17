import "./UpcomingMatches.css";

function UpcomingMatches({ partidos, onSelectPartido }) {
  return (
    <div className="upcoming-block" id="partidos">
      <div className="upcoming-header">
        <h2>Próximos partidos</h2>
      </div>

      {partidos.length === 0 ? (
        <div className="upcoming-empty">
          <p>No hay próximos partidos cargados.</p>
        </div>
      ) : (
        <div className="upcoming-grid">
          {partidos.map((partido) => (
            <div
              className="upcoming-card"
              key={partido.idPartido}
              onClick={() => onSelectPartido(partido)}
            >
              <div className="upcoming-badge">Fecha {partido.numeroFecha}</div>

              <h3>
                {partido.nombreEquipoLocal} vs {partido.nombreEquipoVisitante}
              </h3>

              <p>
                <strong>Hora:</strong> {partido.fechaHora}
              </p>
              <p>
                <strong>Estado:</strong> {partido.estado}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default UpcomingMatches;