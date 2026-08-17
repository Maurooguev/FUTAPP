import "./SectionTitle.css";

function SectionTitle({ chip, title, description, center = false }) {
  return (
    <div className={`section-title ${center ? "section-title-center" : ""}`}>
      <div>
        {chip && <span className="section-title-chip">{chip}</span>}
        <h2>{title}</h2>
      </div>

      {description && <p>{description}</p>}
    </div>
  );
}

export default SectionTitle;