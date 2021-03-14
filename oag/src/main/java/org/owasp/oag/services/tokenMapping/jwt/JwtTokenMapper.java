package org.owasp.oag.services.tokenMapping.jwt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.jwt.JWTClaimsSet;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.JwtSigner;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMapperUtils;
import org.owasp.oag.session.UserModel;
import org.owasp.oag.utils.ReactiveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * This implementation of a token mapper transports the user principal in a signed JsonWebToken.
 * The actual signing process is performed with the separate jwtSigner object. Created tokens are cached in-memory to
 * avoid bottlenecks due to the relatively slow crypto operations.
 */
public class JwtTokenMapper implements UserMapper {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenMapper.class);

    private Cache<CacheKey, String> tokenCache;
    private final GlobalClockSource clockSource;
    private final JwtSigner jwtSigner;
    private final JwtTokenMappingSettings settings;
    private final SecureRandom secureRandom;
    private final String issuer;


    public JwtTokenMapper(JwtSigner jwtSigner, GlobalClockSource clockSource, JwtTokenMappingSettings settings, String hostUri) {

        this.jwtSigner = jwtSigner;
        this.clockSource = clockSource;
        this.settings = settings;
        this.secureRandom = new SecureRandom();
        this.issuer = "<<hostUri>>".equals(settings.issuer) ? hostUri : settings.issuer;

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

    public String mapUserModelToToken(GatewayRouteContext context, String audience, String provider) {

        // Assemble claims set
        var now = clockSource.getGlobalClock().instant();
        var exp = now.plusSeconds(settings.tokenLifetimeSeconds);
        var tokenId = createJti();
        var claimsBuilder = new JWTClaimsSet.Builder();
        var model = context.getSessionOptional().get().getUserModel();

        // Add mandatory claims
        claimsBuilder.subject(model.getId())
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("provider", provider)
                .jwtID(tokenId);

        for(var entry: this.settings.mappings.entrySet()){

            var value = UserMapperUtils.getMappingFromUserModel(context, entry.getValue());
            claimsBuilder = claimsBuilder.claim(entry.getKey(), value);
        }

        // Sign claims set
        JWTClaimsSet claimsSet = claimsBuilder.build();
        var signedJWT = jwtSigner.signJwt(claimsSet);
        return signedJWT;
    }

    private String createJti() {

        return Long.toHexString(secureRandom.nextLong());
    }

    private static class CacheKey {

        private final UserModel userModel;
        private final String audience;
        private final String provider;

        private CacheKey(UserModel userModel, String audience, String provider) {
            this.userModel = userModel;
            this.audience = audience;
            this.provider = provider;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey that = (CacheKey) o;
            return userModel.equals(that.userModel) &&
                    audience.equals(that.audience) &&
                    provider.equals(that.provider);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userModel, audience, provider);
        }
    }
}
