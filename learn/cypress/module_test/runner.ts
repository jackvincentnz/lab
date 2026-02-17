import cypress from "cypress";
import fs from "fs";
import dockerode from "dockerode";
import { GenericContainer, type StartedTestContainer } from "testcontainers";

const docker = new dockerode();

const NGINX_TAR_PATH =
  process.env["JS_BINARY__RUNFILES"] +
  "/_main/learn/cypress/module_test/load/tarball.tar";
const NGINX_IMAGE_TAG = "lab/learn/cypress/module_test/nginx:latest";

async function main() {
  let exitCode = 0;
  let container: StartedTestContainer | undefined;

  try {
    const data = fs.createReadStream(NGINX_TAR_PATH);
    await docker.loadImage(data);

    container = await new GenericContainer(NGINX_IMAGE_TAG)
      .withExposedPorts(80)
      .start();

    const port = container.getFirstMappedPort();

    const result = await cypress.run({
      headless: true,
      config: {
        e2e: {
          baseUrl: "http://localhost:" + port,
        },
      },
    });

    if (isFailedRunResult(result)) {
      console.error("Cypress tests failed with status", result.status);
      console.error(result.message);
      exitCode = 2;
    }
    if (!isFailedRunResult(result) && result.totalFailed !== 0) {
      console.error("One or more cypress tests have failed");
      exitCode = 1;
    }
  } catch (e) {
    console.error("Cypress encountered unexpected exception. Exiting.", e);
    exitCode = 3;
  } finally {
    if (container) {
      try {
        await container.stop();
      } catch (e) {
        console.error("Failed to stop test container cleanly.", e);
        if (exitCode === 0) {
          exitCode = 4;
        }
      }
    }
  }

  process.exit(exitCode);
}

function isFailedRunResult(
  result:
    | CypressCommandLine.CypressRunResult
    | CypressCommandLine.CypressFailedRunResult,
): result is CypressCommandLine.CypressFailedRunResult {
  return "failures" in result;
}

main();
