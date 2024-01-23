/*global describe, it, cy */

describe("Module test", () => {
  it("should find running Bazel built container", () => {
    cy.visit("/");

    cy.contains("Welcome to nginx!");
  });
});
