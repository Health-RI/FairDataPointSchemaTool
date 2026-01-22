package nl.healthri.fdp.uploadschema.dto.auth;

public record LoginResponseDto(String token) {
    public String asHeaderString() {
        return "Bearer " + token;
    }
}
