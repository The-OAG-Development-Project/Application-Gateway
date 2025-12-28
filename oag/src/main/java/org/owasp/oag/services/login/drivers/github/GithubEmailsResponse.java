package org.owasp.oag.services.login.drivers.github;

import java.util.ArrayList;

/**
 * Represents the response from GitHub API for user emails.
 * This class extends ArrayList to hold multiple GithubUserEmail objects
 * returned by GitHub's emails API endpoint. The structure matches
 * the JSON array returned by GitHub.
 */
public class GithubEmailsResponse extends ArrayList<GithubUserEmail> {

}
