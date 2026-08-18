import { useState } from "react";
import { API_URL, apiFetch } from "../../config/api";

function PlayerForm({ equipoId, player, onSaved, onCancel }) {
  const isEditing = Boolean(player?.idJugador);
  const [form, setForm] = useState({
    nombre: player?.nombre ?? "",
    apellido: player?.apellido ?? "",
    dni: player?.dni ?? "",
    fechaNacimiento: player?.fechaNacimiento ?? "",
    numeroCamiseta: player?.numeroCamiseta ?? "",
    posicion: player?.posicion ?? "Delantero",
    foto: player?.foto ?? "",
    apodo: player?.apodo ?? "",
    piernaHabil: player?.piernaHabil ?? "",
    estado: player?.estado ?? "ACTIVO",
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const updateField = ({ target: { name, value } }) => setForm((current) => ({ ...current, [name]: value }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      const response = await apiFetch(
        isEditing ? `${API_URL}/jugadores/${player.idJugador}` : `${API_URL}/jugadores`,
        {
        method: isEditing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, numeroCamiseta: Number(form.numeroCamiseta), idEquipo: equipoId }),
        }
      );
      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        throw new Error(payload?.message || "No se pudo guardar el jugador");
      }
      onSaved(await response.json());
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <form className="roster-player-form" onSubmit={handleSubmit}>
      <div className="tournament-form-grid">
        <label>Nombre<input name="nombre" value={form.nombre} onChange={updateField} required /></label>
        <label>Apellido<input name="apellido" value={form.apellido} onChange={updateField} required /></label>
        <label>Apodo<input name="apodo" value={form.apodo} onChange={updateField} /></label>
        <label>DNI (opcional)<input name="dni" value={form.dni} onChange={updateField} /></label>
        <label>Fecha de nacimiento<input type="date" name="fechaNacimiento" value={form.fechaNacimiento} onChange={updateField} required /></label>
        <label>Número de camiseta<input type="number" name="numeroCamiseta" value={form.numeroCamiseta} onChange={updateField} min="1" max="99" required /></label>
        <label>Posición<select name="posicion" value={form.posicion} onChange={updateField}><option>Arquero</option><option>Defensor</option><option>Mediocampista</option><option>Delantero</option></select></label>
        <label>Pierna hábil<select name="piernaHabil" value={form.piernaHabil} onChange={updateField}><option value="">Sin informar</option><option>Derecha</option><option>Izquierda</option><option>Ambas</option></select></label>
      </div>
      <label>Foto (URL opcional)<input name="foto" value={form.foto} onChange={updateField} /></label>
      {error && <p className="tournament-form-error">{error}</p>}
      <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onCancel}>Cancelar</button><button type="submit" className="tournament-form-save" disabled={saving}>{saving ? "Guardando..." : isEditing ? "Guardar cambios" : "Agregar jugador"}</button></div>
    </form>
  );
}

export default PlayerForm;
