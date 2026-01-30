/// <reference types="cypress" />

describe("routing", () => {
  it("should redirect root path to tasks page", () => {
    cy.visit("/");

    // Assert we are on the Tasks page
    cy.url().should("include", "/task");
  });

  it("should load tasks page", () => {
    cy.visit("task");
  });

  it("should load journal page", () => {
    cy.visit("journal");
  });
});

export {};
