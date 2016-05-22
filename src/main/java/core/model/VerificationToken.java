package core.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by Adrian on 13/05/2015.
 */
@Entity(name = "verification_token")
public class VerificationToken {

    // 24 h
    private static final int DEFAULT_EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String token;

    @Column(name = "expiry_date")
    private Timestamp expiryDate;

    @OneToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "account_id")
    private Account acc;


    public VerificationToken(String token, Account acc) {
        this.token = token;
        this.acc = acc;
        expiryDate = calculateExpiryDate(DEFAULT_EXPIRATION);
    }

    public VerificationToken(String token, Account acc, Timestamp expiryDate) {
        this.token = token;
        this.acc = acc;
        this.expiryDate = expiryDate;
    }

    public VerificationToken() {

    }

    private Timestamp calculateExpiryDate(int expiryTimeMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryTimeMinutes);
        return new Timestamp(cal.getTime().getTime());
    }

    public Account getAcc() {
        return acc;
    }

    public void setAcc(Account acc) {
        this.acc = acc;
    }


    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }
}
