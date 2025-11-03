package nl.healthri.fdp.uploadschema.domain;

public class ResourceTask {
    public final String resource;
    public String UUID;
    public String shapeUUUID;
    public String childUUuid;
    public String childRelationIri;
    public String childName;

    public ResourceTask(String resource, String uuid, String shapeUUUID, String childUUuid, String childRelationIri, String childName){
        this.resource = resource;
        this.UUID = uuid;
        this.shapeUUUID = shapeUUUID;
        this.childUUuid = childUUuid;
        this.childRelationIri = childRelationIri;
        this.childName = childName;

    }

    public Validate(){

    }
}
