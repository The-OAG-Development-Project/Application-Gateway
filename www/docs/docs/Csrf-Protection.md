# CsrfProtection

If you need a different CSRF protection mechanism, you can implement this by providing your own component.
1. Implement the CsrfProtectionValidation interface
2. Give your component a unique name that follows this pattern: Csrf<NameUsedInConfig>Validation
3. Configure it in the securityProfile section of the settings.

```Java
@Component
public class CsrfMySpecialValidation implements CsrfProtectionValidation {
// ...
}
```

Configuration:
```yaml
securityProfiles:
  webapplication:
    allowedMethods: [ "GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS", "HEAD" ]
    csrfProtection: mySpecial
    csrfSafeMethods: [ "GET", "OPTIONS", "HEAD" ]
    responseHeaders:
      Server: <<remove>>
      X-Powered-By: <<remove>>
      X-XSS-Protection: 1; mode=block;
      X-Frame-Options: SAMEORIGIN
      X-Content-Type-Options: nosniff
      Referrer-Policy: strict-origin-when-cross-origin
      Content-Security-Policy: upgrade-insecure-requests;base-uri 'self';object-src 'self'
      Permissions-Policy: geolocation=(),notifications=(),push=(),microphone=(),camera=(),speaker=(),vibrate=(),fullscreen=(),payment=(),usb=(),magnetometer=(),gyroscope=(),accelerometer=()
      Strict-Transport-Security: max-age=31536000; includeSubDomains
```
