import { Link } from "react-router-dom";
import useAuth from "../../context/useAuth";
import "./Header.css";

function Header() {
  const { usuario, logout } = useAuth();

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

          {(usuario?.rol === "ADMIN" || usuario?.rol === "ORGANIZADOR") && (
            <Link to="/mis-torneos">Mis torneos</Link>
          )}
          {(usuario?.rol === "ADMIN" || usuario?.rol === "ORGANIZADOR" || usuario?.rol === "DIRECTOR_EQUIPO") && <Link to="/solicitudes">Solicitudes</Link>}
          {usuario?.rol === "JUGADOR" && <Link to="/mi-perfil-jugador">Mi perfil</Link>}
        </nav>

        <div className="header-actions">
          {usuario ? (
            <><span className="user-pill">{usuario.nombre} · {usuario.rol}</span><button className="header-btn-outline" onClick={logout}>Salir</button></>
          ) : (
            <><Link to="/login" className="header-link-button outline">Ingresar</Link><Link to="/register" className="header-link-button primary">Registrarse</Link></>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
