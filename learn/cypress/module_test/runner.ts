import cypress from "cypress";
import fs from "fs";
import dockerode from "dockerode";
import { GenericContainer } from "testcontainers";

const docker = new dockerode();

const NGINX_TAR_PATH =
  process.env["JS_BINARY__RUNFILES"] +
  "/_main/learn/cypress/module_test/load/tarball.tar";
const NGINX_IMAGE_TAG = "lab/learn/cypress/module_test/nginx:latest";

async function main() {
  const data = fs.createReadStream(NGINX_TAR_PATH);
  await docker.loadImage(data);

  const container = await new GenericContainer(NGINX_IMAGE_TAG)
    .withExposedPorts(80)
    .start();

  const port = container.getFirstMappedPort();

  try {
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
      process.exit(2);
    }
    if (result.totalFailed !== 0) {
      console.error("One or more cypress tests have failed");
      process.exit(1);
    }
  } catch (e) {
    console.error("Cypress encountered unexpected exception. Exiting.", e);
    process.exit(3);
  }
}

function isFailedRunResult(
  result:
    | CypressCommandLine.CypressRunResult
    | CypressCommandLine.CypressFailedRunResult,
): result is CypressCommandLine.CypressFailedRunResult {
  return "failures" in result;
}

main();
