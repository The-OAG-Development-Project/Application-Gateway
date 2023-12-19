# Terms you need to understand

Here we introduce the most important terms we use and explain what they mean in the context of OAG.

| Term | Explanation |
| ---- | ----------- |
| Downstream System | Any system or application that is called (forwarded to) by OAG. Often services and applications implementing business logic. |
| Upstream System | Callers (initiating requests) of OAG. Often also known as clients. |
| Logging | Writing information about the inner workings (state) of OAG to a file or other destination. |
| Tracing | The goal of tracing is to following a flow of activity and data processing (often known as request) through one or more systems/applications involved. |
| Log correlation / Correlation logging | The ability to assign log statements to a flow effectively enabling tracing. Usually log correlation is implemented by means of assigning a unique id to each flow and writing this unique id out with every log statement. |