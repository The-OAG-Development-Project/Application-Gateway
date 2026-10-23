/**
 * This package contains filters applied during request and response handling of the gateway.
 * <p>
 * The filters are arranged in two tiers:
 * <ul>
 *   <li>{@code org.owasp.oag.filters.servlet} holds servlet filters. They run before routing for
 *       every request, including the gateway's own {@code /auth} and {@code /.well-known}
 *       endpoints, and cover tracing, request logging, session extraction and session renewal.</li>
 *   <li>{@code org.owasp.oag.filters.proxy} holds
 *       {@code org.springframework.web.servlet.function.HandlerFilterFunction} route filters. They
 *       are attached to each configured gateway route, so they run for proxied traffic only, and
 *       have access to the route's security profile and session through
 *       {@link org.owasp.oag.filters.GatewayRouteContext}.</li>
 * </ul>
 * Within each tier the filters run in {@code @Order} sequence, lowest value first.
 */
package org.owasp.oag.filters;
