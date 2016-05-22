package core.service.listener;

import core.model.Account;
import core.model.VerificationToken;
import core.service.AccountService;
import core.service.event.OnResendEmailEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

/**
 * Created by Adrian on 28/06/2015.
 */
@Component
public class ResendEmailListener implements ApplicationListener<OnResendEmailEvent> {

    @Autowired
    private AccountService accountService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JavaMailSender mailSender;


    @Override
    public void onApplicationEvent(OnResendEmailEvent onResendEmailEvent) {
        System.out.println("ResendEmailListener - onApplicationEvent");
        this.resendEmail(onResendEmailEvent);

    }

    public void resendEmail(OnResendEmailEvent event) {
        Account acc = event.getAccount();

        String newId = UUID.randomUUID().toString();
        VerificationToken newToken =  new VerificationToken(newId, acc);
        VerificationToken updatedToken =  accountService.updateVerificationToken(newToken, acc);

        System.out.println("VerificationToken " + ((updatedToken == null) ? "not updated" : "updated and id is: " + updatedToken.getId()));
        if (updatedToken != null) {
            try {
                String recipentEmail = acc.getEmail();
                String subject = messageSource.getMessage("registration.email.subject", null, event.getLocale());
                String confirmationURL = event.getAppUrl() + "/registrationConfirm?token=" + updatedToken.getToken();

                String msg = messageSource.getMessage("registration.email.message", null, event.getLocale());
                msg += " " + confirmationURL + " \n Alternatively input the following activation code into your app: " + updatedToken.getToken();
                System.out.println("The email message is: " + msg);

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipentEmail));
                mimeMessage.setSubject(subject, "UTF-8");
                mimeMessage.setText(msg, "UTF-8");

                // Send the mail
                mailSender.send(mimeMessage);
            }   catch (NoSuchMessageException nsme) {
                System.err.println("No such message in the .properties file");
                nsme.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error while trying to send email");
                e.printStackTrace();
            }
        }

    }
}
