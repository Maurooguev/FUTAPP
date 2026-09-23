import { useEffect } from "react";
import "./ImageLightbox.css";

function ImageLightbox({ src, alt, onClose }) {
  useEffect(() => {
    const closeWithEscape = (event) => { if (event.key === "Escape") onClose(); };
    document.addEventListener("keydown", closeWithEscape);
    return () => document.removeEventListener("keydown", closeWithEscape);
  }, [onClose]);

  return (
    <div className="image-lightbox" role="dialog" aria-modal="true" aria-label={alt} onClick={onClose}>
      <button type="button" onClick={onClose} aria-label="Cerrar imagen">×</button>
      <img src={src} alt={alt} onClick={(event) => event.stopPropagation()} />
    </div>
  );
}

export default ImageLightbox;
