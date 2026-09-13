import { useState } from "react";
import "./ResultModal.css";
import { API_URL, apiFetch } from "../../config/api";

function ResultModal({ partidos, partidoInicial, onClose, onResultadoGuardado }) {
  const partidosDisponibles = partidos.filter((partido) => ["PROGRAMADO", "EN_JUEGO", "FINALIZADO"].includes(partido.estado?.toUpperCase()));

  const [idPartido, setIdPartido] = useState(partidoInicial?.idPartido ?? "");
  const [golesLocal, setGolesLocal] = useState(partidoInicial?.golesLocal ?? 0);
  const [golesVisitante, setGolesVisitante] = useState(partidoInicial?.golesVisitante ?? 0);
  const [penalesLocal, setPenalesLocal] = useState(partidoInicial?.penalesLocal ?? 0);
  const [penalesVisitante, setPenalesVisitante] = useState(partidoInicial?.penalesVisitante ?? 0);

  const partidoSeleccionado = partidos.find(
    (partido) => partido.idPartido === Number(idPartido)
  );
  const esEliminatorio = ["Final", "Semifinales", "Cuartos de final", "Octavos de final", "Primera ronda eliminatoria"].includes(partidoSeleccionado?.observaciones);
  const necesitaPenales = esEliminatorio && Number(golesLocal) === Number(golesVisitante);

const guardarResultado = async (e) => {
  e.preventDefault();

  if (!partidoSeleccionado) return;

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
    onResultadoGuardado(partidoActualizado);
    onClose();
  } catch (error) {
    console.error(error);
    alert(error.message || "No se pudo guardar el resultado");
  }
};

  return (
    <div className="result-modal-backdrop" onClick={onClose}>
      <div className="result-modal" onClick={(e) => e.stopPropagation()}>
        <button className="result-modal-close" onClick={onClose}>
          ×
        </button>

        <h2>Cargar resultado</h2>
        <p>Seleccioná un partido y cargá el resultado final.</p>

        <form onSubmit={guardarResultado}>
          <label>Partido</label>
          {partidoInicial ? (
            <div className="result-selected-match">
              <strong>{partidoInicial.nombreEquipoLocal}</strong>
              <span>vs</span>
              <strong>{partidoInicial.nombreEquipoVisitante}</strong>
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

          <button type="submit" className="result-save-btn">
            Guardar resultado
          </button>
        </form>
      </div>
    </div>
  );
}

export default ResultModal;
