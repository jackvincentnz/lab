package lab.task.adapter.persistence;

import lab.libs.ddd.persistence.InMemoryAggregateStore;
import lab.task.domain.Task;
import lab.task.domain.TaskId;
import lab.task.domain.TaskRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTaskRepository extends InMemoryAggregateStore<TaskId, Task>
    implements TaskRepository {}
