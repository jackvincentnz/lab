import cypress from "cypress";
import fs from "fs";
import Dockerode from "dockerode";
import { GenericContainer, Wait, Network } from "testcontainers";
import { KafkaContainer } from "@testcontainers/kafka";

const RUNFILES = process.env["JS_BINARY__RUNFILES"];

const TASKLIST_TARBALL = `${RUNFILES}/_main/apps/tasklist/tarball/tarball.tar`;
const TASKLIST_TAG = "lab/tasklist:latest";
const JOURNAL_APP_TARBALL = `${RUNFILES}/_main/apps/journal_app/tarball/tarball.tar`;
const JOURNAL_APP_TAG = "lab/journal_app:latest";
const TASK_TARBALL = `${RUNFILES}/_main/services/task/src/main/tarball/tarball.tar`;
const TASK_TAG = "lab/task:latest";
const JOURNAL_TARBALL = `${RUNFILES}/_main/services/journal/src/main/tarball/tarball.tar`;
const JOURNAL_TAG = "lab/journal:latest";
const AUTOJOURNAL_TARBALL = `${RUNFILES}/_main/services/autojournal/src/main/tarball/tarball.tar`;
const AUTOJOURNAL_TAG = "lab/autojournal:latest";
const PROXY_TARBALL = `${RUNFILES}/_main/infra/local/proxy/tarball/tarball.tar`;
const PROXY_TAG = "lab/proxy:latest";

const DOCKER = new Dockerode();

async function main() {
  const network = await new Network().start();

  await new KafkaContainer()
    .withNetworkAliases("broker")
    .withNetwork(network)
    .start();

  await new GenericContainer("confluentinc/cp-schema-registry:7.5.3")
    .withNetwork(network)
    .withNetworkAliases("schema-registry")
    .withEnvironment({
      SCHEMA_REGISTRY_HOST_NAME: "schema-registry",
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: "broker:9092",
    })
    .start();

  const taskContainer = loadContainer(TASK_TARBALL, TASK_TAG).then(
    (container) =>
      container
        .withNetwork(network)
        .withExposedPorts({
          host: 3001,
          container: 3001,
        })
        .withEnvironment({
          SPRING_KAFKA_BOOTSTRAP_SERVERS: "broker:9092",
          SPRING_KAFKA_PROPERTIES_SCHEMA_REGISTRY_URL:
            "http://schema-registry:8081",
        })
        .withWaitStrategy(Wait.forLogMessage("Started TaskApplication"))
        .start(),
  );

  const journalContainer = loadContainer(JOURNAL_TARBALL, JOURNAL_TAG).then(
    (container) =>
      container
        .withExposedPorts({
          host: 3003,
          container: 3003,
        })
        .withWaitStrategy(Wait.forLogMessage("Started JournalApplication"))
        .start(),
  );

  const autojournalContainer = loadContainer(
    AUTOJOURNAL_TARBALL,
    AUTOJOURNAL_TAG,
  ).then((container) =>
    container
      .withNetwork(network)
      .withExposedPorts({
        host: 3002,
        container: 3002,
      })
      .withEnvironment({
        LAB_SERVICES_TASK_GRAPHQL_URL:
          "http://host.docker.internal:3001/graphql",
        LAB_SERVICES_JOURNAL_GRAPHQL_URL:
          "http://host.docker.internal:3003/graphql",
        SPRING_KAFKA_BOOTSTRAP_SERVERS: "broker:9092",
        SPRING_KAFKA_PROPERTIES_SCHEMA_REGISTRY_URL:
          "http://schema-registry:8081",
      })
      .withExtraHosts([
        { host: "host.docker.internal", ipAddress: "host-gateway" },
      ])
      .withWaitStrategy(Wait.forLogMessage("Started AutoJournalApplication"))
      .start(),
  );

  const schema = fs.readFileSync(
    `${RUNFILES}/_main/infra/local/router/schema/supergraph.graphql`,
  );
  const routerConfig = fs.readFileSync(
    `${RUNFILES}/_main/infra/local/router/config/router.conf`,
  );

  const routerContainer = new GenericContainer(
    "ghcr.io/apollographql/router:v1.38.0",
  )
    .withCopyContentToContainer([
      {
        content: schema,
        target: "/dist/schema/local.graphql",
      },
      {
        content: routerConfig,
        target: "/dist/config/router.conf",
      },
    ])
    .withEnvironment({
      APOLLO_ROUTER_SUPERGRAPH_PATH: "schema/local.graphql",
      APOLLO_ROUTER_CONFIG_PATH: "config/router.conf",
    })
    .withExtraHosts([
      { host: "host.docker.internal", ipAddress: "host-gateway" },
    ])
    .withExposedPorts({ host: 4000, container: 4000 })
    .start();

  const tasklistContainer = loadContainer(TASKLIST_TARBALL, TASKLIST_TAG).then(
    (container) =>
      container
        .withExposedPorts({
          host: 3000,
          container: 80,
        })
        .start(),
  );
  const journalAppContainer = loadContainer(
    JOURNAL_APP_TARBALL,
    JOURNAL_APP_TAG,
  ).then((container) =>
    container
      .withExposedPorts({
        host: 3004,
        container: 80,
      })
      .start(),
  );

  const proxyContainer = loadContainer(PROXY_TARBALL, PROXY_TAG).then(
    (container) =>
      container
        .withExposedPorts({
          host: 8080,
          container: 8080,
        })
        .withExtraHosts([
          { host: "host.docker.internal", ipAddress: "host-gateway" },
        ])
        .start(),
  );

  Promise.all([
    taskContainer,
    journalContainer,
    autojournalContainer,
    routerContainer,
    tasklistContainer,
    journalAppContainer,
    proxyContainer,
  ]).then(() => runCypressAgainstUrl(`http://localhost:8080`));
}

async function runCypressAgainstUrl(baseUrl: string) {
  try {
    const result = await cypress.run({
      headless: true,
      config: {
        e2e: {
          baseUrl,
        },
      },
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

async function loadContainer(tarballPath: string, imageTag: string) {
  return loadImage(tarballPath).then(() => new GenericContainer(imageTag));
}

async function loadImage(tarballPath: string) {
  return DOCKER.loadImage(fs.createReadStream(tarballPath));
}

main();
