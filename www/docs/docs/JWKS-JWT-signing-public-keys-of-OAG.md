# JWKS JWT signing public keys of OAG

## Overview
OAG provides an endpoint to request the current (and historical) signing public keys that can be used to verify the signature of OAG signed ID-Tokens (JWT) sent to downstream systems.

## General Endpoint
<p>http(s)://host:port/.well-known/jwks
(https mandatory to make sure attackers cannot inject fraudulent keys).</p>

### Method to request
GET

### Response
A JWK Set as defined in [RFC 7517](https://tools.ietf.org/html/rfc7517) in the body of the http response.

```
{
  "keys":[{
    "kty":"RSA",
    "e":"AQAB",
    "use":"sig",
    "kid":"7a3f3889-944d-46bc-b1bc-cb72532abd4c",
    "alg":"RS256",
    "n":"k4JmoSM4XyhIos9MSUDJxnuSYWSFZpgl_Djujolvn3e8nZKoMaamZM-wKbg9V1vxkFLQzEGk6Vcu8zEkLLdqfPUMoGBBI0VcVT0kMzS8u3n9PVZt4548D2vcOIth58UsRpM0E1mJyjFaZ5gdgNXdORXNq4Kx38jGjw9G5K02CS3T0pzKI2r_i9iq6mVjJv77vz4HAoN2kiDZQOlP3q_42KOPAwADLRwSVEf3P8ViH2OC4QNtnCBtKftcbF_KVMdg60IbgFqrbHIgxDs0Ullfi79p5Pu5MDWXYmjFQOoYnUGIGkIy1EHwC1QgFFTISFRXU5sA_dgzU0FRYgu9oY1QSdWLHPcEoLHtyRrqzUEO6zwgihMdHGJIeG5Fb6b0JvIKJZQKIfgzPzMsfx3OEvS6DejECpG8YnlkUS5HdNb6TRDa-SMSf93owwknzCG_yEp-MjX1NC66vuSkoktORkxOcvpvdNGs-R68yNiDdKRvSNURbGCDu0yY6ST2HNlvvelDX46iGbs83BZXYLvs6-p8-lTFk60YTfoObKKHZU2tJ8wCfTA8qNPAf_gPNW_VlLPBjcJ8FruY9yDGPJTY20GcC_CcRh248LRAYLCpbh4s6Qs4LXCt3jXvq4nZRBpQKUMn5Opj0F9a0ioJ0IBZv4ATWepQPnV2yBIM0HhZ7Qvt3U0"
  }]
}    
```

## Specific endpoint
<p>http(s)://host:port/.well-known/jwks/&lt;kid&gt;
(https mandatory to make sure attackers cannot inject fraudulent keys).
Where &lt;kid&gt; is the key id of the signing key that should be returned.</p>

**Example:** https://oag.owasp.org/.well-known/jwks/7a3f3889-944d-46bc-b1bc-cb72532abd4c

### Method to request
GET

### Response
A JWK Set as defined in [RFC 7517](https://tools.ietf.org/html/rfc7517) in the body of the http response.

```
{
  "keys":[{
    "kty":"RSA",
    "e":"AQAB",
    "use":"sig",
    "kid":"7a3f3889-944d-46bc-b1bc-cb72532abd4c",
    "alg":"RS256",
    "n":"k4JmoSM4XyhIos9MSUDJxnuSYWSFZpgl_Djujolvn3e8nZKoMaamZM-wKbg9V1vxkFLQzEGk6Vcu8zEkLLdqfPUMoGBBI0VcVT0kMzS8u3n9PVZt4548D2vcOIth58UsRpM0E1mJyjFaZ5gdgNXdORXNq4Kx38jGjw9G5K02CS3T0pzKI2r_i9iq6mVjJv77vz4HAoN2kiDZQOlP3q_42KOPAwADLRwSVEf3P8ViH2OC4QNtnCBtKftcbF_KVMdg60IbgFqrbHIgxDs0Ullfi79p5Pu5MDWXYmjFQOoYnUGIGkIy1EHwC1QgFFTISFRXU5sA_dgzU0FRYgu9oY1QSdWLHPcEoLHtyRrqzUEO6zwgihMdHGJIeG5Fb6b0JvIKJZQKIfgzPzMsfx3OEvS6DejECpG8YnlkUS5HdNb6TRDa-SMSf93owwknzCG_yEp-MjX1NC66vuSkoktORkxOcvpvdNGs-R68yNiDdKRvSNURbGCDu0yY6ST2HNlvvelDX46iGbs83BZXYLvs6-p8-lTFk60YTfoObKKHZU2tJ8wCfTA8qNPAf_gPNW_VlLPBjcJ8FruY9yDGPJTY20GcC_CcRh248LRAYLCpbh4s6Qs4LXCt3jXvq4nZRBpQKUMn5Opj0F9a0ioJ0IBZv4ATWepQPnV2yBIM0HhZ7Qvt3U0"
  }]
}
```
