package transferdata.requestresponses;

public record TokenResponse(String token) {
    public String asHeaderString() {
        return "Bearer " + token;
    }
}
