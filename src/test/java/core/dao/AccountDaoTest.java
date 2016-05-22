package core.dao;

import config.PersistenceConfig;
import config.SpringConfig;
import config.WebSecurityConfig;
import core.model.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Adrian on 30/03/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PersistenceConfig.class, WebSecurityConfig.class, SpringConfig.class}, loader = AnnotationConfigContextLoader.class)
@Transactional
public class AccountDaoTest {

    @Autowired
    private AccountDao repo;

    // First account
    private Account firstAcc;

    @Before
    @Rollback(true)
    public void setup() throws  Exception{
        firstAcc = new Account();
        firstAcc.setEmail("xa@xa");
        firstAcc.setPassword("superhard");
        repo.createAccount(firstAcc);
    }

    @Test
    public void accountNotActivatedTest() throws Exception {
        assertEquals(false, repo.findAccountByEmail("xa@xa").isEnabled());
    }

    @Test
    public void findTheFirstAccountById() throws Exception {
        assertNotNull(repo.findAccount(firstAcc.getId()));
    }

    @Test
    public void findAccountByEmail() throws Exception {
        assertNotNull(repo.findAccountByEmail(firstAcc.getEmail()));
    }

    @Test
    public void updateAccount() throws Exception {
        // Modify the first acc
        firstAcc.setEmail("re@re.re");
        assertNotNull(repo.updateAccount(firstAcc));
        assertEquals("re@re.re", firstAcc.getEmail());
    }

}
