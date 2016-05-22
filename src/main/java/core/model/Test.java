package core.model;

import javax.persistence.*;

/**
 * Created by Adrian on 04/07/2015.
        */
@Entity
@Table(name = "Test")
public class Test {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String test_word;

    @OneToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "account_id")
    private Account acc;

    public Account getAcc() {
        return acc;
    }

    public void setAcc(Account acc) {
        this.acc = acc;
    }

    public Long getId() {
        return id;
    }

    public String getTest_word() {
        return test_word;
    }

    public void setTest_word(String test_word) {
        this.test_word = test_word;
    }
}
