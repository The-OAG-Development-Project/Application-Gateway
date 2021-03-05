package org.owasp.oag.services.tokenMapping.jwt;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.JwtSigner;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.utils.SettingsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("jwt-mapping")
public class JwtTokenMapperBridge implements UserMapper {

    private JwtTokenMapper mapper;

    @Autowired
    public JwtTokenMapperBridge(ApplicationContext context, GlobalClockSource clockSource, MainConfig config) {

        // Load settings
        JwtTokenMappingSettings settings;
        try{
            var settingsMap = config.getDownstreamAuthentication().getTokenMapping().getSettings();
            settings = SettingsUtils.settingsFromMap(settingsMap, JwtTokenMappingSettings.class);
        }
        catch(Exception ex){
            throw new RuntimeException("Cannot deserialize jwt-mapping settings", ex);
        }
        settings.requireValidSettings();

        // Init jwt signer
        var name = settings.signatureImplementation + "-jwt-signer";
        var signer = context.getBean(name, JwtSigner.class);

        mapper = new JwtTokenMapper(signer, clockSource, settings, config.getHostUri());
    }

    @Override
    public Mono<ServerWebExchange> mapUserToRequest(ServerWebExchange exchange, GatewayRouteContext context) {
        return mapper.mapUserToRequest(exchange, context);
    }
}
