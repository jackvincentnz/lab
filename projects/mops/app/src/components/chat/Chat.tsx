import { Box } from "@mantine/core";
import { ChatHistory } from "./ChatHistory";
import { ChatInput } from "./ChatInput";
import { ChatHeader } from "./ChatHeader";
import { ChatTranscript } from "./ChatTranscript";
import { useChatController } from "./useChatController";
import { CHAT_HISTORY_VIEW } from "./chatView";

export function Chat() {
  const controller = useChatController();

  return (
    <Box
      p="md"
      style={{
        height: "100%",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <ChatHeader
        onNewChat={controller.handleNewChat}
        onShowChats={controller.handleShowChats}
      />

      {controller.view === CHAT_HISTORY_VIEW ? (
        <ChatHistory
          onSelectChat={controller.handleSelectChat}
          onBack={controller.handleBackToChat}
        />
      ) : (
        <>
          <ChatTranscript
            currentChatId={controller.currentChatId}
            messages={controller.messages}
            editingMessageId={controller.editingMessageId}
            editContent={controller.editContent}
            setEditContent={controller.setEditContent}
            onEditMessage={controller.handleEditMessage}
            onSaveEdit={controller.handleSaveEdit}
            onCancelEdit={controller.handleCancelEdit}
            onRetryMessage={controller.handleRetryMessage}
            onApproveToolCall={controller.handleApproveToolCall}
            onRejectToolCall={controller.handleRejectToolCall}
            isLoading={controller.isLoading}
            editingMessage={controller.editingMessage}
          />

          <ChatInput
            value={controller.input}
            onChange={controller.setInput}
            onSend={controller.sendMessage}
            isLoading={controller.isLoading}
          />
        </>
      )}
    </Box>
  );
}
