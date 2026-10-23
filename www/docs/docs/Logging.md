# Logging

In this section we explain how the log subsystem is configured.

## Where the configuration lives

OAG configures logging with Logback directly, in the file `logback.xml` found in the resources folder of OAG.
The `logging:` section of `application.yaml` is not used.

The shipped configuration defines two appenders:

* `CONSOLE` — writes to standard output, with the log level highlighted in colour.
* `FILE-ROLLING` — writes to `logs/oag.log` and rolls the file over by size and by day.

Both are attached to the root logger, which is set to `INFO`. The `org.owasp` and `org.springframework.web`
loggers are set to `DEBUG`, which is useful while developing. For production we suggest a root level of
`WARN`, `org.owasp` at `INFO`, and no additional loggers configured.

## Changing the configuration

You can either edit `logback.xml` before building your own image, or point OAG at a configuration file of your
own without rebuilding:

```
java -Dlogback.configurationFile=/path/to/your/logback.xml -jar oag.jar
```

Everything Logback supports is available to you — additional appenders, other log destinations, per-logger
levels and your own patterns. See the [Logback documentation](https://logback.qos.ch/manual/configuration.html)
for the details.

## Where the log files are written

The rolling file appender writes relative to the process working directory, so log files end up in `./logs`.
In the official docker image the working directory is `/app`, which means logs are written to `/app/logs`.
Mount a volume there if you want the files to survive a container restart, or remove the `FILE-ROLLING`
appender from the root logger and rely on the console output if your platform collects standard output
anyway.

Archived files are named `logs/oag.<date>.<index>.log.gz`. The shipped rollover policy caps a single file at
10 MB, retains 60 days of history and limits the total size of all archived files to 20 GB. Size these values
for the disk that is actually available to your deployment.

## Writing the trace id with every log statement

One special thing we are using is the log-pattern. Here we apply `%scopedContext{oag.CorrId}`. This makes sure
that the trace id (or correlation id) is written with each log statement. So make sure your log pattern
contains `%scopedContext{oag.CorrId}` if you need log correlation — which we strongly recommend.

The trace id is held in a scoped value rather than in Logback's classic MDC, because OAG binds it around the
request rather than attaching it to a thread. Two consequences:

* `%X{oag.CorrId}` does **not** print the trace id. Use `%scopedContext{oag.CorrId}` instead.
* The conversion word has to be registered in your configuration before a pattern can use it:

```xml
<conversionRule conversionWord="scopedContext"
                converterClass="ch.qos.logback.classic.scoped.ScopedMDCConverter"/>
```

The converter reads the scoped value at the moment the log line is formatted. That works with the synchronous
appenders OAG ships. If you add an asynchronous appender, formatting happens on a different thread where the
scoped value is no longer bound, and the trace id comes out empty.
