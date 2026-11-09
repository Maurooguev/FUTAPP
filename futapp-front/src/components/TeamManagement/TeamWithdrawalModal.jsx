import { useState } from "react";
import { API_URL, apiFetch } from "../../config/api";

function TeamWithdrawalModal({ torneoId, team, onClose, onSaved }) {
  const [resolution, setResolution] = useState("WALKOVER");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true); setError("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/equipos/torneo/${torneoId}/equipo/${team.idEquipo}/baja`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ resolucionPartidos: resolution, motivo: reason }),
      });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo registrar la baja");
      onSaved(team);
    } catch (requestError) { setError(requestError.message); }
    finally { setSaving(false); }
  };

  return <div className="modal-backdrop" onMouseDown={onClose}>
    <form className="team-withdrawal-modal" onSubmit={submit} onMouseDown={(event) => event.stopPropagation()}>
      <button className="team-withdrawal-close" type="button" onClick={onClose} aria-label="Cerrar">×</button>
      <span className="team-withdrawal-kicker">Baja del torneo</span>
      <h2>{team.nombre}</h2>
      <p>Los partidos ya jugados y el perfil del club se conservan. Elegí qué hacer con los partidos pendientes.</p>
      <label className={`team-withdrawal-option ${resolution === "WALKOVER" ? "selected" : ""}`}>
        <input type="radio" name="resolution" value="WALKOVER" checked={resolution === "WALKOVER"} onChange={(event) => setResolution(event.target.value)} />
        <span><strong>Dar los partidos por perdidos 0–3</strong><small>Recomendado: los rivales suman los puntos y el torneo puede continuar.</small></span>
      </label>
      <label className={`team-withdrawal-option ${resolution === "CANCELAR" ? "selected" : ""}`}>
        <input type="radio" name="resolution" value="CANCELAR" checked={resolution === "CANCELAR"} onChange={(event) => setResolution(event.target.value)} />
        <span><strong>Cancelar los partidos pendientes</strong><small>No otorga puntos. Útil si la organización va a rearmar el fixture.</small></span>
      </label>
      <label className="team-withdrawal-reason">Motivo (opcional)<textarea value={reason} maxLength={300} onChange={(event) => setReason(event.target.value)} placeholder="Ej.: el equipo no puede continuar por falta de jugadores" /></label>
      {error && <p className="tournament-form-error">{error}</p>}
      <div className="team-withdrawal-actions"><button type="button" onClick={onClose}>Volver</button><button className="danger" disabled={saving}>{saving ? "Guardando…" : "Confirmar baja"}</button></div>
    </form>
  </div>;
}

export default TeamWithdrawalModal;
