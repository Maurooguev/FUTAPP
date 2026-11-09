import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import "./MisTorneos.css";
import "./OrganizerProfile.css";
import { tieneRol } from "../utils/roles";
import { FEATURES } from "../config/features";

const GRUPOS = [
  { id: "EN_CURSO", label: "En curso", estados: ["ACTIVO", "PUBLICADO"] },
  { id: "BORRADORES", label: "Borradores", estados: ["BORRADOR"] },
  { id: "FINALIZADOS", label: "Finalizados", estados: ["FINALIZADO"] },
  { id: "OCULTOS", label: "Ocultos", estados: ["ELIMINADO", "CANCELADO", "ARCHIVADO"] },
];
const ESTADOS_OCULTOS = new Set(["ELIMINADO", "CANCELADO", "ARCHIVADO"]);

const getTournamentAction = (item, hidden = false) => {
  const state = item.torneo.estado?.toUpperCase();
  const base = `/torneo/${item.torneo.idTorneo}`;

  if (hidden || ["FINALIZADO", "ELIMINADO", "CANCELADO", "ARCHIVADO"].includes(state)) {
    return { label: "Ver torneo", to: base, tone: "neutral" };
  }
  if (item.equiposInscriptos < 2 || item.equiposSinPlantel > 0) {
    return { label: item.equiposInscriptos < 2 ? "Agregar equipos" : "Completar planteles", to: `${base}#equipos`, tone: "warning" };
  }
  if (item.partidosJugados === 0 && item.partidosPendientes === 0) {
    return { label: "Generar fixture", to: `${base}#fixture`, tone: "warning" };
  }
  if (state === "BORRADOR") return { label: "Publicar torneo", to: base, tone: "ready" };
  if (state === "PUBLICADO") return { label: "Iniciar torneo", to: base, tone: "ready" };
  if (item.idProximoPartido) {
    return { label: "Cargar resultado", to: `${base}?partido=${item.idProximoPartido}&accion=resultado`, tone: "primary" };
  }
  return { label: "Revisar torneo", to: base, tone: "primary" };
};

const getPriority = (item) => {
  const state = item.torneo.estado?.toUpperCase();
  if (item.equiposInscriptos < 2) return 0;
  if (item.equiposSinPlantel > 0) return 1;
  if (item.partidosJugados === 0 && item.partidosPendientes === 0) return 2;
  if (state === "BORRADOR" || state === "PUBLICADO") return 3;
  if (item.idProximoPartido) return 4;
  return 5;
};

function MisTorneos() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const [resumenes, setResumenes] = useState([]);
  const [pendientes, setPendientes] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [perfilOrganizador, setPerfilOrganizador] = useState(null);
  const [grupoActivo, setGrupoActivo] = useState("EN_CURSO");

  useEffect(() => {
    if (!usuario || (!tieneRol(usuario, "ORGANIZADOR") && !tieneRol(usuario, "ADMIN"))) return;
    Promise.all([
      apiFetch(`${API_URL}/torneos/mios/panel`).then(async (response) => {
        if (!response.ok) throw new Error("No se pudo cargar el panel");
        return response.json();
      }),
      FEATURES.claims ? apiFetch(`${API_URL}/reclamos/pendientes/count`)
        .then((response) => response.ok ? response.json() : { cantidad: 0 })
        .catch(() => ({ cantidad: 0 })) : Promise.resolve({ cantidad: 0 }),
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

  const torneosVisibles = useMemo(() => resumenes.filter(
    (item) => !ESTADOS_OCULTOS.has(item.torneo.estado?.toUpperCase())
  ), [resumenes]);
  const totales = useMemo(() => torneosVisibles.reduce((acc, item) => ({
    equipos: acc.equipos + item.equiposInscriptos,
    partidos: acc.partidos + item.partidosPendientes,
    alertas: acc.alertas + item.equiposSinPlantel,
  }), { equipos: 0, partidos: 0, alertas: 0 }), [torneosVisibles]);

  const grupos = useMemo(() => GRUPOS.map((grupo) => ({
    ...grupo,
    torneos: resumenes.filter((item) => grupo.estados.includes(item.torneo.estado?.toUpperCase())),
  })), [resumenes]);
  const grupoSeleccionado = grupos.find((grupo) => grupo.id === grupoActivo) || grupos[0];
  const torneosSeleccionados = useMemo(() => [...grupoSeleccionado.torneos].sort((a, b) => getPriority(a) - getPriority(b)), [grupoSeleccionado.torneos]);
  const siguienteTorneo = useMemo(() => {
    const candidates = grupos.find((grupo) => grupo.id === "EN_CURSO")?.torneos || [];
    return [...candidates].sort((a, b) => getPriority(a) - getPriority(b))[0] || null;
  }, [grupos]);

  if (!usuario) return <Navigate to="/login" replace />;
  if (!tieneRol(usuario, "ORGANIZADOR") && !tieneRol(usuario, "ADMIN")) return <main className="organizer-page"><div className="organizer-shell"><section className="organizer-empty"><h2>Acceso exclusivo para organizadores</h2><p>Esta versión de FUTAPP no habilita cuentas públicas. El acceso se entrega por invitación.</p><Link to="/login">Volver al acceso</Link></section></div></main>;

  return <main className="organizer-page"><div className="organizer-shell">
    <header className="organizer-heading"><div><span>Panel del organizador</span><h1>Mis torneos</h1><p>Lo importante para continuar cada competencia, en un solo lugar.</p></div><button onClick={() => navigate("/")}>+ Crear torneo</button></header>
    {!loading && siguienteTorneo && (() => { const action = getTournamentAction(siguienteTorneo); return <section className="organizer-focus"><div><span>Lo próximo</span><h2>{siguienteTorneo.accionRecomendada}</h2><p>{siguienteTorneo.torneo.nombre}{siguienteTorneo.idProximoPartido ? ` · ${siguienteTorneo.proximoPartidoLocal} vs ${siguienteTorneo.proximoPartidoVisitante}` : ""}</p></div><button className={`tone-${action.tone}`} onClick={() => navigate(action.to)}>{action.label} →</button></section>; })()}
    <section className="organizer-totals">
      <article><span>Torneos visibles</span><strong>{torneosVisibles.length}</strong></article>
      <article><span>Equipos</span><strong>{totales.equipos}</strong></article>
      <article><span>Partidos pendientes</span><strong>{totales.partidos}</strong></article>
      <article className={totales.alertas ? "has-alert" : ""}><span>Planteles incompletos</span><strong>{totales.alertas}</strong></article>
      {FEATURES.claims && <article className={pendientes ? "has-alert" : ""}><span>Solicitudes</span><strong>{pendientes}</strong>{pendientes > 0 && <button onClick={() => navigate("/solicitudes")}>Revisar</button>}</article>}
    </section>
    {loading ? <div className="organizer-message">Cargando panel…</div> : error ? <div className="organizer-message error">{error}</div> : resumenes.length === 0 ? <div className="organizer-empty"><h2>Todavía no creaste torneos</h2><p>Creá el primero y empezá cargando sus equipos.</p><button onClick={() => navigate("/")}>Crear torneo</button></div> : <>
      <nav className="organizer-filters" aria-label="Filtrar torneos">
        {grupos.map((grupo) => <button type="button" className={grupoActivo === grupo.id ? "active" : ""} onClick={() => setGrupoActivo(grupo.id)} key={grupo.id}>
          {grupo.label}<span>{grupo.torneos.length}</span>
        </button>)}
      </nav>
      {grupoSeleccionado.id === "OCULTOS" && <p className="organizer-hidden-note">Acá se guardan los torneos eliminados, cancelados o archivados. No aparecen públicamente y podés abrirlos en modo consulta.</p>}
      {grupoSeleccionado.torneos.length === 0 ? <div className="organizer-category-empty">No hay torneos en esta sección.</div> : <section className="organizer-list">
      {torneosSeleccionados.map((item) => { const action = getTournamentAction(item, grupoSeleccionado.id === "OCULTOS"); return <article className={`organizer-tournament state-${item.torneo.estado?.toLowerCase()}`} key={item.torneo.idTorneo}>
        <div className="organizer-tournament-main"><div className="organizer-tournament-identity"><div className="organizer-mark">{item.torneo.logoOrganizador ? <img src={item.torneo.logoOrganizador} alt="" /> : <span>🏆</span>}</div><div><span>{item.torneo.codigoPublico}</span><h2>{item.torneo.nombre}</h2><p>{item.torneo.ubicacion} · {item.torneo.formato}</p></div></div><div className={`organizer-badge state-${item.torneo.estado?.toLowerCase()}`}>{item.torneo.estado}</div></div>
        <div className="organizer-progress">
          <div><span>Equipos</span><strong>{item.equiposInscriptos}/{item.torneo.cantidadEquipos}</strong></div><div className={item.equiposSinPlantel ? "needs-attention" : ""}><span>Planteles pendientes</span><strong>{item.equiposSinPlantel}</strong></div><div><span>Partidos jugados</span><strong>{item.partidosJugados}</strong></div><div><span>Próxima jornada</span><strong>{item.proximaJornada ? `Fecha ${item.proximaJornada}` : "—"}</strong></div>
        </div>
        <footer><div><span>{grupoSeleccionado.id === "OCULTOS" ? "Estado" : "Siguiente paso"}</span><strong>{item.accionRecomendada}</strong>{item.idProximoPartido && grupoSeleccionado.id !== "OCULTOS" && <small>{item.proximoPartidoLocal} vs {item.proximoPartidoVisitante}</small>}</div><button className={`organizer-primary-action tone-${action.tone}`} onClick={() => navigate(action.to)}>{action.label} →</button></footer>
      </article>; })}
      </section>}
    </>}
    {!loading && resumenes.length > 0 && <section className="organizer-profile-entry"><div><strong>{perfilOrganizador ? "Tu perfil público está activo" : "Perfil público de organizador"}</strong><span>{perfilOrganizador ? "Mostrá quién organiza y reuní tus torneos publicados." : "Es opcional: podés completarlo cuando tus torneos estén listos."}</span></div><Link to={perfilOrganizador ? `/organizador/${perfilOrganizador.idUsuario}` : "/mi-perfil-organizador"}>{perfilOrganizador ? "Ver perfil" : "Configurar"}</Link></section>}
  </div></main>;
}

export default MisTorneos;
