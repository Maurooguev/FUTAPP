import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "../TournamentFormModal/TournamentFormModal.css";
import ImageUploadField from "../ImageUploadField/ImageUploadField";

const EMPTY_TEAM = {
  nombre: "",
  escudo: "",
  descripcion: "",
  fotoPlantel: "",
  emailContacto: "",
  colorCamiseta: "",
  barrio: "",
  responsable: "",
  telefonoContacto: "",
  estado: "ACTIVO",
};

function TeamFormModal({ torneoId, equipo, onClose, onSaved }) {
  const [form, setForm] = useState(EMPTY_TEAM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const isEditing = Boolean(equipo?.idEquipo);

  useEffect(() => {
    setForm(
      equipo
        ? {
            nombre: equipo.nombre ?? "",
            escudo: equipo.escudo ?? "",
            descripcion: equipo.descripcion ?? "",
            fotoPlantel: equipo.fotoPlantel ?? "",
            emailContacto: equipo.emailContacto ?? "",
            colorCamiseta: equipo.colorCamiseta ?? "",
            barrio: equipo.barrio ?? "",
            responsable: equipo.responsable ?? "",
            telefonoContacto: equipo.telefonoContacto ?? "",
            estado: equipo.estado ?? "ACTIVO",
          }
        : EMPTY_TEAM
    );
  }, [equipo]);

  const updateField = ({ target: { name, value } }) => {
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      const response = await apiFetch(
        isEditing ? `${API_URL}/equipos/${equipo.idEquipo}` : `${API_URL}/equipos`,
        {
          method: isEditing ? "PUT" : "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ...form, idTorneo: Number(torneoId) }),
        }
      );
      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        throw new Error(payload?.message || "No se pudo guardar el equipo");
      }
      onSaved(await response.json());
      onClose();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="tournament-form-backdrop" onMouseDown={onClose}>
      <section className="tournament-form-modal" role="dialog" aria-modal="true" onMouseDown={(e) => e.stopPropagation()}>
        <button type="button" className="tournament-form-close" aria-label="Cerrar" onClick={onClose}>×</button>
        <span className="tournament-form-chip">Equipos</span>
        <h2>{isEditing ? "Editar equipo" : "Agregar equipo"}</h2>
        <p>Registrá los datos básicos del club y su responsable.</p>
        <form onSubmit={handleSubmit}>
          <div className="tournament-form-grid">
            <label>Nombre<input name="nombre" value={form.nombre} onChange={updateField} maxLength="120" required /></label>
            <label>Barrio<input name="barrio" value={form.barrio} onChange={updateField} maxLength="120" required /></label>
            <label>Color de camiseta<input name="colorCamiseta" value={form.colorCamiseta} onChange={updateField} maxLength="80" required /></label>
            <label>Responsable<input name="responsable" value={form.responsable} onChange={updateField} maxLength="120" required /></label>
            <label>Teléfono<input name="telefonoContacto" value={form.telefonoContacto} onChange={updateField} maxLength="40" required /></label>
            <label>Email de contacto<input type="email" name="emailContacto" value={form.emailContacto} onChange={updateField} maxLength="160" /></label>
            <label>Estado<select name="estado" value={form.estado} onChange={updateField}><option value="ACTIVO">Activo</option><option value="INACTIVO">Inactivo</option></select></label>
          </div>
          <ImageUploadField label="Escudo" value={form.escudo} shape="avatar" onUploaded={(escudo) => setForm((current) => ({ ...current, escudo }))} />
          <ImageUploadField label="Foto del plantel" value={form.fotoPlantel} onUploaded={(fotoPlantel) => setForm((current) => ({ ...current, fotoPlantel }))} />
          <label>Descripción<textarea name="descripcion" value={form.descripcion} onChange={updateField} maxLength="1000" rows="4" /></label>
          {error && <p className="tournament-form-error">{error}</p>}
          <div className="tournament-form-actions">
            <button type="button" className="tournament-form-cancel" onClick={onClose}>Cancelar</button>
            <button type="submit" className="tournament-form-save" disabled={saving}>{saving ? "Guardando..." : "Guardar equipo"}</button>
          </div>
        </form>
      </section>
    </div>
  );
}

export default TeamFormModal;
