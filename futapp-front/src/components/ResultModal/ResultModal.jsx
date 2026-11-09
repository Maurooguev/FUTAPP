import { useEffect, useState } from "react";
import "./ResultModal.css";
import { API_URL, apiFetch } from "../../config/api";
import EntityImage from "../EntityImage/EntityImage";

function ResultModal({ partidos, fechas = [], partidoInicial, onClose, onResultadoGuardado, onFechaCerrada }) {
  const partidosDisponibles = partidos.filter((partido) => ["PROGRAMADO", "EN_JUEGO", "FINALIZADO"].includes(partido.estado?.toUpperCase()));

  const [idPartido, setIdPartido] = useState(partidoInicial?.idPartido ?? "");
  const [golesLocal, setGolesLocal] = useState(partidoInicial?.golesLocal ?? 0);
  const [golesVisitante, setGolesVisitante] = useState(partidoInicial?.golesVisitante ?? 0);
  const [penalesLocal, setPenalesLocal] = useState(partidoInicial?.penalesLocal ?? 0);
  const [penalesVisitante, setPenalesVisitante] = useState(partidoInicial?.penalesVisitante ?? 0);
  const [historial, setHistorial] = useState([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const partidoSeleccionado = partidos.find(
    (partido) => partido.idPartido === Number(idPartido)
  );
  const esEliminatorio = ["Final", "Semifinales", "Cuartos de final", "Octavos de final", "Primera ronda eliminatoria"].includes(partidoSeleccionado?.observaciones);
  const necesitaPenales = esEliminatorio && Number(golesLocal) === Number(golesVisitante);
  const fechaSeleccionada = fechas.find((fecha) => Number(fecha.idFecha) === Number(partidoSeleccionado?.idFecha));
  const partidosDeFecha = partidos.filter((partido) => Number(partido.idFecha) === Number(partidoSeleccionado?.idFecha));
  const partidosCompletos = partidosDeFecha.filter((partido) => ["FINALIZADO", "CANCELADO"].includes(partido.estado?.toUpperCase())).length;
  const numeroFecha = partidoSeleccionado?.numeroFecha ?? fechaSeleccionada?.numeroFecha;
  const siguientePendiente = [...partidos]
    .filter((partido) => partido.idPartido !== partidoSeleccionado?.idPartido
      && partido.idFecha === partidoSeleccionado?.idFecha
      && ["PROGRAMADO", "EN_JUEGO"].includes(partido.estado?.toUpperCase()))
    .sort((a, b) => (a.numeroFecha ?? 0) - (b.numeroFecha ?? 0) || a.idPartido - b.idPartido)[0];
  const puedeCerrarFecha = Boolean(partidoSeleccionado?.idFecha
    && fechaSeleccionada?.estado?.toUpperCase() !== "FINALIZADA"
    && !siguientePendiente);
  const editandoResultado = partidoSeleccionado?.estado?.toUpperCase() === "FINALIZADO";
  const resultadoCambio = Number(golesLocal) !== Number(partidoSeleccionado?.golesLocal ?? 0)
    || Number(golesVisitante) !== Number(partidoSeleccionado?.golesVisitante ?? 0)
    || (necesitaPenales && (Number(penalesLocal) !== Number(partidoSeleccionado?.penalesLocal ?? 0)
      || Number(penalesVisitante) !== Number(partidoSeleccionado?.penalesVisitante ?? 0)));
  const adjustScore = (setter, value, delta) => setter(String(Math.max(0, Number(value || 0) + delta)));

  useEffect(() => {
    if (!partidoSeleccionado) return;
    setGolesLocal(partidoSeleccionado.golesLocal ?? 0);
    setGolesVisitante(partidoSeleccionado.golesVisitante ?? 0);
    setPenalesLocal(partidoSeleccionado.penalesLocal ?? 0);
    setPenalesVisitante(partidoSeleccionado.penalesVisitante ?? 0);
    setError("");
  }, [partidoSeleccionado]);

  useEffect(() => {
    if (!partidoSeleccionado?.idPartido) { setHistorial([]); return; }
    apiFetch(`${API_URL}/partidos/${partidoSeleccionado.idPartido}/historial-resultado`)
      .then((response) => response.ok ? response.json() : []).then(setHistorial).catch(() => setHistorial([]));
  }, [partidoSeleccionado?.idPartido]);

const guardarResultado = async (e, accion = "CERRAR") => {
  e.preventDefault();

  if (!partidoSeleccionado || saving) return;
  if (necesitaPenales && Number(penalesLocal) === Number(penalesVisitante)) {
    setError("En una llave eliminatoria los penales deben definir un ganador.");
    return;
  }
  if (editandoResultado && resultadoCambio && !window.confirm(`¿Modificar el resultado ya cargado de ${partidoSeleccionado.nombreEquipoLocal} vs ${partidoSeleccionado.nombreEquipoVisitante}? El cambio quedará registrado en el historial.`)) return;
  setSaving(true);
  setError("");

  const resultado = {
    golesLocal: Number(golesLocal),
    golesVisitante: Number(golesVisitante),
    penalesLocal: necesitaPenales ? Number(penalesLocal) : null,
    penalesVisitante: necesitaPenales ? Number(penalesVisitante) : null,
  };

  try {
    const res = await apiFetch(
      `${API_URL}/partidos/${partidoSeleccionado.idPartido}/resultado`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(resultado),
      }
    );

    if (!res.ok) { const payload = await res.json().catch(() => null); throw new Error(payload?.message || "Error al guardar resultado"); }

    const partidoActualizado = await res.json();
    onResultadoGuardado(partidoActualizado, accion === "DETALLES");
    if (accion === "SIGUIENTE" && siguientePendiente) {
      setIdPartido(siguientePendiente.idPartido);
      setGolesLocal(siguientePendiente.golesLocal ?? 0);
      setGolesVisitante(siguientePendiente.golesVisitante ?? 0);
      setPenalesLocal(siguientePendiente.penalesLocal ?? 0);
      setPenalesVisitante(siguientePendiente.penalesVisitante ?? 0);
    } else if (accion === "CERRAR_FECHA") {
      const cierre = await apiFetch(`${API_URL}/fechas/${partidoSeleccionado.idFecha}/cerrar`, { method: "PUT" });
      const fecha = await cierre.json().catch(() => null);
      if (!cierre.ok) throw new Error(fecha?.message || "El resultado se guardó, pero no se pudo cerrar la jornada");
      onFechaCerrada?.(fecha);
      onClose();
    } else {
      onClose();
    }
  } catch (error) {
    console.error(error);
    setError(error.message || "No se pudo guardar el resultado");
  } finally {
    setSaving(false);
  }
};

  return (
    <div className="result-modal-backdrop" onClick={onClose}>
      <div className="result-modal" onClick={(e) => e.stopPropagation()}>
        <button className="result-modal-close" onClick={onClose}>
          ×
        </button>

        <span className="result-flow-label">{numeroFecha ? `Fecha ${numeroFecha}` : "Administrar partido"}</span>
        <h2>{editandoResultado ? "Editar resultado" : "Cargar resultado"}</h2>
        {partidoSeleccionado && <div className="result-date-progress"><div><strong>{Math.min(partidosCompletos + (editandoResultado ? 0 : 1), partidosDeFecha.length)} de {partidosDeFecha.length} partidos</strong><small>{editandoResultado ? "Resultado ya cargado" : "Progreso al guardar este marcador"}</small></div><span><i style={{ width: `${partidosDeFecha.length ? Math.min(100, ((partidosCompletos + (editandoResultado ? 0 : 1)) / partidosDeFecha.length) * 100) : 0}%` }} /></span></div>}
        <p>Ingresá el marcador. Los goles individuales, las tarjetas y el MVP se pueden completar después.</p>

        <form onSubmit={(event) => guardarResultado(event, "DETALLES")}>
          <label>Partido</label>
          {partidoInicial ? (
            <div className="result-selected-match">
              <strong><EntityImage type="team" value={partidoSeleccionado?.escudoEquipoLocal} alt="" /><span>{partidoSeleccionado?.nombreEquipoLocal}</span></strong>
              <span>vs</span>
              <strong><EntityImage type="team" value={partidoSeleccionado?.escudoEquipoVisitante} alt="" /><span>{partidoSeleccionado?.nombreEquipoVisitante}</span></strong>
            </div>
          ) : <select
            value={idPartido}
            onChange={(e) => setIdPartido(e.target.value)}
            required
          >
            <option value="">Seleccionar partido</option>

            {partidosDisponibles.map((partido) => (
              <option key={partido.idPartido} value={partido.idPartido}>
                Fecha {partido.numeroFecha} - {partido.nombreEquipoLocal} vs{" "}
                {partido.nombreEquipoVisitante}
              </option>
            ))}
          </select>}

          <div className="result-score-inputs main-score">
            <div><label>{partidoSeleccionado?.nombreEquipoLocal || "Local"}</label><div className="score-stepper"><button type="button" aria-label="Quitar gol al local" onClick={() => adjustScore(setGolesLocal, golesLocal, -1)}>−</button><input aria-label="Goles del equipo local" type="number" inputMode="numeric" min="0" value={golesLocal} onChange={(e) => setGolesLocal(e.target.value)} /><button type="button" aria-label="Agregar gol al local" onClick={() => adjustScore(setGolesLocal, golesLocal, 1)}>+</button></div></div>
            <b>–</b>
            <div><label>{partidoSeleccionado?.nombreEquipoVisitante || "Visitante"}</label><div className="score-stepper"><button type="button" aria-label="Quitar gol al visitante" onClick={() => adjustScore(setGolesVisitante, golesVisitante, -1)}>−</button><input aria-label="Goles del equipo visitante" type="number" inputMode="numeric" min="0" value={golesVisitante} onChange={(e) => setGolesVisitante(e.target.value)} /><button type="button" aria-label="Agregar gol al visitante" onClick={() => adjustScore(setGolesVisitante, golesVisitante, 1)}>+</button></div></div>
          </div>

          {necesitaPenales && <div className="result-penalties"><strong>Definición por penales</strong><small>El marcador terminó empatado. Indicá quién ganó la serie.</small><div className="result-score-inputs penalty-score"><div><label>{partidoSeleccionado?.nombreEquipoLocal}</label><input type="number" inputMode="numeric" min="0" value={penalesLocal} onChange={(e) => setPenalesLocal(e.target.value)} /></div><b>–</b><div><label>{partidoSeleccionado?.nombreEquipoVisitante}</label><input type="number" inputMode="numeric" min="0" value={penalesVisitante} onChange={(e) => setPenalesVisitante(e.target.value)} /></div></div></div>}

          {error && <p className="result-error">{error}</p>}
          <div className="result-actions">
            {siguientePendiente ? <button type="button" className="result-save-btn" disabled={saving} onClick={(event) => guardarResultado(event, "SIGUIENTE")}>{saving ? "Guardando…" : "Guardar y cargar siguiente"}</button> : puedeCerrarFecha ? <button type="button" className="result-save-btn close-date" disabled={saving} onClick={(event) => guardarResultado(event, "CERRAR_FECHA")}>{saving ? "Guardando…" : "Guardar y cerrar jornada"}</button> : <button type="button" className="result-save-btn" disabled={saving} onClick={(event) => guardarResultado(event, "CERRAR")}>{saving ? "Guardando…" : "Guardar resultado"}</button>}
            <button type="submit" className="result-secondary-btn" disabled={saving}>Guardar y agregar detalles</button>
          </div>
          {siguientePendiente && <small className="result-flow-help">Después se abrirá automáticamente {siguientePendiente.nombreEquipoLocal} vs {siguientePendiente.nombreEquipoVisitante}.</small>}
          {historial.length > 0 && <div className="result-history"><strong>Historial de cambios</strong>{historial.slice(0, 5).map((item) => <p key={item.id}><span>{item.usuario} · {new Date(item.fechaCambio).toLocaleString("es-AR")}</span><b>{item.golesLocalAnterior ?? "–"}-{item.golesVisitanteAnterior ?? "–"} → {item.golesLocalNuevo}-{item.golesVisitanteNuevo}</b></p>)}</div>}
        </form>
      </div>
    </div>
  );
}

export default ResultModal;
