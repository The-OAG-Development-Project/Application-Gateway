# Routes

You can specify routes for your upstream servers in the OWAG configuration

Example:
```yaml
routes:
  route1:
    type: webapplication
    path: /myservice/**
    url: https://backend.my/
    allowAnonymous: yes
```

## Configuration

You can specify multiple routes under the `routes` property. You must specify a unique name for each route. It is recommended to use something meaningful like frontend, backend, or the name of the service. 

### `type`

The type property specifies the name of the [Security Profile](/docs/Configuration-SecurityProfiles) that is used for this route. You must set a valid security profile, you can either use a default one or define your own security profile.

### `path`

The path setting specifies which requests to OWAG are routed with this route. In the upper example all requests to `/myservice` are routes to `https://backend.my/`. You can use ant-style patterns for routes:

* `/myservice/` matches only `/myservice/`
* `/myservice/*` matches with one level wildcard, i.e. `/myservice/foo`
* `/myservice/**` matches all hierarchical request, i.e. `/myservice/foo/bar`

### `url`

The URL setting specifies the upstream service to which OWAG forwards the request. The path from the route will not be forwarded. For instance, if you call with the example before `/myservice/foo` OWAG forwards the request to `https://backend.my/foo`.

### `allowAnonymous`

Boolean variable: If you specify `no` OWAG only allows authenticated requests to this route. This setting is useful if your backend application expects only authenticated users. Another use case for this option is if you want to secure a web service that has no authentication implemented. This method is then only secure if you specify only Login provider from private IdPs and not social logins.

### `rewrite` (optional)

The default rewrite behaviour is designed to be intuitive. This is why strips the heading path segments when wildcards are used. i:e. `/api/endpoint` is rewritten to `https://backend/endpoint` if the route path is `/api/**` and the route url is `https://backend/`. This is because most of the time a backend api is only interested in relevant api endpoint and not the full path. If the backend server needs the "api" part in the url the route url can simply be set to `https://backend/api/`, then the request would get forwarded to with the original path `https://backend/api/endpoint`.

We expect that this behaviour is good enough and intuitive to understand for most use-cases. However, if you want to implement more complex rewrite behaviour you can use arbitrary regex and replacement strings.

With the rewrite configuration you can overwrite the default path rewrite behaviour of OAG.

- `regex` Regex that matches the route and might extract relevant path segments
- `replacement` Replacement string that can use defines path segments

Internally the regex and replacement is processed by the Spring Cloud Gateway with the Java String replaceAll method. See snippet bellow:

``` java
String path = req.getURI().getRawPath();
String newPath = path.replaceAll(config.regexp, replacement);
```

#### Default Rewrite Behaviour (Detailed)

If no rewrite configuration is defined the following default configuration is used:

- `regex`: `<route-path-base>(?<segment>.*)` where `<route-path-base>` is the path of the route without trailing wildcards
- `replacement`: `<route-uri-path>${segment}` where `<route-uri-path>` is the path of the defined url for this route with a trailing slash

This leads to the following default behaviour:

##### Example 1

```
Route Path:                   /api/**
Route Url:                    http://localhost:7777/
Request Path:                 /api/messages/123
Resulting downstream request: http://localhost:7777/messages/123
```

##### Example 2

```
Route Path:                   /rewrite1/**
Route Url:                    http://localhost:7777/rewritten/
Request Path:                 /rewrite1/message.txt
Resulting downstream request: http://localhost:7777/rewritten/message.txt
```
