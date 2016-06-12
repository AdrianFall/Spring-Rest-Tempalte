package core.model.dto;

/**
 * Created by Adrian on 12/06/2016.
 */
public class SocialAccessTokenDTO {
    private String accessToken;

    // Constructors

    public SocialAccessTokenDTO(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getters & Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
