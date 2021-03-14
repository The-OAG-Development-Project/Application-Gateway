package org.owasp.oag.services.tokenMapping.header;

import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.tokenMapping.UserMappingFactory.USER_MAPPER_TYPE_POSTFIX;

@Component("header-mapping" + USER_MAPPER_TYPE_POSTFIX)
public class RequestHeaderUserMappingFactory implements UserMappingFactory {

    @Override
    public UserMapper load(Map<String, Object> settings) {

        return new RequestHeaderUserMapping();
    }
}
