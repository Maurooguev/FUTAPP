import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";

function MatchIncidentForm({ partido, type, onSaved, onCancel }) {
  const [players, setPlayers] = useState([]);
  const [form, setForm] = useState({ idJugador: "", minuto: "", tipo: type === "goal" ? "Jugada" : "AMARILLA", detail: "" });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetch(`${API_URL}/partidos/${partido.idPartido}/jugadores`).then((response) => response.json()).then(setPlayers);
  }, [partido.idPartido]);

  const updateField = ({ target: { name, value } }) => setForm((current) => ({ ...current, [name]: value }));
  const handleSubmit = async (event) => {
    event.preventDefault(); setSaving(true); setError("");
    const player = players.find((item) => item.idJugador === Number(form.idJugador));
    const endpoint = type === "goal" ? "goles" : "tarjetas";
    const payload = type === "goal"
      ? { minuto: Number(form.minuto), tipoGol: form.tipo, observacion: form.detail, idPartido: partido.idPartido, idJugador: player?.idJugador, idEquipo: player?.idEquipo }
      : { minuto: form.minuto ? Number(form.minuto) : null, tipoTarjeta: form.tipo, motivo: form.detail, idPartido: partido.idPartido, idJugador: player?.idJugador, idEquipo: player?.idEquipo };
    try {
      const response = await apiFetch(`${API_URL}/${endpoint}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
      if (!response.ok) { const body = await response.json().catch(() => null); throw new Error(body?.message || "No se pudo guardar la incidencia"); }
      onSaved(await response.json());
      setForm((current) => ({ ...current, idJugador: "", minuto: "", detail: "" }));
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  return <form className="match-incident-form" onSubmit={handleSubmit}>
    <select name="idJugador" value={form.idJugador} onChange={updateField} required><option value="">Seleccionar jugador</option>{players.map((player) => <option key={player.idJugador} value={player.idJugador}>{player.nombre} {player.apellido} — {player.nombreEquipo}</option>)}</select>
    <div className="match-incident-row"><input type="number" name="minuto" min="1" max="130" placeholder="Minuto" value={form.minuto} onChange={updateField} required={type === "goal"} />
      <select name="tipo" value={form.tipo} onChange={updateField}>{type === "goal" ? <><option>Jugada</option><option>Penal</option><option>Tiro libre</option><option>En contra</option></> : <><option value="AMARILLA">Amarilla</option><option value="ROJA">Roja</option></>}</select></div>
    <input name="detail" value={form.detail} onChange={updateField} placeholder={type === "goal" ? "Observación opcional" : "Motivo opcional"} />
    {error && <p className="tournament-form-error">{error}</p>}
    <div className="match-incident-actions"><button type="button" onClick={onCancel}>Cancelar</button><button type="submit" disabled={saving}>{saving ? "Guardando..." : "Guardar"}</button></div>
  </form>;
}
export default MatchIncidentForm;
