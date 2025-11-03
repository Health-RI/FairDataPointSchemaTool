package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.integration.FdpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceTaskService {
    public FdpService fdpService;

    private static final Logger logger = LoggerFactory.getLogger(nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask.class);

    public ResourceTaskService(FdpService fdpService) {
            this.fdpService = fdpService;
    }


}
