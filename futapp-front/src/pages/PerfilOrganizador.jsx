import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { API_URL, assetUrl } from "../config/api";
import useAuth from "../context/useAuth";
import "./OrganizerProfile.css";

function PerfilOrganizador() {
  const { id } = useParams();
  const { usuario } = useAuth();
  const [perfil, setPerfil] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    fetch(`${API_URL}/perfiles-organizador/${id}`)
      .then(async (response) => {
        const body = await response.json().catch(() => null);
        if (!response.ok) throw new Error(body?.message || "No se pudo abrir el perfil");
        setPerfil(body);
      })
      .catch((requestError) => setError(requestError.message));
  }, [id]);

  if (error) return <main className="organizer-profile-page"><div className="organizer-profile-message"><p>{error}</p><Link to="/">Volver al inicio</Link></div></main>;
  if (!perfil) return <main className="organizer-profile-page"><div className="organizer-profile-message">Cargando perfil…</div></main>;

  const propio = usuario?.idUsuario === perfil.idUsuario;
  return <main className="organizer-profile-page"><div className="organizer-profile-shell">
    <section className="organizer-profile-hero">
      <div className="organizer-profile-cover">{perfil.fotoPortada ? <img src={assetUrl(perfil.fotoPortada)} alt="Portada del organizador" /> : <span>Organizador de torneos</span>}</div>
      <div className="organizer-profile-identity">
        <div className="organizer-profile-avatar">{perfil.fotoPerfil ? <img src={assetUrl(perfil.fotoPerfil)} alt={perfil.nombrePublico} /> : perfil.nombrePublico.slice(0, 2).toUpperCase()}</div>
        <div><span>Perfil de organizador</span><h1>{perfil.nombrePublico}</h1><p>{perfil.descripcion}</p></div>
        {propio && <Link to="/mi-perfil-organizador">Editar perfil</Link>}
      </div>
    </section>

    <section className="organizer-profile-tournaments">
      <div><span>Competencias</span><h2>Torneos publicados</h2><p>{perfil.torneos.length} torneo{perfil.torneos.length === 1 ? "" : "s"}</p></div>
      {perfil.torneos.length === 0 ? <div className="organizer-profile-empty">Todavía no hay torneos públicos.</div> : <div className="organizer-public-grid">
        {perfil.torneos.map((torneo) => <Link to={`/torneo/${torneo.idTorneo}`} key={torneo.idTorneo} className="organizer-public-tournament">
          <div><span>{torneo.estado}</span><small>{torneo.codigoPublico}</small></div>
          <h3>{torneo.nombre}</h3><p>{torneo.descripcion || "Sin descripción"}</p>
          <footer><span>{torneo.ubicacion}</span><span>{torneo.formato}</span><strong>Ver torneo →</strong></footer>
        </Link>)}
      </div>}
    </section>
  </div></main>;
}

export default PerfilOrganizador;
