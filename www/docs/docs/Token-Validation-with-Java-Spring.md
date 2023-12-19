# Token Validation with Java Spring

If you have a Java Spring application the recommended way validate the OAG authentication is using Spring Security.


Sou need the following two dependencies in your project:
```
org.springframework.boot:spring-boot-starter-security
org.springframework.boot:spring-boot-starter-oauth2-resource-server
```

Spring needs to know where it can find the public key to verify the JWT tokens that OAG attaches to the request. For that you need to configure the following application property:

```
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://path-to-your-oag-instance/.well-known/jwks
```

Next you need to enable JWT validation in your Spring security configuration:

```java
@Override
    protected void configure(HttpSecurity http) throws Exception {

        // Enable JWT validation
        http.oauth2ResourceServer().jwt();

        // Authorize requests
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/somePublicEndpoint").permitAll()
                .anyRequest().authenticated();

        // Sessions and csrf protection are covered by OAG
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
```

Thats it! 🎉 Spring Security will now validate the token that is sent with OAG and block all requests.

## Advanced

If you want to add additional validation logic to your application you best use a Spring filter for that. In the above example we look up if the user id is already in a database and add this information to the granted authorities.

```java
public class RegisteredUserFilter implements Filter {

    public RegisteredUserFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    private UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication instanceof JwtAuthenticationToken)
        {
            // Get authentication object
            JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
            ArrayList<GrantedAuthority> updatedAuthorities = new ArrayList<>(jwtAuthentication.getAuthorities());
            Jwt principal = jwtAuthentication.getToken();

            // Add REGISTERED authority if the user has already registered themself
            String id = authentication.getName();
            Optional<User> userOptional = userRepository.findById(id);
            if(userOptional.isPresent()){
                updatedAuthorities.add(new SimpleGrantedAuthority("REGISTERED"));
            }else {
                updatedAuthorities.add(new SimpleGrantedAuthority("NOT-REGISTERED"));
            }

            // Safe updated authentication
            JwtAuthenticationToken updatedAuthentication = new JwtAuthenticationToken(jwtAuthentication.getToken(), updatedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
        }
        chain.doFilter(request,  response);
    }
}
```

Authorities can for instance be used in the Spring Security configuration:

```java
http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(HttpMethod.GET, "/articles/feed").hasAuthority("REGISTERED")
                .antMatchers(HttpMethod.POST, "/users").hasAuthority("NOT-REGISTERED")
                .antMatchers(HttpMethod.GET, "/articles/**", "/profiles/**", "/user", "/tags").permitAll()
                .anyRequest().authenticated();
```

If you want to consume details of the token for example in a controller method you can access the JWT claims in the following way:
```java
    @RequestMapping(path = "/", method = POST)
    public ResponseEntity index(@AuthenticationPrincipal Jwt jwt) {
        String id = jwt.getSubject();
        String email = jwt.getClaim("email");
        String picture = jwt.getClaim("picture");
        String provider = jwt.getClaim("provider");
    }
```
