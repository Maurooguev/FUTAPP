import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import "./MisTorneos.css";
import "./OrganizerProfile.css";
import { tieneRol } from "../utils/roles";

function MisTorneos() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const [resumenes, setResumenes] = useState([]);
  const [pendientes, setPendientes] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [perfilOrganizador, setPerfilOrganizador] = useState(null);

  useEffect(() => {
    if (!usuario || (!tieneRol(usuario, "ORGANIZADOR") && !tieneRol(usuario, "ADMIN"))) return;
    Promise.all([
      apiFetch(`${API_URL}/torneos/mios/panel`).then(async (response) => {
        if (!response.ok) throw new Error("No se pudo cargar el panel");
        return response.json();
      }),
      apiFetch(`${API_URL}/reclamos/pendientes/count`)
        .then((response) => response.ok ? response.json() : { cantidad: 0 })
        .catch(() => ({ cantidad: 0 })),
      apiFetch(`${API_URL}/perfiles-organizador/mio`).then(async (response) => {
        if (!response.ok) return null;
        return response.json();
      }).catch(() => null),
    ]).then(([panel, solicitudes, perfil]) => {
      setResumenes(panel);
      setPendientes(solicitudes.cantidad || 0);
      setPerfilOrganizador(perfil);
    }).catch((requestError) => setError(requestError.message)).finally(() => setLoading(false));
  }, [usuario]);

  const totales = useMemo(() => resumenes.reduce((acc, item) => ({
    equipos: acc.equipos + item.equiposInscriptos,
    partidos: acc.partidos + item.partidosPendientes,
    alertas: acc.alertas + item.equiposSinPlantel,
  }), { equipos: 0, partidos: 0, alertas: 0 }), [resumenes]);

  if (!usuario) return <Navigate to="/login" replace />;
  if (!tieneRol(usuario, "ORGANIZADOR") && !tieneRol(usuario, "ADMIN")) return <main className="organizer-page"><div className="organizer-shell"><section className="organizer-empty"><h2>Cuenta de organizador requerida</h2><p>Debés crear una cuenta de organizador de torneos. El tipo de una cuenta existente no se puede cambiar.</p><Link to="/register?intent=crear-torneo">Crear cuenta de organizador</Link></section></div></main>;

  return <main className="organizer-page"><div className="organizer-shell">
    <header className="organizer-heading"><div><span>Panel del organizador</span><h1>Mis torneos</h1><p>Lo importante para continuar cada competencia, en un solo lugar.</p></div><button onClick={() => navigate("/")}>+ Crear torneo</button></header>
    {!loading && resumenes.length > 0 && <section className="organizer-profile-entry"><div><strong>{perfilOrganizador ? "Tu perfil público está activo" : "Perfil público de organizador"}</strong><span>{perfilOrganizador ? "Mostrá quién organiza y reuní tus torneos publicados." : "Es opcional: crealo cuando tengas lista tu descripción y tus fotos."}</span></div><Link to={perfilOrganizador ? `/organizador/${perfilOrganizador.idUsuario}` : "/mi-perfil-organizador"}>{perfilOrganizador ? "Ver perfil" : "Generar perfil"}</Link></section>}
    <section className="organizer-totals">
      <article><span>Torneos</span><strong>{resumenes.length}</strong></article>
      <article><span>Equipos</span><strong>{totales.equipos}</strong></article>
      <article><span>Partidos pendientes</span><strong>{totales.partidos}</strong></article>
      <article className={totales.alertas ? "has-alert" : ""}><span>Planteles incompletos</span><strong>{totales.alertas}</strong></article>
      <article className={pendientes ? "has-alert" : ""}><span>Solicitudes</span><strong>{pendientes}</strong>{pendientes > 0 && <button onClick={() => navigate("/solicitudes")}>Revisar</button>}</article>
    </section>
    {loading ? <div className="organizer-message">Cargando panel…</div> : error ? <div className="organizer-message error">{error}</div> : resumenes.length === 0 ? <div className="organizer-empty"><h2>Todavía no creaste torneos</h2><p>Creá el primero y empezá cargando sus equipos.</p><button onClick={() => navigate("/")}>Crear torneo</button></div> : <section className="organizer-list">
      {resumenes.map((item) => <article className="organizer-tournament" key={item.torneo.idTorneo}>
        <div className="organizer-tournament-main"><div className="organizer-badge">{item.torneo.estado}</div><div><span>{item.torneo.codigoPublico}</span><h2>{item.torneo.nombre}</h2><p>{item.torneo.ubicacion} · {item.torneo.formato}</p></div></div>
        <div className="organizer-progress">
          <div><span>Equipos</span><strong>{item.equiposInscriptos}/{item.torneo.cantidadEquipos}</strong></div><div><span>Sin plantel</span><strong>{item.equiposSinPlantel}</strong></div><div><span>Jugados</span><strong>{item.partidosJugados}</strong></div><div><span>Pendientes</span><strong>{item.partidosPendientes}</strong></div><div><span>Próxima jornada</span><strong>{item.proximaJornada ? `Fecha ${item.proximaJornada}` : "—"}</strong></div>
        </div>
        <footer><div><span>Siguiente paso</span><strong>{item.accionRecomendada}</strong>{item.idProximoPartido && <small>{item.proximoPartidoLocal} vs {item.proximoPartidoVisitante}</small>}</div><div className="organizer-tournament-actions">{item.idProximoPartido && <button className="result" onClick={() => navigate(`/torneo/${item.torneo.idTorneo}?partido=${item.idProximoPartido}&accion=resultado`)}>Cargar resultado</button>}<button onClick={() => navigate(`/torneo/${item.torneo.idTorneo}`)}>Administrar →</button></div></footer>
      </article>)}
    </section>}
  </div></main>;
}

export default MisTorneos;
