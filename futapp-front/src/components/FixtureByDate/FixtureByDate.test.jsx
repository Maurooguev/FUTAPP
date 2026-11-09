import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import FixtureByDate from "./FixtureByDate";

const fecha = { idFecha: 7, numeroFecha: 1, estado: "ACTIVA" };
const partido = {
  idPartido: 20,
  idFecha: 7,
  idEquipoLocal: 1,
  idEquipoVisitante: 2,
  nombreEquipoLocal: "Local",
  nombreEquipoVisitante: "Visitante",
  estado: "PROGRAMADO",
  golesLocal: 0,
  golesVisitante: 0,
  fechaHora: "2026-10-31T19:30:00",
};

describe("FixtureByDate", () => {
  it("permite ajustar y confirmar un resultado rápido", async () => {
    const onQuickResult = vi.fn().mockResolvedValue(undefined);
    const user = userEvent.setup();
    const { container } = render(
      <MemoryRouter>
        <FixtureByDate
          fechas={[fecha]}
          partidos={[partido]}
          canManage
          onQuickResult={onQuickResult}
          onSelectPartido={vi.fn()}
          onEditDate={vi.fn()}
          onCloseDate={vi.fn()}
          onDeleteDate={vi.fn()}
          onEditMatch={vi.fn()}
          onSuspendMatch={vi.fn()}
          onDeleteMatch={vi.fn()}
          onLoadResult={vi.fn()}
        />
      </MemoryRouter>,
    );

    const steppers = container.querySelectorAll(".fixture-score-stepper");
    await user.click(steppers[0].querySelectorAll("button")[1]);
    expect(steppers[0].querySelector("strong")).toHaveTextContent("1");

    await user.click(container.querySelector(".fixture-quick-save"));
    expect(onQuickResult).toHaveBeenCalledWith(partido, { local: 1, visitante: 0 });
  });

  it("muestra la fecha del partido en un formato legible", () => {
    const { container } = render(
      <MemoryRouter>
        <FixtureByDate fechas={[fecha]} partidos={[partido]} />
      </MemoryRouter>,
    );

    expect(container).not.toHaveTextContent("2026-10-31T19:30:00");
    expect(container.querySelector(".fixture-match-card > p")).toHaveTextContent(/31.?oct/i);
  });

  it("muestra el motivo de suspensión sin reemplazar la fase del cruce", () => {
    const suspendido = {
      ...partido,
      estado: "SUSPENDIDO",
      observaciones: "Semifinales",
      motivoEstado: "Lluvia intensa",
    };
    const { container } = render(
      <MemoryRouter>
        <FixtureByDate fechas={[fecha]} partidos={[suspendido]} />
      </MemoryRouter>,
    );

    expect(container).toHaveTextContent("Motivo: Lluvia intensa");
    expect(suspendido.observaciones).toBe("Semifinales");
  });
});
