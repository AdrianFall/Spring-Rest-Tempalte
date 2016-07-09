package core.security.service;

import core.dao.AccountDao;
import core.dao.impl.AccountDaoImpl;
import core.model.Account;
import core.security.social.FacebookTokenUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Created by Adrian on 09/07/2016.
 */
@Service
public class SimpleSocialUserDetailsService implements SocialUserDetailsService {

//    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSocialUserDetailsService.class);

    @Autowired
    private AccountDao accountDao;

    @Override
    public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {
//        LOGGER.debug("Loading user by user id: {}", userId);

        Account account = accountDao.findAccountByEmail(userId);
//        LOGGER.debug("Found user: {}", user);

        if (account == null) {
            throw new UsernameNotFoundException("No user found with username: " + userId);
        }

        FacebookTokenUserDetails principal = new FacebookTokenUserDetails(account.getEmail(), account.getPassword(),
                account.getAuthorities());
//        principal.setFirstName(account.getFirstName());
        principal.setId(account.getEmail());
//        principal.setLastName(account.getLastName());
        principal.setSocialSignInProvider(account.getSignInProvider());

//        LOGGER.debug("Found user details: {}", principal);

        return principal;
    }
}
