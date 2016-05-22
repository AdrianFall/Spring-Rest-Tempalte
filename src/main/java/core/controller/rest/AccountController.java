package core.controller.rest;

import core.service.AccountService;
import core.service.EmailService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

    @RequestMapping(value = "/api/user", method = RequestMethod.GET, produces = "application/json")
    public String user(Principal user) {
        System.out.println("/user");
        if (user != null)
            System.out.println("User is authenticated : " + user.getName());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", (user != null) ? user.getName() : "");
        return jsonObject.toString();
    }

    @RequestMapping(value = "/deleteAll", method = RequestMethod.GET, produces = "application/json")
    public String deleteAllAccounts() {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("deleted", (accountService.deleteAllAccounts()) ? true : false);
        return jsonResponse.toString();
    }
}
