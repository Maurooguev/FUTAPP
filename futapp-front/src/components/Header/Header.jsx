import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import useAuth from "../../context/useAuth";
import { API_URL, apiFetch } from "../../config/api";
import { tieneRol } from "../../utils/roles";
import "./Header.css";

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

        <nav className="header-nav">
          <Link to="/">Inicio</Link>
          <a href="#destacados">Destacados</a>
          <a href="#beneficios">Beneficios</a>

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
