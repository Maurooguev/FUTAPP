import { Link } from "react-router-dom";
import "./KnockoutBracket.css";

const roundsFor = (teams) => {
  const power = 2 ** Math.floor(Math.log2(Math.max(2, teams)));
  if (power !== teams) return ["Primera ronda eliminatoria", ...roundsFor(power)];
  if (teams <= 2) return ["Final"];
  if (teams <= 4) return ["Semifinales", "Final"];
  if (teams <= 8) return ["Cuartos de final", "Semifinales", "Final"];
  return ["Octavos de final", "Cuartos de final", "Semifinales", "Final"];
};

function Team({ id, name, score, penalties, winner }) {
  if (!id) return <div className="bracket-team pending"><span>Por definir</span><b>–</b></div>;
  return <div className={`bracket-team ${winner ? "winner" : ""}`}><Link to={`/equipo/${id}`}>{name}</Link><b>{score ?? "–"}{penalties != null ? ` (${penalties})` : ""}</b></div>;
}

function KnockoutBracket({ equipos, teamCount = equipos.length, partidos, canManage, onNextRound, loading }) {
  const rounds = roundsFor(teamCount);
  const matchesByRound = Object.fromEntries(rounds.map((round) => [round, partidos.filter((match) => match.observaciones === round)]));
  const generatedRounds = rounds.filter((round) => matchesByRound[round].length > 0);
  const latest = generatedRounds.length ? matchesByRound[generatedRounds.at(-1)] : [];
  const canAdvance = latest.length > 1 && latest.every((match) => match.estado?.toUpperCase() === "FINALIZADO");
  const canStart = generatedRounds.length === 0 && partidos.length > 0 && partidos.every((match) => match.estado?.toUpperCase() === "FINALIZADO");
  const final = matchesByRound.Final?.[0];
  const champion = final?.estado?.toUpperCase() === "FINALIZADO"
    ? final.golesLocal > final.golesVisitante || final.golesLocal === final.golesVisitante && final.penalesLocal > final.penalesVisitante ? final.nombreEquipoLocal : final.nombreEquipoVisitante
    : null;

  return <section className="bracket-block" id="llave">
    <div className="bracket-heading"><div><span>Eliminación directa</span><h2>Llave del torneo</h2><p>Los ganadores avanzan de izquierda a derecha hasta la final.</p></div>{canManage && (canAdvance || canStart) && <button onClick={onNextRound} disabled={loading}>{loading ? "Generando…" : canStart ? "Generar playoffs" : "Generar siguiente ronda"}</button>}</div>
    {champion && <div className="bracket-champion">🏆 Campeón: <strong>{champion}</strong></div>}
    <div className="bracket-scroll"><div className="bracket-grid" style={{ gridTemplateColumns: `repeat(${rounds.length}, minmax(200px, 1fr))` }}>
      {rounds.map((round, roundIndex) => {
        const power = 2 ** Math.floor(Math.log2(Math.max(2, teamCount)));
        const expected = round === "Primera ronda eliminatoria"
          ? teamCount - power
          : Math.max(1, power / (2 ** (roundIndex + (rounds[0] === "Primera ronda eliminatoria" ? 0 : 1))));
        const matches = matchesByRound[round];
        return <div className="bracket-round" key={round}><h3>{round}</h3><div className="bracket-round-matches">
          {Array.from({ length: Math.max(expected, matches.length) }, (_, index) => {
            const match = matches[index];
            const finished = match?.estado?.toUpperCase() === "FINALIZADO";
            return <article className="bracket-match" key={match?.idPartido || `${round}-${index}`}>
              <Team id={match?.idEquipoLocal} name={match?.nombreEquipoLocal} score={finished ? match.golesLocal : null} penalties={finished ? match.penalesLocal : null} winner={finished && (match.golesLocal > match.golesVisitante || match.golesLocal === match.golesVisitante && match.penalesLocal > match.penalesVisitante)} />
              <Team id={match?.idEquipoVisitante} name={match?.nombreEquipoVisitante} score={finished ? match.golesVisitante : null} penalties={finished ? match.penalesVisitante : null} winner={finished && (match.golesVisitante > match.golesLocal || match.golesLocal === match.golesVisitante && match.penalesVisitante > match.penalesLocal)} />
            </article>;
          })}
        </div></div>;
      })}
    </div></div>
  </section>;
}

export default KnockoutBracket;
