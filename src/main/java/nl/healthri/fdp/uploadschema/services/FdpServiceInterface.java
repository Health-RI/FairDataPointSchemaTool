package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.config.fdp.Settings;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.schema.SchemaDataResponseDto;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceResponseDto;

import java.util.List;

public interface FdpServiceInterface {
    void authenticate(String username, String password);

    List<SchemaDataResponseDto> getAllSchemas();

    void createSchema(ShapeTask task);
    void updateSchema(ShapeTask task);
    void releaseSchema(ShapeTask task);

    List<ResourceResponseDto> getAllResources();
    void createResource(ResourceTask task);
    void updateResource(ResourceTask task);

    void updateSettings(Settings settings);
}
