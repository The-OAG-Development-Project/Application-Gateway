package org.owasp.oag.services.tokenMapping;

import java.util.Map;

public interface UserMappingFactory {

    final static String USER_MAPPER_TYPE_POSTFIX = "-userMapping-factory";

    UserMapper load(Map<String, Object> settings);
}
