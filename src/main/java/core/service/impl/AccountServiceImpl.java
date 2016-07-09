package core.service.impl;

import core.model.Account;
import core.model.PasswordResetToken;
import core.model.Test;
import core.model.VerificationToken;
import core.dao.AccountDao;
import core.dao.PasswordResetTokenDao;
import core.dao.VerificationTokenDao;
import core.model.dto.SocialRegistrationDTO;
import core.service.AccountService;
import core.service.exception.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by Adrian on 10/05/2015.
 */
@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private VerificationTokenDao tokenRepo;

    @Autowired
    private PasswordResetTokenDao passwordResetRepo;


    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Test getTest(Account acc) {
        return accountDao.getTest(acc);
    }

    @Override
    public Test setTest(Account acc, String testWord) {
        return accountDao.setTestWord(acc, testWord);
    }

    @Override
    public boolean deleteAllAccounts() {
        return accountDao.deleteAllAccounts();
    }

    @Override
    public Account createAccount(Account acc) throws EmailExistsException {
        if (accountDao.findAccountByEmail(acc.getEmail()) != null)
            throw new EmailExistsException("Email already exists.");


        // Hash the password
        acc.setPassword(passwordEncoder.encode(acc.getPassword()));

        return accountDao.createAccount(acc);
    }

    @Override
    public Account registerSocialAccount(SocialRegistrationDTO registration) throws EmailExistsException {
        if (accountDao.findAccountByEmail(registration.getEmail()) != null)
            throw new EmailExistsException("Email already exists");

        String encodedPassword = passwordEncoder.encode((registration.getPassword() != null ? registration.getPassword() : UUID.randomUUID().toString()));

        Account newAccount = new Account(registration.getEmail(), encodedPassword);

        return accountDao.createSocialAccount(newAccount);
    }

    @Override
    public Account updateAccount(Account acc) {
        return accountDao.updateAccount(acc);
    }

    @Override
    public VerificationToken createVerificationToken(Account acc, String token) {
        return tokenRepo.createVerificationToken(new VerificationToken(token, acc));
    }

    @Override
    public VerificationToken findVerificationToken(String token) {
        return tokenRepo.findVerificationToken(token);
    }

    @Override
    public VerificationToken findCurrentVerificationTokenOfAccountByEmail(String email) {
        Account acc = findAccount(email);
        return (acc == null) ? null : tokenRepo.findCurrentVerificationTokenOfAccount(acc);
    }

    @Override
    public Account findAccount(String email) {
        return accountDao.findAccountByEmail(email);
    }

    @Override
    public VerificationToken updateVerificationToken(VerificationToken newToken, Account acc) {
        return tokenRepo.updateVerificationToken(newToken, acc);
    }

    @Override
    public PasswordResetToken createPasswordResetToken(Account acc, String token) {
        return passwordResetRepo.createPasswordResetToken(new PasswordResetToken(token, acc));
    }

    @Override
    public PasswordResetToken findPasswordResetToken(String token) {
        return passwordResetRepo.findPasswordResetToken(token);
    }

    @Override
    public PasswordResetToken findCurrentPasswordResetTokenOfAccountByEmail(String email) {
        Account acc = findAccount(email);
        return (acc == null) ? null : passwordResetRepo.findCurrentPasswordResetTokenOfAccount(acc);
    }
}
