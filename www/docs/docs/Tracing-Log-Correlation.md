# Tracing, Log Correlation

In this section we explain how you can hook your own trace implementation into the trace subsystem.

If you need, you can write your own implementation for tracing / log correlation.\
As a good point to start you may take a look at class `...logging.simpleTrace.SimpleTraceContext`.

## Define the context and its factory
Your trace implementation implements interface `...logging.TraceContext`. OAG needs a fresh instance for every request, so the
context is a plain class and a `...logging.TraceContextFactory` component creates it. The factory's `type()` is the value
configured in the master configuration file.\
Example:
```java
public class MyTraceContext implements TraceContext {/*...*/}

@Component
public class MyTraceContextFactory implements TraceContextFactory {

    @Override
    public String type() {
        return "myTraceContext";
    }

    @Override
    public TraceContext create() {
        return new MyTraceContext();
    }
}
```
If the context needs the configuration, inject `MainConfig` into the factory and pass it to the context's constructor
(see `...logging.simpleTrace.SimpleTraceContextFactory`).
## Implement methods
Study the Javadoc of `...logging.TraceContext` to understand under what conditions the methods are invoked by OAG. Implement them accordingly.\
If you do not support some of the common configuration settings in the `traceProfile`:
* Return the value matching the intention of your implementation. E.g. if your implementation does not support upstream trace id communication, make sure you hardcode `return false;` in the `sendTraceResponse` method implementation.
* Document this in Javadoc of your class (recommended)

## Configure your custom implementation to be used by OAG
The value returned by your factory's `type()` must be configured in the master configuration file, section `traceProfile`, attribute `type` in order to be used by OAG.\
For the example provided above, the required configuration is:
```yaml
traceProfile:
  type: myTraceContext
  # ...
```

## Test your implementation
Write tests to verify functionality of your trace implementation. Start OAG and verify your trace id is found in the log and sent to downstream and upstream systems as required.
