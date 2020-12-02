package ch.gianlucafrei.nellygateway.filters.session;

import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;

import javax.servlet.http.HttpServletResponse;

public interface SessionCreationFilter {

    int order();

    void doFilter(String providerKey, UserModel model, HttpServletResponse response);
}
