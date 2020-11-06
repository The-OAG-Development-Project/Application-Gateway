package ch.gianlucafrei.nellygateway.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static Cookie getCookieOrNull(String name, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        for (Cookie c : cookies) {
            if (name.equals(c.getName()))
                return c;
        }

        return null;
    }

    public static void addSameSiteCookie(Cookie cookie, String sameSiteValue, HttpServletResponse response){

        String cookieHeader = getCookieAsHeader(cookie, sameSiteValue);
        response.addHeader("Set-Cookie", cookieHeader);
    }

    public static void removeSameSiteCookie(Cookie cookie, String sameSiteValue, HttpServletResponse response) {

        cookie.setMaxAge(0);
        addSameSiteCookie(cookie, sameSiteValue, response);
    }

    public static String getCookieAsHeader(Cookie cookie, String sameSiteValue) {
        StringBuilder builder = new StringBuilder(
                String.format("%s=%s", cookie.getName(), cookie.getValue())
        );

        if(cookie.getMaxAge() >= 0)
            builder.append(String.format("; Max-Age=%d", cookie.getMaxAge()));

        if(cookie.isHttpOnly())
            builder.append("; HttpOnly");

        if(cookie.getSecure())
            builder.append("; Secure");

        if(sameSiteValue != null && !sameSiteValue.equals(""))
            builder.append(String.format("; SameSite=%s", sameSiteValue));

        if(cookie.getPath() != null && !cookie.getPath().equals(""))
            builder.append(String.format("; Path=%s", cookie.getPath()));

        if(cookie.getDomain() != null && !cookie.getDomain().equals(""))
            builder.append(String.format("; Domain=%s", cookie.getDomain()));

        return builder.toString();
    }
}
