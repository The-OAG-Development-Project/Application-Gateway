package ch.gianlucafrei.nellygateway.controllers;

import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.session.Session;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class DebugController {

    @GetMapping("whoami")
    public String whoami(HttpServletResponse response,
                         HttpServletRequest request) {

        Optional<Session> sessionOptional = (Optional<Session>) request.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if (sessionOptional.isPresent()) {
            return sessionOptional.get().getUserModel().getId();
        } else {

            return "Anonymous";
        }
    }

}
