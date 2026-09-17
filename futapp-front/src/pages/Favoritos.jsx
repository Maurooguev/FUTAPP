import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { API_URL, apiFetch, assetUrl } from "../config/api";
import useAuth from "../context/useAuth";
import "./Favoritos.css";

const loadItems = async (ids, path) => Promise.all((ids || []).map(async (id) => {
  const response = await apiFetch(`${API_URL}/${path}/${id}`);
  return response.ok ? response.json() : null;
})).then((items) => items.filter(Boolean));

function Favoritos() {
  const { usuario } = useAuth();
  const [data, setData] = useState({ torneos: [], equipos: [], jugadores: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    apiFetch(`${API_URL}/favoritos`).then(async (response) => {
      if (!response.ok) throw new Error("No pudimos cargar tus favoritos");
      const ids = await response.json();
      const [torneos, equipos, jugadores] = await Promise.all([
        loadItems(ids.torneos, "torneos"), loadItems(ids.equipos, "equipos"), loadItems(ids.jugadores, "jugadores"),
      ]);
      setData({ torneos, equipos, jugadores });
    }).catch((requestError) => setError(requestError.message)).finally(() => setLoading(false));
  }, []);

  if (!usuario) return <Navigate to="/login" replace />;

  const remove = async (type, id, key, idField) => {
    const response = await apiFetch(`${API_URL}/favoritos/${type}/${id}`, { method: "DELETE" });
    if (response.ok) setData((current) => ({ ...current, [key]: current[key].filter((item) => item[idField] !== id) }));
  };
  const total = data.torneos.length + data.equipos.length + data.jugadores.length;

  return <main className="favorites-page">
    <header><span>Tu colección</span><h1>Mis favoritos</h1><p>Guardá los torneos, equipos y jugadores que querés seguir de cerca.</p></header>
    {loading ? <div className="favorites-message">Cargando favoritos…</div> : error ? <div className="favorites-message error">{error}</div> : total === 0 ? <div className="favorites-message"><h2>Todavía no guardaste favoritos</h2><p>Buscá la estrella en cualquier torneo, equipo o jugador.</p><Link to="/">Explorar torneos</Link></div> : <>
      <FavoriteSection title="Torneos" count={data.torneos.length}>{data.torneos.map((item) => <article className="favorite-card" key={item.idTorneo}><Link to={`/torneo/${item.idTorneo}`}><span className="favorite-card-icon">🏆</span><div><strong>{item.nombre}</strong><small>{item.ubicacion} · {item.formato}</small></div><b>Ver torneo →</b></Link><button onClick={() => remove("torneos", item.idTorneo, "torneos", "idTorneo")} aria-label={`Quitar ${item.nombre} de favoritos`}>★</button></article>)}</FavoriteSection>
      <FavoriteSection title="Equipos" count={data.equipos.length}>{data.equipos.map((item) => <article className="favorite-card" key={item.idEquipo}><Link to={`/equipo/${item.idEquipo}`}><span className="favorite-card-avatar">{item.escudo ? <img src={assetUrl(item.escudo)} alt="" /> : item.nombre.slice(0, 2)}</span><div><strong>{item.nombre}</strong><small>{item.barrio || "Sin barrio"}</small></div><b>Ver equipo →</b></Link><button onClick={() => remove("equipos", item.idEquipo, "equipos", "idEquipo")} aria-label={`Quitar ${item.nombre} de favoritos`}>★</button></article>)}</FavoriteSection>
      <FavoriteSection title="Jugadores" count={data.jugadores.length}>{data.jugadores.map((item) => <article className="favorite-card" key={item.idJugador}><Link to={`/jugador/${item.idJugador}`}><span className="favorite-card-avatar">{item.foto ? <img src={assetUrl(item.foto)} alt="" /> : item.nombre.slice(0, 1)}</span><div><strong>{item.nombre} {item.apellido}</strong><small>{item.posicion || "Sin posición"} · {item.nombreEquipo}</small></div><b>Ver jugador →</b></Link><button onClick={() => remove("jugadores", item.idJugador, "jugadores", "idJugador")} aria-label={`Quitar ${item.nombre} de favoritos`}>★</button></article>)}</FavoriteSection>
    </>}
  </main>;
}

function FavoriteSection({ title, count, children }) {
  if (count === 0) return null;
  return <section className="favorites-section"><div><h2>{title}</h2><span>{count}</span></div><div className="favorites-grid">{children}</div></section>;
}

export default Favoritos;
