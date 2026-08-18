import { useMemo, useState } from "react";
import "./FixtureByDate.css";

function FixtureByDate({ fechas, partidos, onSelectPartido, canManage, onEditDate, onDeleteDate, onEditMatch, onDeleteMatch, onLoadResult }) {
  const fechasOrdenadas = useMemo(() => {
    return [...fechas].sort((a, b) => a.numeroFecha - b.numeroFecha);
  }, [fechas]);

    const fechaInicial = useMemo(() => {
        if (fechasOrdenadas.length === 0) return null;

     const fechaConPartidoProgramado = fechasOrdenadas.find((fecha) =>
        partidos.some(
            (partido) =>
                partido.idFecha === fecha.idFecha &&
                partido.estado?.toUpperCase() === "PROGRAMADO"
        )
     );

        return fechaConPartidoProgramado || fechasOrdenadas[fechasOrdenadas.length - 1];
    }, [fechasOrdenadas, partidos]);

    const [fechaSeleccionada, setFechaSeleccionada] = useState(null);

    const fechaActual = fechaSeleccionada || fechaInicial;

  const partidosDeFecha = useMemo(() => {
    if (!fechaActual) return [];

    return partidos.filter(
      (partido) => partido.idFecha === fechaActual.idFecha
    );
  }, [partidos, fechaActual]);

  if (fechasOrdenadas.length === 0) {
    return (
      <section className="fixture-block" id="fixture">
        <h2>Fixture</h2>
        <div className="fixture-empty">
          <p>No hay fechas cargadas.</p>
        </div>
      </section>
    );
  }

  return (
    <section className="fixture-block" id="fixture">
      <div className="fixture-header">
        <div>
          <h2>Fixture del torneo</h2>
          <p>Consultá las fechas jugadas y las próximas a disputar.</p>
        </div>
      </div>

      <div className="fixture-tabs">
        {fechasOrdenadas.map((fecha) => (
          <button
            key={fecha.idFecha}
            className={
              fechaActual?.idFecha === fecha.idFecha
                ? "fixture-tab active"
                : "fixture-tab"
            }
            onClick={() => setFechaSeleccionada(fecha)}
          >
            Fecha {fecha.numeroFecha}
          </button>
        ))}
      </div>

      <div className="fixture-current">
        <h3>Fecha {fechaActual.numeroFecha}</h3>
        <span>{fechaActual.estado}</span>
        {canManage && <div className="fixture-admin-actions"><button onClick={() => onEditDate(fechaActual)}>Editar</button><button className="danger" onClick={() => onDeleteDate(fechaActual)}>Eliminar</button></div>}
      </div>

      {partidosDeFecha.length === 0 ? (
        <div className="fixture-empty">
          <p>No hay partidos cargados para esta fecha.</p>
        </div>
      ) : (
        <div className="fixture-grid">
          {partidosDeFecha.map((partido) => (
            <div
              className="fixture-match-card"
              key={partido.idPartido}
              onClick={() => onSelectPartido(partido)}
            >
              <div className="fixture-match-status">{partido.estado}</div>

              <div className="fixture-score-line">
                <span>{partido.nombreEquipoLocal}</span>

                <strong>
                  {partido.estado?.toUpperCase() === "FINALIZADO"
                    ? `${partido.golesLocal} - ${partido.golesVisitante}`
                    : "vs"}
                </strong>

                <span>{partido.nombreEquipoVisitante}</span>
              </div>

              <p>{partido.fechaHora || partido.observaciones || "Horario a confirmar"}</p>
              {canManage && <div className="fixture-quick-actions" onClick={(event) => event.stopPropagation()}>
                <button className="result" onClick={() => onLoadResult(partido)}>Cargar resultado</button>
                <button className="incidents" onClick={() => onSelectPartido(partido)}>Goles y tarjetas</button>
                <button onClick={() => onEditMatch(partido)}>Editar</button>
                <button className="danger" onClick={() => onDeleteMatch(partido)}>Eliminar</button>
              </div>}
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export default FixtureByDate;
