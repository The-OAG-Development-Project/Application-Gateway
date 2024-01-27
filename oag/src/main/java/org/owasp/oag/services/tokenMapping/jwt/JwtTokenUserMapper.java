package org.owasp.oag.services.tokenMapping.jwt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.jwt.JWTClaimsSet;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.jwt.JwtSignerFactory;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;
import org.owasp.oag.session.UserModel;
import org.owasp.oag.utils.ReactiveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * This implementation of a token mapper transports the user principal in a signed JsonWebToken.
 * The actual signing process is performed with the separate jwtSigner object. Created tokens are cached in-memory to
 * avoid bottlenecks due to the relatively slow crypto operations.
 */
public class JwtTokenUserMapper implements UserMapper {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUserMapper.class);
    private final GlobalClockSource clockSource;
    // Note: Must use factory because each signature must be created with new signer to make sure key rotation etc. is heeded!
    private final JwtSignerFactory jwtSignerFactory;
    private final JwtTokenUserMappingSettings settings;
    private final SecureRandom secureRandom;
    private final String issuer;
    private final String hostUri;
    private Cache<CacheKey, String> tokenCache;


    public JwtTokenUserMapper(JwtSignerFactory signerFactory, GlobalClockSource clockSource, JwtTokenUserMappingSettings settings, String hostUri) {

        this.jwtSignerFactory = signerFactory;
        this.clockSource = clockSource;
        this.settings = settings;
        this.secureRandom = new SecureRandom();
        this.issuer = "<<hostUri>>".equals(settings.issuer) ? hostUri : settings.issuer;
        this.hostUri = hostUri;

        settings.requireValidSettings();
        initCache();
    }

    private void initCache() {

        this.tokenCache = CacheBuilder.newBuilder()
                .initialCapacity(1_000)
                .expireAfterWrite(Duration.ofSeconds(settings.tokenLifetimeSeconds))
                .maximumSize(100_000)
                .build();
    }

    @Override
    public Mono<ServerWebExchange> mapUserToRequest(ServerWebExchange exchange, GatewayRouteContext context) {

        // Only add downstream authentication if there is a user session
        if (context.getSessionOptional().isPresent()) {

            var session = context.getSessionOptional().get();

            // Assemble all information to identify the relevant token
            var userModel = session.getUserModel();
            var audience = "<<route-url>>".equals(settings.audience) ? context.getRoute().getUrl() : settings.audience;
            var provider = session.getProvider();

            // Load token from cache or create a new one
            // Tokens are cached by userModel, audience and login provider
            Mono<String> tokenMono = getTokenMono(userModel, audience, provider, exchange, context);

            // Add token to request
            return tokenMono.map(token -> {

                var mutatedRequest = exchange.getRequest().mutate()
                        .header(settings.headerName, settings.headerPrefix + token).build();

                return exchange.mutate().request(mutatedRequest).build();
            });
        }

        return Mono.just(exchange);
    }

    public Mono<String> getTokenMono(UserModel model, String audience, String provider, ServerWebExchange exchange, GatewayRouteContext context) {

        var cacheKey = new CacheKey(model, audience, provider);
        var cachedToken = tokenCache.getIfPresent(cacheKey);

        Mono<String> tokenMono;
        if (cachedToken != null) {
            logTrace(log, exchange, "Loaded downstream token from cache");
            tokenMono = Mono.just(cachedToken);
        } else {
            logTrace(log, exchange, "Create new downstream token");
            tokenMono = ReactiveUtils
                    .runBlockingProcedure(() -> mapUserModelToToken(context, cacheKey.audience, cacheKey.provider))
                    .doOnSuccess(token -> tokenCache.put(cacheKey, token));
        }
        return tokenMono;
    }

    public String mapUserModelToToken(GatewayRouteContext context, String audience, String ignoredProvider) {

        // Assemble claims set
        var now = clockSource.getGlobalClock().instant();
        var exp = now.plusSeconds(settings.tokenLifetimeSeconds);
        var tokenId = createJti();
        var claimsBuilder = new JWTClaimsSet.Builder();
        var sessionOptional = context.getSessionOptional();
        UserModel model;
        if (sessionOptional.isPresent()) {
            model = sessionOptional.get().getUserModel();
        } else {
            throw new ConfigurationException("Missing session part of the configuration.");
        }

        // Add mandatory claims
        claimsBuilder.subject(model.getId())
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(tokenId);

        var mappingEngine = new UserMappingTemplatingEngine(sessionOptional.get());
        for (var entry : this.settings.mappings.entrySet()) {

            var value = mappingEngine.processTemplate(entry.getValue());
            claimsBuilder = claimsBuilder.claim(entry.getKey(), value);
        }

        // Sign claims set
        JWTClaimsSet claimsSet = claimsBuilder.build();

        // Note: in order to allow key rotation use a new signer per request!
        var jwtSigner = jwtSignerFactory.create(hostUri, settings.signatureSettings);

        return jwtSigner.createSignedJwt(claimsSet);
    }

    private String createJti() {
        return Long.toHexString(secureRandom.nextLong());
    }

    private record CacheKey(UserModel userModel, String audience, String provider){}
}
