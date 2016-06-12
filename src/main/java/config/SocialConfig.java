package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

/**
 * Created by Adrian on 11/06/2016.
 */
@Configuration
public class SocialConfig {

    /*@Autowired
    ApplicationConfig config;*/

    @Autowired
    private Environment env;

  /*  @Autowired
    SocialUserRepository socialUserRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TextEncryptor textEncryptor;*/


    @Bean
    public ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        registry.addConnectionFactory(new FacebookConnectionFactory(
                env.getProperty("social.facebook.clientid"),
                env.getProperty("social.facebook.clientsecret")));
        return registry;
    }

  /*  @Bean
    public UsersConnectionRepository usersConnectionRepository() {
        JpaUsersConnectionRepository usersConnectionRepository = new JpaUsersConnectionRepository(socialUserRepository, userRepository,
                connectionFactoryLocator(), textEncryptor);

        return usersConnectionRepository;
    }*/

}
