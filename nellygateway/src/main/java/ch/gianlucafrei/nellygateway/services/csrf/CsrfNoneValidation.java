package ch.gianlucafrei.nellygateway.services.csrf;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component("csrf-none-validation")
public class CsrfNoneValidation implements CsrfProtectionValidation {

    @Override
    public boolean shouldBlockRequest(HttpServletRequest request) {

        return false;
    }
}
