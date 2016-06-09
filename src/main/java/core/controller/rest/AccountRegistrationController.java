package core.controller.rest;

import core.model.Account;
import core.model.PasswordResetToken;
import core.model.VerificationToken;
import core.model.form.RegistrationForm;
import core.model.form.RequestResetPasswordForm;
import core.model.form.ResendEmailForm;
import core.model.form.ResetPasswordForm;
import core.service.AccountService;
import core.service.EmailService;
import core.service.event.OnRegistrationCompleteEvent;
import core.service.event.OnResendEmailEvent;
import core.service.event.OnResetPasswordEvent;
import core.service.exception.EmailExistsException;
import core.service.exception.EmailNotSentException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;

/**
 * Created by Adrian on 31/03/2016.
 */
@RestController
public class AccountRegistrationController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    EmailService emailService;

    @Autowired
    AccountService accountService;

    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping(value = "/requestResetPassword", method = RequestMethod.POST)
    public ResponseEntity<String> requestResetPassword(@Valid @RequestBody RequestResetPasswordForm requestResetPasswordForm,
                                                       BindingResult bResult, HttpServletRequest request) {

        JSONObject responseJson = new JSONObject();

        if (bResult.getFieldError("email") != null) {
            if (bResult.getFieldError("email").getCode().equals("Pattern"))
                responseJson.put("error", messageSource.getMessage("Pattern.email", null, request.getLocale()));
            else
                responseJson.put("error", bResult.getFieldError("email").getCode());
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }

        // Check if the account exists
        Account acc = accountService.findAccount(requestResetPasswordForm.getEmail());
        if (acc == null) {
            responseJson.put("error", messageSource.getMessage("request.reset.password.doesNotExist", null, request.getLocale()));
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }

        String appUrl = request.getRequestURL().toString().split("/requestResetPassword")[0];

        emailService.sendResetPasswordEmail(new OnResetPasswordEvent(acc, request.getLocale(), appUrl));

        // Send the reset password email
        responseJson.put("message", messageSource.getMessage("request.reset.password.success", null, request.getLocale()));
        return ResponseEntity.status(200).body(responseJson.toJSONString());

    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public ResponseEntity<String> getResetPassword(@RequestParam("token") String token, @RequestParam String email, HttpServletRequest request) {
        JSONObject jsonResponse = new JSONObject();
        PasswordResetToken passwordResetToken = accountService.findPasswordResetToken(token);
        if (passwordResetToken == null) {
            jsonResponse.put("error", messageSource.getMessage("reset.password.token.invalid", null, request.getLocale()));
            return ResponseEntity.status(406).body(jsonResponse.toJSONString());
        }

        Account acc = passwordResetToken.getAcc();
        // Check if expired
        Calendar cal = Calendar.getInstance();
        Long timeDiff = passwordResetToken.getExpiryDate().getTime() - cal.getTime().getTime();
        if (timeDiff <= 0) {
            jsonResponse.put("error", messageSource.getMessage("reset.password.token.expired", null, request.getLocale()));
            return ResponseEntity.status(406).body(jsonResponse.toJSONString());
        }

        /*// Proceed to authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(acc, null, userDetailsService.loadUserByUsername(acc.getEmail()).getAuthorities());
        System.out.println("Authorities of " + acc.getEmail() + " are : " + auth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);*/
        jsonResponse.put("message", messageSource.getMessage("reset.password.token.valid", null, request.getLocale()));
        jsonResponse.put("token", token);
        return ResponseEntity.status(200).body(jsonResponse.toJSONString());
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordForm resetPasswordForm,
                                                BindingResult bResult, HttpServletRequest request, WebRequest webRequest) {

        JSONObject responseJson = new JSONObject();

        if (resetPasswordForm.getPassword() == null) {
            bResult.rejectValue("password", messageSource.getMessage("reset.password.password.null", null, request.getLocale()));
        } else if (resetPasswordForm.getConfirmPassword() == null) {
            bResult.rejectValue("confirmPassword", messageSource.getMessage("reset.password.password.null", null, request.getLocale()));
        } else if (!resetPasswordForm.getPassword().equals(resetPasswordForm.getConfirmPassword())) {
            bResult.rejectValue("password", messageSource.getMessage("registration.passwordMismatch", null, webRequest.getLocale()));
            bResult.rejectValue("confirmPassword", messageSource.getMessage("registration.passwordMismatch", null, webRequest.getLocale()));
            System.out.println("Password match validation - failed.");
        }

        if (bResult.hasErrors())
            return processRegistrationBindingResultErrors(bResult, webRequest);

        // Find the token from resetPasswordForm
        PasswordResetToken passwordResetToken = accountService.findPasswordResetToken(resetPasswordForm.getToken());
        if (passwordResetToken == null || passwordResetToken.getAcc() == null) {
            responseJson.put("error", messageSource.getMessage("reset.password.token.invalid", null, request.getLocale()));
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }

        // Check if the provided email corresponds to the one associated with passwordResetToken
        // nevermind that check, lets allow users to change password just through having a valid token
        /*if (resetPasswordForm.getEmail() == null || !resetPasswordForm.getEmail().equals(passwordResetToken.getAcc().getEmail())) {
            responseJson.put("error", messageSource.getMessage("reset.password.error.email", null, request.getLocale()));
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }*/

        // Check if expired
        Calendar cal = Calendar.getInstance();
        Long timeDiff = passwordResetToken.getExpiryDate().getTime() - cal.getTime().getTime();
        if (timeDiff <= 0) {
            responseJson.put("error", messageSource.getMessage("reset.password.token.expired", null, request.getLocale()));
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }

        Account acc = passwordResetToken.getAcc();
        /* Attempt to reset the password */
        acc.setPassword(accountService.encodePassword(resetPasswordForm.getPassword()));
        if (accountService.updateAccount(acc) != null) {
            responseJson.put("message", messageSource.getMessage("reset.password.success", null, request.getLocale()));
            return ResponseEntity.status(200).body(responseJson.toJSONString());
        } else {
            responseJson.put("error", messageSource.getMessage("reset.password.error", null, request.getLocale()));
            return ResponseEntity.status(500).body(responseJson.toJSONString());
        }
    }


    @RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
    public ResponseEntity<String> registrationConfirm(@RequestParam("token") String token, HttpServletRequest request) {
        JSONObject responseJson = new JSONObject();

        // Obtain verification token
        VerificationToken verificationToken = accountService.findVerificationToken(token);
        if (verificationToken == null) {
            // No such token
            String tokenErrorMessage = messageSource.getMessage("registration.invalidToken", null, request.getLocale());
            responseJson.put("tokenError", tokenErrorMessage);
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }

        // Obtain the account associated to the verification token
        Account acc = verificationToken.getAcc();

        if (acc.isEnabled()) {
            responseJson.put("message", messageSource.getMessage("resend.email.alreadyActivated", null, request.getLocale()));
            responseJson.put("token", verificationToken.getToken());
            return ResponseEntity.status(200).body(responseJson.toJSONString());
        }

        // Ensure token is not expired
        Calendar cal = Calendar.getInstance();
        Long timeDiff = verificationToken.getExpiryDate().getTime() - cal.getTime().getTime();
        if (timeDiff <= 0) {
            responseJson.put("tokenError", messageSource.getMessage("registration.tokenExpired", null, request.getLocale()));
            String appUrl = request.getRequestURL().toString().split("/registrationConfirm")[0];
            // Send email
            try {
                emailService.sendConfirmationEmail(new OnRegistrationCompleteEvent(verificationToken.getAcc(), request.getLocale(), appUrl));
            } catch (EmailNotSentException e) {
                e.printStackTrace();
            }
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }

        // Activate account
        acc.setEnabled(true);
        Account updatedAccount = accountService.updateAccount(acc);
        if (updatedAccount == null) {
            responseJson.put("error", messageSource.getMessage("registration.couldNotBeActivated", null, request.getLocale()));
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }

        responseJson.put("message", messageSource.getMessage("registration.activated", null, request.getLocale()));
        responseJson.put("token", verificationToken.getToken());

        return ResponseEntity.status(200).body(responseJson.toJSONString());
    }

    @RequestMapping(value = "/resendConfirmationEmail", method = RequestMethod.POST)
    public ResponseEntity<String> resendConfirmationEmail(@Valid @RequestBody ResendEmailForm resendEmailForm, BindingResult bResult, HttpServletRequest request) {
        JSONObject responseJson = new JSONObject();
        if (bResult.getFieldError("email") != null) {
            if (bResult.getFieldError("email").getCode().equals("Pattern"))
                responseJson.put("emailError", messageSource.getMessage("Pattern.email", null, request.getLocale()));
            else
                responseJson.put("emailError", bResult.getFieldError("email").getCode());
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        }
        Account acc = accountService.findAccount(resendEmailForm.getEmail());
        if (acc == null) {
            responseJson.put("emailError", messageSource.getMessage("resend.email.doesNotExist", null, request.getLocale()));
            return ResponseEntity.status(406).body(responseJson.toJSONString());
        } else if (acc.isEnabled()) {
            responseJson.put("message", messageSource.getMessage("resend.email.alreadyActivated", null, request.getLocale()));
            return ResponseEntity.status(200).body(responseJson.toJSONString());
        }

        // No error & account is disabled
        String appUrl = request.getRequestURL().toString().split("/resendConfirmationEmail")[0];
        // Attempt to resend the activation email
        emailService.resendConfirmationEmail(new OnResendEmailEvent(acc, request.getLocale(), appUrl));

        responseJson.put("message", messageSource.getMessage("resend.email.success", null, request.getLocale()));
        return ResponseEntity.status(200).body(responseJson.toJSONString());
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegistrationForm registrationForm, BindingResult bResult, HttpServletRequest request, WebRequest webRequest) {

        JSONObject responseJson = new JSONObject();

        if (!registrationForm.getPassword().equals(registrationForm.getConfirmPassword())) {
            bResult.rejectValue("password", messageSource.getMessage("registration.passwordMismatch", null, webRequest.getLocale()));
            bResult.rejectValue("confirmPassword", messageSource.getMessage("registration.passwordMismatch", null, webRequest.getLocale()));
            System.out.println("Password match validation - failed.");
        }

        if (bResult.hasErrors())
            return processRegistrationBindingResultErrors(bResult, webRequest);

        /* Attempt to create the account */
        Account newAcc = new Account();
        newAcc.setEmail(registrationForm.getEmail());
        newAcc.setPassword(registrationForm.getPassword());

        try {// Attempt acc creation
            Account acc = accountService.createAccount(newAcc);
            if (acc != null) {
                // Acc created
                String appUrl = request.getRequestURL().toString().split("/register")[0];
                System.out.println("APP URL = " + appUrl);

                /*ProviderSignInUtils.handlePostSignUp(acc.getEmail(), request);*/
                emailService.sendConfirmationEmail(new OnRegistrationCompleteEvent(newAcc, request.getLocale(), appUrl));
                // Forward to the login, acknowleding that account has been created
                responseJson.put("message", messageSource.getMessage("registration.created", null, request.getLocale()));
            } else { // Acc not created
                bResult.rejectValue("email", "registration.accCreationError");
            }
        }  catch(EmailExistsException eee) {
            System.out.println("Catched EmailexistsException, attaching the rejected value to BindingREsult");
            bResult.rejectValue("email", messageSource.getMessage("registration.emailExists", null, request.getLocale()));
        } catch (EmailNotSentException uee) {
            System.out.println("Catched EmailNotSentException, attaching the rejected value to BindingResult");
            bResult.rejectValue("email", messageSource.getMessage("registration.emailCouldNotBeSent", null, request.getLocale()));
        }

        if (bResult.hasErrors())
            return processRegistrationBindingResultErrors(bResult, webRequest);


        responseJson.put("email", accountService.findAccount(registrationForm.getEmail()).getEmail());
        return ResponseEntity.ok(responseJson.toJSONString());
    }

    private ResponseEntity<String> processRegistrationBindingResultErrors(BindingResult bResult, WebRequest webRequest) {
        // Process errors
        System.out.println(bResult.getAllErrors().toString());
        JSONObject json = new JSONObject();

        if (bResult.getFieldError("email") != null) {
            if (bResult.getFieldError("email").getCode().equals("Pattern"))
                json.put("emailError", messageSource.getMessage("Pattern.email", null, webRequest.getLocale()));
            else
                json.put("emailError", bResult.getFieldError("email").getCode());
        }
        if (bResult.getFieldError("password") != null) {
            if (bResult.getFieldError("password").getCode().equals("Size"))
                json.put("passwordError", messageSource.getMessage("Size.password", null, webRequest.getLocale()));
            else
                json.put("passwordError", bResult.getFieldError("password").getCode());
        }
        if (bResult.getFieldError("confirmPassword") != null) {
            if (bResult.getFieldError("confirmPassword").getCode().equals("Size"))
                json.put("confirmPasswordError", messageSource.getMessage("Size.confirmPassword", null, webRequest.getLocale()));
            else
                json.put("confirmPasswordError", bResult.getFieldError("confirmPassword").getCode());
        }
        return ResponseEntity.status(406).body(json.toJSONString());
    }

}
