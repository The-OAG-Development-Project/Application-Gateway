# Setup for OAG development

## Preparation
* Install Java 11 or higher
* Install Maven
* Install Git
* Install IntelliJ (Community Version is fine)
* git clone the OAG repository:
  + create a new folder for OAG: 
  ````bash
  mkdir oag
  ````
  + jump into the folder:
  ````bash
  cd oag
  ````
  + clone the repository:
  ````bash
  git clone https://github.com/gianlucafrei/Application-Gateway.git
  ````

## Import project
* Start IntelliJ.
* Open project: ../oag/Application-Gateway/oag
  + This should trigger maven and download dependencies
* Build of the whole project (select Build -> Build Project in the menu).
  + You will have to select a project JDK (IntelliJ prompts for this in the upper right corner.). OAG requires a SDK/JDK >= 11 and runs on Java 11 or higher. 

## Run project
* Verify tests are green: Right click on the top level of the project ("oag" in the Project window) and select "Run All Tests"
* Run the gateway:
  + Open class OWASPApplicationGatewayApplication
  + Click the green triangle (Line 7) to run OWASPApplicationGatewayApplication.main().
    - note that you need 3 client secrets (i.e. registered OAG as an application in Google, GitHub and oAuth0) to be fully functional with the default sample configuration.
    - For GitHub, this means you will have to register an [OAuth App in GitHub](https://github.com/settings/applications/new). Not that the app needs to reflect your local OAG that runs in IntelliJ.
    - If you do not have these 3 client secrets you will see the following in the logs:
````bash
WARN  - Environment variable 'GOOGLE_CLIENT_SECRET' does not exist
WARN  - Environment variable 'GITHUB_CLIENT_SECRET' does not exist
WARN  - Environment variable 'AUTH0_CLIENT_SECRET' does not exist
````
  + these 3 clientID's can be set as environment variables in the operating system or directly in IntelliJ as follows:
      * Menu: Run -> Edit Configuration
      * "+" (Add new Configuration) -> Application (or edit the existing entry)
      * select "org.owasp.oag.OWASPApplicationGatewayApplication" as Main Class
      * in the text field for "environment variables:" add the 3 Secrets: "GOOGLE_CLIENT_SECRET=xxxxx;GITHUB_CLIENT_SECRET=xxxxxx;AUTH0_CLIENT_SECRET=xxxxxx"
* Point your browser to: `http://localhost:8080/echo/` -> Note "X-Oag-Status: anonymous" in the returned page
  + Click on "Login with Github"
  + Enter your GitHub credentials
  + If all works fine you will end up on localhost:8080 which displays "httpbin.org"
* Point your browser again to: `http://localhost:8080/echo/` -> Note "X-Oag-Status: authenticated" in the returned page

Now you are ready to develop new features in OAG.
With this you are ready to develop.
