package core.model.form;

import javax.validation.constraints.Size;

/**
 * Created by Adrian on 29/06/2015.
 */
public class ResetPasswordForm {
    @Size(min = 5, max = 120, message = "{validation.message.Size.password}")
    private String password;

    @Size(min = 5, max = 120)
    private String confirmPassword;

    private String token;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
