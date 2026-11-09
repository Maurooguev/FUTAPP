import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import MatchIncidentForm from "./MatchIncidentForm";
import { apiFetch } from "../../config/api";

vi.mock("../../config/api", () => ({
  API_URL: "/api/v1",
  apiFetch: vi.fn(),
}));

const partido = {
  idPartido: 30,
  idEquipoLocal: 10,
  idEquipoVisitante: 11,
  nombreEquipoLocal: "Local",
  nombreEquipoVisitante: "Visitante",
};

const jsonResponse = (body, ok = true) => ({
  ok,
  json: vi.fn().mockResolvedValue(body),
});

describe("MatchIncidentForm", () => {
  afterEach(cleanup);

  beforeEach(() => {
    apiFetch.mockImplementation((url, options = {}) => {
      if (!options.method) return Promise.resolve(jsonResponse([
        { idJugador: 5, idEquipo: 10, nombre: "Franco", apellido: "Romero", suspendido: false },
      ]));
      return Promise.resolve(jsonResponse({ idGol: 99 }));
    });
  });

  it("guarda un gol sin enviar el minuto cuando queda vacío", async () => {
    const onSaved = vi.fn();
    const user = userEvent.setup();

    render(<MatchIncidentForm
      partido={partido}
      type="goal"
      allowedTeamIds={[10]}
      onSaved={onSaved}
      onCancel={vi.fn()}
    />);

    await user.selectOptions(await screen.findByLabelText("Jugador"), "5");
    await user.click(screen.getByRole("button", { name: "Guardar" }));

    await waitFor(() => expect(onSaved).toHaveBeenCalledWith({ idGol: 99 }));
    const payload = JSON.parse(apiFetch.mock.calls[1][1].body);
    expect(payload).toMatchObject({ idPartido: 30, idJugador: 5, idEquipo: 10, tipoGol: "Jugada" });
    expect(payload).not.toHaveProperty("minuto");
  });

  it("envía la cantidad elegida de fechas cuando carga una tarjeta roja", async () => {
    apiFetch.mockImplementation((url, options = {}) => {
      if (!options.method) return Promise.resolve(jsonResponse([
        { idJugador: 5, idEquipo: 10, nombre: "Franco", apellido: "Romero", suspendido: false },
      ]));
      return Promise.resolve(jsonResponse({ idTarjeta: 77 }));
    });
    const onSaved = vi.fn();
    const user = userEvent.setup();

    render(<MatchIncidentForm
      partido={partido}
      type="card"
      onSaved={onSaved}
      onCancel={vi.fn()}
    />);

    await user.selectOptions(await screen.findByLabelText("Jugador"), "5");
    await user.selectOptions(screen.getByLabelText("Tarjeta"), "ROJA");
    await user.clear(screen.getByLabelText("Fechas de suspensión"));
    await user.type(screen.getByLabelText("Fechas de suspensión"), "3");
    expect(screen.getByText(/próximas 3 fechas/i)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Guardar" }));

    await waitFor(() => expect(onSaved).toHaveBeenCalledWith({ idTarjeta: 77 }));
    expect(JSON.parse(apiFetch.mock.calls[1][1].body)).toMatchObject({
      tipoTarjeta: "ROJA",
      fechasSuspension: 3,
      idPartido: 30,
      idJugador: 5,
      idEquipo: 10,
    });
  });

  it("no ofrece jugadores que ya están suspendidos", async () => {
    apiFetch.mockResolvedValue(jsonResponse([
      { idJugador: 5, idEquipo: 10, nombre: "Franco", apellido: "Romero", suspendido: false },
      { idJugador: 6, idEquipo: 10, nombre: "Carlos", apellido: "Suspendido", suspendido: true },
    ]));

    render(<MatchIncidentForm
      partido={partido}
      type="card"
      onSaved={vi.fn()}
      onCancel={vi.fn()}
    />);

    const selector = await screen.findByLabelText("Jugador");
    expect(selector).toHaveTextContent("Franco Romero");
    expect(selector).not.toHaveTextContent("Carlos Suspendido");
    expect(screen.getByText(/jugadores suspendidos no aparecen/i)).toBeInTheDocument();
  });
});
