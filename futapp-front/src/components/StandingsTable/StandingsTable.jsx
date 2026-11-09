import "./StandingsTable.css";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import EntityImage from "../EntityImage/EntityImage";

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
          <th>#</th><th className="crest-column" aria-label="Escudo"></th><th>Equipo</th><th>Pts</th><th>PJ</th><th>PG</th><th>PE</th><th>PP</th><th>GF</th><th>GC</th><th>DG</th>
        </tr>
      </thead>
      <tbody>
        {tabla.map((fila, index) => (
          <tr key={fila.idEquipo} className={index === 0 ? "top-row" : ""}>
            <td>{index + 1}</td>
            <td className="crest-cell"><Link to={`/equipo/${fila.idEquipo}`} aria-label={`Ver ${fila.nombreEquipo}`}><EntityImage type="team" value={fila.escudoEquipo} alt="" /></Link></td>
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
            <p className="standings-swipe-hint">Deslizá hacia los costados para ver todas las estadísticas.</p>
            <div className="standings-modal-scroll">{table("standings-table-full")}</div>
          </section>
        </div>
      )}
    </div>
  );
}

export default StandingsTable;
