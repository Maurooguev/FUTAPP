import { useEffect, useState } from "react";
import "./MatchModal.css";
import { API_URL, apiFetch } from "../../config/api";
import MatchIncidentForm from "./MatchIncidentForm";

function MatchModal({ partido, onClose, canManage, onChanged }) {
  const [goles, setGoles] = useState([]);
  const [tarjetas, setTarjetas] = useState([]);
  const [formType, setFormType] = useState(null);
  const [error, setError] = useState("");

  const deleteIncident = async (type, id) => {
    if (!window.confirm("¿Eliminar esta incidencia?")) return;
    setError("");
    try {
      const response = await apiFetch(`${API_URL}/${type}/${id}`, { method: "DELETE" });
      if (!response.ok) throw new Error("No se pudo eliminar la incidencia");
      if (type === "goles") setGoles((current) => current.filter((item) => item.idGol !== id));
      else setTarjetas((current) => current.filter((item) => item.idTarjeta !== id));
      onChanged?.();
    } catch (requestError) { setError(requestError.message); }
  };

  useEffect(() => {
    if (!partido) return;

    fetch(`${API_URL}/goles/partido/${partido.idPartido}`)
      .then((res) => res.json())
      .then((data) => setGoles(data))
      .catch((err) => console.error("Goles partido:", err));

    fetch(`${API_URL}/tarjetas/partido/${partido.idPartido}`)
      .then((res) => res.json())
      .then((data) => setTarjetas(data))
      .catch((err) => console.error("Tarjetas partido:", err));
  }, [partido]);

  if (!partido) return null;
  const isScheduled = partido.estado?.toUpperCase() === "PROGRAMADO";

  return (
    <div className="match-modal-backdrop" onClick={onClose}>
      <div className="match-modal" onClick={(e) => e.stopPropagation()}>
        <button className="match-modal-close" onClick={onClose}>
          ×
        </button>

        <div className="match-modal-header">
          <span>Fecha {partido.numeroFecha}</span>
          <h2>
            {partido.nombreEquipoLocal} vs {partido.nombreEquipoVisitante}
          </h2>
          <p>{partido.fechaHora} · {partido.estado}</p>
        </div>

        <div className="match-score">
          <div>
            <h3>{partido.nombreEquipoLocal}</h3>
            <strong>{partido.golesLocal}</strong>
          </div>

          <span>VS</span>

          <div>
            <h3>{partido.nombreEquipoVisitante}</h3>
            <strong>{partido.golesVisitante}</strong>
          </div>
        </div>

        <div className="match-modal-grid">
          <section>
            <h3>Goles</h3>
            {canManage && !isScheduled && !formType && <button className="match-add-event" onClick={() => setFormType("goal")}>+ Agregar gol</button>}
            {formType === "goal" && <MatchIncidentForm partido={partido} type="goal" onCancel={() => setFormType(null)} onSaved={(goal) => { setGoles((current) => [...current, goal]); onChanged?.(); }} />}

            {goles.length === 0 ? (
              <p className="match-empty">No hay goles cargados.</p>
            ) : (
              goles.map((gol) => (
                <div className="match-event" key={gol.idGol}>
                  <strong>{gol.minuto}'</strong>
                  <div>
                    <h4>{gol.nombreJugador}</h4>
                    <p>{gol.nombreEquipo} · {gol.tipoGol}</p>
                  </div>
                  {canManage && <button className="match-delete-event" onClick={() => deleteIncident("goles", gol.idGol)}>×</button>}
                </div>
              ))
            )}
          </section>

          <section>
            <h3>Tarjetas</h3>
            {canManage && !isScheduled && !formType && <button className="match-add-event" onClick={() => setFormType("card")}>+ Agregar tarjeta</button>}
            {formType === "card" && <MatchIncidentForm partido={partido} type="card" onCancel={() => setFormType(null)} onSaved={(card) => { setTarjetas((current) => [...current, card]); onChanged?.(); }} />}

            {tarjetas.length === 0 ? (
              <p className="match-empty">No hay tarjetas cargadas.</p>
            ) : (
              tarjetas.map((tarjeta) => (
                <div className="match-event" key={tarjeta.idTarjeta}>
                  <strong>{tarjeta.minuto ? `${tarjeta.minuto}'` : "-"}</strong>
                  <div>
                    <h4>{tarjeta.nombreJugador}</h4>
                    <p>
                      {tarjeta.nombreEquipo} · {tarjeta.tipoTarjeta}
                      {tarjeta.motivo ? ` · ${tarjeta.motivo}` : ""}
                    </p>
                  </div>
                  {canManage && <button className="match-delete-event" onClick={() => deleteIncident("tarjetas", tarjeta.idTarjeta)}>×</button>}
                </div>
              ))
            )}
          </section>
        </div>
        {canManage && isScheduled && <p className="match-result-hint">Primero cargá el resultado del partido para habilitar goles y tarjetas.</p>}
        {error && <p className="tournament-form-error">{error}</p>}
      </div>
    </div>
  );
}

export default MatchModal;
