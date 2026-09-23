import { useEffect, useState } from "react";
import "./ResultModal.css";
import { API_URL, apiFetch } from "../../config/api";

function ResultModal({ partidos, partidoInicial, onClose, onResultadoGuardado, onFechaCerrada }) {
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
  const siguientePendiente = [...partidos]
    .filter((partido) => partido.idPartido !== partidoSeleccionado?.idPartido
      && partido.idFecha === partidoSeleccionado?.idFecha
      && ["PROGRAMADO", "EN_JUEGO"].includes(partido.estado?.toUpperCase()))
    .sort((a, b) => (a.numeroFecha ?? 0) - (b.numeroFecha ?? 0) || a.idPartido - b.idPartido)[0];
  const puedeCerrarFecha = Boolean(partidoSeleccionado?.idFecha
    && ["PROGRAMADO", "EN_JUEGO"].includes(partidoSeleccionado.estado?.toUpperCase())
    && !siguientePendiente);

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

        <span className="result-flow-label">Administrar partido</span>
        <h2>Cargar resultado</h2>
        <div className="result-flow-steps" aria-label="Pasos de carga del partido">
          <strong><b>1</b> Resultado</strong>
          <span><b>2</b> Goles y tarjetas</span>
          <span><b>3</b> MVP</span>
        </div>
        <p>Confirmá el marcador y después podés completar el detalle del partido.</p>

        <form onSubmit={(event) => guardarResultado(event, "DETALLES")}>
          <label>Partido</label>
          {partidoInicial ? (
            <div className="result-selected-match">
              <strong>{partidoSeleccionado?.nombreEquipoLocal}</strong>
              <span>vs</span>
              <strong>{partidoSeleccionado?.nombreEquipoVisitante}</strong>
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

          <div className="result-score-inputs">
            <div>
              <label>Goles local</label>
              <input
                type="number"
                min="0"
                value={golesLocal}
                onChange={(e) => setGolesLocal(e.target.value)}
              />
            </div>

            <div>
              <label>Goles visitante</label>
              <input
                type="number"
                min="0"
                value={golesVisitante}
                onChange={(e) => setGolesVisitante(e.target.value)}
              />
            </div>
          </div>

          {necesitaPenales && <div className="result-penalties"><strong>Definición por penales</strong><div className="result-score-inputs"><div><label>Penales local</label><input type="number" min="0" value={penalesLocal} onChange={(e) => setPenalesLocal(e.target.value)} /></div><div><label>Penales visitante</label><input type="number" min="0" value={penalesVisitante} onChange={(e) => setPenalesVisitante(e.target.value)} /></div></div></div>}

          {error && <p className="result-error">{error}</p>}
          <div className="result-actions">
            <button type="submit" className="result-save-btn" disabled={saving}>{saving ? "Guardando…" : "Guardar y continuar"}</button>
            <button type="button" className="result-secondary-btn" disabled={saving} onClick={(event) => guardarResultado(event, "CERRAR")}>Guardar sin detalles</button>
            {siguientePendiente && <button type="button" className="result-next-btn" disabled={saving} onClick={(event) => guardarResultado(event, "SIGUIENTE")}>Guardar y cargar siguiente</button>}
            {puedeCerrarFecha && <button type="button" className="result-next-btn close-date" disabled={saving} onClick={(event) => guardarResultado(event, "CERRAR_FECHA")}>Guardar y cerrar jornada</button>}
          </div>
          <small className="result-flow-help">Los goles individuales, las tarjetas y el MVP son opcionales y se pueden completar o editar después.</small>
          {historial.length > 0 && <div className="result-history"><strong>Historial de cambios</strong>{historial.slice(0, 5).map((item) => <p key={item.id}><span>{item.usuario} · {new Date(item.fechaCambio).toLocaleString("es-AR")}</span><b>{item.golesLocalAnterior ?? "–"}-{item.golesVisitanteAnterior ?? "–"} → {item.golesLocalNuevo}-{item.golesVisitanteNuevo}</b></p>)}</div>}
        </form>
      </div>
    </div>
  );
}

export default ResultModal;
