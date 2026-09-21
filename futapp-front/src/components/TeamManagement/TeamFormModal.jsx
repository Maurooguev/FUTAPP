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

function TeamFormModal({ torneoId, equipo, onClose, onSaved, onLinked }) {
  const [form, setForm] = useState(EMPTY_TEAM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [linkCode, setLinkCode] = useState("");
  const [linkTarget, setLinkTarget] = useState(null);
  const [linking, setLinking] = useState(false);
  const [linkMessage, setLinkMessage] = useState("");
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

  const searchLinkTarget = async () => {
    setError(""); setLinkMessage(""); setLinkTarget(null);
    if (!linkCode.trim()) return setError("Ingresá el código público del club.");
    try {
      const response = await fetch(`${API_URL}/equipos/codigo/${encodeURIComponent(linkCode.trim())}`);
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No encontramos un club con ese código");
      if (payload.idEquipo === equipo.idEquipo) throw new Error("Ese código pertenece a este mismo equipo.");
      setLinkTarget(payload);
    } catch (requestError) { setError(requestError.message); }
  };

  const linkTeam = async () => {
    if (!linkTarget || !window.confirm(`¿Vincular ${equipo.nombre} con ${linkTarget.nombre}? Se conservará el perfil ${linkTarget.codigoPublico} y se trasladará el historial de este equipo.`)) return;
    setLinking(true); setError(""); setLinkMessage("");
    try {
      const response = await apiFetch(`${API_URL}/equipos/${linkTarget.idEquipo}/fusionar/${equipo.idEquipo}`, { method: "POST" });
      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        if (response.status === 403) {
          const requestResponse = await apiFetch(`${API_URL}/reclamos/equipos/${equipo.idEquipo}/fusionar/${encodeURIComponent(linkTarget.codigoPublico)}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              idTorneo: Number(torneoId),
              mensaje: `Solicito vincular ${equipo.nombre} con el perfil oficial ${linkTarget.nombre}.`,
            }),
          });
          const requestPayload = await requestResponse.json().catch(() => null);
          if (!requestResponse.ok) throw new Error(requestPayload?.message || "No se pudo enviar la solicitud de vinculación");
          setLinkTarget(null); setLinkCode("");
          setLinkMessage(`Solicitud enviada a los responsables de ${linkTarget.nombre}. La vinculación se hará cuando la aprueben.`);
          window.dispatchEvent(new Event("requests-updated"));
          return;
        }
        throw new Error(payload?.message || "No se pudieron vincular los clubes");
      }
      const officialResponse = await fetch(`${API_URL}/equipos/${linkTarget.idEquipo}`);
      const officialTeam = officialResponse.ok ? await officialResponse.json() : linkTarget;
      onLinked?.(officialTeam, equipo.idEquipo); onClose();
    } catch (requestError) { setError(requestError.message); }
    finally { setLinking(false); }
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
          {isEditing && <section className="team-link-panel">
            <div><span>Perfiles duplicados</span><h3>Fusionar con el perfil oficial</h3><p>Usá esta opción solamente si ambos perfiles representan al mismo club. Esto no lo invita al torneo: para eso usá “Inscribir club existente”.</p><p>El código actual quedará como alias y no se perderán partidos ni estadísticas.</p></div>
            <label>Código público<div className="team-code-search"><input value={linkCode} onChange={(event) => { setLinkCode(event.target.value.toUpperCase()); setLinkTarget(null); }} placeholder="EQ-XXXXXXX" /><button type="button" onClick={searchLinkTarget}>Buscar</button></div></label>
            {linkTarget && <div className="team-link-preview"><span>{linkTarget.nombre.slice(0, 2).toUpperCase()}</span><div><strong>{linkTarget.nombre}</strong><small>{linkTarget.barrio || "Sin barrio"} · {linkTarget.codigoPublico}</small></div><button type="button" onClick={linkTeam} disabled={linking}>{linking ? "Fusionando…" : "Solicitar fusión"}</button></div>}
            {linkMessage && <p className="profile-action-message">{linkMessage}</p>}
          </section>}
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
