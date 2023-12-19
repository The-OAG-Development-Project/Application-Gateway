# Session Behaviour

The session behavior section of the configuration defines how OWAG handles sessions.

Example:

```yaml
sessionBehaviour:
  sessionDuration: 3600
  renewWhenLessThan: 1800
  redirectLoginSuccess: /
  redirectLoginFailure: /
  redirectLogout: /
```

## Configuration

### `sessionDuration`

Defines how long a session cookie is valid after the user has authenticated in seconds.
The default value is 3600.

### `renewWhenLessThan`

If defined, this setting can be used to have a rolling session behavior. If OWAG gets a request with a token that is still valid but for less than the specified value OWAG refreshes the session and issues a new session with the `sessionDuration` length. If the value is <= 0 or not defined the user needs to sign in again after the session is expired. The default value is 1800.

### `redirectLoginSuccess`

Default URL where the user is redirected to after successful authentication. If there is a `returnUrl` parameter during the logout the request parameter has precedence.

### `redirectLoginFailure`

Default URL where the user if there is an error during authentication (For instance if the user entered false credentials).

### `redirectLogout`

Default URL where the user if there is an after logout. If there is a `returnUrl` parameter during the logout the request parameter has precedence.
