import { useState } from "react";
import "./TournamentExports.css";

const csvCell = (value) => `"${String(value ?? "").replaceAll('"', '""')}"`;
const downloadCsv = (filename, rows) => {
  const content = "\uFEFF" + rows.map((row) => row.map(csvCell).join(";")).join("\r\n");
  const url = URL.createObjectURL(new Blob([content], { type: "text/csv;charset=utf-8" }));
  const link = document.createElement("a"); link.href = url; link.download = filename; link.click(); URL.revokeObjectURL(url);
};
const safeName = (name) => name.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

function TournamentExports({ torneo, tabla, fechas, partidos, goleadores }) {
  const [open, setOpen] = useState(false);
  const name = safeName(torneo.nombre) || "torneo";
  const sortedDates = [...fechas].sort((a, b) => a.numeroFecha - b.numeroFecha);
  const exportStandings = () => downloadCsv(`${name}-tabla.csv`, [["Posición", "Equipo", "Pts", "PJ", "PG", "PE", "PP", "GF", "GC", "DG"], ...tabla.map((row, index) => [index + 1, row.nombreEquipo, row.puntos, row.partidosJugados, row.ganados, row.empatados, row.perdidos, row.golesFavor, row.golesContra, row.diferenciaGol])]);
  const exportFixture = () => downloadCsv(`${name}-fixture.csv`, [["Jornada", "Fecha y hora", "Local", "GL", "GV", "Visitante", "Estado", "Cancha", "MVP"], ...[...partidos].sort((a, b) => a.numeroFecha - b.numeroFecha || a.fechaHora.localeCompare(b.fechaHora)).map((match) => [match.numeroFecha, match.fechaHora, match.nombreEquipoLocal, match.estado === "FINALIZADO" ? match.golesLocal : "", match.estado === "FINALIZADO" ? match.golesVisitante : "", match.nombreEquipoVisitante, match.estado, match.cancha, match.nombreMvp])]);

  return <><button type="button" onClick={() => setOpen(true)}>Exportar</button>{open && <div className="export-backdrop" onMouseDown={() => setOpen(false)}><section className="export-modal" onMouseDown={(event) => event.stopPropagation()}><button className="export-close" onClick={() => setOpen(false)}>×</button><span>Informes</span><h2>Exportar torneo</h2><p>Descargá los datos para trabajar en Excel o generá un informe listo para guardar como PDF.</p><div className="export-options"><button onClick={exportFixture}><strong>Fixture CSV</strong><small>Jornadas, horarios, resultados y canchas</small></button><button onClick={exportStandings}><strong>Tabla CSV</strong><small>Posiciones y estadísticas completas</small></button><button onClick={() => window.print()}><strong>Imprimir / PDF</strong><small>Resumen, tabla, goleadores y fixture</small></button></div></section></div>}
    <article className="tournament-print-report"><header><b>FUTAPP</b><h1>{torneo.nombre}</h1><p>{torneo.ubicacion} · {torneo.formato} · {torneo.estado} · {torneo.codigoPublico}</p></header><section><h2>Tabla de posiciones</h2><table><thead><tr><th>#</th><th>Equipo</th><th>Pts</th><th>PJ</th><th>PG</th><th>PE</th><th>PP</th><th>GF</th><th>GC</th><th>DG</th></tr></thead><tbody>{tabla.map((row, index) => <tr key={row.idEquipo}><td>{index + 1}</td><td>{row.nombreEquipo}</td><td>{row.puntos}</td><td>{row.partidosJugados}</td><td>{row.ganados}</td><td>{row.empatados}</td><td>{row.perdidos}</td><td>{row.golesFavor}</td><td>{row.golesContra}</td><td>{row.diferenciaGol}</td></tr>)}</tbody></table></section>
      {goleadores.length > 0 && <section><h2>Goleadores</h2><ol>{goleadores.slice(0, 10).map((player) => <li key={`${player.idJugador}-${player.idEquipo}`}>{player.nombreJugador} — {player.nombreEquipo}: <b>{player.cantidadGoles} goles</b></li>)}</ol></section>}
      <section><h2>Fixture</h2>{sortedDates.map((date) => <div className="print-date" key={date.idFecha}><h3>Fecha {date.numeroFecha}</h3>{partidos.filter((match) => match.idFecha === date.idFecha).map((match) => <p key={match.idPartido}><span>{match.fechaHora}</span><b>{match.nombreEquipoLocal} {match.estado === "FINALIZADO" ? `${match.golesLocal} - ${match.golesVisitante}` : "vs"} {match.nombreEquipoVisitante}</b><span>{match.cancha || ""}</span></p>)}</div>)}</section><footer>Generado desde FUTAPP · {new Date().toLocaleDateString("es-AR")}</footer></article>
  </>;
}

export default TournamentExports;
