package ch.gianlucafrei.nellygateway.controllers;

import ch.gianlucafrei.nellygateway.cookies.SessionCookie;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class DebugController {

    @Autowired
    private CookieEncryptor cookieEncryptor;

    @GetMapping("whoami")
    public String whoami(HttpServletResponse response,
                         HttpServletRequest request) {
        SessionCookie cookie = SessionCookie.loadFromRequest(request, cookieEncryptor);

        if (cookie == null)
            return "Anonymous";

        long expiryIn = cookie.getSessionExp() - (System.currentTimeMillis() / 1000);

        return String.format("Id: %s\nProvider: %s\nExpiry in: %s\nOriginal token: %s",
                cookie.getSubject(),
                cookie.getProvider(),
                expiryIn,
                cookie.getOrginalToken());
    }

}
