package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.session.Session;
import org.stringtemplate.v4.ST;

/**
 * Implements the user mapping template syntax with the stringtemplate4 library.
 * See documentation: https://github.com/gianlucafrei/Application-Gateway/wiki/Configuration:-User-Mapping#mapping-syntax
 */
public class UserMappingTemplatingEngine {

    private final Session session;

    /**
     * Creates a new instance for a given user session object
     *
     * @param session The user session containing the user model for template mapping
     */
    public UserMappingTemplatingEngine(Session session) {

        if (session == null)
            throw new IllegalArgumentException("Session must not be null for template mapping");

        if (session.getUserModel() == null)
            throw new IllegalArgumentException("User model must not be null for template mapping");

        this.session = session;
    }

    /**
     * Checks if a given string is a valid string template.
     * Does not check if any references are used that do not exist.
     *
     * @param mappingTemplate The template string to validate
     * @return true if the template is syntactically valid, false otherwise
     */
    public static boolean isValidTemplate(String mappingTemplate) {

        try {
            var st = new ST(mappingTemplate);
            return true;
        } catch (Exception ex) {
            return false;
        }

    }

    /**
     * Renders a template and returns the value as a string.
     *
     * @param mappingTemplate Template to render with the session object from the constructor
     * @return Rendered mapping value
     */
    public String processTemplate(String mappingTemplate) {

        ST st = new ST(mappingTemplate);

        st.add("session", this.session);
        st.add("mappings", this.session.getUserModel().getMappings());

        return st.render();
    }
}
