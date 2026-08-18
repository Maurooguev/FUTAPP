import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import "./Profiles.css";

function Solicitudes() {
  const [items, setItems] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const load = async () => {
    try {
      const response = await apiFetch(`${API_URL}/reclamos/pendientes`);
      const data = await response.json().catch(() => null);
      if (!response.ok) throw new Error(data?.message || "No se pudieron cargar las solicitudes");
      setItems(Array.isArray(data) ? data : []);
    } catch (requestError) {
      setMessage(requestError.message);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { load(); }, []);
  const resolve = async (id, approve) => {
    const response = await apiFetch(`${API_URL}/reclamos/${id}/${approve ? "aprobar" : "rechazar"}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: "{}" });
    const body = await response.json().catch(() => null);
    setMessage(response.ok ? (approve ? "Solicitud aprobada" : "Solicitud rechazada") : body?.message || "No se pudo resolver");
    if (response.ok) load();
  };
  return <main className="profile-page"><h1>Solicitudes pendientes</h1>{message && <p className="profile-action-message">{message}</p>}<section className="profile-panel"><div className="career-list">{loading ? <p>Cargando solicitudes...</p> : items.length === 0 ? <p>No tenés solicitudes pendientes.</p> : items.map((item) => <article key={item.idSolicitud}><div><strong><Link to={item.tipo === "EQUIPO" ? `/equipo/${item.idEquipo}` : `/jugador/${item.idJugador}`}>{item.tipo === "EQUIPO" ? item.nombreEquipo : item.nombreJugador}</Link></strong><small>{item.nombreSolicitante} · {item.nombreTorneo}</small></div><button onClick={() => resolve(item.idSolicitud, true)}>Aprobar</button><button onClick={() => resolve(item.idSolicitud, false)}>Rechazar</button></article>)}</div></section></main>;
}
export default Solicitudes;
