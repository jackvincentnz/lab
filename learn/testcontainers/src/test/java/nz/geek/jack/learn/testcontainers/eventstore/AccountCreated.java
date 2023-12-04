package nz.geek.jack.learn.testcontainers.eventstore;

import java.util.UUID;

public record AccountCreated(UUID id, String login) {}
