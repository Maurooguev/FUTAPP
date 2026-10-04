import { useEffect, useMemo, useState } from "react";

const MAX_PROCESS_SIZE = 1400;
const COLOR_TOLERANCE = 54;
const FEATHER_RANGE = 28;

function colorDistance(data, offset, background) {
  const red = data[offset] - background[0];
  const green = data[offset + 1] - background[1];
  const blue = data[offset + 2] - background[2];
  return Math.sqrt(red * red + green * green + blue * blue);
}

function loadImage(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const image = new Image();
    image.onload = () => resolve({ image, url });
    image.onerror = () => { URL.revokeObjectURL(url); reject(new Error("No pudimos leer la imagen")); };
    image.src = url;
  });
}

function canvasBlob(canvas) {
  return new Promise((resolve, reject) => canvas.toBlob(
    (blob) => blob ? resolve(blob) : reject(new Error("No pudimos preparar el escudo")),
    "image/png",
  ));
}

async function removeEdgeBackground(file) {
  const { image, url } = await loadImage(file);
  try {
    const scale = Math.min(1, MAX_PROCESS_SIZE / Math.max(image.naturalWidth, image.naturalHeight));
    const width = Math.max(1, Math.round(image.naturalWidth * scale));
    const height = Math.max(1, Math.round(image.naturalHeight * scale));
    const canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    const context = canvas.getContext("2d", { willReadFrequently: true });
    context.drawImage(image, 0, 0, width, height);
    const imageData = context.getImageData(0, 0, width, height);
    const { data } = imageData;

    let red = 0; let green = 0; let blue = 0; let samples = 0;
    const sampleSize = Math.max(2, Math.min(12, Math.floor(Math.min(width, height) * 0.04)));
    const corners = [[0, 0], [width - sampleSize, 0], [0, height - sampleSize], [width - sampleSize, height - sampleSize]];
    corners.forEach(([startX, startY]) => {
      for (let y = startY; y < startY + sampleSize; y += 1) {
        for (let x = startX; x < startX + sampleSize; x += 1) {
          const offset = (y * width + x) * 4;
          if (data[offset + 3] < 20) continue;
          red += data[offset]; green += data[offset + 1]; blue += data[offset + 2]; samples += 1;
        }
      }
    });

    if (!samples) return { blob: file, alreadyTransparent: true };
    const background = [red / samples, green / samples, blue / samples];
    const visited = new Uint8Array(width * height);
    const queue = new Int32Array(width * height);
    let head = 0; let tail = 0;
    const enqueue = (index) => {
      if (visited[index]) return;
      const offset = index * 4;
      const distance = colorDistance(data, offset, background);
      if (data[offset + 3] > 20 && distance > COLOR_TOLERANCE + FEATHER_RANGE) return;
      visited[index] = 1;
      queue[tail] = index;
      tail += 1;
    };

    for (let x = 0; x < width; x += 1) { enqueue(x); enqueue((height - 1) * width + x); }
    for (let y = 1; y < height - 1; y += 1) { enqueue(y * width); enqueue(y * width + width - 1); }

    while (head < tail) {
      const index = queue[head]; head += 1;
      const x = index % width; const y = Math.floor(index / width);
      if (x > 0) enqueue(index - 1);
      if (x + 1 < width) enqueue(index + 1);
      if (y > 0) enqueue(index - width);
      if (y + 1 < height) enqueue(index + width);
    }

    let removed = 0;
    for (let index = 0; index < visited.length; index += 1) {
      if (!visited[index]) continue;
      const offset = index * 4;
      const distance = colorDistance(data, offset, background);
      const alphaFactor = Math.max(0, Math.min(1, (distance - COLOR_TOLERANCE) / FEATHER_RANGE));
      const nextAlpha = Math.round(data[offset + 3] * alphaFactor);
      if (nextAlpha < data[offset + 3]) removed += 1;
      data[offset + 3] = nextAlpha;
    }

    if (removed < width * height * 0.005) return { blob: file, alreadyTransparent: true };
    context.putImageData(imageData, 0, 0);
    return { blob: await canvasBlob(canvas), alreadyTransparent: false };
  } finally {
    URL.revokeObjectURL(url);
  }
}

function BackgroundRemovalModal({ file, onCancel, onConfirm }) {
  const [processed, setProcessed] = useState(null);
  const [mode, setMode] = useState("transparent");
  const [error, setError] = useState("");
  const [processing, setProcessing] = useState(true);
  const originalUrl = useMemo(() => URL.createObjectURL(file), [file]);
  const processedUrl = useMemo(() => processed?.blob ? URL.createObjectURL(processed.blob) : "", [processed]);

  useEffect(() => () => URL.revokeObjectURL(originalUrl), [originalUrl]);
  useEffect(() => () => { if (processedUrl) URL.revokeObjectURL(processedUrl); }, [processedUrl]);
  useEffect(() => {
    let active = true;
    removeEdgeBackground(file)
      .then((result) => { if (active) { setProcessed(result); if (result.alreadyTransparent) setMode("original"); } })
      .catch((processError) => { if (active) { setError(processError.message); setMode("original"); } })
      .finally(() => { if (active) setProcessing(false); });
    return () => { active = false; };
  }, [file]);

  const confirm = () => {
    if (mode === "original" || !processed?.blob || processed.alreadyTransparent) return onConfirm(file);
    const png = new File([processed.blob], `${file.name.replace(/\.[^.]+$/, "") || "escudo"}-sin-fondo.png`, { type: "image/png" });
    return onConfirm(png);
  };

  return <div className="crop-backdrop" onMouseDown={onCancel}>
    <section className="background-removal-modal" role="dialog" aria-modal="true" aria-labelledby="background-removal-title" onMouseDown={(event) => event.stopPropagation()}>
      <h2 id="background-removal-title">Preparar escudo</h2>
      <p>Elegí cómo querés guardar la imagen. Podés comparar ambas opciones antes de subirla.</p>
      <div className="background-removal-options">
        <button type="button" className={mode === "transparent" ? "selected" : ""} onClick={() => setMode("transparent")} disabled={processing || processed?.alreadyTransparent || Boolean(error)}>
          <span className="background-removal-preview">{processedUrl && !processed?.alreadyTransparent ? <img src={processedUrl} alt="Escudo sin fondo" /> : <em>{processing ? "Procesando…" : "Ya es transparente"}</em>}</span>
          <strong>Quitar fondo</strong><small>Recomendado</small>
        </button>
        <button type="button" className={mode === "original" ? "selected" : ""} onClick={() => setMode("original")}>
          <span className="background-removal-preview"><img src={originalUrl} alt="Escudo original" /></span>
          <strong>Mantener original</strong><small>Sin modificaciones</small>
        </button>
      </div>
      {processed?.alreadyTransparent && <p className="background-removal-notice">La imagen ya tiene transparencia o no encontramos un fondo uniforme para quitar.</p>}
      {error && <p className="image-upload-error">{error}. Podés conservar la imagen original.</p>}
      <div className="crop-actions"><button type="button" onClick={onCancel}>Cancelar</button><button type="button" className="primary" onClick={confirm} disabled={processing}>Usar esta versión</button></div>
    </section>
  </div>;
}

export default BackgroundRemovalModal;
