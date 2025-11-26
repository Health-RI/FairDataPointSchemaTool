package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.config.fdp.Settings;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.Resource.ResourceResponse;

import java.util.List;

public interface FdpServiceInterface {
    void authenticate(String username, String password);

    List<SchemaDataResponse> getAllSchemas();

    void createSchema(ShapeTask task);
    void updateSchema(ShapeTask task);
    void releaseSchema(ShapeTask task);

    List<ResourceResponse> getAllResources();
    void createResource(ResourceTask task);
    void updateResource(ResourceTask task);

    void updateSettings(Settings settings);
}
