package org.owasp.oag.extension;

import java.lang.reflect.Proxy;

/**
 * Creates stubs of extension interfaces that only answer {@link TypedExtension#type()}.
 */
public final class TypedExtensionStub {

    private TypedExtensionStub() {
    }

    /**
     * Creates a stub of the given extension interface.
     *
     * @param extensionInterface The extension interface
     * @param type               The value returned by {@code type()}
     * @param <T>                The extension type
     * @return A stub whose other methods throw {@link UnsupportedOperationException}
     */
    public static <T extends TypedExtension> T stub(Class<T> extensionInterface, String type) {
        return extensionInterface.cast(Proxy.newProxyInstance(extensionInterface.getClassLoader(),
                new Class<?>[]{extensionInterface}, (proxy, method, args) -> {
                    if (method.getName().equals("type"))
                        return type;
                    throw new UnsupportedOperationException(method.getName());
                }));
    }
}
