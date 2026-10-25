package org.owasp.oag.extension;

import org.junit.jupiter.api.Test;
import org.owasp.oag.exception.ConfigurationException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TypeRegistryTest {

    record Ext(String type) implements TypedExtension {
    }

    static class ExtRegistry extends TypeRegistry<Ext> {
        ExtRegistry(List<Ext> extensions) {
            super("test extension", extensions);
        }
    }

    @Test
    void getReturnsExtensionRegisteredForType() {
        var a = new Ext("a");
        var b = new Ext("b");
        var registry = new ExtRegistry(List.of(a, b));

        assertSame(a, registry.get("a"));
        assertSame(b, registry.get("b"));
    }

    @Test
    void getUnknownTypeThrowsWithKnownTypes() {
        var registry = new ExtRegistry(List.of(new Ext("a"), new Ext("b")));

        var ex = assertThrows(ConfigurationException.class, () -> registry.get("x"));

        assertTrue(ex.getMessage().contains("test extension"), ex.getMessage());
        assertTrue(ex.getMessage().contains("'x'"), ex.getMessage());
        assertTrue(ex.getMessage().contains("[a, b]"), ex.getMessage());
    }

    @Test
    void duplicateTypeFailsOnConstruction() {
        var extensions = List.of(new Ext("a"), new Ext("a"));

        var ex = assertThrows(ConfigurationException.class, () -> new ExtRegistry(extensions));

        assertTrue(ex.getMessage().contains("'a'"), ex.getMessage());
    }

    @Test
    void containsAndTypesReflectRegisteredExtensions() {
        var registry = new ExtRegistry(List.of(new Ext("b"), new Ext("a")));

        assertTrue(registry.contains("a"));
        assertFalse(registry.contains("x"));
        assertFalse(registry.contains(null));
        assertEquals(Set.of("a", "b"), registry.types());
    }
}
