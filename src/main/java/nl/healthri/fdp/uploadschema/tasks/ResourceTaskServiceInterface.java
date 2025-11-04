package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;

import java.util.List;

public interface ResourceTaskServiceInterface {
    List<ResourceTask> createTasks();
    List<ResourceTask> createParentTasks();
}
