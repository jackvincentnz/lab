import { OpenAI } from "openai";
import {
  ChatCompletionMessageParam,
  ChatCompletionTool,
  ChatCompletionToolMessageParam,
} from "openai/resources";
import { addLineItem, allLineItems } from "./line-items.js";

interface Provider {
  name: string;
  apiKey?: string;
  baseUrl?: string;
  model: string;
}

const PROVIDERS: Provider[] = [
  {
    name: "openai",
    model: "gpt-4o-mini",
  },
  {
    name: "google",
    apiKey: process.env["GEMINI_API_KEY"],
    baseUrl: "https://generativelanguage.googleapis.com/v1beta/openai/",
    model: "gemini-2.0-flash",
  },
];

const SELECTED_PROVIDER = PROVIDERS[0];

const openai = new OpenAI({
  apiKey: SELECTED_PROVIDER.apiKey,
  baseURL: SELECTED_PROVIDER.baseUrl,
});

const tools: ChatCompletionTool[] = [
  {
    type: "function",
    function: {
      name: "get_line_items",
      description:
        "Gets all line items, including their names, and category names from a graphql api.",
    },
  },
  {
    type: "function",
    function: {
      name: "add_line_item",
      description: "Adds a line item using a graphql api.",
      parameters: {
        type: "object",
        properties: {
          name: {
            type: "string",
            description: "The name of the line item to be added.",
          },
        },
        required: ["name"],
        additionalProperties: false,
      },
      strict: true,
    },
  },
];

export async function runAssistant(userPrompt: string): Promise<string> {
  const messages: ChatCompletionMessageParam[] = [
    { role: "user", content: userPrompt },
  ];

  let completed = false;
  while (!completed) {
    const completion = await openai.chat.completions.create({
      model: SELECTED_PROVIDER.model,
      messages,
      tools,
    });

    const assistantMessage = completion.choices[0].message;

    if (!assistantMessage.tool_calls) {
      completed = true;
      return assistantMessage.content as string;
    }

    const toolResponses: ChatCompletionToolMessageParam[] = [];
    for (const toolCall of assistantMessage.tool_calls) {
      const args = JSON.parse(toolCall.function.arguments);
      let result;
      if (toolCall.function.name === "add_line_item") {
        result = await addLineItem({ name: args.name });
      } else if (toolCall.function.name === "get_line_items") {
        result = await allLineItems();
      }

      toolResponses.push({
        role: "tool",
        tool_call_id: toolCall.id,
        content: JSON.stringify(result),
      });
    }

    messages.push(assistantMessage, ...toolResponses);
  }

  throw new Error("Completed without returning a response");
}
