# Nellygateway
An elephant strong web application gateway that handles oauth2 authentication and session management.

🏗️ **Nellygateway is work-in-progress. No productive version has been released yet.**

<img src="https://github.com/gianlucafrei/nellygateway/blob/main/doc/overview.png?raw=true" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="500" />

## What is Nelly?

Nellygateway is an HTTP reverse proxy that sits between your web application and the client and handles Oauth2 login and session management. For you, as a developer, Nelly removes the hassle to implement complicated oauth2 logic in the backend and frontend so you can focus totally on your application.

## What are the design principles for Nelly?

### Secure by default
Implementing secure logins and session management became much more complicated within the last few years. Nellygateway aims to make this easier. Also, it implements many security hardening measures out of the box.

### Stateless

Wherever possible, Nelly is stateless. All session information is stored within encrypted cookies on the clients. Stateless session management makes it a lot easier to deploy Nelly on multiple nodes.

### Configuration based

Nelly's behavior is controlled with a central configuration file describing all routes and Oauth2 integrations. This makes it easier to review the configuration for security issues and to debug on different environments. The deployment and scaling are straightforward; configure the config file's file path, and that's all you need to do.
