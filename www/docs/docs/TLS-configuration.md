# TLS configuration in OAG
## Default situation

By default OAG is built with a self-signed TLS certificate (for localhost) that is used to provide TLS.
Usually this is not what you want and you need to replace it with a production strength certificate.

# Options to change the certificate

OAG is a SpringBoot application and uses the SpringBoot way to configure TLS. This means we ship an application.yaml as
part of the fat jar. It is containing the following settings:

```
server:
  port: 8080
  ssl:
    key-store-type: PKCS12
    key-store: keystore.pkcs12
    key-store-password: password
    key-alias: tls
    enabled-protocols: TLSv1.3
```

you can now change this settings by all means SpringBoot supports, including:

1. Providing your own application.yaml OR
2. Override the settings with environment variables

## Provide your own application.yaml

You can override the application.yaml with your own version by eigther creating your own docker image or starting the
provided docker image with an environment variable JAVA_OPTS such as:

```
JAVA_OPTS="--spring.config.location=path/to/your/application.yaml" 
```

(Make sure your application.yaml is properly protected in the file-system).
The easiest way to get the original application.yaml to customize is by downloading it directly from the git repository:
[https://raw.githubusercontent.com/The-OAG-Development-Project/Application-Gateway/refs/heads/main/oag/src/main/resources/application.yaml](https://raw.githubusercontent.com/The-OAG-Development-Project/Application-Gateway/refs/heads/main/oag/src/main/resources/application.yaml)

## Override the settings with environment variables

Similar as above you can use JAVA_OPTS to override the settings you need to change. For example:

```
JAVA_OPTS="-Dserver.ssl.key-store=path/to/your/keystore -Dserver.ssl.key-store-password=yourPassword"
```

(Make sure only the oag process has access to the environment-variable).
