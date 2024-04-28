package nz.geek.jack.task.adapter.persistence;

import nz.geek.jack.libs.ddd.persistence.InMemoryAggregateStore;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskId;
import nz.geek.jack.task.domain.TaskRepository;
import org.springframework.stereotype.Repository;

@Repository
public class HashMapTaskRepository extends InMemoryAggregateStore<TaskId, Task>
    implements TaskRepository {}
