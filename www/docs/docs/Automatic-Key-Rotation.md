# Automatic Key Rotation

If you need more advanced key rotation, you can implement this by providing your own component.
Implement the KeyRotation interface, return the configured type from `type()` and configure it in the KeyManagement section of the settings.
OAG calls `start()` once at startup, only on the configured implementation. It must generate the first signing key, assign it in the
`CurrentSigningKeyHolder`, publish it in the JwkStore and schedule further rotations.
Note that depending on your implementation you might also have to provide different implementations for JWTSigner, KeyGenerator, JwkStore. Yet if you follow the guidance found in DefaultKeyRotation that  should not be required.
```Java
@Component
public class MyKeyRotation implements KeyRotation {

    @Override
    public String type() {
        return "myKeyRotation";
    }

    @Override
    public void start() {
        // ...
    }
}
```

Configuration:
```yaml
KeyManagementProfile:
  keyRotationProfile:
    type: myKeyRotation
    useSigningKeyRotation: true
    signingKeyRotationSeconds: 28800
    cleanupFrequencySeconds: 10000
```

See also: <br/> 
[JWT Signer (add custom new signature type to JWT)](https://github.com/gianlucafrei/Application-Gateway/wiki/JWT-Signer-(add-custom-new-signature-type-to-JWT)) <br/> 
[Key management and JWT signer](https://github.com/gianlucafrei/Application-Gateway/wiki/Key-management-and-JWT-signer)