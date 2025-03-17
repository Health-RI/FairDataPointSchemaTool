package nl.healthri.fdp.uploadschema.requestresponses;

public record FDPInfoResponse(String name, String version, String builtAt) {
    @Override

    public String toString() {
        return name + " (" + version + ") build: " + builtAt;
    }
}

