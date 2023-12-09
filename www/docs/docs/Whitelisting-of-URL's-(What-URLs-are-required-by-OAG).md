# Whitelisting of URL's (What URLs are required by OAG)

## Required for upstream systems (callers)
In general you will have to whitelist (i.e. in the Firewall or WAF in front of OAG) the following URLS:
- /auth** -> this is to allow authentication (login using a given provider, OIDC callback, logout)
- all your api/web calls that should be available publicly (i.e. the routes you configured for your services)

## Required to be accessible for downstream systems (your services, OAG cluster instances)
- /.well-known/jwks** -> This is the api where downstream-systems (i.e. your services) can get the public keys to verify JWT signatures of OAG.
- /oag/cluster** -> internal OAG communication for cluster synchronization (when using multiple OAG instances)
- /oag/admin** -> the admin UI of OAG where some temporary manual config override may be done
- /oag/monitoring** -> monitoring / status information of OAG