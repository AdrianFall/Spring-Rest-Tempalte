package core.model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Adrian on 09/05/2015.
 */
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String password;
    private String email;
    private boolean enabled;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "accounts_roles",
            joinColumns = {@JoinColumn(name = "account_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private Set<Role> accRoles;

    /*@ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "accounts_social_providers",
            joinColumns = {@JoinColumn(name = "account_id")},
            inverseJoinColumns = {@JoinColumn(name = "provider_name")})
    private Set<SocialProvider> accSocialProviders;*/

    /*@Enumerated(EnumType.STRING)
    @Column(name = "sign_in_provider", length = 20)
    private SocialMediaEnum signInProvider;*/

    public Account(String email, String password) {
        this.password = password;
        this.email = email;
    }

    public Account() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public Set<Role> getAccRoles() {
        return accRoles;
    }

    public void setAccRoles(Set<Role> accRoles) {
        this.accRoles = accRoles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /*public Set<SocialProvider> getAccSocialProviders() { return accSocialProviders; }*/
    /*public void setAccSocialProviders(Set<SocialProvider> accSocialProviders) { this.accSocialProviders = accSocialProviders; }*/
}
