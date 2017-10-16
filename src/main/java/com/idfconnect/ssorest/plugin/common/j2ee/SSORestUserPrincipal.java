package com.idfconnect.ssorest.plugin.common.j2ee;

import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;

import com.idfconnect.ssorest.plugin.common.PluginConfiguration;

/**
 * A simple principal to represent a user authenticated by SSO/Rest
 *
 * @author Richard Sand
 */
public class SSORestUserPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 5167111298204304040L;
    String                    name             = null;
    String                    sessionId        = null;
    String[]                  roles            = null;
    PluginConfiguration       pc               = null;

    /**
     * <p>
     * Constructor for SSORestUserPrincipal.
     * </p>
     *
     * @param name
     *            a {@link java.lang.String} object.
     * @param sessionId
     *            a {@link java.lang.String} object.
     */
    public SSORestUserPrincipal(String name, String sessionId) {
        this(name, sessionId, null);
    }

    /**
     * <p>
     * Constructor for SSORestUserPrincipal.
     * </p>
     *
     * @param name
     *            a {@link java.lang.String} object.
     * @param sessionId
     *            a {@link java.lang.String} object.
     * @param roles
     *            an array of {@link java.lang.String} objects.
     */
    public SSORestUserPrincipal(String name, String sessionId, String[] roles) {
        this.name = name;
        this.sessionId = sessionId;
        this.roles = roles;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Does the user represented by this Principal possess the specified role?
     *
     * @param role
     *            Role to be tested
     * @return a boolean.
     */
    public boolean hasRole(String role) {
        if (role == null)
            return (false);
        return (Arrays.binarySearch(roles, role) >= 0);
    }

    /**
     * <p>
     * Getter for the field <code>sessionId</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * <p>
     * Setter for the field <code>sessionId</code>.
     * </p>
     *
     * @param sessionId
     *            a {@link java.lang.String} object.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * <p>
     * Getter for the field <code>roles</code>.
     * </p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getRoles() {
        return roles;
    }

    /**
     * <p>
     * Setter for the field <code>roles</code>.
     * </p>
     *
     * @param roles
     *            an array of {@link java.lang.String} objects.
     */
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    /**
     * <p>
     * Setter for the field <code>name</code>.
     * </p>
     *
     * @param name
     *            a {@link java.lang.String} object.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SSORestUserPrincipal [");
        builder.append("name=");
        builder.append(name);
        builder.append(", ");
        if (roles != null) {
            builder.append("roles=");
            builder.append(Arrays.toString(roles));
            builder.append(", ");
        }
        if (sessionId != null) {
            builder.append("sessionId=");
            builder.append(sessionId);
        }
        builder.append("]");
        return builder.toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(roles);
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SSORestUserPrincipal))
            return false;
        SSORestUserPrincipal other = (SSORestUserPrincipal) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (!Arrays.equals(roles, other.roles))
            return false;
        if (sessionId == null) {
            if (other.sessionId != null)
                return false;
        } else if (!sessionId.equals(other.sessionId))
            return false;
        return true;
    }
}
