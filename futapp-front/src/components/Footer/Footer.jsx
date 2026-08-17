import { Link } from "react-router-dom";
import "./Footer.css";

function Footer() {
  return (
    <footer className="main-footer">
      <div>
        <h3>FUTAPP</h3>
        <p>Torneos. Equipos. Pasión.</p>
      </div>

      <div className="footer-nav">
        <Link to="/">Inicio</Link>
        <a href="#destacados">Destacados</a>
        <a href="#beneficios">Beneficios</a>
      </div>

      <p className="footer-copy">© 2026 FUTAPP. Todos los derechos reservados.</p>
    </footer>
  );
}

export default Footer;