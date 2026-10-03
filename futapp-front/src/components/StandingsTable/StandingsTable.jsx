import "./StandingsTable.css";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";

function StandingsTable({ tabla, title = "Tabla de posiciones" }) {
  const [showFullTable, setShowFullTable] = useState(false);

  useEffect(() => {
    if (!showFullTable) return undefined;
    const previousOverflow = document.body.style.overflow;
    const closeOnEscape = (event) => { if (event.key === "Escape") setShowFullTable(false); };
    document.body.style.overflow = "hidden";
    window.addEventListener("keydown", closeOnEscape);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", closeOnEscape);
    };
  }, [showFullTable]);

  const table = (className = "") => (
    <table className={`standings-table ${className}`.trim()}>
      <thead>
        <tr>
          <th>#</th><th>Equipo</th><th>Pts</th><th>PJ</th><th>PG</th><th>PE</th><th>PP</th><th>GF</th><th>GC</th><th>DG</th>
        </tr>
      </thead>
      <tbody>
        {tabla.map((fila, index) => (
          <tr key={fila.idEquipo} className={index === 0 ? "top-row" : ""}>
            <td>{index + 1}</td>
            <td className="team-cell"><Link to={`/equipo/${fila.idEquipo}`}>{fila.nombreEquipo}</Link></td>
            <td>{fila.puntos}</td><td>{fila.partidosJugados}</td><td>{fila.ganados}</td><td>{fila.empatados}</td><td>{fila.perdidos}</td><td>{fila.golesFavor}</td><td>{fila.golesContra}</td><td>{fila.diferenciaGol}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  return (
    <div className="standings-block" id="tabla">
      <div className="standings-header">
        <h2>{title}</h2>
      </div>

      <div className="standings-wrap">
        {table()}
      </div>
      <button type="button" className="standings-full-button" onClick={() => setShowFullTable(true)}>Ver tabla completa</button>
      {showFullTable && (
        <div className="standings-modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowFullTable(false); }}>
          <section className="standings-modal" role="dialog" aria-modal="true" aria-labelledby="standings-modal-title">
            <header><div><span>Clasificación</span><h2 id="standings-modal-title">{title}</h2></div><button type="button" onClick={() => setShowFullTable(false)} aria-label="Cerrar tabla completa">×</button></header>
            <p className="standings-swipe-hint">Todas las estadísticas de cada equipo.</p>
            <div className="standings-mobile-full-list">
              {tabla.map((fila, index) => (
                <article className={index === 0 ? "leader" : ""} key={fila.idEquipo}>
                  <div className="standings-mobile-team">
                    <span>{index + 1}</span>
                    <Link to={`/equipo/${fila.idEquipo}`} onClick={() => setShowFullTable(false)}>{fila.nombreEquipo}</Link>
                    <strong>{fila.puntos}<small>Pts</small></strong>
                  </div>
                  <dl>
                    <div><dt>PJ</dt><dd>{fila.partidosJugados}</dd></div>
                    <div><dt>PG</dt><dd>{fila.ganados}</dd></div>
                    <div><dt>PE</dt><dd>{fila.empatados}</dd></div>
                    <div><dt>PP</dt><dd>{fila.perdidos}</dd></div>
                    <div><dt>GF</dt><dd>{fila.golesFavor}</dd></div>
                    <div><dt>GC</dt><dd>{fila.golesContra}</dd></div>
                    <div><dt>DG</dt><dd>{fila.diferenciaGol}</dd></div>
                  </dl>
                </article>
              ))}
            </div>
          </section>
        </div>
      )}
    </div>
  );
}

export default StandingsTable;
