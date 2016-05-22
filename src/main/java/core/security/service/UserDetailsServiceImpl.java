package core.security.service;

import core.dao.AccountDao;
import core.model.Account;
import core.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Adrian on 30/03/2016.
 */
@Service("accountDetailsService")
public class UserDetailsServiceImpl implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private AccountDao accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Finding by email: " + (email.equals("") ? "NULL" : email));
        if (email.equals(""))
            return null;
        Account acc = accountRepository.findAccountByEmail(email);
        if (acc == null) {
            System.out.println("UserDetailsServiceImpl - No username was found.");
            throw new UsernameNotFoundException("Email not found");
        }

       /* AccountUserDetails principal = AccountUserDetails.getBuilder()
                .id(acc.getId())
                .password(acc.getPassword())
                .roles(acc.getAccRoles())
                .socialSignInProviders(acc.getAccSocialProviders())
                .username(acc.getEmail())
                .enabled(acc.isEnabled())
                .build();

        return principal;*/

        List<GrantedAuthority> authorities =
                buildUserAuthority(acc.getAccRoles());

        return buildUserForAuthentication(acc, authorities);

    }

    private User buildUserForAuthentication(Account acc,
                                            List<GrantedAuthority> authorities) {
        return new User(acc.getEmail(), acc.getPassword(),
                acc.isEnabled(), true, true, true, authorities);
    }

    private List<GrantedAuthority> buildUserAuthority(Set<Role> userRoles) {

        Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();

        // Build user's authorities
        for (Role userRole : userRoles) {
            setAuths.add(new SimpleGrantedAuthority(userRole.getRole()));
        }

        List<GrantedAuthority> Result = new ArrayList<GrantedAuthority>(setAuths);

        return Result;
    }



}

