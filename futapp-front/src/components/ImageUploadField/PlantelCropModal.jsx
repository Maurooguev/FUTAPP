import { useEffect, useMemo, useRef, useState } from "react";

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

function PlantelCropModal({ file, variant = "wide", onCancel, onConfirm }) {
  const avatar = variant === "avatar";
  const viewportWidth = avatar ? 400 : 600;
  const viewportHeight = avatar ? 400 : 200;
  const outputWidth = avatar ? 800 : 1200;
  const outputHeight = avatar ? 800 : 400;
  const objectUrl = useMemo(() => URL.createObjectURL(file), [file]);
  const [imageSize, setImageSize] = useState(null);
  const [zoom, setZoom] = useState(1);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [saving, setSaving] = useState(false);
  const drag = useRef(null);
  const imageRef = useRef(null);
  const canvasRef = useRef(null);

  const dimensions = imageSize ? (() => {
    const baseScale = Math.max(viewportWidth / imageSize.width, viewportHeight / imageSize.height);
    const scale = baseScale * zoom;
    return { scale, width: imageSize.width * scale, height: imageSize.height * scale };
  })() : null;

  const constrain = (next, currentDimensions = dimensions) => {
    if (!currentDimensions) return next;
    const maxX = Math.max(0, (currentDimensions.width - viewportWidth) / 2);
    const maxY = Math.max(0, (currentDimensions.height - viewportHeight) / 2);
    return { x: clamp(next.x, -maxX, maxX), y: clamp(next.y, -maxY, maxY) };
  };

  const changeZoom = (event) => {
    const nextZoom = Number(event.target.value);
    const baseScale = Math.max(viewportWidth / imageSize.width, viewportHeight / imageSize.height);
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
    const factor = viewportWidth / box.width;
    setPosition(constrain({
      x: drag.current.position.x + (event.clientX - drag.current.clientX) * factor,
      y: drag.current.position.y + (event.clientY - drag.current.clientY) * factor,
    }));
  };

  useEffect(() => {
    const image = imageRef.current;
    const canvas = canvasRef.current;
    if (!image || !canvas || !dimensions) return;
    const left = (viewportWidth - dimensions.width) / 2 + position.x;
    const top = (viewportHeight - dimensions.height) / 2 + position.y;
    const context = canvas.getContext("2d");
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.drawImage(image, -left / dimensions.scale, -top / dimensions.scale,
      viewportWidth / dimensions.scale, viewportHeight / dimensions.scale,
      0, 0, canvas.width, canvas.height);
  }, [dimensions, position, viewportHeight, viewportWidth]);

  const crop = async () => {
    if (!imageSize || !dimensions) return;
    setSaving(true);
    const canvas = canvasRef.current;
    const blob = await new Promise((resolve) => canvas.toBlob(resolve, "image/jpeg", 0.9));
    URL.revokeObjectURL(objectUrl);
    await onConfirm(new File([blob], avatar ? "foto-jugador.jpg" : "foto-plantel.jpg", { type: "image/jpeg" }));
    setSaving(false);
  };

  const cancel = () => {
    URL.revokeObjectURL(objectUrl);
    onCancel();
  };

  return <div className="crop-backdrop" onMouseDown={cancel}><section className="crop-modal" onMouseDown={(event) => event.stopPropagation()}>
    <h2>{avatar ? "Encuadrar cara del jugador" : "Encuadrar foto del plantel"}</h2><p>{avatar ? "Mové y acercá la foto hasta que la cara quede centrada dentro del círculo." : "Arrastrá la foto y usá el control para acercar o alejar."}</p>
    <div className={`crop-viewport ${avatar ? "avatar" : "wide"}`} onPointerDown={pointerDown} onPointerMove={pointerMove} onPointerUp={() => { drag.current = null; }} onPointerCancel={() => { drag.current = null; }}>
      <img ref={imageRef} className="crop-source" src={objectUrl} alt="" onLoad={(event) => setImageSize({ width: event.currentTarget.naturalWidth, height: event.currentTarget.naturalHeight })} />
      <canvas ref={canvasRef} width={outputWidth} height={outputHeight} aria-label="Vista previa del encuadre" />
    </div>
    <label className="crop-zoom">Zoom<input type="range" min="1" max="3" step="0.05" value={zoom} onChange={changeZoom} disabled={!imageSize} /></label>
    <div className="crop-actions"><button type="button" onClick={cancel}>Cancelar</button><button type="button" className="primary" onClick={crop} disabled={!imageSize || saving}>{saving ? "Guardando..." : "Usar este encuadre"}</button></div>
  </section></div>;
}

export default PlantelCropModal;
