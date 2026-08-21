import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../config/api";
import { Link } from "react-router-dom";
import ImageUploadField from "../components/ImageUploadField/ImageUploadField";
import "./Profiles.css";

function MiPerfilJugador() {
  const [form, setForm] = useState(null); const [message, setMessage] = useState("");
  useEffect(() => { apiFetch(`${API_URL}/perfil-jugador/mio`).then(async (r) => { const body = await r.json(); if (!r.ok) throw new Error(body.message); setForm(body); }).catch((e) => setMessage(e.message)); }, []);
  if (!form) return <main className="profile-page"><div className="profile-message">{message || "Cargando perfil..."}</div></main>;
  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });
  const save = async (e) => { e.preventDefault(); const response = await apiFetch(`${API_URL}/perfil-jugador/mio`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ...form, alturaCm: form.alturaCm ? Number(form.alturaCm) : null }) }); setMessage(response.ok ? "Perfil actualizado" : (await response.json()).message); };
  const fields = [["apodo", "Apodo"], ["fechaNacimiento", "Fecha de nacimiento"], ["piernaHabil", "Pierna hábil"], ["equipoFavorito", "Equipo favorito"], ["barrio", "Barrio"], ["alturaCm", "Altura en cm"], ["nacionalidad", "Nacionalidad"], ["instagram", "Instagram"]];
  return <main className="profile-page"><Link className="profile-back-link" to={`/jugador/${form.idJugador}`}>← Volver a mi perfil</Link><h1>Editar perfil</h1><form className="profile-panel personal-profile-form" onSubmit={save}><ImageUploadField label="Foto de perfil" value={form.foto} shape="avatar" onUploaded={(foto) => setForm((current) => ({ ...current, foto }))} />{fields.map(([name, label]) => <label key={name}>{label}<input name={name} value={form[name] || ""} onChange={change} /></label>)}<label>Biografía<textarea name="biografia" value={form.biografia || ""} onChange={change} /></label><button>Guardar perfil</button>{message && <p>{message}</p>}</form></main>;
}
export default MiPerfilJugador;
