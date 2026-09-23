import { assetUrl } from "../../config/api";
import { equipoGenerico, jugadorGenerico } from "../../utils/defaultEntityImages";
import "./EntityImage.css";

const defaults = { player: jugadorGenerico, team: equipoGenerico };

function EntityImage({ type, value, alt = "", className = "", ...props }) {
  const fallback = defaults[type];
  return <img {...props} className={`${className} ${value ? "" : "entity-image-default"}`.trim()} src={value ? assetUrl(value) : fallback} alt={alt} onError={(event) => {
    event.currentTarget.onerror = null;
    event.currentTarget.src = fallback;
    event.currentTarget.classList.add("entity-image-default");
  }} />;
}

export default EntityImage;
