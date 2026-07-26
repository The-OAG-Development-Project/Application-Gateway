package org.owasp.oag.filters.proxy;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.filters.GatewayRouteContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the set-semantics of the headers OAG puts on the downstream request.
 * <p>
 * {@code ServerRequest.Builder.header(...)} appends, so using it would leave a client supplied value next
 * to the one OAG produces and a downstream system that trusts these headers could read the attacker's
 * value. The filter therefore uses {@code headers(h -> h.set(...))}, and these tests fail if that is
 * ever changed back.
 */
class DownstreamHeaderFilterTest {

    private static final List<HttpMessageConverter<?>> MESSAGE_CONVERTERS =
            List.of(new StringHttpMessageConverter());

    private final DownstreamHeaderFilter filter = new DownstreamHeaderFilter();

    @Test
    void replacesAClientSuppliedOagStatusHeaderInsteadOfAppendingToIt() {

        var servletRequest = new MockHttpServletRequest("GET", "/foo");
        servletRequest.addHeader(DownstreamHeaderFilter.X_OAG_STATUS, SessionInformation.SESSION_STATE_AUTHENTICATED);
        servletRequest.addHeader(DownstreamHeaderFilter.X_PROXY, "some other proxy");

        var headers = filter.addOagHeaders(anonymousRouteContext(), builderFor(servletRequest)).build().headers();

        assertEquals(List.of(SessionInformation.SESSION_STATE_ANONYMOUS),
                headers.header(DownstreamHeaderFilter.X_OAG_STATUS),
                "the client supplied authentication status must not survive next to OAG's");
        assertEquals(List.of(DownstreamHeaderFilter.X_PROXY_VALUE),
                headers.header(DownstreamHeaderFilter.X_PROXY),
                "the client supplied proxy header must not survive next to OAG's");
    }

    @Test
    void forwardsASingleCookieHeaderWithoutTheOagCookies() {

        var servletRequest = new MockHttpServletRequest("GET", "/foo");
        servletRequest.setCookies(
                new Cookie("downstream-cookie", "keep-me"),
                new Cookie(LoginCookie.NAME, "oag-secret"),
                new Cookie(CsrfCookie.NAME, "oag-csrf"));

        var request = ServerRequest.create(servletRequest, MESSAGE_CONVERTERS);
        var headers = filter.filterRequestCookies(request, ServerRequest.from(request)).build().headers();

        assertEquals(List.of("downstream-cookie=keep-me"), headers.header(HttpHeaders.COOKIE),
                "there must be exactly one Cookie header downstream, and it must not carry OAG's own cookies");
    }

    private ServerRequest.Builder builderFor(MockHttpServletRequest servletRequest) {
        return ServerRequest.from(ServerRequest.create(servletRequest, MESSAGE_CONVERTERS));
    }

    private GatewayRouteContext anonymousRouteContext() {
        return new GatewayRouteContext("routeName", null, null, "/foo", "/foo", Optional.empty());
    }
}
