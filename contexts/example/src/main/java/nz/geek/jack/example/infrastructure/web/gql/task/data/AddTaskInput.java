package nz.geek.jack.example.infrastructure.web.gql.task.data;

import nz.geek.jack.example.application.task.data.AddTaskCommand;

public record AddTaskInput(String title) implements AddTaskCommand {}
