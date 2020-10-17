package ch.gianlucafrei.nellygateway.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtils {

    public static Cookie getCookieOrNull(String name, HttpServletRequest request)
    {

        Cookie[] cookies = request.getCookies();
        if(cookies == null)
            return null;

        for(Cookie c : cookies)
        {
            if(name.equals(c.getName()))
                return c;
        }

        return null;
    }

}
