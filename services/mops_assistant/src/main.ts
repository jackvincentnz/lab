import { OpenAI } from "openai";
import {
  ChatCompletionTool,
  ChatCompletionMessageParam,
  ChatCompletionToolMessageParam,
} from "openai/resources";
import { addLineItem, allLineItems } from "./line-items.js";

const openai = new OpenAI();

const tools: ChatCompletionTool[] = [
  {
    type: "function",
    function: {
      name: "get_line_items",
      description:
        "Gets all line items, their categorizations, and the category names from a graphql api.",
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

async function runAssistant(userPrompt: string) {
  const messages: ChatCompletionMessageParam[] = [
    { role: "user", content: userPrompt },
  ];

  let completed = false;
  while (!completed) {
    console.log(JSON.stringify(messages) + "\n\n");

    const completion = await openai.chat.completions.create({
      model: "gpt-4o-mini",
      messages,
      tools,
    });

    const assistantMessage = completion.choices[0].message;

    if (!assistantMessage.tool_calls) {
      console.log(assistantMessage.content);
      completed = true;
      return;
    }

    console.log(JSON.stringify(assistantMessage) + "\n\n");

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
}

// Get user prompt from command-line argument
const userPrompt = process.argv.slice(2).join(" ");
if (!userPrompt) {
  console.log("Please provide a prompt as a command-line argument.");
  process.exit(1);
}

runAssistant(userPrompt);
