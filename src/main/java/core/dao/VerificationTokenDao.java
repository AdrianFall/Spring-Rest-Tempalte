package core.dao;

import core.model.Account;
import core.model.VerificationToken;

/**
 * Created by Adrian on 14/05/2015.
 */
public interface VerificationTokenDao {
    public VerificationToken createVerificationToken(VerificationToken token);
    public VerificationToken findVerificationToken(String verificationToken);
    public VerificationToken updateVerificationToken(VerificationToken token, Account acc);
    public VerificationToken findCurrentVerificationTokenOfAccount(Account acc);
}
