# Automatic Key Rotation

If you need more advanced key rotation, you can implement this by providing your own component.
Implement the KeyRotation interface, give your component a unique name and configure it in the KeyManagement section of the settings.
Note that depending on your implementation you might also have to provide different implementations for JWTSigner, KeyGenerator, JwkStore. Yet if you follow the guidance found in DefaultKeyRotation that  should not be required.
```Java
@Component("MyKeyRotation")
public class MyKeyRotation implements KeyRotation {
...
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