import { useEffect, useState } from "react";

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

async function normalizeTransparentCanvas(source) {
  const context = source.getContext("2d", { willReadFrequently: true });
  const { data } = context.getImageData(0, 0, source.width, source.height);
  let minX = source.width; let minY = source.height; let maxX = -1; let maxY = -1;
  for (let y = 0; y < source.height; y += 1) {
    for (let x = 0; x < source.width; x += 1) {
      if (data[(y * source.width + x) * 4 + 3] <= 10) continue;
      minX = Math.min(minX, x); minY = Math.min(minY, y);
      maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
    }
  }
  if (maxX < minX || maxY < minY) throw new Error("La imagen quedó vacía al quitar el fondo");
  const contentWidth = maxX - minX + 1;
  const contentHeight = maxY - minY + 1;
  const outputSize = Math.min(MAX_PROCESS_SIZE, Math.max(512, contentWidth, contentHeight));
  const padding = Math.round(outputSize * 0.08);
  const available = outputSize - padding * 2;
  const scale = Math.min(available / contentWidth, available / contentHeight);
  const drawWidth = Math.round(contentWidth * scale);
  const drawHeight = Math.round(contentHeight * scale);
  const output = document.createElement("canvas");
  output.width = outputSize; output.height = outputSize;
  output.getContext("2d").drawImage(
    source,
    minX, minY, contentWidth, contentHeight,
    Math.round((outputSize - drawWidth) / 2), Math.round((outputSize - drawHeight) / 2), drawWidth, drawHeight,
  );
  return canvasBlob(output);
}

function usePreviewUrl(blob) {
  const [url, setUrl] = useState("");
  useEffect(() => {
    if (!blob) return undefined;
    const reader = new FileReader();
    reader.onload = () => setUrl(typeof reader.result === "string" ? reader.result : "");
    reader.readAsDataURL(blob);
    return () => reader.readyState === FileReader.LOADING && reader.abort();
  }, [blob]);
  return url;
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
    let transparentPixels = 0;
    for (let offset = 3; offset < data.length; offset += 4) {
      if (data[offset] < 245) transparentPixels += 1;
    }
    const alreadyTransparent = transparentPixels > width * height * 0.005;

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

    if (!samples) return { blob: await normalizeTransparentCanvas(canvas), alreadyTransparent: true, failed: false };
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

    if (removed < width * height * 0.005) return { blob: alreadyTransparent ? await normalizeTransparentCanvas(canvas) : file, alreadyTransparent, failed: !alreadyTransparent };
    context.putImageData(imageData, 0, 0);
    return { blob: await normalizeTransparentCanvas(canvas), alreadyTransparent: false, failed: false };
  } finally {
    URL.revokeObjectURL(url);
  }
}

function BackgroundRemovalModal({ file, onCancel, onConfirm }) {
  const [processed, setProcessed] = useState(null);
  const [error, setError] = useState("");
  const [processing, setProcessing] = useState(true);
  const originalUrl = usePreviewUrl(file);
  const processedUrl = usePreviewUrl(processed?.blob);
  useEffect(() => {
    let active = true;
    removeEdgeBackground(file)
      .then((result) => { if (active) setProcessed(result); })
      .catch((processError) => { if (active) setError(processError.message); })
      .finally(() => { if (active) setProcessing(false); });
    return () => { active = false; };
  }, [file]);

  const confirm = () => {
    if (!processed?.blob) return;
    const png = new File([processed.blob], `${file.name.replace(/\.[^.]+$/, "") || "escudo"}-sin-fondo.png`, { type: "image/png" });
    return onConfirm(png);
  };

  return <div className="crop-backdrop" onMouseDown={onCancel}>
    <section className="background-removal-modal" role="dialog" aria-modal="true" aria-labelledby="background-removal-title" onMouseDown={(event) => event.stopPropagation()}>
      <h2 id="background-removal-title">Preparar escudo</h2>
      <p>Todos los escudos de FUTAPP se guardan sin fondo para que se vean iguales en cualquier pantalla.</p>
      <div className="background-removal-options">
        <div className="background-removal-option">
          <span className="background-removal-preview"><img src={originalUrl} alt="Escudo original" /></span>
          <strong>Original</strong><small>Imagen seleccionada</small>
        </div>
        <div className="background-removal-option selected">
          <span className="background-removal-preview">{processedUrl ? <img src={processedUrl} alt="Escudo preparado" /> : <em>{processing ? "Quitando fondo…" : "No se pudo quitar"}</em>}</span>
          <strong>Resultado FUTAPP</strong><small>Fondo transparente obligatorio</small>
        </div>
      </div>
      {processed?.alreadyTransparent && <p className="background-removal-notice">La imagen ya era transparente. FUTAPP solo la centró y ajustó sus márgenes.</p>}
      {processed?.failed && <p className="image-upload-error">No pudimos separar el escudo del fondo. Quitale el fondo por tu cuenta y volvé a subirlo como PNG transparente.</p>}
      {error && <p className="image-upload-error">{error}. Probá con un PNG transparente.</p>}
      <div className="crop-actions"><button type="button" onClick={onCancel}>Cancelar</button><button type="button" className="primary" onClick={confirm} disabled={processing || processed?.failed || Boolean(error)}>Subir escudo</button></div>
    </section>
  </div>;
}

export default BackgroundRemovalModal;
