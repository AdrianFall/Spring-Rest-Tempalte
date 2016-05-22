package core.service;

import core.service.event.OnRegistrationCompleteEvent;
import core.service.event.OnResendEmailEvent;
import core.service.event.OnResetPasswordEvent;
import core.service.exception.EmailNotSentException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

/**
 * Created by Adrian on 14/05/2015.
 */
@Service
public class EmailService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;



    public void sendConfirmationEmail(OnRegistrationCompleteEvent onRegistrationCompleteEvent) throws EmailNotSentException {
        eventPublisher.publishEvent(onRegistrationCompleteEvent);
        System.out.println("Published OnRegistrationCompleteEvent");
    }

    public void resendConfirmationEmail(OnResendEmailEvent onResendEmailEvent) {
        eventPublisher.publishEvent(onResendEmailEvent);
        System.out.println("Published OnResendEmailEvent");
    }

    public void sendResetPasswordEmail(OnResetPasswordEvent onResetPasswordEvent) {
        eventPublisher.publishEvent(onResetPasswordEvent);
        System.out.println("Published onResetPasswordEvent");
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        eventPublisher = applicationEventPublisher;
    }
}
