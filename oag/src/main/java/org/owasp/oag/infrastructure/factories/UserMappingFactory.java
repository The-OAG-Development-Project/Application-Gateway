package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.services.tokenMapping.UserMapper;

public interface UserMappingFactory {

    UserMapper getUserMapperForSecurityProfile(String securityProfileName);
}
