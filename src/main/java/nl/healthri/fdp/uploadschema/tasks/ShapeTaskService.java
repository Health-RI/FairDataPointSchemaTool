package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.integration.FdpClient;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.FileHandler;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeTaskService {
    public FdpService fdpService;
    public FileHandler fileHandler;
    public Properties properties;

    private static final Logger logger = LoggerFactory.getLogger(ShapeTaskService.class);

    public ShapeTaskService(FdpService fdpService, FileHandler fileHandler, Properties properties) {
        this.fdpService = fdpService;
        this.fileHandler = fileHandler;
        this.properties = properties;
    }


}

