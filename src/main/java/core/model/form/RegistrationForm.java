package core.model.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Created by Adrian on 10/05/2015.
 */
/*@PasswordMatches()*/
public class RegistrationForm {

    @NotNull
    @Pattern(regexp = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$")
    private String email;

    @Size(min = 5, max = 120, message = "{Size.password}")
    @NotNull
    private String password;

    @Size(min = 5, max = 120, message = "{Size.confirmPassword}")
    @NotNull
    private String confirmPassword;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

}
