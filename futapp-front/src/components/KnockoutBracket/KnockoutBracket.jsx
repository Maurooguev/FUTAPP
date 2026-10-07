import { Link } from "react-router-dom";
import "./KnockoutBracket.css";
import EntityImage from "../EntityImage/EntityImage";

const roundsFor = (teams) => {
  const power = 2 ** Math.floor(Math.log2(Math.max(2, teams)));
  if (power !== teams) return ["Primera ronda eliminatoria", ...roundsFor(power)];
  if (teams <= 2) return ["Final"];
  if (teams <= 4) return ["Semifinales", "Final"];
  if (teams <= 8) return ["Cuartos de final", "Semifinales", "Final"];
  return ["Octavos de final", "Cuartos de final", "Semifinales", "Final"];
};

function Team({ id, name, image, score, penalties, winner }) {
  if (!id) return <div className="bracket-team pending"><span>Por definir</span><b>–</b></div>;
  return <div className={`bracket-team ${winner ? "winner" : ""}`}><Link to={`/equipo/${id}`}><EntityImage type="team" value={image} alt="" /><span>{name}</span></Link><b>{score ?? "–"}{penalties != null ? ` (${penalties})` : ""}</b></div>;
}

const winnerId = (match) => {
  if (match?.estado?.toUpperCase() !== "FINALIZADO") return null;
  if (match.golesLocal > match.golesVisitante) return match.idEquipoLocal;
  if (match.golesVisitante > match.golesLocal) return match.idEquipoVisitante;
  if (match.penalesLocal > match.penalesVisitante) return match.idEquipoLocal;
  if (match.penalesVisitante > match.penalesLocal) return match.idEquipoVisitante;
  return null;
};

const orderByBracketBranch = (rounds, partidos) => {
  const result = {};
  let previousWinners = [];

  rounds.forEach((round, roundIndex) => {
    const matches = partidos
      .filter((match) => match.observaciones === round)
      .sort((a, b) => a.idPartido - b.idPartido);

    if (roundIndex > 0 && previousWinners.length) {
      const branchIndex = (match) => {
        const local = previousWinners.indexOf(match.idEquipoLocal);
        const visitor = previousWinners.indexOf(match.idEquipoVisitante);
        const origins = [local, visitor].filter((index) => index >= 0);
        return origins.length ? Math.min(...origins) : Number.MAX_SAFE_INTEGER;
      };
      matches.sort((a, b) => branchIndex(a) - branchIndex(b) || a.idPartido - b.idPartido);
    }

    result[round] = matches;
    previousWinners = matches.map(winnerId).filter(Boolean);
  });

  return result;
};

function KnockoutBracket({ equipos, teamCount = equipos.length, partidos, canManage, onNextRound, loading }) {
  const rounds = roundsFor(teamCount);
  const matchesByRound = orderByBracketBranch(rounds, partidos);
  const generatedRounds = rounds.filter((round) => matchesByRound[round].length > 0);
  const latest = generatedRounds.length ? matchesByRound[generatedRounds.at(-1)] : [];
  const canAdvance = latest.length > 1 && latest.every((match) => match.estado?.toUpperCase() === "FINALIZADO");
  const canStart = generatedRounds.length === 0 && partidos.length > 0 && partidos.every((match) => match.estado?.toUpperCase() === "FINALIZADO");
  const final = matchesByRound.Final?.[0];
  const championId = winnerId(final);
  const champion = championId === final?.idEquipoLocal
    ? final.nombreEquipoLocal
    : championId === final?.idEquipoVisitante ? final.nombreEquipoVisitante : null;

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
              <Team id={match?.idEquipoLocal} name={match?.nombreEquipoLocal} image={match?.escudoEquipoLocal} score={finished ? match.golesLocal : null} penalties={finished ? match.penalesLocal : null} winner={finished && (match.golesLocal > match.golesVisitante || match.golesLocal === match.golesVisitante && match.penalesLocal > match.penalesVisitante)} />
              <Team id={match?.idEquipoVisitante} name={match?.nombreEquipoVisitante} image={match?.escudoEquipoVisitante} score={finished ? match.golesVisitante : null} penalties={finished ? match.penalesVisitante : null} winner={finished && (match.golesVisitante > match.golesLocal || match.golesLocal === match.golesVisitante && match.penalesVisitante > match.penalesLocal)} />
              {round === "Final" && match?.idPartido === final?.idPartido && champion && <div className="bracket-final-champion">🏆 {champion}</div>}
            </article>;
          })}
        </div></div>;
      })}
    </div></div>
  </section>;
}

export default KnockoutBracket;
