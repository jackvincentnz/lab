/// <reference types="cypress" />

describe("autojournal", () => {
  it("should automatically journal a completed task", () => {
    const task = Date.now().toString();

    // Load tasks page
    cy.visit("task");

    // Enter task name
    cy.get('[data-test="add-task-input"]').type(task);

    // Add task
    cy.get('[data-test="add-task-button"]').click();

    // Complete task
    cy.contains(task).click();

    // Navigate to journal page
    cy.contains("Journal").click();

    // Assert we are on the journal page
    cy.url().should("include", "/journal");

    // Assert task completion was journaled
    cy.contains("Completed: " + task);
  });
});

export {};
