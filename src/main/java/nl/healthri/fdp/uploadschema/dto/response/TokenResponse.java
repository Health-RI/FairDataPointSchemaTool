package nl.healthri.fdp.uploadschema.dto.response;

public record TokenResponse(String token) {
    public String asHeaderString() {
        return "Bearer " + token;
    }
}
