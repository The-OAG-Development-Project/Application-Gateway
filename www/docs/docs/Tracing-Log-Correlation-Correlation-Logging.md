# Tracing, Log Correlation, Correlation Logging

In this section we explain how the trace subsystem is configured.

OAG comes with a set of tracing implementations. A unique trace id is applied to each request reaching OAG.
OAG can be configured if it should accept passed in trace id's from the caller (which we usually do not recommend).

Currently OAG ships with these implementations of tracing:
* `w3cTrace`: The default implementation used by OAG is compliant with the [W3C Trace Context specification](https://w3c.github.io/trace-context/) and using the specified standard headers to communicate the trace id to downstream and upstream systems.
* `simpleTrace`: An implementation using a simple UUID as unique trace id and a default header of X-Correlation-Id to pass the trace id to downstream systems.
* `noTrace`: An implementation disabling all tracing functionality (which we do not recommend).


Example configuration as found in the main configuration file:
```yaml
traceProfile:
  forwardIncomingTrace: false
  maxLengthIncomingTrace: 254
  acceptAdditionalTraceInfo: false
  maxLengthAdditionalTraceInfo: 254
  sendTraceResponse: false
  type: w3cTrace
  traceImplSpecificSettings:
    traceImplSpecificParameter: "not used"
```

## Attributes of the configuration

### `traceProfile`
The section name where tracing / log correlation relevant configuration is specified.\
Note that the exact means how trace information is sent to downstream and upstream systems (i.e. what http header names are used) is trace implementation specific and documented and defined by the trace implementation. Whatever additional information is required to be configured should be put in `traceImplSpecificSettings`.

### `forwardIncomingTrace`
Default: false.\
Possible values: true, false\
Defines if an incoming trace id from an upstream system should be used by OAG internally and forwarded to downstream systems.
We recommend to keep the default value. Especially when OAG is the entry point into a trust zone (i.e. sits at the trust boundary). False means, that OAG always creates a new unique trace id for every request received from upstream systems and sends this to downstream systems.

### `maxLengthIncomingTrace`
Default 254\
Possible values: 0..max Int\
Defines the maximum length that is accepted as an incoming (i.e. sent by upstream system) trace id. Should be as short as possible. Some trace implementations may ignore this value when for example it is specified how long the trace id can be.

### `acceptAdditionalTraceInfo`
Default: false\
Possible values: true, false\
Defines if secondary trace info provided by upstream systems should be accepted by OAG (potentially internally used) and sent to downstream systems.
We recommend to keep the default for the same reason as stated for `forwardIncomingTrace`. Some trace implementations may not support secondary trace information in which case this setting is ignored.

### `maxLengthAdditionalTraceInfo`
Default 254\
Possible values: 0..max Int\
Defines the maximum length that is accepted as incoming (i.e. sent by upstream system) additional trace info. Should be as short as possible. Some trace implementations may ignore this value when for example it is specified how long the additional trace info can be.

### `sendTraceResponse`
Default: false\
Possible values: true, false\
Defines if OAG should send the used trace id to upstream systems. This may be useful to simply debugging from the client side.

### `type`
Default: w3cTrace\
Possible values: w3cTrace, simpleTrace, noTrace, \<Beanname of custom trace implementation see [here](/docs/Tracing-Log-Correlation)\>
* `w3cTrace`: Uses http-headers for upstream and downstream trace id communication as specified by [W3C Trace Context](https://w3c.github.io/trace-context/). Ignores `maxLengthIncomingTrace`as the length of a trace id is specified. Supports all other configuration options. Does not need any `traceImplSpecificSettings`.
* `simpleTrace`: Creates a random UUID as trace id. Ignores the following configuration settings: `acceptAdditionalTraceInfo`,`maxLengthAdditionalTraceInfo` because it does not support any additional trace information beside the trace id. Uses as a default http-header X-Correlation-Id to communicate the trace id to upstream (depending on setting `sendTraceResponse`) and downstream systems. This can be customized by providing the following `traceImplSpecificSettings`:
  * `headerName: <X-your-header>`
* `noTrace`: Disabe tracing. Does only use a trace id for OAG internal logs, does not forward the traceId to downstream systems and does not take over any traceId from upstream systems. Ignores all trace configuration. This effectively disables the trace sub-system - which we do not recommend.
* Custom trace implementation: You can write your own trace implementation if you need. See [here](/docs/Tracing-Log-Correlation) for instructions.

## Configuring how the trace id is written to the log
In the file `application.yaml` you can change the log-pattern to match your needs where and how the trace id is logged with each statement. The trace id is available in the variable `oag.CorrId`. In the log pattern you can therefore use `%X{oag.CorrId}` to print the trace id.

