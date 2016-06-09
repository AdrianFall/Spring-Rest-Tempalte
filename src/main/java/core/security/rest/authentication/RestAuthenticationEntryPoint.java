package core.security.rest.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Adrian on 29/03/2016.
 */
public class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Autowired
    MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        if (req instanceof HttpServletRequest && httpRequest.getHeader("Origin") != null)  {
            response.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        /*response.addHeader("Access-Control-Allow-Origin", "*");*/
        response.addHeader("WWW-Authenticate", "RestBasic realm=\"" + getRealmName() + "\"");
        if (authException instanceof DisabledException) {
           response.setHeader("error", messageSource.getMessage("login.disabled", null, httpRequest.getLocale()));
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentLength(0);
    }
}
