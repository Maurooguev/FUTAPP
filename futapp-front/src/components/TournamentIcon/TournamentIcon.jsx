import torneoClaro from "../../assets/torneo-generico-azul.png";
import torneoOscuro from "../../assets/torneo-generico.png";
import "./TournamentIcon.css";

function TournamentIcon({ className = "", ...props }) {
  return <span {...props} className={`tournament-icon ${className}`.trim()}>
    <img className="tournament-icon-light" src={torneoClaro} alt="" />
    <img className="tournament-icon-dark" src={torneoOscuro} alt="" />
  </span>;
}

export default TournamentIcon;
