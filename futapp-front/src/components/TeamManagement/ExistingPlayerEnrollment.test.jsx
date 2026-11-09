import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import ExistingPlayerEnrollment from "./ExistingPlayerEnrollment";
import { apiFetch } from "../../config/api";

vi.mock("../../config/api", () => ({
  API_URL: "/api/v1",
  apiFetch: vi.fn(),
}));

vi.mock("../EntityImage/EntityImage", () => ({
  default: () => <span data-testid="player-image" />,
}));

const players = [
  { idJugador: 1, nombre: "Ana", apellido: "Pérez", codigoPublico: "JUG-1", posicion: "Arquera", numeroCamiseta: 1 },
  { idJugador: 2, nombre: "Lucía", apellido: "Gómez", codigoPublico: "JUG-2", posicion: "Delantera", numeroCamiseta: 9 },
];

const jsonResponse = (body, ok = true) => ({
  ok,
  json: vi.fn().mockResolvedValue(body),
});

describe("ExistingPlayerEnrollment", () => {
  beforeEach(() => {
    apiFetch.mockImplementation((url, options = {}) => {
      if (!options.method) return Promise.resolve(jsonResponse(players));
      const request = JSON.parse(options.body);
      return Promise.resolve(jsonResponse({
        idInscripcionJugador: request.idJugador + 100,
        ...request,
        estado: "ACTIVA",
      }));
    });
  });

  it("permite seleccionar todo el plantel y convocarlo en una sola acción", async () => {
    const onSaved = vi.fn();
    const user = userEvent.setup();

    render(<ExistingPlayerEnrollment
      equipoId={10}
      enrollmentId={20}
      currentPlayers={[]}
      onSaved={onSaved}
      onCancel={vi.fn()}
    />);

    await screen.findByText("Ana Pérez");
    await user.click(screen.getByRole("button", { name: "Convocar todos" }));

    expect(screen.getByText("2 de 2 seleccionados")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Convocar 2 al torneo" }));

    await waitFor(() => expect(onSaved).toHaveBeenCalledTimes(1));
    expect(apiFetch).toHaveBeenCalledTimes(3);
    expect(JSON.parse(apiFetch.mock.calls[1][1].body)).toMatchObject({ idJugador: 1, numeroCamiseta: 1 });
    expect(JSON.parse(apiFetch.mock.calls[2][1].body)).toMatchObject({ idJugador: 2, numeroCamiseta: 9 });
  });
});
