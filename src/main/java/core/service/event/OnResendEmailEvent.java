package core.service.event;

import core.model.Account;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

/**
 * Created by Adrian on 28/06/2015.
 */
public class OnResendEmailEvent extends ApplicationEvent {
    private final String appUrl;
    private final Locale locale;
    private final Account account;

    public OnResendEmailEvent(Account account, Locale locale, String appUrl) {
        super(account);
        this.account = account;
        this.locale = locale;
        this.appUrl = appUrl;
    }



    public Account getAccount() {
        return account;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getAppUrl() {
        return appUrl;
    }
}
