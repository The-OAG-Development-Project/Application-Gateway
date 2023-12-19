# JWT Signer (add custom new signature type to JWT)

## Overview
To provide a new JWT signer, you will have to create the following components:
- A custom JwtSignerFactory
- A custom JwtSigner

If you want to profit from automatic key rotation and your signing key being published/avvailable in the JWKS endpoint you need also to implement:
- A custom JwkStore
- A custom KeyGenerator

Note: As of now OAG only supports one JWT signer for an asymmetric signature and one for a symmetric algorithm at the same time with automatic key rotation.

## JwtSignerFactory
When you want to use automatic key rotation, take a look at the RSAJwtSignerFactory. When using a keystore or another configuration option to pass in the key, you may want to look at the HmacJwtSignerFactory.
Your component needs to be named as follows: \<uniqueName\>-jwt-signer-factory
````java
@Component("signerAlgoName" + JWT_SIGNER_FACTORY_BEAN_POSTFIX)
public class MyJwtSignerFactory implements JwtSignerFactory {

    @Autowired
    CurrentSigningKeyHolder keyHolder;

    public JwtSigner create(String hostUri, Map<String, Object> settings) {

       ...

        return new MyJwtSigner(...);
    }
````
Note: In general, make sure you return a unique instance of MyJwtSigner, else key rotation may not work.
You can now configure the 'signerAlgoName' as `signatureImplementation` in section `userMapping`.

## JwtSigner
When you want to use asymmetric signatures, take a look at the RSAJwtSigner. When creating a symmetric signer, you may want to look at the HmacJwtSigner.
Your component needs to be named as follows: \<uniqueName\>-jwt-signer-factory
``` java
public class MyJwtSigner extends JwtSigner {
 
    public RsaJwtSigner(PrivateKey signingKey, String kid, ....) {
       ...
    }

    @Override
    public boolean supportsJku() {
        return true; // only when using asymmetric
    }

    @Override
    public URI getJku() {
        return jku;
    }

    @Override
    protected String getKeyId() {
        return kid;
    }

    @Override
    protected JWSAlgorithm getSigningAlgorithm() {
        return signingAlgo;
    }

    @Override
    protected JWSSigner getJwtSigner() {
        return signer;
    }
}
```

## JwkStore
The JwkStore provides the storage for the keys available with the JWKS Endpoint and is automatically updated by the key rotation.
If you need to distribute keys between multiple instances of OAG, you could consider implementing this in a dedicated JwkStore too.
You will have to implement JwkStore interface and give your component a unique name. See `LocalRsaJwkStore`for an example.
```Java
@Component("myJwkStore")
public class MyJwkStore implements JwkStore {
...
}
```

You configure the JwkStore in the KeyManagementProfile:
```yaml
keyManagementProfile:
  ...
  jwkStoreProfile:
    type: myJwkStore
    implSpecificSettings:
      exampleParameter: "if you need parameters"
```

## KeyGenerator
The key generator creates new keys and is invoked by the automatic key rotation. It is responsible to generate the "right" key for the Jwt signing algorithm you want to provide. You should give the component a unique name. See `RsaKeyGenerator`for an example.
```Java
Component("myKeyGenerator")
public class RsaKeyGenerator implements KeyGenerator {
...
}
```

You configure the JwkStore in the KeyManagementProfile:
```yaml
keyManagementProfile:
  ...
  keyGeneratorProfile:
    type: myKeyGenerator
    keySize: 4096
    implSpecificSettings:
      exampleParameter: "if you need extra parameters"
```