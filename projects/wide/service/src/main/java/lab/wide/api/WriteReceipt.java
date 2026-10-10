package lab.wide.api;

import java.util.UUID;

/** Successful MCP writes acknowledge the saved (or unlinked) identity without echoing content. */
public record WriteReceipt(UUID id) {}
