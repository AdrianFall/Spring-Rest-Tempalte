package core.controller;

import config.PersistenceConfig;
import config.SpringConfig;
import config.WebConfig;
import config.WebSecurityConfig;
import core.TestBase;
import core.model.Account;
import core.model.PasswordResetToken;
import core.model.VerificationToken;
import core.service.AccountService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Adrian on 31/03/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceConfig.class, WebSecurityConfig.class, SpringConfig.class, WebConfig.class}, loader = AnnotationConfigWebContextLoader.class)
@Transactional
@WebAppConfiguration
public class AccountRegistrationControllerTest extends TestBase {

    public static final String VALID_EMAIL = "chupa@ca.bra";
    public static final String VALID_PASSWORD = "chup4c4br4";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MessageSource messageSource;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception{

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()) // will perform all of the initial setup we need to integrate Spring Security with Spring MVC Test
                .build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createAccountWithEmptyParamsTest() throws Exception {

        MvcResult mvcResult = mockMvc.perform(postForm("", "", ""))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("emailError"));
        Assert.assertNotNull(o.get("passwordError"));
        Assert.assertNotNull(o.get("confirmPasswordError"));
    }

    @Test
    public void registerValidAccountTest() throws Exception {

        MvcResult mvcResult = mockMvc.perform(postForm(VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains email
        Assert.assertEquals(VALID_EMAIL, o.get("email"));
    }

    @Test
    public void registerAccountAndAttemptLoginTest() throws Exception {
        // User should not be authenticated before email activation
        registerValidAccountTest();
        mockMvc.perform(get("/api/user").with(httpBasic(VALID_EMAIL, VALID_PASSWORD)))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // Should be unauthorized because account is not enabled yet
    }

    @Test
    public void loginActivatedAccountTest() throws Exception {
        registrationConfirmWithValidTokenTest();

        MvcResult mvcResult = mockMvc.perform(get("/api/user").with(httpBasic(VALID_EMAIL, VALID_PASSWORD)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("email"));
        Assert.assertEquals(o.get("email"), VALID_EMAIL);

    }

    @Test
    public void registerWithInvalidEmailPatternTest() throws Exception {
        // no @ sign
        String email = "adrianfalldevgmail.com";
        MvcResult mvcResult = mockMvc.perform(postForm(email, "n3wpassword", "n3wpassword"))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains emailError
        Assert.assertNotNull(o.get("emailError"));
        Assert.assertFalse(o.get("emailError").equals(""));
        Assert.assertEquals(o.get("emailError"), messageSource.getMessage("Pattern.email", null, Locale.ENGLISH));
    }

    @Test
    public void registerWithPasswordMismatchTest() throws Exception {
        String email = "adrianfalldev@gmail.com";
        String password = "003343adaja";
        String confirmPassword = "003343adajoo";
        MvcResult mvcResult = mockMvc.perform(postForm(email, password, confirmPassword))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains passwordErrors
        Assert.assertNotNull(o.get("passwordError"));
        Assert.assertFalse(o.get("passwordError").equals(""));
        Assert.assertNotNull(o.get("confirmPasswordError"));
        Assert.assertFalse(o.get("confirmPasswordError").equals(""));
    }

    @Test
    public void registerWithAlreadyTakenEmailTest() throws Exception {

        registerValidAccountTest();

        MvcResult mvcResult = mockMvc.perform(postForm(VALID_EMAIL, "n3wpassword", "n3wpassword"))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains email
        Assert.assertNotNull(o.get("emailError"));
        Assert.assertFalse(o.get("emailError").equals(""));
        Assert.assertEquals(o.get("emailError"), messageSource.getMessage("registration.emailExists", null, Locale.ENGLISH));
    }

    private MockHttpServletRequestBuilder postForm(String email, String password, String confirmPassword) {
        JSONObject json = new JSONObject();

        json.put("email", email);
        json.put("password", password);
        json.put("confirmPassword", confirmPassword);
        return post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString());
    }

    /* confirmRegistration tests */

    @Test
    public void registrationConfirmWithInvalidTokenTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/registrationConfirm")
                .param("token", "asdasdas0bv0"))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        // Expect the result to have json file with key: tokenError, value: verificationToken.getToken()
        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains tokenError
        Assert.assertNotNull(o.get("tokenError"));
        Assert.assertFalse(o.get("tokenError").equals(""));
        Assert.assertEquals(o.get("tokenError"), messageSource.getMessage("registration.invalidToken", null, Locale.ENGLISH));
    }

    @Test
    public void registrationConfirmWithExpiredTokenTest() throws Exception {
        // Requires creating new acc
        registerValidAccountTest(); // Creates acc
        // Ensure the account has been created
        Account acc = accountService.findAccount(VALID_EMAIL);
        Assert.assertNotNull(acc);
        // Ensure the account is not activated
        Assert.assertEquals(false, acc.isEnabled());

        // Find the current activation token for the acc
        VerificationToken verificationToken = accountService.findCurrentVerificationTokenOfAccountByEmail(VALID_EMAIL);
        Assert.assertNotNull(verificationToken);

        // Update verification token (to modify the expiration date)
        String newToken = UUID.randomUUID().toString();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);
        VerificationToken tokenToUpdate =  new VerificationToken(newToken, verificationToken.getAcc(), new Timestamp(cal.getTime().getTime()));
        System.out.println("tokenToUpdate expiry date = " + tokenToUpdate.getExpiryDate().toString());
        VerificationToken updatedToken = accountService.updateVerificationToken(tokenToUpdate, verificationToken.getAcc());

        // By the way lets ensure the accountService.findCurrentVerificationTokenOfAccountByEmail returns the updated token
        VerificationToken currentUpdatedToken = accountService.findCurrentVerificationTokenOfAccountByEmail(verificationToken.getAcc().getEmail());
        Assert.assertEquals(currentUpdatedToken.getToken(), updatedToken.getToken());
        Assert.assertEquals(currentUpdatedToken.getToken(), verificationToken.getToken());

        // Now lets continue with the main test - attempt registration confirm with expired token
        MvcResult mvcResult = mockMvc.perform(get("/registrationConfirm")
                .param("token", updatedToken.getToken()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        // Expect the result to have json file with key: tokenError, value: The link has expired, please activate your account with the new link that has been just sent to your email.
        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains tokenError
        Assert.assertNotNull(o.get("tokenError"));
        Assert.assertFalse(o.get("tokenError").equals(""));
        Assert.assertEquals(o.get("tokenError"), messageSource.getMessage("registration.tokenExpired", null, Locale.ENGLISH));
    }

    @Test
    public void registrationConfirmWithValidTokenTest() throws Exception {
        // Requires creating new acc
        registerValidAccountTest(); // Creates acc
        // Ensure the account has been created
        Account acc = accountService.findAccount(VALID_EMAIL);
        Assert.assertNotNull(acc);
        // Ensure the account is not activated
        Assert.assertEquals(false, acc.isEnabled());

        // Find the current activation token for the acc
        VerificationToken verificationToken = accountService.findCurrentVerificationTokenOfAccountByEmail(VALID_EMAIL);
        Assert.assertNotNull(verificationToken);

        // Obtain the token that would be normally available through email message
        System.out.println("Current verification token = " + verificationToken.getToken());
        Assert.assertNotNull(accountService.findVerificationToken(verificationToken.getToken()));
        JSONObject json = new JSONObject();
        json.put("token", verificationToken.getToken());

        MvcResult mvcResult = mockMvc.perform(get("/registrationConfirm")
                .param("token", verificationToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Expect the result to have json file with key: token, value: verificationToken.getToken()
        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains token
        Assert.assertNotNull(o.get("token"));
        Assert.assertFalse(o.get("token").equals(""));
        Assert.assertEquals(o.get("token"), verificationToken.getToken());

        // and access protected resources with basic auth
        mvcResult = mockMvc.perform(get("/api/user").with(httpBasic(VALID_EMAIL, VALID_PASSWORD)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        o = getJSONFromMVCResult(mvcResult);
        Assert.assertNotNull(o);
        // Make sure the response body contains email
        Assert.assertEquals(o.get("email"), VALID_EMAIL);

        // and finally make sure that performing get(/registrationConfirm) returns 200 message: The email is already activated. & token: value
        mvcResult = mockMvc.perform(get("/registrationConfirm")
                .param("token", verificationToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Expect the result to have json file with key: token, value: verificationToken.getToken()
        o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        // Make sure the response body contains token
        Assert.assertNotNull(o.get("token"));
        Assert.assertFalse(o.get("token").equals(""));
        Assert.assertEquals(o.get("token"), verificationToken.getToken());
        Assert.assertNotNull(o.get("message"));
        Assert.assertEquals(o.get("message"), messageSource.getMessage("resend.email.alreadyActivated", null, Locale.ENGLISH));
    }

    @Test
    public void resendActivationEmailToDisabledAccountTest() throws Exception {
        // Requires creating new acc
        registerValidAccountTest(); // Creates acc
        // Ensure the account has been created
        Account acc = accountService.findAccount(VALID_EMAIL);
        Assert.assertNotNull(acc);
        // Ensure the account is not activated
        Assert.assertEquals(false, acc.isEnabled());

        // Find the current activation token for the acc (just to make sure it is a newly created account)
        VerificationToken verificationToken = accountService.findCurrentVerificationTokenOfAccountByEmail(VALID_EMAIL);
        Assert.assertNotNull(verificationToken);

        JSONObject json = new JSONObject();
        json.put("email", acc.getEmail());
        // Expect status 200
        MvcResult mvcResult = mockMvc.perform(post("/resendConfirmationEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Expect message = messageSource.getMessage("resend.email.success", null, request.getLocale())
        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("message"));
        Assert.assertFalse(o.get("message").equals(""));
        Assert.assertEquals(o.get("message"), messageSource.getMessage("resend.email.success", null, Locale.ENGLISH));
    }

    @Test
    public void resendActivationEmailToActiveAccountTest() throws Exception {
        // Expect status 200
        // Expect message = messageSource.getMessage("resend.email.alreadyActivated", null, request.getLocale())

        registrationConfirmWithValidTokenTest(); // Creates the acc with VALID_EMAIL and activates it
        // Ensure the account is created & activated
        Account acc = accountService.findAccount(VALID_EMAIL);
        Assert.assertNotNull(acc);
        Assert.assertEquals(true, acc.isEnabled());

        JSONObject json = new JSONObject();
        json.put("email", acc.getEmail());

        MvcResult mvcResult = mockMvc.perform(post("/resendConfirmationEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("message"));
        Assert.assertFalse(o.get("message").equals(""));
        Assert.assertEquals(o.get("message"), messageSource.getMessage("resend.email.alreadyActivated", null, Locale.ENGLISH));


    }

    @Test
    public void resendActivationEmailToNotExistingAccountTest() throws Exception {
        // Expect status 406
        // Expect emailError = messageSource.getMessage("resend.email.doesNotExist", null, request.getLocale())

        JSONObject json = new JSONObject();
        json.put("email", "not@exis.ting");
        MvcResult mvcResult = mockMvc.perform(post("/resendConfirmationEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("emailError"));
        Assert.assertFalse(o.get("emailError").equals(""));
        Assert.assertEquals(o.get("emailError"), messageSource.getMessage("resend.email.doesNotExist", null, Locale.ENGLISH));
    }

    @Test
    public void resendActivationEmailWithInvalidEmailPatternTest() throws Exception {
        // Expect status 406
        // Expect emailError = messageSource.getMessage("Pattern.email", null, request.getLocale())
        JSONObject json = new JSONObject();
        json.put("email", "notvalidpattern");
        MvcResult mvcResult = mockMvc.perform(post("/resendConfirmationEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("emailError"));
        Assert.assertFalse(o.get("emailError").equals(""));
        Assert.assertEquals(o.get("emailError"), messageSource.getMessage("Pattern.email", null, Locale.ENGLISH));
    }

    /* Reset Password tests */
    @Test
    public void requestResetPasswordTest() throws Exception {
        //  both enabled and disabled accounts are permitted to change account

        // Expect status 200
        // Expect message = messageSource.getMessage("request.reset.password.success"......

        // Requires creating new acc
        registrationConfirmWithValidTokenTest(); // Creates acc and activates the account with token
        // Ensure the account has been created
        Account acc = accountService.findAccount(VALID_EMAIL);
        Assert.assertNotNull(acc);
        // Ensure the account is activated - otherwise the authentication test on get(/api/user) can't be performed
        Assert.assertEquals(true, acc.isEnabled());

        // Find the current reset token for the acc - and make sure it doesn't yet exist
        PasswordResetToken passwordResetToken = accountService.findCurrentPasswordResetTokenOfAccountByEmail(VALID_EMAIL);
        Assert.assertNull(passwordResetToken);


        JSONObject json = new JSONObject();
        json.put("email", VALID_EMAIL);
        // Expect status 200
        MvcResult mvcResult = mockMvc.perform(post("/requestResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("message"));
        Assert.assertFalse(o.get("message").equals(""));
        Assert.assertEquals(o.get("message"), messageSource.getMessage("request.reset.password.success", null, Locale.ENGLISH));

        // Obtain the token that would be normally available through email message
        passwordResetToken = accountService.findCurrentPasswordResetTokenOfAccountByEmail(VALID_EMAIL);
        Assert.assertNotNull(passwordResetToken);

    }

    @Test
    public void requestResetPasswordWithInvalidEmailPatternTest() throws Exception {
        // Expect status 406
        // Expect error = messageSource.getMessage("Pattern.email", null, Locale.ENGLISH)

        JSONObject json = new JSONObject();
        json.put("email", "wrongemailpattern");
        MvcResult mvcResult = mockMvc.perform(post("/requestResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("error"));
        Assert.assertFalse(o.get("error").equals(""));
        Assert.assertEquals(o.get("error"), messageSource.getMessage("Pattern.email", null, Locale.ENGLISH));

        // Ensure that no token was created for that email
        PasswordResetToken passwordResetToken = accountService.findCurrentPasswordResetTokenOfAccountByEmail("rongemailpattern");
        Assert.assertNull(passwordResetToken);
    }

    @Test
    public void requestResetPasswordWithNotExistingEmailTest() throws Exception {
        // Expect status 406
        // Expect message = "request.reset.password.doesNotExist"
        JSONObject json = new JSONObject();
        json.put("email", "not@exist.ing");
        MvcResult mvcResult = mockMvc.perform(post("/requestResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("error"));
        Assert.assertFalse(o.get("error").equals(""));
        Assert.assertEquals(o.get("error"), messageSource.getMessage("request.reset.password.doesNotExist", null, Locale.ENGLISH));

        // Ensure that no token was created for that email
        PasswordResetToken passwordResetToken = accountService.findCurrentPasswordResetTokenOfAccountByEmail("rongemailpattern");
        Assert.assertNull(passwordResetToken);
    }

    @Test
    public void resetPasswordTest() throws Exception {
        // Expect status 200
        // Expect "reset.password.success"
        // PasswordResetToken to be associated with an account
        requestResetPasswordTest();  // for account with email : VALID_EMAIL

        PasswordResetToken passwordResetToken = accountService.findCurrentPasswordResetTokenOfAccountByEmail(VALID_EMAIL);
        Assert.assertNotNull(passwordResetToken);

        // Ensure http basic does still work for current password
        mockMvc.perform(get("/api/user").with(httpBasic(VALID_EMAIL, VALID_PASSWORD)))
                .andDo(print())
                .andExpect(status().isOk());

        JSONObject json = new JSONObject();
        json.put("token", passwordResetToken.getToken());
        json.put("password", "newvalidpassword");
        json.put("confirmPassword", "newvalidpassword");
        MvcResult mvcResult = mockMvc.perform(post("/resetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("message"));
        Assert.assertFalse(o.get("message").equals(""));
        Assert.assertTrue(o.get("message").toString().contains(messageSource.getMessage("reset.password.success", null, Locale.ENGLISH)));
        /*Assert.assertEquals(o.get("message"), messageSource.getMessage("reset.password.success", null, Locale.ENGLISH));*/

        // Ensure http basic works for new password
        mockMvc.perform(get("/api/user").with(httpBasic(VALID_EMAIL, "newvalidpassword")))
                .andDo(print())
                .andExpect(status().isOk());
        // Ensure http basic does not work for old password
        mockMvc.perform(get("/api/user").with(httpBasic(VALID_EMAIL, VALID_PASSWORD)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void resetPasswordMismatchingTest() throws Exception {
        // Expect status 406
        // Expect error = registration.passwordMismatch

        JSONObject json = new JSONObject();
        json.put("password", VALID_PASSWORD);
        json.put("confirmPassword", "notthesameone");
        MvcResult mvcResult = mockMvc.perform(post("/resetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("passwordError"));
        Assert.assertFalse(o.get("passwordError").equals(""));
        Assert.assertEquals(o.get("passwordError"), messageSource.getMessage("registration.passwordMismatch", null, Locale.ENGLISH));

        Assert.assertNotNull(o.get("confirmPasswordError"));
        Assert.assertFalse(o.get("confirmPasswordError").equals(""));
        Assert.assertEquals(o.get("confirmPasswordError"), messageSource.getMessage("registration.passwordMismatch", null, Locale.ENGLISH));

    }

    @Test
    public void resetPasswordWithIncorrectSizeTest() throws Exception {
        // Expect status 406
        // Expect "Size.password"
        JSONObject json = new JSONObject();
        json.put("password", "shor");
        json.put("confirmPassword", "shor");
        MvcResult mvcResult = mockMvc.perform(post("/resetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("passwordError"));
        Assert.assertFalse(o.get("passwordError").equals(""));
        Assert.assertEquals(o.get("passwordError"), messageSource.getMessage("Size.password", null, Locale.ENGLISH));

        Assert.assertNotNull(o.get("confirmPasswordError"));
        Assert.assertFalse(o.get("confirmPasswordError").equals(""));
        Assert.assertEquals(o.get("confirmPasswordError"), messageSource.getMessage("Size.password", null, Locale.ENGLISH));
    }

    @Test
    public void resetPasswordWithInvalidTokenTest() throws Exception {
        // Expect status 406
        // Expect "reset.password.token.invalid"

        requestResetPasswordTest();  // for account with email : VALID_EMAIL

        PasswordResetToken passwordResetToken = accountService.findCurrentPasswordResetTokenOfAccountByEmail(VALID_EMAIL);
        Assert.assertNotNull(passwordResetToken);

        JSONObject json = new JSONObject();
        json.put("token", passwordResetToken.getToken() + "appended");
        json.put("password", "newvalidpassword");
        json.put("confirmPassword", "newvalidpassword");
        MvcResult mvcResult = mockMvc.perform(post("/resetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        JSONObject o = getJSONFromMVCResult(mvcResult);
        System.out.println(o.toString());
        Assert.assertNotNull(o);
        Assert.assertNotNull(o.get("error"));
        Assert.assertFalse(o.get("error").equals(""));
        Assert.assertEquals(o.get("error"), messageSource.getMessage("reset.password.token.invalid", null, Locale.ENGLISH));

    }

}