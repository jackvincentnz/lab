import { type Transport } from "@modelcontextprotocol/sdk/shared/transport.js";
import {
  ErrorCode,
  InitializeResultSchema,
  isInitializeRequest,
  isJSONRPCRequest,
  isJSONRPCResultResponse,
  type JSONRPCMessage,
  type RequestId,
} from "@modelcontextprotocol/sdk/types.js";

export async function forward(
  client: Transport,
  slate: Transport,
  slateUrl: string,
): Promise<void> {
  const initializeIds = new Set<RequestId>();

  client.onmessage = (message) => {
    if (isJSONRPCRequest(message) && isInitializeRequest(message)) {
      initializeIds.add(message.id);
    }
    slate.send(message).catch(() => {
      if (isJSONRPCRequest(message)) {
        void client.send(unreachable(message.id, slateUrl));
      }
    });
  };
  slate.onmessage = (message) => {
    if (isJSONRPCResultResponse(message) && initializeIds.delete(message.id)) {
      const result = InitializeResultSchema.safeParse(message.result);
      if (result.success) {
        slate.setProtocolVersion?.(result.data.protocolVersion);
      }
    }
    void client.send(message);
  };
  client.onclose = () => void slate.close();
  slate.onerror = (error) => console.error(error);

  await slate.start();
  await client.start();
}

function unreachable(id: RequestId, slateUrl: string): JSONRPCMessage {
  return {
    jsonrpc: "2.0",
    id,
    error: {
      code: ErrorCode.InternalError,
      message: `Slate did not answer at ${slateUrl}. Check that Slate is running.`,
    },
  };
}
