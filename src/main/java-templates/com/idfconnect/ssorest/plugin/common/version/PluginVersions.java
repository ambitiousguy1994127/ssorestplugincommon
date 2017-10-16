package com.idfconnect.ssorest.plugin.common.version;

import org.slf4j.Logger;

/**
 * Template class to provide current versions of dependencies
 * 
 * @author Richard Sand
 */
public class PluginVersions {
    public static final String SSOREST_BASE   = "${project.parent.version}";
    public static final String SSOREST_COMMON = "${common-tools.version}";
    public static final String PLUGIN_COMMON  = "${project.version}";

    public static final void logVersions(Logger logger) {
        logger.info("SSO/Rest Base version: " + SSOREST_BASE);
        logger.info("SSO/Rest Common Tools version: " + SSOREST_COMMON);
        logger.info("SSO/Rest Plugin Common Library version: " + PLUGIN_COMMON);
    }
}
