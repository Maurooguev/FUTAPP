import { useEffect, useRef, useState } from "react";
import "./CopyableCode.css";

async function copyText(value) {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(value);
    return;
  }

  const textarea = document.createElement("textarea");
  textarea.value = value;
  textarea.setAttribute("readonly", "");
  textarea.style.position = "fixed";
  textarea.style.opacity = "0";
  document.body.appendChild(textarea);
  textarea.select();
  document.execCommand("copy");
  textarea.remove();
}

function CopyableCode({ value, className = "" }) {
  const [copied, setCopied] = useState(false);
  const timeoutRef = useRef(null);

  useEffect(() => () => window.clearTimeout(timeoutRef.current), []);

  const handleCopy = async (event) => {
    event.stopPropagation();
    if (!value) return;
    try {
      await copyText(value);
      setCopied(true);
      window.clearTimeout(timeoutRef.current);
      timeoutRef.current = window.setTimeout(() => setCopied(false), 1600);
    } catch {
      setCopied(false);
    }
  };

  return (
    <button
      type="button"
      className={`copyable-code ${className}`.trim()}
      onClick={handleCopy}
      aria-label={`Copiar código ${value}`}
      title="Tocar para copiar"
    >
      <span>{value}</span>
      <span className="copyable-code-icon" aria-hidden="true">{copied ? "✓" : "⧉"}</span>
      <span className={`copyable-code-feedback ${copied ? "visible" : ""}`} role="status">{copied ? "Copiado" : ""}</span>
    </button>
  );
}

export default CopyableCode;
