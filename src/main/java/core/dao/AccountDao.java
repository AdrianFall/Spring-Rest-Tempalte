package core.dao;
import core.model.Account;
import core.model.Test;

/**
 * Created by Adrian on 10/05/2015.
 */
public interface AccountDao {

    public Account findAccount(Long id);
    public Account findAccountByUsername(String username);
    public Account findAccountByEmail(String email);
    public Account createAccount(Account acc) throws RuntimeException;
    public Account updateAccount(Account acc);
    public Test getTest(Account acc);
    public Test setTestWord(Account acc, String testWord);
    public boolean deleteAllAccounts();
    public Account createSocialAccount(Account acc) throws RuntimeException;
}
