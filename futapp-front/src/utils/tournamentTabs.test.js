import { describe, expect, it } from "vitest";
import { tournamentTabFromHash } from "./tournamentTabs";

describe("tournamentTabFromHash", () => {
  it("abre la sección indicada por un enlace directo", () => {
    expect(tournamentTabFromHash("#fixture")).toBe("fixture");
    expect(tournamentTabFromHash("#equipos")).toBe("equipos");
  });

  it("vuelve al resumen ante una sección inexistente", () => {
    expect(tournamentTabFromHash("#cualquier-cosa")).toBe("resumen");
    expect(tournamentTabFromHash("")).toBe("resumen");
  });
});
