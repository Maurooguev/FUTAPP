import { useState } from "react";
import { API_URL, apiFetch, assetUrl } from "../../config/api";
import "./ImageUploadField.css";
import PlantelCropModal from "./PlantelCropModal";
import BackgroundRemovalModal from "./BackgroundRemovalModal";

function ImageUploadField({ label, value, onUploaded, shape = "wide", allowBackgroundRemoval = false }) {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [cropFile, setCropFile] = useState(null);
  const [backgroundFile, setBackgroundFile] = useState(null);

  const uploadFile = async (file) => {
    if (!file) return;
    setUploading(true);
    setError("");
    try {
      const data = new FormData();
      data.append("archivo", file);
      const response = await apiFetch(`${API_URL}/archivos/imagenes`, { method: "POST", body: data });
      const body = await response.json().catch(() => null);
      if (!response.ok) throw new Error(body?.message || "No se pudo subir la imagen");
      onUploaded(body.url);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setUploading(false);
    }
  };

  const selectFile = (event) => {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;
    if (allowBackgroundRemoval) setBackgroundFile(file);
    else if (shape === "wide" || shape === "avatar") setCropFile(file);
    else uploadFile(file);
  };

  return <div className="image-upload-field">
    <span>{label}</span>
    <div className={`image-upload-preview ${shape}`}>{value ? <img src={assetUrl(value)} alt="Vista previa" /> : <small>Sin imagen</small>}</div>
    <label className="image-upload-button">{uploading ? "Subiendo..." : value ? "Cambiar imagen" : "Elegir imagen"}<input type="file" accept="image/jpeg,image/png,image/webp" onChange={selectFile} disabled={uploading} /></label>
    {value && <button type="button" className="image-upload-remove" onClick={() => onUploaded("")}>Quitar</button>}
    {error && <small className="image-upload-error">{error}</small>}
    {cropFile && <PlantelCropModal file={cropFile} variant={shape === "avatar" ? "avatar" : "wide"} onCancel={() => setCropFile(null)} onConfirm={async (cropped) => { await uploadFile(cropped); setCropFile(null); }} />}
    {backgroundFile && <BackgroundRemovalModal file={backgroundFile} onCancel={() => setBackgroundFile(null)} onConfirm={async (prepared) => { await uploadFile(prepared); setBackgroundFile(null); }} />}
  </div>;
}

export default ImageUploadField;
