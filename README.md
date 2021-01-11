# Nellygateway

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/gianlucafrei/nellygateway)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5eaa206a103e4b28be9da2ba857d1653)](https://app.codacy.com/gh/gianlucafrei/nellygateway?utm_source=github.com&utm_medium=referral&utm_content=gianlucafrei/nellygateway&utm_campaign=Badge_Grade)
![Java CI with Maven](https://github.com/gianlucafrei/nellygateway/workflows/Java%20CI%20with%20Maven/badge.svg)

An elephant strong web application gateway that handles oauth2 authentication and session management.

🏗️ **Nellygateway is work-in-progress. No productive version has been released yet.**

Nellygateway is an HTTP reverse proxy that sits between your web application and the client and handles Oauth2 login and session management. For you, as a developer, Nelly removes the hassle to implement complicated oauth2 logic in the backend and frontend so you can focus totally on your applications logic.

<img src="https://github.com/gianlucafrei/nellygateway/blob/main/doc/overview.png?raw=true" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="500" />

## Table of Contents

- [Nellygateway](#nellygateway)
  - [Table of Contents](#table-of-contents)
  - [Design Principles](#design-principles)
    - [Secure by default](#secure-by-default)
    - [Stateless](#stateless)
    - [Configuration based](#configuration-based)
  - [Configuration File](#configuration-file)
  - [How to run Nelly](#how-to-run-nelly)
    - [Docker Release](#docker-release)
    - [Jar release](#jar-release)
    - [Compile Nelly Yourself](#compile-nelly-yourself)
  - [Functionality](#functionality)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>


## Design Principles

### Secure by default

Implementing secure logins and session management became much more complicated within the last few years. Nellygateway aims to make this easier. Also, it implements many security hardening measures out of the box.

### Stateless

Wherever possible, Nelly is stateless. All session information is stored within encrypted cookies on the clients. Stateless session management makes it a lot easier to deploy Nelly on multiple nodes.

### Configuration based

Nelly's behavior is controlled with a central configuration file describing all routes and Oauth2 integrations. This makes it easier to review the configuration for security issues and to debug on different environments. The deployment and scaling are straightforward; configure the config file's file path, and that's all you need to do.

## Configuration File

Nelly is fully configured with a simple and easy to understand configuration file. Details are documented in the [GitHub wiki](https://github.com/gianlucafrei/nellygateway/wiki).

```yaml
hostUri: http://example.com

routes:
  default:
    type: webapplication
    path: /**
    url: https://nellydemoapp.azurewebsites.net
    allowAnonymous: yes
  authenticated:
    type: webapplication
    path: /secure/**
    url: https://nellydemoapp.azurewebsites.net
    allowAnonymous: no

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

sessionBehaviour:
  sessionDuration: 3600
  redirectLoginSuccess: /app
  redirectLoginFailure: /uups
  redirectLogout: /

securityProfiles:
  webapplication:
    allowedMethods: [ "GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS", "HEAD" ]
    csrfProtection: samesite-strict-cookie
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

logoutRedirectUri: http://example.com/
nellyApiKey: env:NELLY_API_KEY
trustedRedirectHosts: [subdomain.example.com]
```

## How to run Nelly

You have two options on how to run Nelly: There is an official docker image that you can just works out of the box. You just need to mount the nelly config file via docker volumes. If you don't want to use docker you can also use the download the released jar file. Of course you can also build Nelly by yourself with Maven.

### Docker Release

You can find the Nelly Docker image at https://hub.docker.com/r/gianlucafrei/nellygateway

Download and Start:
```bash
# Download image of nelly
docker pull gianlucafrei/nellygateway:main-SNAPSHOT

# Download sample config and adapt it to your needs
curl https://raw.githubusercontent.com/gianlucafrei/nellygateway/main/nellygateway/sample-nelly-config.yaml >> nelly-config.yaml
vim nelly-config.yaml

# Start the container
docker run -e NELLY_CONFIG_PATH=/app/config.nelly -v ${PWD}/nelly-config.yaml:/app/config.nelly gianlucafrei/nellygateway:main-SNAPSHOT
```

### Jar release

todo

### Compile Nelly Yourself

todo

## Functionality

- [x] HTTPS Redirection with Proxy Awareness
- [x] OpenID Connect Login with multiple providers
- [x] Multiple Backend routes
- [x] Authenticated routes
- [x] Request Logging
- [x] Add and remove response headers
- [x] Secure, HTTP-only and same-site session cookies
- [x] Forward id token to backend
- [x] Upstream authentication with API key
- [x] GitHub Login support
- [x] Method whitelisting
- [x] CSRF protection
- [x] Rolling sessions 

Ideas:

- [ ] Header whitelisting
- [ ] Report URI Endpoint
- [ ] Default configuration
- [ ] ...
