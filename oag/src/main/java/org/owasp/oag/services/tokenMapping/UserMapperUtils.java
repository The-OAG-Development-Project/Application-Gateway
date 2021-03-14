package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMapperUtils {


    private static final Logger log = LoggerFactory.getLogger(UserMapperUtils.class);
    public static final String MAPPING_PREFIX_USER_MODEL = "userModel:";
    public static final String MAPPING_PREFIX_CONSTANT = "constant:";
    public static final String MAPPING_USER_ID = "<<user-id>>";
    public static final String MAPPING_LOGIN_PROVIDER = "<<login-provider>>";

    public static boolean isValidMapping(String mappingString){

        if(mappingString == null) return false;

        if(mappingString.equals(MAPPING_USER_ID))
            return true;

        if(mappingString.equals(MAPPING_LOGIN_PROVIDER))
            return true;

        if(mappingString.startsWith(MAPPING_PREFIX_USER_MODEL))
            return mappingString.length() > MAPPING_PREFIX_USER_MODEL.length();

        if(mappingString.startsWith(MAPPING_PREFIX_CONSTANT))
            return mappingString.length() > MAPPING_PREFIX_CONSTANT.length();

        return false;
    }

    public static String getMappingFromUserModel(GatewayRouteContext context, String mappingString) {

        var session = context.getSessionOptional().get();
        var model = session.getUserModel();


        if(mappingString.equals(MAPPING_USER_ID))
            return model.getId();

        if(mappingString.equals(MAPPING_LOGIN_PROVIDER))
            return session.getProvider();

        // Mapping with prefix
        String mappingValue = mappingString.split(":")[1];
        if(mappingString.startsWith(MAPPING_PREFIX_CONSTANT)){
            return mappingValue;
        }
        else if(mappingString.startsWith(MAPPING_PREFIX_USER_MODEL)){

            if(model.getMappings().containsKey(mappingValue))
            {
                return model.getMappings().get(mappingValue);
            }
            else{
                log.debug("No value with key {} found in user model", mappingValue);
            }
        }
        else{
            log.warn("Invalid mapping in JwtTokenMapper will be ignored: '{}'", mappingString);
        }

        return null;
    }
}
