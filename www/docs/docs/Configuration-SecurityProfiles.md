# Security Profiles

Security profiles allow you to define different security behavior for different routes. OWAG already comes with a predefined set of security profiles that cover most use-cases. However, you can always add your own profile in the config file or overwrite a default security profile.

Example:

```yaml
securityProfiles:
  webapplication:
    allowedMethods: [ "GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS", "HEAD" ]
    csrfProtection: samesite-strict-cookie
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

## Configuration

### `allowedMethods`

Defines the allowed HTTP method. Requests with HTTP methods that are not whitelisted will be blocked with a `405 Method Not Allowed` response.

### `csrfProtection`

Defines the type of CSRF Protection. Please check [here](#csrf-protections) for more details.

### `csrfSafeMethods`

Defines the list of HTTP methods that are excluded from the CSRF protection.

### `responseHeaders`

Defines actions for response headers. Currently, the following types are supported:

* `<<remove>>` Removes the header from the response.
* `value` sets or overwrites the header in the response.

### `userMapping`

Defines the way how OAG tells the backend who the user is. </br>
See also: [User Mapping](/docs/Configuration-User-Mapping)

## Predefined Profiles

* `static`: This is for static content. It adds default security headers and only allowed the GET, HEAD, and OPTIONS method. If you have a single page application this is profile is suitable to host static application files.
* `webapplication`: This profile is suitable for most webapplications. It allows all default normal http methods and adds csrf protection via samesite cookie.
* `apiforspa` Use this profile if you have an API that is called from the web-browser. It adds csrf protection with the double submit cookit pattern. You need to implement the custom header when you call the api from your code.
* `apifornonebrowsers`  Use this profile only for apis that are not called from the browser. It removes most security functionality of OWAG.

You can see the detailed configuration of all predefined profiles in the default configuration file [here](https://github.com/gianlucafrei/nellygateway/blob/main/nellygateway/src/main/resources/default-config.yaml)

## Details

A security profile consists of the following configuration values:

```yaml
   allowedMethods: [ "GET", "PUT", "POST"] # List if allowed http methods
   csrfProtection: doubleSubmitCookie # type of csrf protection
   responseHeaders: #Here you can add a set of response header
      Server: <<remove>> # Use <<remove>> to remove a header from the backend response
      X-Content-Type-Options: nosniff # OWAG adds the X-Content-Type-Options with the nosniff value to each response. Existing header will be overwritten.
      ... # You can add as many different headers as you want
```

### CSRF Protections

OWAG implements the following strategies for csrf protection.

#### `double-submit-cookie`

With this mode OWAG requires the `csrf` cookie in the `X-CSRF-TOKEN` header. This method of csrf protection in only applicable for XHR requests and recommened when you have a singe page application. If you use form posts you can also add the token as a hidden field with name `CSRFToken` to the form.

This method of csrf protection is the strongest because OWAG cryptographically binds the csrf cookie to the session cookie, but the downside of this method is that your application needs to implement the following logic for every XHR request:
```js
<script type="text/javascript">
    function getCookie(name) {
       var nameEQ = name + "=";
       var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0)==' ') c = c.substring(1,c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
        }
        return null;
    }
    var csrf_token = getCookie('csrf');
    function csrfSafeMethod(method) {
        // these HTTP methods do not require CSRF protection
        return (/^(GET|HEAD|OPTIONS)$/.test(method));
    }
    var o = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(){
        var res = o.apply(this, arguments);
        var err = new Error();
        if (!csrfSafeMethod(arguments[0])) {
            this.setRequestHeader('X-CSRF-TOKEN', csrf_token);
        }
        return res;
    };
 </script>
```
***

#### `samesite-strict-cookie`

With this mode, OWAG uses a same-site strict cookie to protect your users from csrf attacks.

Most browsers except ie do implement the same-site strict cookie. See [SameSite Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite).
***

#### `none`
OWAG does not protect you against any csrf attack. Use this if your route does not need any csrf protection (for example only static content) or if you implemented csrf protection in your backend