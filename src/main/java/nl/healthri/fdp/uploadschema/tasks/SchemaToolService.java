package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.FileHandler;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.RdfUtils;
import nl.healthri.fdp.uploadschema.utils.XlsToRdfUtils;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.function.Predicate.not;
import static nl.healthri.fdp.uploadschema.domain.enums.ShapeStatus.*;

public class SchemaToolService {
    public FdpService fdpService;
    public ResourceTaskService resourceTaskService;
    public ShapeTaskService shapeTaskService;
    public Properties properties;
    public FileHandler fileHandler;

    private static final Logger logger = LoggerFactory.getLogger(SchemaToolService.class);

    public SchemaToolService(FdpService fdpService, ResourceTaskService resourceTaskService, ShapeTaskService shapeTaskService, Properties properties, FileHandler fileHandler) {
        this.fdpService = fdpService;
        this.resourceTaskService = resourceTaskService;
        this.shapeTaskService = shapeTaskService;
        this.properties = properties;
        this.fileHandler = fileHandler;
    }

    public void createOrUpdateSchemas(boolean force) throws IOException {
        logger.info("Creating/updating schemas from tasks to FDP");

        List<ShapeTask> shapeTaskList = shapeTaskService.createTasks();
        shapeTaskList.forEach(task -> {
            switch (task.status()) {
                case INSERT -> {
                    fdpService.createSchema(task);
                    fdpService.releaseSchema(task);
                }
                case SAME -> {
                    if (force) {
                        fdpService.updateSchema(task);
                        fdpService.releaseSchema(task);
                        logger.info("Schema {} is updated, it was the same but force was set", task.shape);
                    } else {
                        logger.warn("Schema {} is not updated because it's still the same", task.shape);
                    }
                }
                case UPDATE -> {
                    fdpService.updateSchema(task);
                    fdpService.releaseSchema(task);
                }
            }
        });
    }

    public void convertTemplatesToShaclShapes() throws IOException {
        logger.info("reading templates from {} ", properties.templateDir);

        for (var e : XlsToRdfUtils.getTemplateFiles(properties.templateDir).entrySet()) {
            logger.info("  converting {} ", e.getValue());
            Path path = properties.getPiecesDir().resolve(e.getKey() + ".ttl");
            String shacl = XlsToRdfUtils.createShacl(e.getValue());
            Files.write(path, shacl.getBytes());
        }
    }

    public void mergeShapesToFdpSchemas() throws IOException {
        logger.info("Writing files: {}", properties.getFiles().keySet());

        for (var e : properties.getFiles(properties.getPiecesDir()).entrySet()) {
            Path path = properties.getFairDataPointDir().resolve(RdfUtils.schemaToFilename(e.getKey()));
            Model m = fileHandler.readFiles(e.getValue());
            fileHandler.safeModel(path, m);
        }
    }

    public void mergeShapesForValidation() throws IOException {
        logger.info("Merging files: {}", properties.getValidationDir());

        Path path = properties.getValidationDir().resolve("HRI-Datamodel-shapes.ttl");
        logger.info("Write validation file {} combining {} files", path, properties.getAllFiles().size());
        Model m = fileHandler.readFiles(new ArrayList<>(properties.getAllFiles()));
        fileHandler.safeModel(path, m);
    }

    public void addResourceDescriptions() {
        logger.info("Adding resource descriptions from resource tasks to FDP");

        List<ResourceTask> resourceTaskList = this.resourceTaskService.createTasks();
        resourceTaskList.stream().filter(ResourceTask::isInsert).forEach(this.fdpService::createResource);

        if (resourceTaskList.stream().noneMatch(not(ResourceTask::isInsert))) {
            logger.warn("Updating resources is not supported yet, but will try to add children if needed)");
        }

        //add the previous resources as child to parent.
        List<ResourceTask> resourceTasksParents = resourceTaskService.createParentTasks();
        resourceTasksParents.stream().filter(ResourceTask::hasChild).forEach(fdpService::updateResource);
    }

}
