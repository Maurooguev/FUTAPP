import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { API_URL, apiFetch, assetUrl } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";
import "./Profiles.css";

const formatDate = (value) => value
  ? new Intl.DateTimeFormat("es-AR", { dateStyle: "short", timeStyle: "short" }).format(new Date(value))
  : "—";

function SolicitudCard({ item, onResolve, rejecting, onReject, onCancelReject, canResolve = false }) {
  const [reason, setReason] = useState("");
  const isFusion = item.tipo === "FUSION_EQUIPO";
  const isInvitation = item.tipo === "INVITACION_TORNEO";
  const isManagementInvitation = item.tipo === "VINCULACION_EQUIPO";
  const isPlayerChange = item.tipo === "CAMBIO_JUGADOR";
  const isTeam = item.tipo === "EQUIPO";
  const targetUrl = isFusion ? `/equipo/${item.idEquipoDestino}` : (isTeam || isManagementInvitation) ? `/equipo/${item.idEquipo}` : `/jugador/${item.idJugador}`;
  const targetName = isFusion ? item.nombreEquipoDestino : (isTeam || isManagementInvitation) ? item.nombreEquipo : item.nombreJugador;

  return <article className="request-card">
    <div className="request-avatar">{item.fotoSolicitante ? <img src={assetUrl(item.fotoSolicitante)} alt="" /> : item.nombreSolicitante?.slice(0, 2).toUpperCase()}</div>
    <div className="request-content">
      <div className="request-title"><div><span>{isPlayerChange ? "Cambio de datos privados" : isInvitation ? "Invitación a torneo" : isManagementInvitation ? "Invitación para administrar club" : isFusion ? "Vinculación de equipos" : isTeam ? "Reclamo de equipo" : "Reclamo de jugador"}</span><h2>{item.nombreSolicitante}</h2></div><strong className={`request-status ${item.estado.toLowerCase()}`}>{item.estado}</strong></div>
      <div className="request-meta"><span>{item.rolSolicitante?.replaceAll("_", " ")}</span><a href={`mailto:${item.emailSolicitante}`}>{item.emailSolicitante}</a><span>{formatDate(item.fechaSolicitud)}</span></div>
      {isPlayerChange
        ? <><p>Propone modificar los datos privados de <Link to={targetUrl}>{targetName}</Link>.</p><dl className="request-changes"><div><dt>Nombre</dt><dd>{item.nombrePropuesto || "—"}</dd></div><div><dt>Apellido</dt><dd>{item.apellidoPropuesto || "—"}</dd></div><div><dt>DNI</dt><dd>{item.dniPropuesto || "Sin informar"}</dd></div><div><dt>Nacimiento</dt><dd>{item.fechaNacimientoPropuesta || "—"}</dd></div></dl></>
        : isInvitation
        ? <p>Invita a <Link to={`/equipo/${item.idEquipo}`}>{item.nombreEquipo}</Link> a participar en <Link to={`/torneo/${item.idTorneo}`}>{item.nombreTorneo}</Link>{item.categoria ? ` en la categoría ${item.categoria}` : ""}. Al aceptar, el director podrá elegir qué jugadores del club disputarán el torneo.</p>
        : isManagementInvitation
        ? <p>Te invita a administrar <Link to={targetUrl}>{targetName}</Link>. Al aceptar, este será el único club asociado a tu cuenta de director.</p>
        : isFusion
        ? <p>Solicita vincular <Link to={`/equipo/${item.idEquipo}`}>{item.nombreEquipo}</Link> con el perfil oficial <Link to={targetUrl}>{targetName}</Link> ({item.codigoEquipoDestino}).</p>
        : <p>{isTeam ? "Solicita administrar" : "Solicita vincular su cuenta con"} <Link to={targetUrl}>{targetName}</Link> en <Link to={`/torneo/${item.idTorneo}`}>{item.nombreTorneo}</Link>.</p>}
      {!isFusion && !isInvitation && !isPlayerChange && <p className="request-team">Equipo: <Link to={`/equipo/${item.idEquipo}`}>{item.nombreEquipo}</Link></p>}
      {item.mensaje && <blockquote>“{item.mensaje}”</blockquote>}
      {item.estado !== "PENDIENTE" && <div className="request-resolution"><strong>{item.nombreRevisor || "Revisor"}</strong> · {formatDate(item.fechaResolucion)}{item.motivoResolucion && <p>Motivo: {item.motivoResolucion}</p>}</div>}
      {item.estado === "PENDIENTE" && !canResolve && <div className="request-resolution">Esperando revisión.</div>}
      {item.estado === "PENDIENTE" && canResolve && !rejecting && <div className="request-actions"><button onClick={() => onResolve(item.idSolicitud, true)}>Aprobar</button><button className="danger" onClick={() => onReject(item.idSolicitud)}>Rechazar</button></div>}
      {canResolve && rejecting && <div className="request-reject-form"><label>Motivo del rechazo<textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Explicá brevemente por qué se rechaza…" autoFocus /></label><div><button onClick={onCancelReject}>Cancelar</button><button className="danger" disabled={!reason.trim()} onClick={() => onResolve(item.idSolicitud, false, reason)}>Confirmar rechazo</button></div></div>}
    </div>
  </article>;
}

function Solicitudes() {
  const { usuario } = useAuth();
  const canReview = tieneRol(usuario, "ADMIN") || tieneRol(usuario, "ORGANIZADOR") || tieneRol(usuario, "DIRECTOR_EQUIPO") || tieneRol(usuario, "JUGADOR");
  const [reviewItems, setReviewItems] = useState([]);
  const [myItems, setMyItems] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState(canReview ? "REVISAR" : "ENVIADAS");
  const [rejectingId, setRejectingId] = useState(null);
  const [resolving, setResolving] = useState(false);
  const load = useCallback(async () => {
    try {
      const [mineResponse, reviewResponse] = await Promise.all([
        apiFetch(`${API_URL}/reclamos/mios`),
        canReview ? apiFetch(`${API_URL}/reclamos/revisables`) : Promise.resolve(null),
      ]);
      const mine = await mineResponse.json().catch(() => null);
      if (!mineResponse.ok) throw new Error(mine?.message || "No se pudieron cargar tus solicitudes");
      const review = reviewResponse ? await reviewResponse.json().catch(() => null) : [];
      if (reviewResponse && !reviewResponse.ok) throw new Error(review?.message || "No se pudieron cargar las solicitudes pendientes");
      setMyItems(Array.isArray(mine) ? mine : []);
      setReviewItems(Array.isArray(review) ? review : []);
    } catch (requestError) { setMessage(requestError.message); }
    finally { setLoading(false); }
  }, [canReview]);
  useEffect(() => { load(); }, [load]);
  const pendingCount = reviewItems.filter((item) => item.estado === "PENDIENTE").length;
  const sentPendingCount = myItems.filter((item) => item.estado === "PENDIENTE").length;
  const visibleItems = useMemo(() => {
    if (tab === "REVISAR") return reviewItems.filter((item) => item.estado === "PENDIENTE");
    if (tab === "ENVIADAS") return myItems;
    return reviewItems.filter((item) => item.estado !== "PENDIENTE");
  }, [myItems, reviewItems, tab]);
  const resolve = async (id, approve, reason = "") => {
    if (resolving) return;
    setResolving(true);
    const response = await apiFetch(`${API_URL}/reclamos/${id}/${approve ? "aprobar" : "rechazar"}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ motivo: reason }) });
    const body = await response.json().catch(() => null);
    setMessage(response.ok ? (approve ? "Solicitud aprobada." : "Solicitud rechazada.") : body?.message || "No se pudo resolver");
    if (response.ok) {
      setRejectingId(null);
      localStorage.setItem("tournamentDataUpdatedAt", String(Date.now()));
      window.dispatchEvent(new Event("requests-updated"));
      await load();
    }
    setResolving(false);
  };
  return <main className="profile-page requests-page">
    <div className="requests-heading"><div><span>Gestión de accesos</span><h1>Solicitudes</h1><p>Seguí tus reclamos y, si administrás un torneo o equipo, revisá los accesos pendientes.</p></div><div className="requests-count"><strong>{canReview ? pendingCount : sentPendingCount}</strong><span>pendientes</span></div></div>
    {message && <p className="profile-action-message">{message}</p>}
    <div className="requests-tabs">{canReview && <button className={tab === "REVISAR" ? "active" : ""} onClick={() => setTab("REVISAR")}>Para revisar <span>{pendingCount}</span></button>}<button className={tab === "ENVIADAS" ? "active" : ""} onClick={() => setTab("ENVIADAS")}>Enviadas <span>{myItems.length}</span></button>{canReview && <button className={tab === "HISTORIAL" ? "active" : ""} onClick={() => setTab("HISTORIAL")}>Historial <span>{reviewItems.length - pendingCount}</span></button>}</div>
    <section className="requests-list">{loading ? <div className="profile-message">Cargando solicitudes...</div> : visibleItems.length === 0 ? <div className="profile-message">{tab === "REVISAR" ? "No tenés solicitudes para revisar." : tab === "ENVIADAS" ? "Todavía no enviaste solicitudes." : "Todavía no hay solicitudes resueltas."}</div> : visibleItems.map((item) => <SolicitudCard key={item.idSolicitud} item={item} canResolve={tab === "REVISAR"} rejecting={rejectingId === item.idSolicitud} onReject={setRejectingId} onCancelReject={() => setRejectingId(null)} onResolve={resolve} />)}</section>
  </main>;
}
export default Solicitudes;
