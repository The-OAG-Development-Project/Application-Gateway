package org.owasp.oag.services.tokenMapping;

import org.junit.jupiter.api.Test;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;

import static org.junit.jupiter.api.Assertions.*;

class UserMappingTemplatingEngineTest {

    private UserMappingTemplatingEngine engine;

    public UserMappingTemplatingEngineTest() {

        var userModel = new UserModel("user-id");
        userModel.set("email", "user@example.com");
        userModel.set("role", "Admin");

        var session = new Session(9999999, 1000, "provider", userModel, null, "sessionId");
        this.engine = new UserMappingTemplatingEngine(session);
    }

    @Test
    public void testSimpleTemplates(){

        // ACT
        assertEquals("abc", engine.processTemplate("abc"));
        assertEquals("user@example.com", engine.processTemplate("<mappings.email>"));
        assertEquals("user-id", engine.processTemplate("<session.userId>"));
        assertEquals("provider", engine.processTemplate("<session.provider>"));
        assertEquals("ABC=provider",engine.processTemplate("ABC=<session.provider>"));
    }

    @Test
    public void testInvalidMapping(){

        assertEquals("", engine.processTemplate("<doesNotExist>"));
    }

    @Test
    public void testIsValidMapping(){

        assertTrue(UserMappingTemplatingEngine.isValidTemplate("<doesNotExist>"));
        assertTrue(UserMappingTemplatingEngine.isValidTemplate("<session.userId>"));
        assertTrue(UserMappingTemplatingEngine.isValidTemplate("abc"));

        assertFalse(UserMappingTemplatingEngine.isValidTemplate("<"));
    }

}