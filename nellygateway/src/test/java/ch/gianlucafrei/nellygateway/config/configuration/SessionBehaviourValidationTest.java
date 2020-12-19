package ch.gianlucafrei.nellygateway.config.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionBehaviourValidationTest {

    @Test
    public void testValidConfiguration() {

        // Arrange
        SessionBehaviour behaviour = new SessionBehaviour(
                3600,
                "/",
                "/",
                "/");

        // Act
        var errors = behaviour.getErrors(null);

        // Assert
        assertEquals(0, errors.size());
    }

    @Test
    public void testNoFields() {

        // Arrange
        SessionBehaviour behaviour = new SessionBehaviour(
                3600,
                null,
                null,
                null);

        // Act
        var errors = behaviour.getErrors(null);

        // Assert
        assertEquals(3, errors.size());
    }

    @Test
    public void testInvalidSessionDuration() {

        // Arrange
        SessionBehaviour behaviour = new SessionBehaviour(
                10, // too short session duration
                "/",
                "/",
                "/");

        // Act
        var errors = behaviour.getErrors(null);

        // Assert
        assertEquals(1, errors.size());
    }
}