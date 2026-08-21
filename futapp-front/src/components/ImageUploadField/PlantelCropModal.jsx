import { useEffect, useMemo, useRef, useState } from "react";

const VIEWPORT_WIDTH = 600;
const VIEWPORT_HEIGHT = 200;

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

function PlantelCropModal({ file, onCancel, onConfirm }) {
  const objectUrl = useMemo(() => URL.createObjectURL(file), [file]);
  const [imageSize, setImageSize] = useState(null);
  const [zoom, setZoom] = useState(1);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [saving, setSaving] = useState(false);
  const drag = useRef(null);
  const imageRef = useRef(null);
  const canvasRef = useRef(null);

  const dimensions = imageSize ? (() => {
    const baseScale = Math.max(VIEWPORT_WIDTH / imageSize.width, VIEWPORT_HEIGHT / imageSize.height);
    const scale = baseScale * zoom;
    return { scale, width: imageSize.width * scale, height: imageSize.height * scale };
  })() : null;

  const constrain = (next, currentDimensions = dimensions) => {
    if (!currentDimensions) return next;
    const maxX = Math.max(0, (currentDimensions.width - VIEWPORT_WIDTH) / 2);
    const maxY = Math.max(0, (currentDimensions.height - VIEWPORT_HEIGHT) / 2);
    return { x: clamp(next.x, -maxX, maxX), y: clamp(next.y, -maxY, maxY) };
  };

  const changeZoom = (event) => {
    const nextZoom = Number(event.target.value);
    const baseScale = Math.max(VIEWPORT_WIDTH / imageSize.width, VIEWPORT_HEIGHT / imageSize.height);
    const nextDimensions = { width: imageSize.width * baseScale * nextZoom, height: imageSize.height * baseScale * nextZoom };
    setZoom(nextZoom);
    setPosition((current) => constrain(current, nextDimensions));
  };

  const pointerDown = (event) => {
    event.currentTarget.setPointerCapture(event.pointerId);
    drag.current = { clientX: event.clientX, clientY: event.clientY, position };
  };

  const pointerMove = (event) => {
    if (!drag.current) return;
    const box = event.currentTarget.getBoundingClientRect();
    const factor = VIEWPORT_WIDTH / box.width;
    setPosition(constrain({
      x: drag.current.position.x + (event.clientX - drag.current.clientX) * factor,
      y: drag.current.position.y + (event.clientY - drag.current.clientY) * factor,
    }));
  };

  useEffect(() => {
    const image = imageRef.current;
    const canvas = canvasRef.current;
    if (!image || !canvas || !dimensions) return;
    const left = (VIEWPORT_WIDTH - dimensions.width) / 2 + position.x;
    const top = (VIEWPORT_HEIGHT - dimensions.height) / 2 + position.y;
    const context = canvas.getContext("2d");
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.drawImage(image, -left / dimensions.scale, -top / dimensions.scale,
      VIEWPORT_WIDTH / dimensions.scale, VIEWPORT_HEIGHT / dimensions.scale,
      0, 0, canvas.width, canvas.height);
  }, [dimensions, position]);

  const crop = async () => {
    if (!imageSize || !dimensions) return;
    setSaving(true);
    const canvas = canvasRef.current;
    const blob = await new Promise((resolve) => canvas.toBlob(resolve, "image/jpeg", 0.9));
    URL.revokeObjectURL(objectUrl);
    await onConfirm(new File([blob], "foto-plantel.jpg", { type: "image/jpeg" }));
    setSaving(false);
  };

  const cancel = () => {
    URL.revokeObjectURL(objectUrl);
    onCancel();
  };

  return <div className="crop-backdrop" onMouseDown={cancel}><section className="crop-modal" onMouseDown={(event) => event.stopPropagation()}>
    <h2>Encuadrar foto del plantel</h2><p>Arrastrá la foto y usá el control para acercar o alejar.</p>
    <div className="crop-viewport" onPointerDown={pointerDown} onPointerMove={pointerMove} onPointerUp={() => { drag.current = null; }} onPointerCancel={() => { drag.current = null; }}>
      <img ref={imageRef} className="crop-source" src={objectUrl} alt="" onLoad={(event) => setImageSize({ width: event.currentTarget.naturalWidth, height: event.currentTarget.naturalHeight })} />
      <canvas ref={canvasRef} width="1200" height="400" aria-label="Vista previa del encuadre" />
    </div>
    <label className="crop-zoom">Zoom<input type="range" min="1" max="3" step="0.05" value={zoom} onChange={changeZoom} disabled={!imageSize} /></label>
    <div className="crop-actions"><button type="button" onClick={cancel}>Cancelar</button><button type="button" className="primary" onClick={crop} disabled={!imageSize || saving}>{saving ? "Guardando..." : "Usar este encuadre"}</button></div>
  </section></div>;
}

export default PlantelCropModal;
