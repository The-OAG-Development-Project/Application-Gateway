package org.owasp.oag.integration.mockserver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Records whether the thread handling a request is virtual.
 */
@Order(-100)
public class VirtualThreadProbeFilter extends OncePerRequestFilter {

    private static volatile boolean virtual;
    private static volatile String threadName = "none";

    /**
     * @return true if the last handled request ran on a virtual thread
     */
    public static boolean wasVirtual() {
        return virtual;
    }

    /**
     * @return the name of the thread that handled the last request
     */
    public static String lastThreadName() {
        return threadName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        virtual = Thread.currentThread().isVirtual();
        threadName = Thread.currentThread().toString();
        chain.doFilter(request, response);
    }
}
