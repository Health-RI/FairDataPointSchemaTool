package nl.healthri.fdp.uploadschema.dto.auth;

public record LoginResponse(String token) {
    public String asHeaderString() {
        return "Bearer " + token;
    }
}
