import Fastify, { FastifyInstance } from "fastify";
import { runAssistant } from "./assistant.js";

const server: FastifyInstance = Fastify({
  logger: true, // Enable built-in logging
});

// Middleware to log incoming requests
server.addHook("onRequest", async (req) => {
  req.log.info(
    { url: req.url, method: req.method, body: req.body },
    "Incoming request",
  );
});

interface ChatBody {
  prompt: string;
}

server.post<{ Body: ChatBody }>("/chat", async (req, res) => {
  try {
    if (!req.body?.prompt) {
      req.log.error("Missing 'prompt' in request body");
      return res.status(400).send({ error: "Missing 'prompt' field" });
    }

    req.log.info({ prompt: req.body.prompt }, "Processing request");

    const message = await runAssistant(req.body.prompt);

    req.log.info({ response: message }, "Sending response");
    return res.status(200).send({ message });
  } catch (error) {
    req.log.error({ err: error }, "Error processing request");
    return res.status(500).send({ error: "Internal server error" });
  }
});

// Start the server
const start = async () => {
  try {
    await server.listen({ port: 3000, host: "0.0.0.0" }); // Explicit host for Docker compatibility
    server.log.info("Server started on port 3000.");
  } catch (err) {
    server.log.error(err, "Server startup failed");
    process.exit(1);
  }
};

// Graceful shutdown
process.on("SIGINT", async () => {
  server.log.info("Shutting down server...");
  await server.close();
  process.exit(0);
});

start();
