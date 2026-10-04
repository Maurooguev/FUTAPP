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
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
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
    if (!mobileMenuOpen) return undefined;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const closeOnEscape = (event) => {
      if (event.key === "Escape") setMobileMenuOpen(false);
    };
    window.addEventListener("keydown", closeOnEscape);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", closeOnEscape);
    };
  }, [mobileMenuOpen]);

  useEffect(() => {
    loadPendingRequests();
    window.addEventListener("requests-updated", loadPendingRequests);
    return () => window.removeEventListener("requests-updated", loadPendingRequests);
  }, [loadPendingRequests]);

  const loadNotifications = useCallback(() => {
    if (!usuario) return Promise.resolve();
    return apiFetch(`${API_URL}/notificaciones/no-leidas/count`).then((response) => response.ok ? response.json() : { cantidad: 0 }).then((data) => setUnreadNotifications(data.cantidad || 0)).catch(() => setUnreadNotifications(0));
  }, [usuario]);

  const registrationLink = (next, intent) => `/register?next=${encodeURIComponent(next)}&intent=${encodeURIComponent(intent)}`;

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
          <img className="header-logo-badge" src="/futapp-logo.png?v=3" alt="" />
          <span>FUTAPP</span>
        </Link>

        <HeaderSearch />

        <nav className={`header-nav ${usuario ? "" : "guest"}`}>
          <Link to="/">Explorar</Link>

          {usuario && (tieneRol(usuario, "ORGANIZADOR") || tieneRol(usuario, "ADMIN"))
            ? <Link to="/mis-torneos">Mis torneos</Link>
            : <Link className="guest-nav-link" to={usuario ? "/?crearTorneo=1" : registrationLink("/?crearTorneo=1", "crear-torneo")}>Crear torneo</Link>}
          {(tieneRol(usuario, "ADMIN") || tieneRol(usuario, "DIRECTOR_EQUIPO")) && <Link to="/mis-equipos">Mis equipos</Link>}
          {usuario && <Link className="requests-nav-link" to="/solicitudes">Solicitudes{pendingRequests > 0 && <span>{pendingRequests > 99 ? "99+" : pendingRequests}</span>}</Link>}
          {usuario ? <Link to="/favoritos">Favoritos</Link> : <Link className="guest-nav-link" to={registrationLink("/favoritos", "guardar-favoritos")}>Favoritos</Link>}
          {(tieneRol(usuario, "JUGADOR") || tieneRol(usuario, "ORGANIZADOR")) && <Link to={usuario?.rol === "ORGANIZADOR" || !tieneRol(usuario, "JUGADOR") ? "/mi-perfil-organizador" : "/mi-perfil-jugador"}>Mi perfil</Link>}
          {!usuario && <Link className="guest-nav-link" to={registrationLink("/mi-perfil-jugador", "crear-perfil")}>Crear perfil</Link>}
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
          <button
            type="button"
            className="mobile-menu-toggle"
            aria-label={mobileMenuOpen ? "Cerrar menú" : "Abrir menú"}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-navigation"
            onClick={() => setMobileMenuOpen((current) => !current)}
          >
            <span aria-hidden="true">{mobileMenuOpen ? "×" : "☰"}</span>
          </button>
        </div>
      </div>

      {mobileMenuOpen && (
        <>
          <button type="button" className="mobile-menu-scrim" aria-label="Cerrar menú" onClick={() => setMobileMenuOpen(false)} />
          <aside id="mobile-navigation" className="mobile-menu" aria-label="Navegación principal">
            <div className="mobile-menu-head">
              <div>
                <strong>{usuario ? usuario.nombre : "FUTAPP"}</strong>
                <small>{usuario ? usuario.rol : "Fútbol de tu zona"}</small>
              </div>
              <button type="button" aria-label="Cerrar menú" onClick={() => setMobileMenuOpen(false)}>×</button>
            </div>
            <nav onClick={() => setMobileMenuOpen(false)}>
              <Link to="/"><span>⌂</span>Explorar</Link>
              {usuario && (tieneRol(usuario, "ORGANIZADOR") || tieneRol(usuario, "ADMIN"))
                ? <Link to="/mis-torneos"><span>🏆</span>Mis torneos</Link>
                : <Link to={usuario ? "/?crearTorneo=1" : registrationLink("/?crearTorneo=1", "crear-torneo")}><span>＋</span>Crear torneo</Link>}
              {(tieneRol(usuario, "ADMIN") || tieneRol(usuario, "DIRECTOR_EQUIPO")) && <Link to="/mis-equipos"><span>🛡</span>Mis equipos</Link>}
              {usuario && <Link to="/solicitudes"><span>↔</span>Solicitudes{pendingRequests > 0 && <b>{pendingRequests > 99 ? "99+" : pendingRequests}</b>}</Link>}
              <Link to={usuario ? "/favoritos" : registrationLink("/favoritos", "guardar-favoritos")}><span>☆</span>Favoritos</Link>
              {(tieneRol(usuario, "JUGADOR") || tieneRol(usuario, "ORGANIZADOR")) && <Link to={usuario?.rol === "ORGANIZADOR" || !tieneRol(usuario, "JUGADOR") ? "/mi-perfil-organizador" : "/mi-perfil-jugador"}><span>●</span>Mi perfil</Link>}
              {!usuario && <Link to={registrationLink("/mi-perfil-jugador", "crear-perfil")}><span>●</span>Crear perfil</Link>}
              {usuario && <Link to="/notificaciones"><span>🔔</span>Notificaciones{unreadNotifications > 0 && <b>{unreadNotifications > 99 ? "99+" : unreadNotifications}</b>}</Link>}
              {usuario && <Link to="/cuenta"><span>⚙</span>Mi cuenta</Link>}
            </nav>
            <div className="mobile-menu-session">
              {usuario
                ? <button type="button" onClick={() => { setMobileMenuOpen(false); logout(); }}>Cerrar sesión</button>
                : <><Link to="/login">Ingresar</Link><Link className="primary" to="/register">Crear cuenta</Link></>}
            </div>
          </aside>
        </>
      )}
    </header>
  );
}

export default Header;
