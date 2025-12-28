Security profiles allow you to define different security behavior for different routes. OAG already comes with
a [predefined set of security profiles](#predefined-profiles) that cover most use-cases. However, you can always add
your own profile in the config file or overwrite a default security profile.

Example :

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

### name -> webapplication

Each security profile needs a unique name (webapplication in the example above). For predefined
names [see below](#predefined-profiles).

### `allowedMethods`

Defines the allowed HTTP method. Requests with HTTP methods that are not whitelisted will be blocked with a
`405 Method Not Allowed` response.
HTTP Methods are documented for example
at [Mozilla Developer Network](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods).
From a security point of view you should only enable the Methods required for your application to work.

### `csrfProtection`

Defines the type of CSRF Protection. Please check [here](#csrf-protections) further down in this document for more
details.

### `csrfSafeMethods`

Defines the list of HTTP methods that are excluded from the CSRF protection.

### `responseHeaders`

By default OAG returns all headers set by the downstream-service response to the client.
You can manipulate this with actions defined in this section.
The existing actions for response headers are currently, the following types:

* `<<remove>>` Removes the header from the response.
* `value` sets or overwrites the header in the response.

In the example below the `Server` header is of type `<<remove>>` and therefore removed if set in the response from the
downstream-service (clients will never see a `Server` header). The `X-Content-Type-Options` header is of type `value`
and therefore overwrites an existing header of the same name or adds it if no such header exists in the response of the
downstream service (clients will always see `X-Content-Type-Options: nosniff` regardless of what the downstream service
sets.

```yaml
   responseHeaders: #Here you can add a set of response header
      Server: <<remove>> # Use <<remove>> to remove a header from the backend response
      X-Content-Type-Options: nosniff # OAG adds the X-Content-Type-Options with the nosniff value to each response. Existing header will be overwritten.
      ... # You can add as many different headers as you want
```

For details regarding what headers you might want to remove or add, see for
example [OWASP Cheatsheet](https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Headers_Cheat_Sheet.html).

### `userMapping`

Defines the way how OAG tells the backend who the user is. See
also: [User Mapping](https://github.com/gianlucafrei/Application-Gateway/wiki/Configuration:-User-Mapping)

## Predefined Profiles

* `static`: This is for static content. It adds default security headers and only allowed the GET, HEAD, and OPTIONS
  method. If you have a single page application this is profile is suitable to host static application files.
* `webapplication`: This profile is suitable for most webapplications. It allows all default normal http methods and
  adds csrf protection via samesite cookie.
* `apiforspa` Use this profile if you have an API that is called from the web-browser. It adds csrf protection with the
  double submit cookit pattern. You need to implement the custom header when you call the api from your code.
* `apifornonebrowsers`  Use this profile only for apis that are not called from the browser. It removes most security
  functionality of OAG.

You can see the detailed configuration of all predefined profiles in the default configuration
file: https://github.com/The-OAG-Development-Project/Application-Gateway/blob/main/oag/src/main/resources/default-config.yaml

## CSRF Protections

OAG implements the following strategies for csrf protection.

### `double-submit-cookie`

With this mode OAG requires the `csrf` cookie in the `X-CSRF-TOKEN` header. This method of csrf protection in only
applicable for XHR requests and recommened when you have a singe page application. If you use form posts you can also
add the token as a hidden field with name `CSRFToken` to the form.

This method of csrf protection is the strongest because OAG cryptographically binds the csrf cookie to the session
cookie, but the downside of this method is that your application needs to implement the following logic for every XHR
request:

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

### `samesite-strict-cookie`

With this mode, OAG uses a same-site strict cookie to protect your users from csrf attacks.

Most browsers except IE do implement the same-site strict cookie.
See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite.
***

### `none`

OAG does not protect you against any csrf attack. Use this if your route does not need any csrf protection (for example
only static content) or if you implemented csrf protection in your backend

## Example configuration

A security profile consists of the following configuration values:

```yaml
   allowedMethods: [ "GET", "PUT", "POST" ] # List if allowed http methods
   csrfProtection: doubleSubmitCookie # type of csrf protection
   responseHeaders: #Here you can add a set of response header
     Server: <<remove>> # Use <<remove>> to remove a header from the backend response
     X-Content-Type-Options: nosniff # OAG adds the X-Content-Type-Options with the nosniff value to each response. Existing header will be overwritten.
     ... # You can add as many different headers as you want
```