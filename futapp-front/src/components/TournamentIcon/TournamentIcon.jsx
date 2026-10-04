import torneoClaro from "../../assets/torneo-generico-azul.png";
import torneoOscuro from "../../assets/torneo-generico.png";
import { assetUrl } from "../../config/api";
import "./TournamentIcon.css";

function TournamentIcon({ className = "", value, ...props }) {
  return <span {...props} className={`tournament-icon ${className}`.trim()}>
    {value ? <img className="tournament-icon-custom" src={assetUrl(value)} alt="" /> : <>
      <img className="tournament-icon-light" src={torneoClaro} alt="" />
      <img className="tournament-icon-dark" src={torneoOscuro} alt="" />
    </>}
  </span>;
}

export default TournamentIcon;
