package core.security.social;

import core.model.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.social.security.SocialUser;

import java.util.Collection;

/**
 * Created by Adrian on 09/07/2016.
 */
public class FacebookTokenUserDetails extends SocialUser {

    private String id;

    private String firstName;

    private String lastName;

    private Account.SocialMediaService socialSignInProvider;

    public FacebookTokenUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username,  password == null ? "SocialUser" : password, authorities);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Account.SocialMediaService getSocialSignInProvider() {
        return socialSignInProvider;
    }

    public void setSocialSignInProvider(Account.SocialMediaService socialSignInProvider) {
        this.socialSignInProvider = socialSignInProvider;
    }
}
