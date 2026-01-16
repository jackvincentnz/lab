package lab.mops.ai.application.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.ToolResultMessage;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.Message;
import lab.mops.ai.domain.chat.PendingAssistantMessageAddedEvent;
import lab.mops.ai.domain.chat.ToolCallApprovedEvent;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// FIXME: there is no guarantee these will run, so an assistant message could remain PENDING
@Component
public class ChatEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ChatEventHandler.class);

  protected static final int MAX_COMPLETIONS = 10;

  private final ChatRepository chatRepository;

  private final CompletionService completionService;

  private final ToolProvider toolProvider;

  private final MessageMapper messageMapper;

  public ChatEventHandler(
      ChatRepository chatRepository,
      CompletionService completionService,
      ToolProvider toolProvider,
      MessageMapper messageMapper) {
    this.chatRepository = chatRepository;
    this.completionService = completionService;
    this.toolProvider = toolProvider;
    this.messageMapper = messageMapper;
  }

  @Async
  @EventListener
  public void onPendingAssistantMessageAdded(PendingAssistantMessageAddedEvent event) {
    LOG.debug(
        "Handling pending assistant message [{}], in chat [{}]",
        event.pendingAssistantMessageId(),
        event.chatId());

    var chat = chatRepository.getById(event.chatId());
    var tools = toolProvider.getTools();

    var conversationHistory =
        chat.getMessages().stream()
            .filter(Message::isCompleted)
            .map(messageMapper::map)
            .collect(Collectors.toList());

    var completionCounter = 0;
    var response = completionService.getResponse(conversationHistory);
    while (response.hasToolCalls() && !hasPendingToolApprovals(tools, response.getToolCalls())) {
      if (++completionCounter >= MAX_COMPLETIONS) {
        throw new RuntimeException("Max completions reached");
      }

      LOG.debug("Response has tools calls and none need approval");

      conversationHistory.add(response);

      var toolResults = executeToolCalls(response.getToolCalls());
      conversationHistory.addAll(toolResults);

      response = completionService.getResponse(conversationHistory);
    }

    if (hasPendingToolApprovals(tools, response.getToolCalls())) {
      LOG.debug("Response has pending tool calls, adding to chat for approval");
      var toolCalls = mapToolCalls(tools, response.getToolCalls());
      chat.addPendingToolCalls(event.pendingAssistantMessageId(), toolCalls);
    } else {
      LOG.debug("Response has no pending tool calls, completing pending message");
      chat.completeMessage(event.pendingAssistantMessageId(), response.getContent().orElseThrow());
    }

    chatRepository.save(chat);
  }

  private boolean hasPendingToolApprovals(Collection<Tool> tools, Collection<ToolCall> toolCalls) {
    return toolCalls.stream().anyMatch(t -> toolNeedsApproval(tools, t));
  }

  private boolean toolNeedsApproval(Collection<Tool> tools, ToolCall toolCall) {
    return tools.stream()
        .anyMatch(
            t ->
                toolCall.toolName().equals(t.getToolDefinition().name())
                    && t.getToolDefinition().needsApproval());
  }

  private List<ToolResultMessage> executeToolCalls(Collection<ToolCall> toolCalls) {
    var toolResults = new ArrayList<ToolResultMessage>();

    for (var toolCall : toolCalls) {
      var result = executeToolCall(toolCall);

      toolResults.add(new ToolResultMessage(result, toolCall.id(), toolCall.toolName()));
    }

    return toolResults;
  }

  private List<lab.mops.ai.domain.chat.ToolCall> mapToolCalls(
      Collection<Tool> tools, Collection<ToolCall> toolCalls) {
    return toolCalls.stream()
        .map(
            toolCall -> {
              var tool =
                  tools.stream()
                      .filter(t -> t.getToolDefinition().name().equals(toolCall.toolName()))
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new RuntimeException(
                                  "Tool [%s] not available".formatted(toolCall.toolName())));

              var toolCallId =
                  StringUtils.isBlank(toolCall.id())
                      ? ToolCallId.create()
                      : ToolCallId.of(toolCall.id());

              return lab.mops.ai.domain.chat.ToolCall.of(
                  toolCallId,
                  toolCall.toolName(),
                  toolCall.arguments(),
                  tool.getToolDefinition().needsApproval()
                      ? ToolCallStatus.PENDING_APPROVAL
                      : ToolCallStatus.APPROVED);
            })
        .toList();
  }

  @Async
  @EventListener
  public void onToolCallApproved(ToolCallApprovedEvent event) {
    var chat = chatRepository.getById(event.chatId());
    var toolCall = chat.getToolCallById(event.messageId(), event.toolCallId());

    var result =
        executeToolCall(
            new ToolCall(toolCall.id().toString(), toolCall.name(), toolCall.arguments()));

    chat.recordToolResult(event.messageId(), event.toolCallId(), result);

    chatRepository.save(chat);
  }

  private String executeToolCall(ToolCall toolCall) {
    var tool = getToolByName(toolCall.toolName());

    LOG.debug("Executing tool [{}] with arguments [{}]", toolCall.toolName(), toolCall.arguments());

    var result = tool.call(toolCall.arguments());

    LOG.debug("Tool [{}] returned [{}]", toolCall.toolName(), result);

    return result;
  }

  // TODO: check lookup performance -> move to interface that does exactly what we need
  private Tool getToolByName(String name) {
    return toolProvider.getTools().stream()
        .filter(t -> name.equals(t.getToolDefinition().name()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Tool [%s] not available".formatted(name)));
  }
}
