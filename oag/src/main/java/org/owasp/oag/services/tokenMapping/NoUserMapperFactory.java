package org.owasp.oag.services.tokenMapping;

import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.tokenMapping.UserMappingFactory.USER_MAPPER_TYPE_POSTFIX;

@Component("no-mapping" + USER_MAPPER_TYPE_POSTFIX)
public class NoUserMapperFactory implements UserMappingFactory {

    @Override
    public UserMapper load(Map<String, Object> settings) {

        return new NoUserMapper();
    }
}
