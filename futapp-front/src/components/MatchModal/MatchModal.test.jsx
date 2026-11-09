import { describe, expect, it } from "vitest";
import { formatMatchDate } from "../../utils/matchDate";

describe("formatMatchDate", () => {
  it("convierte la fecha técnica del backend en un texto legible", () => {
    const formatted = formatMatchDate("2026-10-17T15:00:00");

    expect(formatted).toContain("17");
    expect(formatted).toContain("oct");
    expect(formatted).not.toContain("T15:00:00");
  });

  it("usa un texto seguro si la fecha no existe", () => {
    expect(formatMatchDate(null)).toBe("Horario a confirmar");
  });
});
