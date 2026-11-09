import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import QuickTeamLoadWizard from "./QuickTeamLoadWizard";
import { apiFetch } from "../../config/api";

vi.mock("../../config/api", () => ({ API_URL: "http://test/api", apiFetch: vi.fn() }));

describe("QuickTeamLoadWizard", () => {
  beforeEach(() => vi.clearAllMocks());

  it("completa los tres pasos y envía una sola carga", async () => {
    const user = userEvent.setup();
    const onSaved = vi.fn();
    apiFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ equipos: [{ idEquipo: 1, nombre: "Los Pibes" }, { idEquipo: 2, nombre: "Racing" }], jugadoresCreados: 20 }),
    });

    render(<QuickTeamLoadWizard torneoId="9" capacidad={8} equiposActuales={[]} onClose={vi.fn()} onSaved={onSaved} />);
    await user.type(screen.getByLabelText("Nombres de los equipos"), "Los Pibes{enter}Racing");
    await user.click(screen.getByRole("button", { name: "Continuar" }));
    expect(screen.getByText("10", { selector: "strong" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Revisar carga" }));
    expect(screen.getByText("2 equipos")).toBeInTheDocument();
    expect(screen.getByText("20 jugadores")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Confirmar carga" }));

    expect(apiFetch).toHaveBeenCalledWith("http://test/api/equipos/torneo/9/carga-rapida", expect.objectContaining({
      method: "POST",
      body: JSON.stringify({ nombresEquipos: ["Los Pibes", "Racing"], jugadoresPorEquipo: 10 }),
    }));
    expect(onSaved).toHaveBeenCalledWith(expect.objectContaining({ jugadoresCreados: 20 }));
  });

  it("detecta nombres repetidos antes de guardar", async () => {
    const user = userEvent.setup();
    render(<QuickTeamLoadWizard torneoId="9" capacidad={8} equiposActuales={[]} onClose={vi.fn()} onSaved={vi.fn()} />);
    await user.type(screen.getByLabelText("Nombres de los equipos"), "Racing{enter}racing");
    await user.click(screen.getByRole("button", { name: "Continuar" }));
    expect(screen.getByRole("alert")).toHaveTextContent("nombres repetidos");
    expect(apiFetch).not.toHaveBeenCalled();
  });
});
