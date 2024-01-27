# Logging

In this section we explain how the log subsystem is configured.

You configure the log in file application.yaml found in the resources folder of OAG.
There the section `logging:` is relevant. This section is standard SpringBoot and you can find details about log configuration [for example here](https://howtodoinjava.com/spring-boot2/logging/configure-logging-application-yml/).

One special thing we are using is the log-pattern. Here we apply `%X{oag.CorrId}`. This makes sure that the trace id (or correlation id) is written with each log statement. So make sure your log pattern contains `%X{oag.CorrId}` if you need log correlation - which we strongly recommend.

When it comes to log destinations we currently ship only what comes with SpringBoot out of the box.