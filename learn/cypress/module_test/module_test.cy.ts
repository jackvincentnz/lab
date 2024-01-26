/// <reference types="cypress" />

describe("Module test", () => {
  it("should find running Bazel built container", () => {
    cy.visit("/");

    cy.contains("Welcome to nginx!");
  });
});

export {};
