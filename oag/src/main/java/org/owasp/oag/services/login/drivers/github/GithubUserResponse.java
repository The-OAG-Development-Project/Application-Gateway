package org.owasp.oag.services.login.drivers.github;

/**
 * Represents the response from GitHub API for user information.
 * This class maps to the JSON response returned by GitHub's user API endpoint.
 * Field names match the exact JSON property names from GitHub's API.
 */
public class GithubUserResponse {

    /** The GitHub username */
    public String login;
    
    /** The unique identifier for the GitHub user */
    public String id;
    
    /** URL to the user's avatar/profile image */
    public String avatar_url;
    
    /** The user's full name */
    public String name;
    
    /** The user's company information */
    public String company;
    
    /** The user's public email address (may be null if not public) */
    public String email;
    
    /** The timestamp when the user's account was created */
    public String created_at;
    
    /** The timestamp when the user's account was last updated */
    public String updated_at;
    
    /** The API URL for the user's profile */
    public String url;
}
