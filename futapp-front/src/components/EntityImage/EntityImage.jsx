import { assetUrl } from "../../config/api";
import { equipoGenerico, jugadorGenerico } from "../../utils/defaultEntityImages";

const defaults = { player: jugadorGenerico, team: equipoGenerico };

function EntityImage({ type, value, alt = "", ...props }) {
  const fallback = defaults[type];
  return <img {...props} src={value ? assetUrl(value) : fallback} alt={alt} onError={(event) => {
    event.currentTarget.onerror = null;
    event.currentTarget.src = fallback;
  }} />;
}

export default EntityImage;
