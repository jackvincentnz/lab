import Fastify, { FastifyInstance } from "fastify";
import { runAssistant } from "./assistant.js";

const server: FastifyInstance = Fastify({});

interface ChatBody {
  prompt: string;
}

server.post<{ Body: ChatBody }>("/chat", async (req, res) => {
  const message = await runAssistant(req.body.prompt);
  res.status(200).send({ message });
});

const start = async () => {
  try {
    await server.listen({ port: 3000 });
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
