import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../config/api";
import "./Profiles.css";

function Solicitudes() {
  const [items, setItems] = useState([]);
  const [message, setMessage] = useState("");
  const load = () => apiFetch(`${API_URL}/reclamos/pendientes`).then((r) => r.json()).then((data) => setItems(Array.isArray(data) ? data : []));
  useEffect(load, []);
  const resolve = async (id, approve) => {
    const response = await apiFetch(`${API_URL}/reclamos/${id}/${approve ? "aprobar" : "rechazar"}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: "{}" });
    const body = await response.json().catch(() => null);
    setMessage(response.ok ? (approve ? "Solicitud aprobada" : "Solicitud rechazada") : body?.message || "No se pudo resolver");
    if (response.ok) load();
  };
  return <main className="profile-page"><h1>Solicitudes pendientes</h1>{message && <p className="profile-action-message">{message}</p>}<section className="profile-panel"><div className="career-list">{items.length === 0 ? <p>No tenés solicitudes pendientes.</p> : items.map((item) => <article key={item.idSolicitud}><div><strong>{item.tipo === "EQUIPO" ? item.nombreEquipo : item.nombreJugador}</strong><small>{item.nombreSolicitante} · {item.nombreTorneo}</small></div><button onClick={() => resolve(item.idSolicitud, true)}>Aprobar</button><button onClick={() => resolve(item.idSolicitud, false)}>Rechazar</button></article>)}</div></section></main>;
}
export default Solicitudes;
