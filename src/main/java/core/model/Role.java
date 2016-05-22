package core.model;

import javax.persistence.*;

@Entity
@Table(name = "role",
        uniqueConstraints = @UniqueConstraint(
                columnNames = { "role"}))
public class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String role;

   /* @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;*/

  /*  @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Set<AccountsRoles> accRoles;*/

    public Role(String role) {
        this.role = role;
    }

    public Role() {

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }




}