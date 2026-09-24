import { useEffect, useMemo, useState } from "react";
import { API_URL, apiFetch } from "../config/api";
import { Link } from "react-router-dom";
import ImageUploadField from "../components/ImageUploadField/ImageUploadField";
import ArgentinaLocationInput from "../components/TournamentFormModal/ArgentinaLocationInput";
import "./Profiles.css";

const PIERNAS = ["Derecha", "Izquierda", "Ambas"];
const NACIONALIDADES = [
  "Argentina", "Boliviana", "Brasileña", "Chilena", "Colombiana", "Costarricense",
  "Cubana", "Dominicana", "Ecuatoriana", "Española", "Estadounidense", "Francesa",
  "Italiana", "Mexicana", "Panameña", "Paraguaya", "Peruana", "Portuguesa",
  "Uruguaya", "Venezolana", "Otra",
];
const ALTURAS = Array.from({ length: 151 }, (_, index) => index + 100);

function MiPerfilJugador() {
  const [form, setForm] = useState(null);
  const [equipos, setEquipos] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    Promise.all([
      apiFetch(`${API_URL}/perfil-jugador/mio`).then(async (response) => {
        const body = await response.json();
        if (!response.ok) throw new Error(body.message);
        return body;
      }),
      apiFetch(`${API_URL}/equipos`).then((response) => response.ok ? response.json() : []),
    ]).then(([perfil, clubes]) => {
      setForm(perfil);
      setEquipos(Array.isArray(clubes) ? clubes : []);
    }).catch((error) => setMessage(error.message));
  }, []);

  const nombresEquipos = useMemo(() => [...new Set(equipos.map((equipo) => equipo.nombre).filter(Boolean))]
    .sort((a, b) => a.localeCompare(b, "es")), [equipos]);

  if (!form) return <main className="profile-page"><div className="profile-message">{message || "Cargando perfil..."}</div></main>;

  const change = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  const save = async (event) => {
    event.preventDefault();
    const response = await apiFetch(`${API_URL}/perfil-jugador/mio`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ...form, alturaCm: form.alturaCm ? Number(form.alturaCm) : null }),
    });
    setMessage(response.ok ? "Perfil actualizado" : (await response.json()).message);
  };

  return <main className="profile-page">
    <Link className="profile-back-link" to={`/jugador/${form.idJugador}`}>← Volver a mi perfil</Link>
    <h1>Editar perfil</h1>
    <form className="profile-panel personal-profile-form" onSubmit={save}>
      <ImageUploadField label="Foto de perfil" value={form.foto} shape="avatar" onUploaded={(foto) => setForm((current) => ({ ...current, foto }))} />
      <label>Apodo<input name="apodo" value={form.apodo || ""} onChange={change} /></label>
      <label>Fecha de nacimiento<input type="date" name="fechaNacimiento" value={form.fechaNacimiento || ""} onChange={change} /></label>
      <label>Pierna hábil<select name="piernaHabil" value={form.piernaHabil || ""} onChange={change}><option value="">Seleccionar</option>{PIERNAS.map((pierna) => <option key={pierna}>{pierna}</option>)}</select></label>
      <label>Equipo favorito<select name="equipoFavorito" value={form.equipoFavorito || ""} onChange={change}><option value="">Seleccionar equipo</option>{form.equipoFavorito && !nombresEquipos.includes(form.equipoFavorito) && <option>{form.equipoFavorito}</option>}{nombresEquipos.map((equipo) => <option key={equipo}>{equipo}</option>)}</select></label>
      <label>Barrio<ArgentinaLocationInput value={form.barrio || ""} onChange={(barrio) => setForm((current) => ({ ...current, barrio }))} onSelect={(barrio) => setForm((current) => ({ ...current, barrio }))} /></label>
      <label>Altura en cm<select name="alturaCm" value={form.alturaCm || ""} onChange={change}><option value="">Seleccionar altura</option>{ALTURAS.map((altura) => <option key={altura} value={altura}>{altura} cm</option>)}</select></label>
      <label>Nacionalidad<select name="nacionalidad" value={form.nacionalidad || ""} onChange={change}><option value="">Seleccionar nacionalidad</option>{form.nacionalidad && !NACIONALIDADES.includes(form.nacionalidad) && <option>{form.nacionalidad}</option>}{NACIONALIDADES.map((nacionalidad) => <option key={nacionalidad}>{nacionalidad}</option>)}</select></label>
      <label>Instagram<input name="instagram" value={form.instagram || ""} onChange={change} /></label>
      <label>Biografía<textarea name="biografia" maxLength="1000" value={form.biografia || ""} onChange={change} placeholder="Contá libremente tu historia como jugador" /></label>
      <button>Guardar perfil</button>
      {message && <p>{message}</p>}
    </form>
  </main>;
}

export default MiPerfilJugador;
