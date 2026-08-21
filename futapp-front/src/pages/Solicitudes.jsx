import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { API_URL, apiFetch, assetUrl } from "../config/api";
import "./Profiles.css";

const formatDate = (value) => value
  ? new Intl.DateTimeFormat("es-AR", { dateStyle: "short", timeStyle: "short" }).format(new Date(value))
  : "—";

function SolicitudCard({ item, onResolve, rejecting, onReject, onCancelReject }) {
  const [reason, setReason] = useState("");
  const targetUrl = item.tipo === "EQUIPO" ? `/equipo/${item.idEquipo}` : `/jugador/${item.idJugador}`;
  const targetName = item.tipo === "EQUIPO" ? item.nombreEquipo : item.nombreJugador;

  return <article className="request-card">
    <div className="request-avatar">{item.fotoSolicitante ? <img src={assetUrl(item.fotoSolicitante)} alt="" /> : item.nombreSolicitante?.slice(0, 2).toUpperCase()}</div>
    <div className="request-content">
      <div className="request-title"><div><span>{item.tipo === "EQUIPO" ? "Reclamo de equipo" : "Reclamo de jugador"}</span><h2>{item.nombreSolicitante}</h2></div><strong className={`request-status ${item.estado.toLowerCase()}`}>{item.estado}</strong></div>
      <div className="request-meta"><span>{item.rolSolicitante?.replaceAll("_", " ")}</span><a href={`mailto:${item.emailSolicitante}`}>{item.emailSolicitante}</a><span>{formatDate(item.fechaSolicitud)}</span></div>
      <p>Solicita administrar <Link to={targetUrl}>{targetName}</Link> en <Link to={`/torneo/${item.idTorneo}`}>{item.nombreTorneo}</Link>.</p>
      <p className="request-team">Equipo: <Link to={`/equipo/${item.idEquipo}`}>{item.nombreEquipo}</Link></p>
      {item.mensaje && <blockquote>“{item.mensaje}”</blockquote>}
      {item.estado !== "PENDIENTE" && <div className="request-resolution"><strong>{item.nombreRevisor || "Revisor"}</strong> · {formatDate(item.fechaResolucion)}{item.motivoResolucion && <p>Motivo: {item.motivoResolucion}</p>}</div>}
      {item.estado === "PENDIENTE" && !rejecting && <div className="request-actions"><button onClick={() => onResolve(item.idSolicitud, true)}>Aprobar</button><button className="danger" onClick={() => onReject(item.idSolicitud)}>Rechazar</button></div>}
      {rejecting && <div className="request-reject-form"><label>Motivo del rechazo<textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Explicá brevemente por qué se rechaza…" autoFocus /></label><div><button onClick={onCancelReject}>Cancelar</button><button className="danger" disabled={!reason.trim()} onClick={() => onResolve(item.idSolicitud, false, reason)}>Confirmar rechazo</button></div></div>}
    </div>
  </article>;
}

function Solicitudes() {
  const [items, setItems] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState("PENDIENTE");
  const [rejectingId, setRejectingId] = useState(null);
  const [resolving, setResolving] = useState(false);
  const load = useCallback(async () => {
    try {
      const response = await apiFetch(`${API_URL}/reclamos/revisables`);
      const data = await response.json().catch(() => null);
      if (!response.ok) throw new Error(data?.message || "No se pudieron cargar las solicitudes");
      setItems(Array.isArray(data) ? data : []);
    } catch (requestError) { setMessage(requestError.message); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { load(); }, [load]);
  const pendingCount = items.filter((item) => item.estado === "PENDIENTE").length;
  const visibleItems = useMemo(() => items.filter((item) => tab === "PENDIENTE" ? item.estado === "PENDIENTE" : item.estado !== "PENDIENTE"), [items, tab]);
  const resolve = async (id, approve, reason = "") => {
    if (resolving) return;
    setResolving(true);
    const response = await apiFetch(`${API_URL}/reclamos/${id}/${approve ? "aprobar" : "rechazar"}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ motivo: reason }) });
    const body = await response.json().catch(() => null);
    setMessage(response.ok ? (approve ? "Solicitud aprobada." : "Solicitud rechazada.") : body?.message || "No se pudo resolver");
    if (response.ok) { setRejectingId(null); await load(); window.dispatchEvent(new Event("requests-updated")); }
    setResolving(false);
  };
  return <main className="profile-page requests-page">
    <div className="requests-heading"><div><span>Gestión de accesos</span><h1>Solicitudes</h1><p>Revisá quién quiere administrar equipos o vincularse con un jugador.</p></div><div className="requests-count"><strong>{pendingCount}</strong><span>pendientes</span></div></div>
    {message && <p className="profile-action-message">{message}</p>}
    <div className="requests-tabs"><button className={tab === "PENDIENTE" ? "active" : ""} onClick={() => setTab("PENDIENTE")}>Pendientes <span>{pendingCount}</span></button><button className={tab === "HISTORIAL" ? "active" : ""} onClick={() => setTab("HISTORIAL")}>Historial <span>{items.length - pendingCount}</span></button></div>
    <section className="requests-list">{loading ? <div className="profile-message">Cargando solicitudes...</div> : visibleItems.length === 0 ? <div className="profile-message">{tab === "PENDIENTE" ? "No tenés solicitudes pendientes." : "Todavía no hay solicitudes resueltas."}</div> : visibleItems.map((item) => <SolicitudCard key={item.idSolicitud} item={item} rejecting={rejectingId === item.idSolicitud} onReject={setRejectingId} onCancelReject={() => setRejectingId(null)} onResolve={resolve} />)}</section>
  </main>;
}
export default Solicitudes;
