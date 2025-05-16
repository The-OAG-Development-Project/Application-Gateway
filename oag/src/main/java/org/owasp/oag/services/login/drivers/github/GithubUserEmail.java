package org.owasp.oag.services.login.drivers.github;

/**
 * Represents a GitHub user's email information.
 * This class contains information about a user's email address, including
 * whether it's the primary email and if it has been verified.
 */
public class GithubUserEmail {

    private String email;
    private boolean primary;
    private boolean verified;

    /**
     * Gets the user's email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Checks if this is the user's primary email address.
     *
     * @return true if this is the primary email, false otherwise
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Sets whether this is the user's primary email address.
     *
     * @param primary true if this is the primary email, false otherwise
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * Checks if this email address has been verified.
     *
     * @return true if the email is verified, false otherwise
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * Sets whether this email address has been verified.
     *
     * @param verified true if the email is verified, false otherwise
     */
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
