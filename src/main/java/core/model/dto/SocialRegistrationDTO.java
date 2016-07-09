package core.model.dto;

import core.model.Account;

/**
 * Created by Adrian on 09/07/2016.
 */
public class SocialRegistrationDTO {
    private String email;

    private String firstName;

    private String lastName;

    private String password;

    private String passwordVerification;

    private Account.SocialMediaService signInProvider;

    public SocialRegistrationDTO() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordVerification() {
        return passwordVerification;
    }

    public void setPasswordVerification(String passwordVerification) {
        this.passwordVerification = passwordVerification;
    }

    public Account.SocialMediaService getSignInProvider() {
        return signInProvider;
    }

    public void setSignInProvider(Account.SocialMediaService signInProvider) {
        this.signInProvider = signInProvider;
    }

}
