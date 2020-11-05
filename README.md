# Nellygateway
An elephant strong web application gateway that handles oauth2 authentication and session management.

🏗️ **Nellygateway is work-in-progress. No productive version has been released yet.**


## What is Nelly?

Nellygateway is an HTTP reverse proxy that sits between your web application and the client and handles Oauth2 login and session management. For you, as a developer, Nelly removes the hassle to implement complicated oauth2 logic in the backend and frontend so you can focus totally on your application.

<img src="https://github.com/gianlucafrei/nellygateway/blob/main/doc/overview.png?raw=true" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="500" />

## What are the design principles for Nelly?

### Secure by default
Implementing secure logins and session management became much more complicated within the last few years. Nellygateway aims to make this easier. Also, it implements many security hardening measures out of the box.

### Stateless

Wherever possible, Nelly is stateless. All session information is stored within encrypted cookies on the clients. Stateless session management makes it a lot easier to deploy Nelly on multiple nodes.

### Configuration based

Nelly's behavior is controlled with a central configuration file describing all routes and Oauth2 integrations. This makes it easier to review the configuration for security issues and to debug on different environments. The deployment and scaling are straightforward; configure the config file's file path, and that's all you need to do.

## Config File
Nelly is fully configured with a simple and easy to undertand configuration file:

```yaml
hostUri: https://yourdomain.com

routes:
  frontend:
    type: webapplication
    path: /**
    url: http://yourserver/frontend
  backend
    type: webapplication
    path: /api/**
    url: http://yourotherserver/api/v1/

authProviders:
  google:
    authEndpoint: https://accounts.google.com/o/oauth2/auth
    tokenEndpoint: https://oauth2.googleapis.com/token
    clientId: yourclientId.apps.googleusercontent.com
    sessionDuration: 300
    redirectSuccess: https://yourdomain.com/myportal

securityProfiles:
    webapplication:
      headers:
        X-Powered-By: <<remove>>
        X-XSS-Protection: 1;mode=block;
        X-Frame-Options: SAMEORIGIN
        X-Content-Type-Options: nosniff
        Referrer-Policy: strict-origin-when-cross-origin
        Content-Security-Policy: upgrade-insecure-requests;base-uri 'self';object-src 'self'

logoutRedirectUri: https://yourdomain.com
```
