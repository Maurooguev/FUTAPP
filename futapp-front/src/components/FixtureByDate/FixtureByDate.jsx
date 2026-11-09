import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import EntityImage from "../EntityImage/EntityImage";
import "./FixtureByDate.css";

const formatMatchDate = (value) => {
  if (!value) return "Horario a confirmar";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("es-AR", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
};

function FixtureByDate({ fechas, partidos, equipos = [], mostrarLibres = false, onSelectPartido, canManage, onEditDate, onCloseDate, onDeleteDate, onEditMatch, onSuspendMatch, onDeleteMatch, onLoadResult, onQuickResult }) {
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

    const [fechaSeleccionadaId, setFechaSeleccionadaId] = useState(null);
    const [quickScores, setQuickScores] = useState({});
    const [savingMatch, setSavingMatch] = useState(null);

    useEffect(() => {
      setQuickScores((current) => Object.fromEntries(partidos.map((partido) => [partido.idPartido, current[partido.idPartido] || { local: partido.golesLocal ?? 0, visitante: partido.golesVisitante ?? 0 }])));
    }, [partidos]);

    const changeScore = (idPartido, side, delta) => setQuickScores((current) => ({
      ...current,
      [idPartido]: { ...current[idPartido], [side]: Math.max(0, (current[idPartido]?.[side] ?? 0) + delta) },
    }));

    const saveQuickScore = async (partido) => {
      const score = quickScores[partido.idPartido] || { local: 0, visitante: 0 };
      setSavingMatch(partido.idPartido);
      try { await onQuickResult(partido, score); } catch { /* El mensaje se muestra en el panel del torneo. */ } finally { setSavingMatch(null); }
    };

    const fechaActual = fechasOrdenadas.find((fecha) => fecha.idFecha === fechaSeleccionadaId) || fechaInicial;

  const partidosDeFecha = useMemo(() => {
    if (!fechaActual) return [];

    return partidos.filter(
      (partido) => partido.idFecha === fechaActual.idFecha
    );
  }, [partidos, fechaActual]);

  const calendarioCompatibleConLibres = useMemo(() => {
    if (!mostrarLibres || equipos.length < 3 || equipos.length % 2 === 0) return false;
    const rondasEsperadas = equipos.length;
    return fechasOrdenadas.length === rondasEsperadas || fechasOrdenadas.length === rondasEsperadas * 2;
  }, [equipos.length, fechasOrdenadas.length, mostrarLibres]);

  const equiposFueraDelFixture = useMemo(() => {
    const participantes = new Set(partidos.flatMap((partido) => [partido.idEquipoLocal, partido.idEquipoVisitante]));
    return equipos.filter((equipo) => !participantes.has(equipo.idEquipo));
  }, [equipos, partidos]);
  const fixtureConResultados = partidos.some((partido) => partido.estado?.toUpperCase() === "FINALIZADO");

  const equiposLibres = useMemo(() => {
    if (!calendarioCompatibleConLibres || !fechaActual) return [];
    const participantes = new Set(partidosDeFecha.flatMap((partido) => [partido.idEquipoLocal, partido.idEquipoVisitante]));
    return equipos.filter((equipo) => !participantes.has(equipo.idEquipo));
  }, [calendarioCompatibleConLibres, equipos, fechaActual, partidosDeFecha]);

  const partidosResueltos = partidosDeFecha.filter((partido) => ["FINALIZADO", "CANCELADO"].includes(partido.estado?.toUpperCase()));
  const partidosPendientes = partidosDeFecha.filter((partido) => !["FINALIZADO", "CANCELADO"].includes(partido.estado?.toUpperCase()));
  const progreso = partidosDeFecha.length === 0 ? 0 : Math.round((partidosResueltos.length / partidosDeFecha.length) * 100);

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
            onClick={() => setFechaSeleccionadaId(fecha.idFecha)}
          >
            Fecha {fecha.numeroFecha}
            <small>{partidos.filter((partido) => partido.idFecha === fecha.idFecha && ["FINALIZADO", "CANCELADO"].includes(partido.estado?.toUpperCase())).length}/{partidos.filter((partido) => partido.idFecha === fecha.idFecha).length}</small>
          </button>
        ))}
      </div>

      <div className="fixture-current">
        <div className="fixture-current-copy"><h3>Fecha {fechaActual.numeroFecha}</h3><span>{fechaActual.estado}</span></div>
        <div className="fixture-progress"><div><strong>{partidosResueltos.length} de {partidosDeFecha.length}</strong><span> partidos resueltos</span></div><div className="fixture-progress-track"><i style={{ width: `${progreso}%` }} /></div></div>
        {canManage && <div className="fixture-admin-actions">
          {partidosPendientes.length > 0 && <button className="next-result" onClick={() => onLoadResult(partidosPendientes[0])}>Cargar próximo resultado</button>}
          {fechaActual.estado?.toUpperCase() !== "FINALIZADA" && <button className="close-date" disabled={partidosDeFecha.length === 0 || partidosPendientes.length > 0} title={partidosPendientes.length > 0 ? "Primero cargá todos los resultados" : "Cerrar jornada"} onClick={() => onCloseDate(fechaActual)}>Cerrar jornada</button>}
          <button onClick={() => onEditDate(fechaActual)}>Editar</button>
          <button className="danger" onClick={() => onDeleteDate(fechaActual)}>Eliminar</button>
        </div>}
      </div>

      {mostrarLibres && equipos.length % 2 !== 0 && !calendarioCompatibleConLibres && <div className="fixture-integrity-warning">
        <strong>El fixture no coincide con los {equipos.length} equipos actuales.</strong>
        {equiposFueraDelFixture.length > 0
          ? <span>{equiposFueraDelFixture.map((equipo) => equipo.nombre).join(", ")} no forma parte de ningún partido. No es una fecha libre: ese equipo fue inscripto después de crear el fixture. {fixtureConResultados ? "Como ya existen resultados, quitá esa inscripción para conservar el calendario." : "Rehacé el fixture para incorporarlo correctamente."}</span>
          : <span>Se agregó o quitó un equipo después de generar el calendario. {fixtureConResultados ? "Revisá las inscripciones antes de continuar." : "Rehacé el fixture para rotar correctamente las fechas libres."}</span>}
      </div>}

      {equiposLibres.length > 0 && <div className="fixture-byes">
        <span>Fecha libre <small>No suma puntos</small></span>
        {equiposLibres.map((equipo) => <Link key={equipo.idEquipo} to={`/equipo/${equipo.idEquipo}`}><EntityImage type="team" value={equipo.escudo} alt="" /><strong>{equipo.nombre}</strong></Link>)}
      </div>}

      {partidosDeFecha.length === 0 ? (
        <div className="fixture-empty">
          <p>No hay partidos cargados para esta fecha.</p>
        </div>
      ) : (
        <div className="fixture-grid">
          {partidosDeFecha.map((partido) => {
            const bajaAdministrativa = partido.observaciones?.startsWith("Baja de ");
            const canceladoPorBaja = bajaAdministrativa && partido.estado?.toUpperCase() === "CANCELADO";
            const estadoVisible = bajaAdministrativa
              ? (canceladoPorBaja ? "CANCELADO POR BAJA" : "VICTORIA POR BAJA")
              : partido.estado;
            return (
            <div
              className="fixture-match-card"
              key={partido.idPartido}
              onClick={() => onSelectPartido(partido)}
            >
              <div className={`fixture-match-status ${bajaAdministrativa ? "administrative" : ""}`}>{estadoVisible}</div>

              <div className="fixture-score-line">
                <Link className="fixture-team" to={`/equipo/${partido.idEquipoLocal}`} onClick={(event) => event.stopPropagation()}><EntityImage type="team" value={partido.escudoEquipoLocal} alt="" /><span>{partido.nombreEquipoLocal}</span></Link>

                <strong>
                  {partido.estado?.toUpperCase() === "FINALIZADO"
                    ? `${partido.golesLocal} - ${partido.golesVisitante}`
                    : canceladoPorBaja ? "Sin puntos" : "vs"}
                </strong>

                <Link className="fixture-team fixture-team-away" to={`/equipo/${partido.idEquipoVisitante}`} onClick={(event) => event.stopPropagation()}><EntityImage type="team" value={partido.escudoEquipoVisitante} alt="" /><span>{partido.nombreEquipoVisitante}</span></Link>
              </div>

              <p>{formatMatchDate(partido.fechaHora)}{partido.cancha ? ` · ${partido.cancha}` : ""}</p>
              {bajaAdministrativa && <p className="fixture-administrative-note">{partido.observaciones}</p>}
              {canManage && ["PROGRAMADO", "EN_JUEGO"].includes(partido.estado?.toUpperCase()) && <div className="fixture-inline-result" onClick={(event) => event.stopPropagation()}>
                <div><span>{partido.nombreEquipoLocal}</span><div className="fixture-score-stepper"><button type="button" disabled={(quickScores[partido.idPartido]?.local ?? 0) === 0} onClick={() => changeScore(partido.idPartido, "local", -1)}>−</button><strong>{quickScores[partido.idPartido]?.local ?? 0}</strong><button type="button" onClick={() => changeScore(partido.idPartido, "local", 1)}>+</button></div></div>
                <i>–</i>
                <div><span>{partido.nombreEquipoVisitante}</span><div className="fixture-score-stepper"><button type="button" disabled={(quickScores[partido.idPartido]?.visitante ?? 0) === 0} onClick={() => changeScore(partido.idPartido, "visitante", -1)}>−</button><strong>{quickScores[partido.idPartido]?.visitante ?? 0}</strong><button type="button" onClick={() => changeScore(partido.idPartido, "visitante", 1)}>+</button></div></div>
                <button type="button" className="fixture-quick-save" disabled={savingMatch === partido.idPartido} onClick={() => saveQuickScore(partido)}>{savingMatch === partido.idPartido ? "Guardando…" : "Confirmar resultado"}</button>
              </div>}
              {partido.estado?.toUpperCase() === "SUSPENDIDO" && partido.motivoEstado && <p className="fixture-suspension-reason">Motivo: {partido.motivoEstado}</p>}
              {partido.idMvp && <Link className="fixture-mvp" to={`/jugador/${partido.idMvp}`} onClick={(event) => event.stopPropagation()}>⭐ MVP: {partido.nombreMvp}</Link>}
              {canManage && <div className="fixture-quick-actions" onClick={(event) => event.stopPropagation()}>
                {["PROGRAMADO", "EN_JUEGO", "FINALIZADO"].includes(partido.estado?.toUpperCase()) && <button className="result" onClick={() => onLoadResult(partido)}>{partido.estado?.toUpperCase() === "FINALIZADO" ? "Editar resultado" : "Cargar resultado"}</button>}
                <button className="incidents" onClick={() => onSelectPartido(partido)}>Goles y tarjetas</button>
                {["PROGRAMADO", "EN_JUEGO"].includes(partido.estado?.toUpperCase()) && <button className="suspend" onClick={() => onSuspendMatch(partido)}>Suspender</button>}
                <button onClick={() => onEditMatch(partido)}>{partido.estado?.toUpperCase() === "SUSPENDIDO" ? "Reprogramar" : "Editar"}</button>
                <button className="danger" onClick={() => onDeleteMatch(partido)}>Eliminar</button>
              </div>}
            </div>
          );})}
        </div>
      )}
    </section>
  );
}

export default FixtureByDate;
