import { useCallback, useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import "./Profiles.css";

const formatDate = (value) => new Intl.DateTimeFormat("es-AR", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
const icons = { SOLICITUD: "👤", PARTIDO: "📅", RESULTADO: "⚽" };

function Notificaciones() {
  const { usuario } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const load = useCallback(() => apiFetch(`${API_URL}/notificaciones`).then(async (response) => {
    const data = await response.json().catch(() => []);
    if (!response.ok) throw new Error(data?.message || "No se pudieron cargar las notificaciones");
    setItems(data);
  }).finally(() => setLoading(false)), []);
  useEffect(() => { load(); }, [load]);
  if (!usuario) return <Navigate to="/login" replace />;
  const read = async (id) => {
    await apiFetch(`${API_URL}/notificaciones/${id}/leer`, { method: "PUT" });
    setItems((current) => current.map((item) => item.idNotificacion === id ? { ...item, leida: true } : item));
    window.dispatchEvent(new Event("notifications-updated"));
  };
  const readAll = async () => {
    await apiFetch(`${API_URL}/notificaciones/leer-todas`, { method: "PUT" });
    setItems((current) => current.map((item) => ({ ...item, leida: true })));
    window.dispatchEvent(new Event("notifications-updated"));
  };
  const unread = items.filter((item) => !item.leida).length;
  return <main className="profile-page notifications-page"><div className="notifications-heading"><div><span>Actividad</span><h1>Notificaciones</h1><p>{unread ? `Tenés ${unread} sin leer.` : "Estás al día."}</p></div>{unread > 0 && <button onClick={readAll}>Marcar todas como leídas</button>}</div><section className="notifications-list">{loading ? <div className="profile-message">Cargando…</div> : items.length === 0 ? <div className="profile-message">Todavía no tenés notificaciones.</div> : items.map((item) => <article className={item.leida ? "notification-item" : "notification-item unread"} key={item.idNotificacion}><div className="notification-icon">{icons[item.tipo] || "🔔"}</div><div><div className="notification-title"><strong>{item.titulo}</strong><time>{formatDate(item.fechaCreacion)}</time></div><p>{item.mensaje}</p>{item.url && <Link to={item.url} onClick={() => read(item.idNotificacion)}>Ver detalle →</Link>}</div>{!item.leida && <button aria-label="Marcar como leída" title="Marcar como leída" onClick={() => read(item.idNotificacion)}>✓</button>}</article>)}</section></main>;
}
export default Notificaciones;
