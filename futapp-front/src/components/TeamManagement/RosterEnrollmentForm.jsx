import { useState } from "react";
import { API_URL, apiFetch } from "../../config/api";

function RosterEnrollmentForm({ player, reactivate, onCancel, onSaved }) {
  const [number, setNumber] = useState(player.numeroCamiseta ?? "");
  const [position, setPosition] = useState(player.posicion ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError("");
    try {
      const suffix = reactivate ? "/reactivar" : "";
      const response = await apiFetch(`${API_URL}/inscripciones/jugadores/${player.idInscripcionJugador}${suffix}`, {
        method: "PUT", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ numeroCamiseta: Number(number), posicion: position }),
      });
      const body = await response.json().catch(() => null);
      if (!response.ok) throw new Error(body?.message || "No se pudo actualizar la inscripción");
      onSaved(body);
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  return <form className="roster-enrollment-form" onSubmit={submit}><strong>{reactivate ? "Reactivar en este torneo" : "Dorsal y posición en este torneo"}</strong><label>Dorsal<input type="number" min="1" max="99" value={number} onChange={(event) => setNumber(event.target.value)} required /></label><label>Posición<input value={position} onChange={(event) => setPosition(event.target.value)} /></label>{error && <p className="tournament-form-error">{error}</p>}<div><button type="button" onClick={onCancel}>Cancelar</button><button className="primary" disabled={saving}>{saving ? "Guardando..." : reactivate ? "Reactivar" : "Guardar"}</button></div></form>;
}

export default RosterEnrollmentForm;
