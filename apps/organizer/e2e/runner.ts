import cypress from "cypress";
import fs from "fs";
import Dockerode from "dockerode";
import {
  GenericContainer,
  Wait,
  Network,
  StartedNetwork,
} from "testcontainers";
import { KafkaContainer } from "@testcontainers/kafka";
import { ContentToCopy, Environment } from "testcontainers/build/types";
import { PortWithOptionalBinding } from "testcontainers/build/utils/port";

import { findUnusedPort } from "../../../libs/utils/ts/find-port";

const RUNFILES = process.env["JS_BINARY__RUNFILES"];

const TASKLIST_TARBALL = `${RUNFILES}/_main/apps/tasklist/deliver.load/tarball.tar`;
const TASKLIST_TAG = "jackvincent/lab-tasklist:latest";
const JOURNAL_APP_TARBALL = `${RUNFILES}/_main/apps/journal_app/deliver.load/tarball.tar`;
const JOURNAL_APP_TAG = "jackvincent/lab-journal-app:latest";
const TASK_TARBALL = `${RUNFILES}/_main/services/task/src/main/deliver.load/tarball.tar`;
const TASK_TAG = "jackvincent/lab-task:latest";
const JOURNAL_TARBALL = `${RUNFILES}/_main/services/journal/src/main/deliver.load/tarball.tar`;
const JOURNAL_TAG = "jackvincent/lab-journal:latest";
const AUTOJOURNAL_TARBALL = `${RUNFILES}/_main/services/autojournal/src/main/deliver.load/tarball.tar`;
const AUTOJOURNAL_TAG = "jackvincent/lab-autojournal:latest";
const PROXY_TARBALL = `${RUNFILES}/_main/infra/local/proxy/load/tarball.tar`;
const PROXY_TAG = "lab/proxy:latest";

const DOCKER = new Dockerode();

async function main() {
  const startedNetwork = new Network().start();

  const startedKafka = startedNetwork.then((startedNetwork) =>
    startKafka(startedNetwork),
  );

  const startedSchemaRegistry = Promise.all([
    startedNetwork,
    startedKafka,
  ]).then(([startedNetwork]) => startSchemaRegistry(startedNetwork));

  const {
    loadedTask,
    loadedJournal,
    loadedAutojournal,
    loadedTasklist,
    loadedJournalApp,
    loadedProxy,
  } = loadContainers();

  const startedTask = Promise.all([
    loadedTask,
    startedNetwork,
    startedKafka,
  ]).then(([loadedTask, startedNetwork]) =>
    startTask(loadedTask, startedNetwork),
  );

  const startedJournal = Promise.all([loadedJournal, startedNetwork]).then(
    ([loadedJournal, startedNetwork]) =>
      startJournal(loadedJournal, startedNetwork),
  );

  const startedAutojournal = Promise.all([
    loadedAutojournal,
    startedNetwork,
    startedKafka,
  ]).then(([loadedAutojournal, startedNetwork]) =>
    startAutojournal(loadedAutojournal, startedNetwork),
  );

  const startedRouter = startedNetwork.then((startedNetwork) =>
    startRouter(startedNetwork),
  );

  const startedTasklist = Promise.all([loadedTasklist, startedNetwork]).then(
    ([loadedTasklist, startedNetwork]) =>
      startTasklist(loadedTasklist, startedNetwork),
  );
  const startedJournalApp = Promise.all([
    loadedJournalApp,
    startedNetwork,
  ]).then(([loadedJournalApp, startedNetwork]) =>
    startJournalApp(loadedJournalApp, startedNetwork),
  );

  const proxyPort = findUnusedPort(5000);

  const startedProxy = Promise.all([
    loadedProxy,
    proxyPort,
    startedNetwork,
    startedTasklist,
    startedJournalApp,
    startedRouter,
  ]).then(([loadedProxy, proxyPort, startedNetwork]) =>
    startProxy(loadedProxy, proxyPort, startedNetwork),
  );

  Promise.all([
    startedProxy,
    startedKafka,
    startedSchemaRegistry,
    startedTask,
    startedJournal,
    startedAutojournal,
    startedRouter,
    startedTasklist,
    startedJournalApp,
  ]).then(([startedProxy]) =>
    runCypressAgainstUrl(
      `http://localhost:${startedProxy.getFirstMappedPort()}`,
    ),
  );
}

async function startKafka(network: StartedNetwork) {
  return new KafkaContainer()
    .withNetworkAliases("broker")
    .withNetwork(network)
    .start();
}

async function startSchemaRegistry(network: StartedNetwork) {
  const environment: Environment = {
    SCHEMA_REGISTRY_HOST_NAME: "schema-registry",
    SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: "broker:9092",
  };

  return new GenericContainer("confluentinc/cp-schema-registry:7.6.0")
    .withNetwork(network)
    .withNetworkAliases("schema-registry")
    .withEnvironment(environment)
    .withExposedPorts(8081 as PortWithOptionalBinding)
    .start();
}

function loadContainers() {
  return {
    loadedTask: loadContainer(TASK_TARBALL, TASK_TAG),
    loadedJournal: loadContainer(JOURNAL_TARBALL, JOURNAL_TAG),
    loadedAutojournal: loadContainer(AUTOJOURNAL_TARBALL, AUTOJOURNAL_TAG),
    loadedTasklist: loadContainer(TASKLIST_TARBALL, TASKLIST_TAG),
    loadedJournalApp: loadContainer(JOURNAL_APP_TARBALL, JOURNAL_APP_TAG),
    loadedProxy: loadContainer(PROXY_TARBALL, PROXY_TAG),
  };
}

async function startTask(
  taskContainer: GenericContainer,
  network: StartedNetwork,
) {
  const environment: Environment = {
    SPRING_KAFKA_BOOTSTRAP_SERVERS: "broker:9092",
    SPRING_KAFKA_PROPERTIES_SCHEMA_REGISTRY_URL: "http://schema-registry:8081",
  };

  return taskContainer
    .withNetwork(network)
    .withNetworkAliases("task")
    .withEnvironment(environment)
    .withWaitStrategy(Wait.forLogMessage("Started TaskApplication"))
    .start();
}

async function startJournal(
  journalContainer: GenericContainer,
  network: StartedNetwork,
) {
  return journalContainer
    .withNetwork(network)
    .withNetworkAliases("journal")
    .withWaitStrategy(Wait.forLogMessage("Started JournalApplication"))
    .start();
}

async function startAutojournal(
  autoJournalContainer: GenericContainer,
  network: StartedNetwork,
) {
  const environment: Environment = {
    LAB_SERVICES_TASK_GRAPHQL_URL: "http://task:3001/graphql",
    LAB_SERVICES_JOURNAL_GRAPHQL_URL: "http://journal:3003/graphql",
    SPRING_KAFKA_BOOTSTRAP_SERVERS: "broker:9092",
    SPRING_KAFKA_PROPERTIES_SCHEMA_REGISTRY_URL: "http://schema-registry:8081",
  };

  return autoJournalContainer
    .withNetwork(network)
    .withEnvironment(environment)
    .withWaitStrategy(Wait.forLogMessage("Started AutoJournalApplication"))
    .start();
}

async function startRouter(network: StartedNetwork) {
  const schema = fs.readFileSync(
    `${RUNFILES}/_main/infra/local/router/schema/supergraph.graphql`,
  );
  const routerConfig = fs.readFileSync(
    `${RUNFILES}/_main/infra/local/router/config/router.conf`,
  );

  const contentToCopy: ContentToCopy[] = [
    {
      content: schema,
      target: "/dist/schema/local.graphql",
    },
    {
      content: routerConfig,
      target: "/dist/config/router.conf",
    },
  ];

  const environment: Environment = {
    APOLLO_ROUTER_SUPERGRAPH_PATH: "schema/local.graphql",
    APOLLO_ROUTER_CONFIG_PATH: "config/router.conf",
    TASK_ROUTING_URL: "http://task:3001/graphql",
    JOURNAL_ROUTING_URL: "http://journal:3003/graphql",
  };

  return new GenericContainer("ghcr.io/apollographql/router:v1.38.0")
    .withCopyContentToContainer(contentToCopy)
    .withEnvironment(environment)
    .withNetworkAliases("router")
    .withNetwork(network)
    .withExposedPorts(4000 as PortWithOptionalBinding)
    .start();
}

async function startTasklist(
  tasklistContainer: GenericContainer,
  network: StartedNetwork,
) {
  return tasklistContainer
    .withNetwork(network)
    .withNetworkAliases("tasklist")
    .withExposedPorts(80 as PortWithOptionalBinding)
    .start();
}

async function startJournalApp(
  journalAppContainer: GenericContainer,
  network: StartedNetwork,
) {
  return journalAppContainer
    .withNetwork(network)
    .withNetworkAliases("journal_app")
    .withExposedPorts(80 as PortWithOptionalBinding)
    .start();
}

async function startProxy(
  proxyContainer: GenericContainer,
  proxyPort: number,
  network: StartedNetwork,
) {
  const portBinding: PortWithOptionalBinding = {
    host: proxyPort,
    container: proxyPort,
  };

  const environment: Environment = {
    NGINX_PORT: proxyPort.toString(),
    TASK_APP_HOST: `http://tasklist`,
    JOURNAL_APP_HOST: `http://journal_app`,
    ROUTER_HOST: `http://router:4000`,
  };

  return proxyContainer
    .withNetwork(network)
    .withExposedPorts(portBinding)
    .withEnvironment(environment)
    .start();
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

async function loadContainer(tarballPath: string, imageTag: string) {
  return loadImage(tarballPath).then(() => new GenericContainer(imageTag));
}

async function loadImage(tarballPath: string) {
  return DOCKER.loadImage(fs.createReadStream(tarballPath));
}

main();
