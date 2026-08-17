import { useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "../TournamentFormModal/TournamentFormModal.css";

function DateFormModal({ torneoId, nextNumber, fecha, onClose, onSaved }) {
  const isEditing = Boolean(fecha?.idFecha);
  const [form, setForm] = useState({ numeroFecha: fecha?.numeroFecha ?? nextNumber, descripcion: fecha?.descripcion ?? `Fecha ${nextNumber}`, estado: fecha?.estado ?? "ACTIVA" });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const updateField = ({ target: { name, value } }) => setForm((current) => ({ ...current, [name]: value }));
  const handleSubmit = async (event) => {
    event.preventDefault(); setSaving(true); setError("");
    try {
      const response = await apiFetch(isEditing ? `${API_URL}/fechas/${fecha.idFecha}` : `${API_URL}/fechas`, { method: isEditing ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ...form, numeroFecha: Number(form.numeroFecha), idTorneo: Number(torneoId) }) });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudo crear la jornada"); }
      onSaved(await response.json()); onClose();
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };
  return <div className="tournament-form-backdrop" onMouseDown={onClose}><section className="tournament-form-modal" role="dialog" aria-modal="true" onMouseDown={(e) => e.stopPropagation()}><button type="button" className="tournament-form-close" onClick={onClose}>×</button><span className="tournament-form-chip">Fixture</span><h2>{isEditing ? "Editar jornada" : "Agregar jornada"}</h2><p>Configurá la jornada y su estado.</p><form onSubmit={handleSubmit}><div className="tournament-form-grid"><label>Número<input type="number" min="1" name="numeroFecha" value={form.numeroFecha} onChange={updateField} required /></label><label>Estado<select name="estado" value={form.estado} onChange={updateField}><option value="ACTIVA">Activa</option><option value="PENDIENTE">Pendiente</option><option value="FINALIZADA">Finalizada</option></select></label></div><label>Descripción<input name="descripcion" value={form.descripcion} onChange={updateField} required /></label>{error && <p className="tournament-form-error">{error}</p>}<div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onClose}>Cancelar</button><button type="submit" className="tournament-form-save" disabled={saving}>{saving ? "Guardando..." : isEditing ? "Guardar cambios" : "Crear jornada"}</button></div></form></section></div>;
}
export default DateFormModal;
