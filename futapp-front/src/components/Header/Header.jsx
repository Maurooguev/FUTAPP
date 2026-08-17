import { Link } from "react-router-dom";
import useAuth from "../../context/useAuth";
import "./Header.css";

function Header() {
  const { usuario, logout } = useAuth();

  return (
    <header className="main-header">
      <Link to="/" className="header-logo">
        <div className="header-logo-badge">⚽</div>
        <span>FUTAPP</span>
      </Link>

      <nav className="header-nav">
        <Link to="/">Inicio</Link>
        <a href="#destacados">Destacados</a>
        <a href="#beneficios">Beneficios</a>

        {(usuario?.rol === "ADMIN" || usuario?.rol === "ORGANIZADOR") && (
          <a href="#admin">Admin</a>
        )}
      </nav>

      <div className="header-actions">
        {usuario ? (
          <>
            <span className="user-pill">
              {usuario.nombre} · {usuario.rol}
            </span>
            <button className="header-btn-outline" onClick={logout}>
              Salir
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="header-link-button outline">
              Ingresar
            </Link>
            <Link to="/register" className="header-link-button primary">
              Registrarse
            </Link>
          </>
        )}
      </div>
    </header>
  );
}

export default Header;
