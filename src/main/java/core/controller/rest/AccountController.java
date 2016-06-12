package core.controller.rest;

import core.model.dto.SocialAccessTokenDTO;
import core.service.AccountService;
import core.service.EmailService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Created by Adrian on 30/03/2016.
 */
@RestController
public class AccountController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    EmailService emailService;

    @Autowired
    AccountService accountService;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;


    @RequestMapping(value = "/api/user", method = RequestMethod.GET, produces = "application/json")
    public String user(Principal user) {
        System.out.println("/user");
        if (user != null)
            System.out.println("User is authenticated : " + user.getName());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", (user != null) ? user.getName() : "");
        return jsonObject.toString();
    }

    @RequestMapping(value = "api/user/facebook/login", method = RequestMethod.POST, produces = "application/json")
    public String loginSocialProvider(@PathVariable(value="providerName") final String providerName, @RequestBody SocialAccessTokenDTO socialAccessTokenDTO) {
        JSONObject jsonResponse = new JSONObject();
        OAuth2ConnectionFactory<?> connectionFactory = (OAuth2ConnectionFactory<?>) connectionFactoryLocator.getConnectionFactory(providerName);
        Connection<?> connection = connectionFactory.createConnection(new AccessGrant(socialAccessTokenDTO.getAccessToken()));
        /**//*AuthenticatedUserToken token = userService.socialLogin(connection);
        return getLoginResponse(token);*/
        return jsonResponse.toString();
    }

    /*@RequestMapping(value = "/deleteAll", method = RequestMethod.GET, produces = "application/json")
    public String deleteAllAccounts() {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("deleted", (accountService.deleteAllAccounts()) ? true : false);
        return jsonResponse.toString();
    }*/
}
