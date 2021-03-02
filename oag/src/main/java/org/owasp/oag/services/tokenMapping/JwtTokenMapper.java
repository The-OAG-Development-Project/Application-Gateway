package org.owasp.oag.services.tokenMapping;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.jwt.JWTClaimsSet;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.proxy.RouteAwareFilter;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.JwtSigner;
import org.owasp.oag.services.login.drivers.UserModel;
import org.owasp.oag.utils.ReactiveUtils;
import org.owasp.oag.utils.SettingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

@Component("jwt-mapping")
public class JwtTokenMapper implements TokenMapper {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenMapper.class);

    private Cache<UserModel, String> tokenCache;
    private GlobalClockSource clockSource;
    private JwtSigner jwtSigner;
    private JwtTokenMappingSettings settings;

    @Autowired
    public JwtTokenMapper(ApplicationContext context, GlobalClockSource clockSource, MainConfig config) {

        // Load signer bean

        this.clockSource = clockSource;

        // Load settings
        try{
            var settingsMap = config.getDownstreamAuthentication().getTokenMapping().getSettings();
            settings = SettingsUtils.settingsFromMap(settingsMap, JwtTokenMappingSettings.class);
        }
        catch(Exception ex){
            throw new RuntimeException("Cannot deserialize jwt-mapping settings", ex);
        }
        verifySettings();

        // Init jwt signer
        var name = settings.signatureImplementation + "-jwt-signer";
        this.jwtSigner = context.getBean(name, JwtSigner.class);

        if(jwtSigner == null)
            throw new RuntimeException("Could not initialize jwtSigner implementation with name: " + name);

        initCache();
    }

    private void verifySettings(){

        if (settings.headerName == null || "".equals(settings.headerName))
            throw new RuntimeException("Config: JwtTokenMapper headerName is invalid");

        if (settings.headerPrefix == null)
            throw new RuntimeException("Config: JwtTokenMapper headerPrefix is invalid");

        if (settings.audience == null)
            throw new RuntimeException("Config: JwtTokenMapper audience is invalid");

        if (settings.issuer == null)
            throw new RuntimeException("Config: JwtTokenMapper issuer is invalid");

        if (settings.signatureImplementation == null)
            throw new RuntimeException("Config: JwtTokenMapper signatureImplementation is invalid");
    }

    public JwtTokenMapper(JwtSigner jwtSigner, GlobalClockSource clockSource, JwtTokenMappingSettings settings) {

        this.jwtSigner = jwtSigner;
        this.clockSource = clockSource;
        this.settings = settings;

        verifySettings();
        initCache();
    }

    private void initCache(){

        this.tokenCache = CacheBuilder.newBuilder()
                .initialCapacity(1_000)
                .expireAfterWrite(Duration.ofSeconds(settings.tokenLifetimeSeconds))
                .maximumSize(100_000)
                .build();
    }

    @Override
    public Mono<ServerWebExchange> mapToken(ServerWebExchange exchange, RouteAwareFilter.GatewayRouteContext context) {

        if(context.getSessionOptional().isPresent()){

            var userModel = context.getSessionOptional().get().getUserModel();
            var cachedToken = tokenCache.getIfPresent(userModel);

            Mono<String> tokenMono;

            if(cachedToken != null){
                logTrace(log, exchange, "Loaded downstream token from cache");
                tokenMono = Mono.just(cachedToken);
            }
            else{
                logTrace(log, exchange, "Create new downstream token");
                tokenMono = ReactiveUtils
                        .runBlockingProcedure(() -> mapUserModelToToken(userModel))
                        .doOnSuccess(token -> tokenCache.put(userModel, token));
            }

            return tokenMono.map(token -> {

                var mutatedRequest = exchange.getRequest().mutate()
                        .header(settings.headerName, settings.headerPrefix + token).build();

                return exchange.mutate().request(mutatedRequest).build();
            });
        }

        return Mono.just(exchange);
    }

    public String mapUserModelToToken(UserModel model){

        var now = clockSource.getGlobalClock().instant();
        var exp = now.plusSeconds(settings.tokenLifetimeSeconds);
        var audience = settings.audience;
        var issuer = settings.issuer;

        var claimsBuilder = new JWTClaimsSet.Builder()
                .subject(model.getId())
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp));

        model.getMappings().forEach((key, value) -> claimsBuilder.claim(key, value));

        JWTClaimsSet claimsSet =claimsBuilder.build();
        var signedJWT = jwtSigner.signJwt(claimsSet);
        return signedJWT;
    }

    public static class JwtTokenMappingSettings{

        public String headerName;
        public String headerPrefix;
        public String audience;
        public String issuer;
        public int tokenLifetimeSeconds;
        public String signatureImplementation;
        public Map<String, Object> signatureSettings;

        public JwtTokenMappingSettings() {
        }

        public JwtTokenMappingSettings(String headerName, String headerPrefix, String audience, String issuer, int tokenLifetimeSeconds, String signatureImplementation, Map<String, Object> signatureSettings) {
            this.headerName = headerName;
            this.headerPrefix = headerPrefix;
            this.audience = audience;
            this.issuer = issuer;
            this.tokenLifetimeSeconds = tokenLifetimeSeconds;
            this.signatureImplementation = signatureImplementation;
            this.signatureSettings = signatureSettings;
        }
    }
}
