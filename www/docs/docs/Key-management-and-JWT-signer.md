# Key management and JWT signer

## Key management configuration
This is a top level configuration element.

```yaml
keyManagementProfile:
  keyRotationProfile:
    type: defaultKeyRotation
    useSigningKeyRotation: true
    signingKeyRotationSeconds: 28800
    cleanupFrequencySeconds: 10000
  jwkStoreProfile:
    type: localRsaJwkStore
    implSpecificSettings:
      exampleParameter: "not used"
  keyGeneratorProfile:
    type: rsaKeyGenerator
    keySize: 4096
    implSpecificSettings:
      exampleParameter: "not used"
```

**keyRotationProfile part**

- `type`: Defines the key rotation strategy. Currently supported: 'defaultKeyRotation'.
- `useSigningKeyRotation`: Set to true to automatically generate a new key from time to time (recommended). Set to false to only generate a key at startup.
- `signingKeyRotationSeconds`: Rotate the signing key after that many seconds.
- `cleanupFrequencySeconds`: Run memory clean-up of expired signing keys after that many seconds. (Note that signing keys have a grace period during which they are still served when requested anyway built-in.)

**jwkStoreProfile part**

- `type`: Defines what type of keys the signing key store can hold. Currently supported: `localRsaJwkStore`.
- `implSpecificSettings`: Key/value pairs required by the store implementation selected above. `localRsaJwkStore` needs none.

**keyGeneratorProfile part**
- `type`: The key generator to use. Currently supported: `rsaKeyGenerator`.
- `keySize`: The number of bits for generated keys.
- `implSpecificSettings`:  key generator impl specific settings. `rsaKeyGenerator` requires no additional settings.

### key rotation implementations
#### `defaultKeyRotation`
Implements a key rotation scheme that uses the configured keyGenerator to generate new keys and make them available in the configured jwkStore.
Makes sure that on startup a new signing key is generated.
Updates also the currentSigningKeyHolder with the new key. Heeds the standard configuration settings and does not require any `implSpecificSettings`.

### jwkStore implementations
#### `localRsaJwkStore`
Stores signing keys as long as they are valid (plus a grace time of 10 minutes) in memory and local only.
Does not require any `implSpecificSettings`.

### key generator implementations
#### `rsaKeyGenerator`
Generates RSA keys.
Does not require any `implSpecificSettings`.

## Jwt signer
The key signer used for a JWT passed to downstream system is configured in the security profile assigned to a route and only available for 'jwt-mapping'.

```yaml
userMapping:
      type: "jwt-mapping"
      settings:
        ...
        signatureImplementation: "rsa"
        signatureSettings:
```

**relevant settings part**
- `signatureImplementation`: The signature implementation to use. Currently supported: `rsa`, `hmac` (not recommended).
- `signatureSettings`: signature implementation specific settings. `rsa` requires none.

### jwt signers (`signatureImplementation`)
#### `rsa`
Implements RS256 JWT signature based on automatically generated (zero-config) keys as implemented by the key rotation configuration.
Adds kid and jku headers to the JWT.
Does not need any configuration settings.

#### `hmac` (not recommended)
Implements HS256, HS385, HS512 (derived  from key size) JWT signatures.
Adds kid header, but naturally no jku header to the JWT.
Requires these `signatureSettings`:
```yaml
...
  signatureSettings:
    keyId: "myKeyId"
    secretKey: "546573744b6579546573744b6579546573744b6579546573744b657....9"
```
**signatureSettings part**
- `keyId`: The kid to be put in the JWT header.
- `secretKey`: The hey encoded shared key used for signing the JWT. Depending on its length HS256 (for 256 bit key), HS348 or HS512 is automatically used.

:::warning
Beware that the jwkStore, the keyGenerator as well as the jwtSigner used depend on each other! So if you use an RSA based JWT signer, you also need an RSA based jwkStore and keyGenerator. The keyRotation implementation is independent and relies on jwkStore and keyGenerator only.
:::