# OAG Configuration File

All functionality of OAG is configured via a central configuration file in the YAML format. You can specify the path of the configuration file via the `OAG_CONFIG_PATH` environment variable. If you don't specify the location, OAG checks if there is a file called `sample-config.yaml` in the current working folder. (Per default OAG releases contain a sample configuration file.)

Besides the configuration file that is loaded during start-up there is also a default configuration. During startup the two files are merged but settings from the custom configuration have precedence. If you want to take a look at the default configuration you finde it [here](https://github.com/gianlucafrei/Application-Gateway/blob/main/oag/src/main/resources/default-config.yaml).

## Configuration file syntax
The configuration file is in YAML format. For details about the specific parts of the configuration please take a look at the following chapters.

You can reference environment variables by using `env:<ENV_NAME>`. This way during deserialisation of the configuration file OAG will replace the content of the setting with the value of the environment variable. This is useful for sensitive settings such as client_secrets. e.g.`clientSecret: env:GITHUB_CLIENT_SECRET` will set the value of the environment variable GITHUB_CLIENT_SECRET as the value of the clientSecret. This currently only works for settings that are strings.

## Top Level Settings

### `hostUri`

Defines the external URI of your OAG instance. It is important that you configure a https url for production instances of OAG.
Default to `http://localhost:8080`.

### `routes`

Defines how traffic is route through OAG. <br/> 
See: [Routes](/docs/Configuration-Routes)

### `loginProviders`

Defines how users can login with OAG. <br/> 
See: [Login Providers](/docs/Configuration-Login-Providers)

### `securityProfiles`

Defines the different security profiles. Default profiles are already configured in the default configuration, but you can add a new one here. <br/> 
See: [Security Profiles](/docs/Configuration-SecurityProfiles)

### `traceProfile`

Defines the request tracing behaviour. <br/> 
See: [Session Behaviour](/docs/Configuration-Session-Behaviour)

### `downstreamAuthentication`

Defines how OAG transport the user principal to the backend. <br/> 
See: [Downstream Authentication](https://github.com/gianlucafrei/Application-Gateway/wiki/Configuration:-DownstreamAuthentication)

## Sample Configuration File
This is a example configuration file:

```yaml
hostUri: http://localhost:8080

routes:

  httpbin:
    type: webapplication
    path: /**
    url: https://httpbin.org
    allowAnonymous: yes

  echo:
    type: webapplication
    path: /echo/**
    url: https://nellydemoapp.azurewebsites.net
    allowAnonymous: yes

loginProviders:
  google:
    type: oidc
    with:
      authEndpoint: https://accounts.google.com/o/oauth2/auth
      tokenEndpoint: https://oauth2.googleapis.com/token
      clientId: 372143946338-48et57uhmcumku7am3ocvva0idc7u0td.apps.googleusercontent.com
      clientSecret: env:GOOGLE_CLIENT_SECRET
      scopes: [ "openid", "email" ]

  github:
    type: github
    with:
      authEndpoint: https://github.com/login/oauth/authorize
      tokenEndpoint: https://github.com/login/oauth/access_token
      clientId: 163ad3b08c3829216ba1
      clientSecret: env:GITHUB_CLIENT_SECRET
      scopes: [ "user", "email" ]

securityProfiles:
  webapplication:
    responseHeaders:
      Server: <<remove>>
      X-Powered-By: <<remove>>
      X-XSS-Protection: 1;mode=block;
      X-Frame-Options: SAMEORIGIN
      X-Content-Type-Options: nosniff
      Referrer-Policy: strict-origin-when-cross-origin
      Content-Security-Policy: base-uri 'self';object-src 'self'
      Permissions-Policy: geolocation=(),notifications=(),push=(),microphone=(),camera=(),speaker=(),vibrate=(),fullscreen=(),payment=(),usb=(),magnetometer=(),gyroscope=(),accelerometer=()
      Strict-Transport-Security: max-age=31536000; includeSubDomains


traceProfile:
  forwardIncomingTrace: false
  maxLengthIncomingTrace: 254
  acceptAdditionalTraceInfo: false
  maxLengthAdditionalTraceInfo: 254
  sendTraceResponse: true
  type: w3cTrace
```
