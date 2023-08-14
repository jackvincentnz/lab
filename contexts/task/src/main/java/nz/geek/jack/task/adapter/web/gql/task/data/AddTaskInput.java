package nz.geek.jack.task.adapter.web.gql.task.data;

import nz.geek.jack.task.application.task.data.AddTaskCommand;

public record AddTaskInput(String title) implements AddTaskCommand {}
