import { useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import ImageUploadField from "../ImageUploadField/ImageUploadField";

function PlayerForm({ equipoId, torneoId, player, onSaved, onCancel }) {
  const isEditing = Boolean(player?.idJugador);
  const today = new Date().toISOString().slice(0, 10);
  const [form, setForm] = useState({
    nombre: player?.nombre ?? "",
    apellido: player?.apellido ?? "",
    dni: player?.dni ?? "",
    fechaNacimiento: player?.fechaNacimiento ?? "",
    numeroCamiseta: player?.numeroCamiseta ?? "",
    posicion: player?.posicion ?? "",
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
      const identityChangeRequested = isEditing && ["nombre", "apellido", "dni", "fechaNacimiento"]
        .some((field) => String(form[field] ?? "").trim() !== String(player[field] ?? "").trim());
      const privateChangeRequested = identityChangeRequested && player.verificado;
      const directChangeRequested = !isEditing || (identityChangeRequested && !player.verificado)
        || ["numeroCamiseta", "posicion", "foto", "apodo", "piernaHabil", "estado"]
        .some((field) => String(form[field] ?? "").trim() !== String(player?.[field] ?? "").trim());
      if (privateChangeRequested) {
        const requestResponse = await apiFetch(`${API_URL}/reclamos/jugadores/${player.idJugador}/cambios`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            idTorneo: Number(torneoId), idEquipo: Number(equipoId), nombre: form.nombre, apellido: form.apellido,
            dni: form.dni, fechaNacimiento: form.fechaNacimiento,
            mensaje: "Solicitud de corrección de datos privados desde el plantel.",
          }),
        });
        if (!requestResponse.ok) {
          const payload = await requestResponse.json().catch(() => null);
          if (!payload?.message?.includes("Ya existe una modificación pendiente")) {
            throw new Error(payload?.message || "No se pudo solicitar la modificación privada");
          }
        }
        window.dispatchEvent(new Event("requests-updated"));
      }
      if (isEditing && !directChangeRequested) {
        onSaved(player, {
          privateChangeRequested,
          message: privateChangeRequested
            ? "Se envió una solicitud al jugador. Los cambios se aplicarán cuando la apruebe."
            : "No había cambios para guardar.",
        });
        return;
      }
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
      onSaved(await response.json(), {
        privateChangeRequested,
        message: privateChangeRequested
          ? "Los cambios del plantel se guardaron y se envió una solicitud al jugador por sus datos privados."
          : isEditing ? "Los cambios se guardaron correctamente." : "El jugador se agregó correctamente.",
      });
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
        <label>Fecha de nacimiento<input type="date" name="fechaNacimiento" value={form.fechaNacimiento} onChange={updateField} min="1900-01-01" max={today} required /></label>
        <label>Número de camiseta<input type="number" name="numeroCamiseta" value={form.numeroCamiseta} onChange={updateField} min="1" max="99" required /></label>
        <label>Posición<select name="posicion" value={form.posicion} onChange={updateField}><option value="">Sin posición</option><option>Arquero</option><option>Defensor</option><option>Mediocampista</option><option>Delantero</option></select></label>
        <label>Pierna hábil<select name="piernaHabil" value={form.piernaHabil} onChange={updateField}><option value="">Sin informar</option><option>Derecha</option><option>Izquierda</option><option>Ambas</option></select></label>
      </div>
      <ImageUploadField label="Foto del jugador" value={form.foto} shape="avatar" onUploaded={(foto) => setForm((current) => ({ ...current, foto }))} />
      {error && <p className="tournament-form-error">{error}</p>}
      <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onCancel}>Cancelar</button><button type="submit" className="tournament-form-save" disabled={saving}>{saving ? "Guardando..." : isEditing ? "Guardar cambios" : "Agregar jugador"}</button></div>
    </form>
  );
}

export default PlayerForm;
