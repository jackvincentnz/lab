import { describe, expect, test } from "../../../../test";
import {
  NAME_MAX_LENGTH,
  NAME_MAX_LENGTH_ERROR,
  NAME_REQUIRED_ERROR,
  validateLineItem,
} from "./validation";

describe("spend table validation", () => {
  test("requires a name", () => {
    expect(validateLineItem({ name: "", fields: [] })).toEqual({
      name: NAME_REQUIRED_ERROR,
    });
  });

  test("rejects names longer than the maximum", () => {
    expect(
      validateLineItem({ name: "x".repeat(NAME_MAX_LENGTH + 1), fields: [] }),
    ).toEqual({
      name: NAME_MAX_LENGTH_ERROR,
    });
  });

  test("accepts a valid line item", () => {
    expect(validateLineItem({ name: "Webinar", fields: [] })).toEqual({
      name: "",
    });
  });
});
