import cypress from "cypress";

async function main() {
  try {
    const result = await cypress.run({
      headless: true,
    });

    if (result.status === "finished" && result.totalFailed !== 0) {
      console.error("One or more cypress tests have failed");
      process.exit(1);
    }
    if (result.status !== "finished") {
      console.error("Cypress tests failed with status", result.status);
      console.error(result.message);
      process.exit(2);
    }
  } catch (e) {
    console.error("Cypress encountered unexpected exception. Exiting.", e);
    process.exit(3);
  }
}

main();
