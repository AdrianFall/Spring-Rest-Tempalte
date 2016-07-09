package filter.oauth;

import org.springframework.security.web.RedirectStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Adrian on 09/07/2016.
 */
public class NoRedirectStrategy implements RedirectStrategy {

    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        //No redirect
    }
}