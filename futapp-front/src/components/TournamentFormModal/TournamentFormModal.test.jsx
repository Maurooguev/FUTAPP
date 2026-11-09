import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import TournamentFormModal from "./TournamentFormModal";

vi.mock("../../config/api", () => ({ API_URL: "http://test/api", apiFetch: vi.fn() }));

const DRAFT_KEY = "futapp:tournament-draft";

describe("TournamentFormModal", () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.stubGlobal("fetch", vi.fn());
  });

  it("permite descartar un borrador recuperado y comenzar limpio", async () => {
    window.localStorage.setItem(DRAFT_KEY, JSON.stringify({
      form: { nombre: "Torneo anterior", ubicacion: "Merlo, Buenos Aires", cantidadEquipos: 8, formato: "Liga" },
      locationConfirmed: true,
    }));

    render(<TournamentFormModal onClose={vi.fn()} onSaved={vi.fn()} />);
    expect(screen.getByLabelText("Nombre del torneo")).toHaveValue("Torneo anterior");

    await userEvent.click(screen.getByRole("button", { name: "Descartar y empezar de cero" }));

    expect(screen.getByLabelText("Nombre del torneo")).toHaveValue("");
    expect(screen.queryByText("Continuamos desde el último dato que habías cargado.")).not.toBeInTheDocument();
    expect(window.localStorage.getItem(DRAFT_KEY)).toBeNull();
  });

  it("no vuelve a buscar una ubicación confirmada del borrador", async () => {
    window.localStorage.setItem(DRAFT_KEY, JSON.stringify({
      form: { nombre: "Torneo anterior", ubicacion: "Merlo, Buenos Aires", cantidadEquipos: 8, formato: "Liga" },
      locationConfirmed: true,
    }));

    render(<TournamentFormModal onClose={vi.fn()} onSaved={vi.fn()} />);
    await new Promise((resolve) => window.setTimeout(resolve, 350));

    expect(fetch).not.toHaveBeenCalled();
    expect(screen.queryByText("No encontramos una localidad con ese nombre.")).not.toBeInTheDocument();
  });
});
