# Tracing, Log Correlation

In this section we explain how you can hook your own trace implementation into the trace subsystem.

If you need, you can write your own implementation for tracing / log correlation.\
As a good point to start you may take a look at class `...logging.simpleTrace.SimpleTraceContext`.

## Define class and bean name
Your trace implementation must implement interface `...logging.TraceContext` and declared to be a named component with prototype scope.\
The name you provide for the component must then be configured in the master configuration file.\
Example:
```java
@Component("myTrace")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MyTraceContext implements TraceContext {...}
```
## Implement methods
Study the Javadoc of `...logging.TraceContext` to understand under what conditions the methods are invoked by OAG. Implement them accordingly.\
If you do not support some of the common configuration settings in the `traceProfile`:
* Return the value matching the intention of your implementation. E.g. if your implementation does not support upstream trace id communication, make sure you hardcode `return false;` in the `sendTraceResponse` method implementation.
* Document this in Javadoc of your class (recommended)

## Configure your custom implementation to be used by OAG
The name you provided for the component must be configured in the master configuration file, section `traceProfile`, attribute `type` in order to be used by OAG.\
For the example provided above, the required configuration is:
```yaml
traceProfile:
  type: myTrace
  ...
```

## Test your implementation
Write tests to verify functionality of your trace implementation. Start OAG and verify your trace id is found in the log and sent to downstream and upstream systems as required.
