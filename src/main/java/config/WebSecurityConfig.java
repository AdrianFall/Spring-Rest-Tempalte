package config;

import core.security.service.SimpleSocialUserDetailsService;
import filter.CORSFilter;
import filter.oauth.FacebookTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.password.PasswordEncoder;
import core.security.rest.authentication.RestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.facebook.security.FacebookAuthenticationService;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialAuthenticationServiceRegistry;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Created by Adrian on 29/03/2016.
 */


@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@PropertySource({ "classpath:properties/social.properties" })
@ComponentScan(basePackages = {"core.security"})
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String REALM = "Secured Environment";

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SimpleSocialUserDetailsService simpleSocialUserDetailsService;

    private AuthenticationNameUserIdSource userIdSource = new AuthenticationNameUserIdSource();

    @Autowired
    Environment environment;

    @Inject
    private DataSource dataSource;

    /*@Bean
    public SocialAuthenticationServiceLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        registry.addConnectionFactory(new FacebookConnectionFactory(environment.getProperty("facebook.clientId"),
                environment.getProperty("facebook.clientSecret")));
        return registry;
    }*/

    @Bean
    public SocialAuthenticationServiceLocator socialAuthenticationServiceLocator() {
        SocialAuthenticationServiceRegistry registry = new SocialAuthenticationServiceRegistry();

        FacebookAuthenticationService facebookAuthenticationService = new FacebookAuthenticationService(
                environment.getProperty("facebook.clientId"), environment
                .getProperty("facebook.clientSecret"));
        facebookAuthenticationService.setDefaultScope("email");
        registry.addAuthenticationService(facebookAuthenticationService);
//        registry.addConnectionFactory(new FacebookConnectionFactory(environment.getProperty("facebook.clientId"),
//                environment.getProperty("facebook.clientSecret")));
        return registry;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public CORSFilter corsFilter() {
        return new CORSFilter();
    }

    @Bean
    @Scope(value="request", proxyMode= ScopedProxyMode.INTERFACES)
    public ConnectionRepository connectionRepository(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Unable to get a ConnectionRepository: no user signed in");
        }
        return getUsersConnectionRepository().createConnectionRepository(authentication.getName());
    }

    public UsersConnectionRepository getUsersConnectionRepository() {
        return new JdbcUsersConnectionRepository(dataSource, socialAuthenticationServiceLocator(), Encryptors.noOpText());
    }

    @Bean
    public ConnectController connectController() {
        return new ConnectController(socialAuthenticationServiceLocator(),
                connectionRepository());
    }

    @Bean
    public SocialAuthenticationProvider socialAuthenticationProvider(){
        return new SocialAuthenticationProvider(getUsersConnectionRepository(), simpleSocialUserDetailsService);
    }
    @Bean
    public FacebookTokenAuthenticationFilter facebookTokenAuthenticationFilter() throws Exception {
        return new FacebookTokenAuthenticationFilter(authenticationManagerBean(), userIdSource, getUsersConnectionRepository(), socialAuthenticationServiceLocator());

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
        RestAuthenticationEntryPoint restAuthenticationEntryPoint = new RestAuthenticationEntryPoint();
        restAuthenticationEntryPoint.setRealmName(WebSecurityConfig.REALM);
        return restAuthenticationEntryPoint;
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
        auth.authenticationProvider(socialAuthenticationProvider());

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
// @formatter:off
        http
                .addFilterBefore(corsFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(basicAuthenticationEntryPoint())
                .and()
                .addFilterAfter(facebookTokenAuthenticationFilter(), CORSFilter.class)
                .sessionManagement().enableSessionUrlRewriting(false).sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/api/*").permitAll()
                .and()
                .authorizeRequests().antMatchers("/api/*").authenticated()
                .and()
                .authorizeRequests().antMatchers("/social/*").hasRole("REST_SOCIAL")
                        .and()
                        .httpBasic().authenticationEntryPoint(basicAuthenticationEntryPoint())
                .and().csrf().disable();
// @formatter:on
    }

}

