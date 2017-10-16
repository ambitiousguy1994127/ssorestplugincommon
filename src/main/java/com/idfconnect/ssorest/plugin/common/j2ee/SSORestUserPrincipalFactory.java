package com.idfconnect.ssorest.plugin.common.j2ee;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.idfconnect.ssorest.plugin.common.CommonPluginConfiguration;

/**
 * Factory for creating {@link com.idfconnect.ssorest.plugin.common.j2ee.SSORestUserPrincipal} objects for the given plugin configuration
 *
 * @author Richard Sand
 */
public final class SSORestUserPrincipalFactory {
    /** Name of the initialization parameter specifying whether legacy headers are in use */
    public static final String  LEGACY_HEADERS_NAME_PROPERTY = "legacyHeaders";

    /** Name of the initialization parameter specifying the roles header */
    public static final String  ROLES_HEADER_NAME_PROPERTY   = "rolesHeader";

    /** Default name for the SSO/Rest roles header */
    public static final String  ROLES_HEADER_NAME_DEFAULT    = "SSOREST_ROLES";

    /** Name of the initialization parameter specifying the groups header */
    public static final String  GROUPS_HEADER_NAME_PROPERTY  = "groupsHeader";

    /** Default name for the groups header */
    public static final String  GROUPS_HEADER_NAME_DEFAULT   = "SM_USERGROUPS";

    private static final String SERVERSESSIONID              = "SERVERSESSIONID";
    private static final String SM                           = "SM";

    static Logger               logger                       = LoggerFactory.getLogger(SSORestUserPrincipalFactory.class);
    String                      userNameHeader               = null;
    String                      sessionIdHeader              = null;
    String                      rolesHeader                  = null;
    String                      groupsHeader                 = null;
    CommonPluginConfiguration   pc                           = null;

    private SSORestUserPrincipalFactory(CommonPluginConfiguration pc) {
        this.pc = pc;
        userNameHeader = pc.getUsernameHeader();
        logger.debug("User name header is {}", userNameHeader);

        boolean useLegacyHeaders = pc.getRawProperties().getPropertyAsBoolean(LEGACY_HEADERS_NAME_PROPERTY, false);
        sessionIdHeader = SM + (useLegacyHeaders ? '_' : "") + SERVERSESSIONID;
        logger.debug("Session ID header is {}", sessionIdHeader);

        rolesHeader = pc.getRawProperties().getPropertyAsString(ROLES_HEADER_NAME_PROPERTY, ROLES_HEADER_NAME_DEFAULT);
        logger.debug("Roles header is {}", rolesHeader);

        groupsHeader = pc.getRawProperties().getPropertyAsString(GROUPS_HEADER_NAME_PROPERTY, GROUPS_HEADER_NAME_DEFAULT);
        logger.debug("Groups header is {}", groupsHeader);
    }

    /**
     * <p>
     * getFactory.
     * </p>
     *
     * @param pc
     *            a {@link com.idfconnect.ssorest.plugin.common.CommonPluginConfiguration} object.
     * @return a {@link com.idfconnect.ssorest.plugin.common.j2ee.SSORestUserPrincipalFactory} object.
     * @since 3.0.1
     */
    public static final SSORestUserPrincipalFactory getFactory(CommonPluginConfiguration pc) {
        return new SSORestUserPrincipalFactory(pc);
    }

    /**
     * <p>
     * getUserPrincipalForRequest.
     * </p>
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link com.idfconnect.ssorest.plugin.common.j2ee.SSORestUserPrincipal} object.
     * @since 3.0.1
     */
    public SSORestUserPrincipal getUserPrincipalForRequest(HttpServletRequest request) {
        // Session ID
        String ssoSessionId = request.getHeader(sessionIdHeader);
        if (ssoSessionId == null || "".equals(ssoSessionId)) {
            logger.debug("No header {} found in request", sessionIdHeader); // TODO change to trace
            return null; // TODO anonymous/identityspec
        } else
            logger.debug("Found session ID header {}={}", sessionIdHeader, ssoSessionId);

        // Username
        String userName = request.getHeader(userNameHeader);
        logger.debug("Username header {}={}", userNameHeader, userName);

        // Roles
        String[] roles = null;
        String rawroles = request.getHeader(rolesHeader);
        if (rawroles != null)
            roles = rawroles.split("^");
        logger.debug("Roles header {}={}", rolesHeader, roles);

        // Groups
        // TODO groups
        // String groups = request.getHeader(groupsHeader);
        // subject.getPublicCredentials().add(createDataCredential(userName, groups));

        // Create the subject
        Subject subject = new Subject();
        SSORestUserPrincipal p = new SSORestUserPrincipal(userName, ssoSessionId, roles);
        subject.getPrincipals().add(p);
        return p;
    }
}
