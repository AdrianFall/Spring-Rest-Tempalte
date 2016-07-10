package filter.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import core.model.Account;
import core.model.dto.SocialRegistrationDTO;
import core.service.AccountService;
import core.service.exception.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.*;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.security.*;
import org.springframework.social.security.provider.SocialAuthenticationService;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Adrian on 09/07/2016.
 */
public class FacebookTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Value("${facebook.clientId}")
    private String access_token;

    private static final String providerId = "facebook";

    private SocialAuthenticationServiceLocator authServiceLocator;

    private UserIdSource userIdSource;

    private UsersConnectionRepository usersConnectionRepository;

    private SimpleUrlAuthenticationFailureHandler delegateAuthenticationFailureHandler;

    @Autowired
    private AccountService service;

    public FacebookTokenAuthenticationFilter(AuthenticationManager authManager, UserIdSource userIdSource,
                                             UsersConnectionRepository usersConnectionRepository,
                                             SocialAuthenticationServiceLocator authServiceLocator) {
        super("/");
        setAuthenticationManager(authManager);
        this.userIdSource = userIdSource;
        this.usersConnectionRepository = usersConnectionRepository;
        this.authServiceLocator = authServiceLocator;
        this.delegateAuthenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler();
        super.setAuthenticationFailureHandler(
                new SocialAuthenticationFailureHandler(delegateAuthenticationFailureHandler));
        SimpleUrlAuthenticationSuccessHandler sas = new SimpleUrlAuthenticationSuccessHandler();
        sas.setRedirectStrategy(new NoRedirectStrategy());
        super.setAuthenticationSuccessHandler(sas);
    }


    public UsersConnectionRepository getUsersConnectionRepository() {
        return usersConnectionRepository;
    }

    public ConnectionFactoryLocator getAuthServiceLocator() {
        return authServiceLocator;
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String path = ((HttpServletRequest) request).getRequestURI();
        if (request.getMethod().equals("OPTIONS") && request.getParameter("input_token") != null) {
//            setContinueChainBeforeSuccessfulAuthentication(true);
            return false;
        }
        else if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
            return false;
        else if (!path.startsWith("/social/"))
            return false;

        return true;
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String path = ((HttpServletRequest) request).getRequestURI();

        Authentication auth = null;
        Set<String> authProviders = authServiceLocator.registeredProviderIds(); /*registeredAuthenticationProviderIds();*/
        if (!authProviders.isEmpty() && authProviders.contains(providerId)) {
            SocialAuthenticationService<?> authService = authServiceLocator.getAuthenticationService(providerId); /*getAuthenticationService(providerId);*/
            auth = attemptAuthService(authService, request, response);
            if (auth == null) {
                throw new AuthenticationServiceException("authentication failed");
            }
        }
        return auth;
    }

 /*   @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println("FacebookTokenAuthFilter -> doFilter()");
    }*/

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }

    protected Connection<?> addConnection(SocialAuthenticationService<?> authService, String userId,
                                          ConnectionData data) {
        HashSet<String> userIdSet = new HashSet<String>();
        userIdSet.add(data.getProviderUserId());
        Set<String> connectedUserIds = usersConnectionRepository.findUserIdsConnectedTo(data.getProviderId(),
                userIdSet);
        if (connectedUserIds.contains(userId)) {
            // already connected
            return null;
        } else if (!authService.getConnectionCardinality().isMultiUserId() && !connectedUserIds.isEmpty()) {
            return null;
        }

        ConnectionRepository repo = usersConnectionRepository.createConnectionRepository(userId);

        if (!authService.getConnectionCardinality().isMultiProviderUserId()) {
            List<Connection<?>> connections = repo.findConnections(data.getProviderId());
            if (!connections.isEmpty()) {
                // TODO maybe throw an exception to allow UI feedback?
                return null;
            }
        }

        // add new connection
        Connection<?> connection = authService.getConnectionFactory().createConnection(data);
        connection.sync();
        repo.addConnection(connection);
        return connection;
    }

    private Authentication attemptAuthService(final SocialAuthenticationService<?> authService,
                                              final HttpServletRequest request, HttpServletResponse response)
            throws SocialAuthenticationRedirectException, AuthenticationException {
        if (request.getParameter("input_token") == null) {
            throw new SocialAuthenticationException("No token in the request");
        }
        URIBuilder builder = URIBuilder.fromUri(String.format("%s/debug_token", "https://graph.facebook.com"));
        builder.queryParam("access_token", "259908567717695|526b86e6c3e6f4c1139d0b82eaeedefa");
        builder.queryParam("input_token", request.getParameter("input_token"));
        URI uri = builder.build();
        RestTemplate restTemplate = new RestTemplate();

        JsonNode resp = null;
        try {
            resp = restTemplate.getForObject(uri, JsonNode.class);
        } catch (HttpClientErrorException e) {
            throw new SocialAuthenticationException("Error validating token");
        }
        Boolean isValid = resp.path("data").findValue("is_valid").asBoolean();
        if (!isValid)
            throw new SocialAuthenticationException("Token is not valid");

        AccessGrant accessGrant = new AccessGrant(request.getParameter("input_token"), null, null,
                resp.path("data").findValue("expires_at").longValue());

        Connection<?> connection = ((OAuth2ConnectionFactory<?>) authService.getConnectionFactory())
                .createConnection(accessGrant);
        SocialAuthenticationToken token = new SocialAuthenticationToken(connection, null);
        Assert.notNull(token.getConnection());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return doAuthentication(authService, request, token);
        } else {
            addConnection(authService, request, token);
            return null;
        }
    }

    private void addConnection(final SocialAuthenticationService<?> authService, HttpServletRequest request,
                               SocialAuthenticationToken token) {
        // already authenticated - add connection instead
        String userId = userIdSource.getUserId();
        Object principal = token.getPrincipal();
        if (userId == null || !(principal instanceof ConnectionData))
            return;

        addConnection(authService, userId, (ConnectionData) principal);

    }

    private Authentication doAuthentication(SocialAuthenticationService<?> authService, HttpServletRequest request,
                                            SocialAuthenticationToken token) {
        try {
            if (!authService.getConnectionCardinality().isAuthenticatePossible())
                return null;
            token.setDetails(authenticationDetailsSource.buildDetails(request));
            Authentication success = getAuthenticationManager().authenticate(token);
            Assert.isInstanceOf(SocialUserDetails.class, success.getPrincipal(), "unexpected principle type");
            updateConnections(authService, token, success);
            return success;
        } catch (BadCredentialsException e) {

            SocialRegistrationDTO registration = createSocialRegistrationDTO(token.getConnection());
            Account registered;
            try {
                registered = service.registerSocialAccount(registration);
            } catch (EmailExistsException e1) {
                throw new SocialAuthenticationException("An email address was found in the database.");
            }
            ConnectionRepository repo = usersConnectionRepository.createConnectionRepository(registered.getEmail());
            repo.addConnection(token.getConnection());
            Authentication success = getAuthenticationManager().authenticate(token);
            return success;

        }
    }

    private void updateConnections(SocialAuthenticationService<?> authService, SocialAuthenticationToken token,
                                   Authentication success) {

        String userId = ((SocialUserDetails) success.getPrincipal()).getUserId();
        Connection<?> connection = token.getConnection();
        ConnectionRepository repo = getUsersConnectionRepository().createConnectionRepository(userId);
        repo.updateConnection(connection);

    }

    private SocialRegistrationDTO createSocialRegistrationDTO(Connection<?> connection) {
        SocialRegistrationDTO dto = new SocialRegistrationDTO();

        if (connection != null) {
            UserProfile socialMediaProfile = connection.fetchUserProfile();
            dto.setEmail(socialMediaProfile.getEmail());
            dto.setFirstName(socialMediaProfile.getFirstName());
            dto.setLastName(socialMediaProfile.getLastName());
            ConnectionKey providerKey = connection.getKey();
            dto.setSignInProvider(Account.SocialMediaService.valueOf(providerKey.getProviderId().toUpperCase()));
        }

        return dto;
    }


}
