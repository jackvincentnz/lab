import { describe, expect, test } from "../../../../test";
import { mapValuesToLineItem } from "./actions";

describe("spend table actions", () => {
  test("maps create-row values to a new line item", () => {
    expect(
      mapValuesToLineItem({
        name: "Webinars",
        country: "nz",
        city: "",
      }),
    ).toEqual({
      name: "Webinars",
      fields: [{ id: "country", value: "nz" }],
    });
  });
});
