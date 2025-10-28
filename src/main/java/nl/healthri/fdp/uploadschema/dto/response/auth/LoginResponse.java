package nl.healthri.fdp.uploadschema.dto.response.auth;

public record LoginResponse(String token) {
    public String asHeaderString() {
        return "Bearer " + token;
    }
}
