import { useCallback, useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import useAuth from "../../context/useAuth";
import { API_URL, apiFetch, assetUrl } from "../../config/api";
import { tieneRol } from "../../utils/roles";
import "./Header.css";

const searchGroups = [
  { key: "torneos", label: "Torneos", type: "torneo", icon: "🏆" },
  { key: "equipos", label: "Equipos", type: "equipo", icon: "🛡️" },
  { key: "jugadores", label: "Jugadores", type: "jugador", icon: "⚽" },
];

function HeaderSearch() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState({ torneos: [], equipos: [], jugadores: [] });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    setOpen(false);
    setQuery("");
  }, [location.pathname]);

  useEffect(() => {
    const term = query.trim();
    if (term.length < 2) {
      setResults({ torneos: [], equipos: [], jugadores: [] });
      setLoading(false);
      setError("");
      return undefined;
    }

    let active = true;
    setLoading(true);
    setOpen(true);
    const timer = window.setTimeout(async () => {
      try {
        const response = await apiFetch(`${API_URL}/busqueda?q=${encodeURIComponent(term)}`);
        if (!response.ok) throw new Error("No se pudo buscar");
        const data = await response.json();
        if (active) {
          setResults(data);
          setError("");
        }
      } catch {
        if (active) setError("No pudimos completar la búsqueda.");
      } finally {
        if (active) setLoading(false);
      }
    }, 250);

    return () => {
      active = false;
      window.clearTimeout(timer);
    };
  }, [query]);

  const total = searchGroups.reduce((sum, group) => sum + (results[group.key]?.length || 0), 0);
  const goToResult = (type, id) => {
    setOpen(false);
    setQuery("");
    navigate(`/${type}/${id}`);
  };

  return (
    <div className="header-search" onKeyDown={(event) => { if (event.key === "Escape") setOpen(false); }}>
      <span aria-hidden="true">⌕</span>
      <input
        type="search"
        value={query}
        placeholder="Buscar torneos, equipos o jugadores"
        aria-label="Buscar torneos, equipos o jugadores"
        onFocus={() => { if (query.trim().length >= 2) setOpen(true); }}
        onChange={(event) => setQuery(event.target.value)}
      />
      {query && <button type="button" className="header-search-clear" aria-label="Limpiar búsqueda" onClick={() => setQuery("")}>×</button>}

      {open && query.trim().length >= 2 && (
        <div className="header-search-results" aria-live="polite">
          {loading && <p>Buscando...</p>}
          {!loading && error && <p className="error">{error}</p>}
          {!loading && !error && total === 0 && <p>No encontramos resultados.</p>}
          {!loading && !error && searchGroups.map((group) => {
            const items = results[group.key] || [];
            if (items.length === 0) return null;
            return (
              <section key={group.key}>
                <h2>{group.label}</h2>
                {items.map((item) => (
                  <button type="button" key={`${group.type}-${item.id}`} onClick={() => goToResult(group.type, item.id)}>
                    <span className="header-result-avatar">{item.imagen ? <img src={assetUrl(item.imagen)} alt="" /> : group.icon}</span>
                    <span><strong>{item.nombre}</strong><small>{item.subtitulo || item.codigoPublico}</small></span>
                    <b>→</b>
                  </button>
                ))}
              </section>
            );
          })}
        </div>
      )}
    </div>
  );
}

function Header() {
  const { usuario, logout } = useAuth();
  const [theme, setTheme] = useState(() => window.localStorage.getItem("futapp-theme") === "dark" ? "dark" : "light");
  const [pendingRequests, setPendingRequests] = useState(0);
  const [unreadNotifications, setUnreadNotifications] = useState(0);
  const canReview = tieneRol(usuario, "ADMIN") || tieneRol(usuario, "ORGANIZADOR") || tieneRol(usuario, "DIRECTOR_EQUIPO");
  const loadPendingRequests = useCallback(() => {
    if (!canReview) return Promise.resolve();
    return apiFetch(`${API_URL}/reclamos/pendientes/count`).then((response) => response.ok ? response.json() : { cantidad: 0 }).then((data) => setPendingRequests(data.cantidad || 0)).catch(() => setPendingRequests(0));
  }, [canReview]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem("futapp-theme", theme);
  }, [theme]);

  useEffect(() => {
    loadPendingRequests();
    window.addEventListener("requests-updated", loadPendingRequests);
    return () => window.removeEventListener("requests-updated", loadPendingRequests);
  }, [loadPendingRequests]);

  const loadNotifications = useCallback(() => {
    if (!usuario) return Promise.resolve();
    return apiFetch(`${API_URL}/notificaciones/no-leidas/count`).then((response) => response.ok ? response.json() : { cantidad: 0 }).then((data) => setUnreadNotifications(data.cantidad || 0)).catch(() => setUnreadNotifications(0));
  }, [usuario]);

  useEffect(() => {
    loadNotifications();
    const interval = window.setInterval(loadNotifications, 30000);
    window.addEventListener("notifications-updated", loadNotifications);
    return () => { window.clearInterval(interval); window.removeEventListener("notifications-updated", loadNotifications); };
  }, [loadNotifications]);

  return (
    <header className="main-header">
      <div className="header-inner">
        <Link to="/" className="header-logo">
          <div className="header-logo-badge">⚽</div>
          <span>FUTAPP</span>
        </Link>

        <HeaderSearch />

        <nav className="header-nav">
          <Link to="/">Explorar</Link>

          {usuario && (
            <Link to="/mis-torneos">Mis torneos</Link>
          )}
          {(tieneRol(usuario, "ADMIN") || tieneRol(usuario, "DIRECTOR_EQUIPO")) && <Link to="/mis-equipos">Mis equipos</Link>}
          {usuario && <Link className="requests-nav-link" to="/solicitudes">Solicitudes{pendingRequests > 0 && <span>{pendingRequests > 99 ? "99+" : pendingRequests}</span>}</Link>}
          {usuario && <Link to="/favoritos">Favoritos</Link>}
          {(tieneRol(usuario, "JUGADOR") || tieneRol(usuario, "ORGANIZADOR")) && <Link to={usuario?.rol === "ORGANIZADOR" || !tieneRol(usuario, "JUGADOR") ? "/mi-perfil-organizador" : "/mi-perfil-jugador"}>Mi perfil</Link>}
        </nav>

        <div className="header-actions">
          <button
            type="button"
            className="theme-toggle"
            onClick={() => setTheme((current) => current === "dark" ? "light" : "dark")}
            aria-label={theme === "dark" ? "Activar modo claro" : "Activar modo oscuro"}
            title={theme === "dark" ? "Modo claro" : "Modo oscuro"}
          >
            <span aria-hidden="true">{theme === "dark" ? "☀" : "☾"}</span>
          </button>
          {usuario ? (
            <><Link to="/notificaciones" className="notification-bell" aria-label="Notificaciones" title="Notificaciones">🔔{unreadNotifications > 0 && <span>{unreadNotifications > 99 ? "99+" : unreadNotifications}</span>}</Link><Link to="/cuenta" className="user-pill">{usuario.nombre} · {usuario.rol}</Link><button className="header-btn-outline" onClick={logout}>Salir</button></>
          ) : (
            <><Link to="/login" className="header-link-button outline">Ingresar</Link><Link to="/register" className="header-link-button primary">Registrarse</Link></>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
