package org.owasp.oag.services.tokenMapping.nouser;

import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NoUserMappingFactory implements UserMappingFactory {

    @Override
    public UserMapper load(Map<String, Object> settings) {

        return new NoUserMapper();
    }
}
