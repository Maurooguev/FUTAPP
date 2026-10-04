import { useEffect, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";
import ImageUploadField from "../components/ImageUploadField/ImageUploadField";
import "./OrganizerProfile.css";

const emptyForm = { nombrePublico: "", descripcion: "", fotoPerfil: "", fotoPortada: "" };

function MiPerfilOrganizador() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [exists, setExists] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!usuario) return;
    apiFetch(`${API_URL}/perfiles-organizador/mio`).then(async (response) => {
      const body = await response.json().catch(() => null);
      if (response.status === 404) return;
      if (!response.ok) throw new Error(body?.message || "No se pudo cargar el perfil");
      setExists(true);
      setForm({ nombrePublico: body.nombrePublico || "", descripcion: body.descripcion || "", fotoPerfil: body.fotoPerfil || "", fotoPortada: body.fotoPortada || "" });
    }).catch((requestError) => setError(requestError.message)).finally(() => setLoading(false));
  }, [usuario]);

  if (!usuario) return <Navigate to="/login" replace />;
  if (!tieneRol(usuario, "ORGANIZADOR") && !tieneRol(usuario, "ADMIN")) return <Navigate to="/mis-torneos" replace />;
  if (loading) return <main className="organizer-profile-page"><div className="organizer-profile-message">Preparando perfil…</div></main>;

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true); setError("");
    try {
      const response = await apiFetch(`${API_URL}/perfiles-organizador/mio`, {
        method: exists ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(form),
      });
      const body = await response.json().catch(() => null);
      if (!response.ok) throw new Error(body?.message || "No se pudo guardar el perfil");
      navigate(`/organizador/${body.idUsuario}`);
    } catch (requestError) { setError(requestError.message); setSaving(false); }
  };

  return <main className="organizer-profile-page"><div className="organizer-editor-shell">
    <Link to="/mis-torneos" className="organizer-profile-back">← Volver a mis torneos</Link>
    <header><span>{exists ? "Editar perfil" : "Perfil opcional"}</span><h1>{exists ? "Actualizá tu perfil de organizador" : "Creá tu perfil de organizador"}</h1><p>Este perfil será público. Completalo cuando tengas información e imágenes que quieras mostrar.</p></header>
    <form className="organizer-profile-form" onSubmit={submit}>
      <ImageUploadField label="Logo del organizador" value={form.fotoPerfil} shape="avatar" allowBackgroundRemoval onUploaded={(fotoPerfil) => setForm((current) => ({ ...current, fotoPerfil }))} />
      <ImageUploadField label="Foto de portada" value={form.fotoPortada} onUploaded={(fotoPortada) => setForm((current) => ({ ...current, fotoPortada }))} />
      <label>Nombre público<input required maxLength="80" value={form.nombrePublico} onChange={(event) => setForm((current) => ({ ...current, nombrePublico: event.target.value }))} placeholder="Ej: Liga Amateur Zona Oeste" /></label>
      <label>Descripción<textarea required maxLength="1200" rows="7" value={form.descripcion} onChange={(event) => setForm((current) => ({ ...current, descripcion: event.target.value }))} placeholder="Contá quién organiza, en qué zona y qué tipo de torneos realiza." /></label>
      <div className="organizer-profile-form-actions"><Link to="/mis-torneos">Cancelar</Link><button disabled={saving}>{saving ? "Guardando…" : exists ? "Guardar cambios" : "Crear perfil público"}</button></div>
      {error && <p className="organizer-profile-error">{error}</p>}
    </form>
  </div></main>;
}

export default MiPerfilOrganizador;
