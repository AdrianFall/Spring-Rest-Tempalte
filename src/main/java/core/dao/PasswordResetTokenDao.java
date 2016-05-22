package core.dao;

import core.model.Account;
import core.model.PasswordResetToken;

/**
 * Created by Adrian on 29/06/2015.
 */
public interface PasswordResetTokenDao {
    public PasswordResetToken createPasswordResetToken(PasswordResetToken token);
    public PasswordResetToken findPasswordResetToken(String token);
    public PasswordResetToken findCurrentPasswordResetTokenOfAccount(Account acc);
    public PasswordResetToken updatePasswordResetToken(PasswordResetToken newToken, Account acc);
}
