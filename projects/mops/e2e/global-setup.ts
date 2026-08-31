import Dockerode from "dockerode";
import fs from "node:fs";
import {
  GenericContainer,
  Network,
  Wait,
  type StartedNetwork,
  type StartedTestContainer,
} from "testcontainers";
import type { Environment } from "testcontainers/build/types";

const RUNFILES = process.env["JS_BINARY__RUNFILES"];

if (!RUNFILES) {
  throw new Error("JS_BINARY__RUNFILES is not set");
}

const APP_TARBALL = `${RUNFILES}/_main/projects/mops/app/deliver.load/tarball.tar`;
const APP_TAG = "jackvincent/lab-mops-app:latest";
const SERVICE_TARBALL = `${RUNFILES}/_main/projects/mops/service/src/main/deliver.load/tarball.tar`;
const SERVICE_TAG = "jackvincent/lab-mops:latest";

const DOCKER = new Dockerode();

type StartedResource = StartedNetwork | StartedTestContainer;

export default async function globalSetup() {
  const cleanupTargets: StartedResource[] = [];

  try {
    const network = await new Network().start();
    cleanupTargets.push(network);

    const [serviceContainer, appContainer] = await Promise.all([
      loadContainer(SERVICE_TARBALL, SERVICE_TAG),
      loadContainer(APP_TARBALL, APP_TAG),
    ]);

    const service = await startService(serviceContainer, network);
    cleanupTargets.push(service);

    const app = await startApp(appContainer, network);
    cleanupTargets.push(app);

    process.env["MOPS_E2E_BASE_URL"] =
      `http://${app.getHost()}:${app.getMappedPort(80)}`;

    return async () => cleanup(cleanupTargets);
  } catch (error) {
    await cleanup(cleanupTargets);
    throw error;
  }
}

async function startService(
  container: GenericContainer,
  network: StartedNetwork,
) {
  const environment: Environment = {
    GEMINI_API_KEY: "unused-by-e2e-test",
  };

  return container
    .withNetwork(network)
    .withNetworkAliases("mops-service")
    .withEnvironment(environment)
    .withWaitStrategy(Wait.forLogMessage("Started MopsApplication"))
    .start();
}

async function startApp(container: GenericContainer, network: StartedNetwork) {
  const environment: Environment = {
    GRAPHQL_HOST: "http://mops-service:8080",
    NGINX_PORT: "80",
  };

  return container
    .withNetwork(network)
    .withEnvironment(environment)
    .withExposedPorts(80)
    .withWaitStrategy(Wait.forHttp("/spend", 80))
    .start();
}

async function loadContainer(tarballPath: string, imageTag: string) {
  await DOCKER.loadImage(fs.createReadStream(tarballPath));
  return new GenericContainer(imageTag);
}

async function cleanup(targets: StartedResource[]) {
  for (const target of targets.reverse()) {
    await target.stop();
  }
}
