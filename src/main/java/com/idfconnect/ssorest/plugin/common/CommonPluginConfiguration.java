package com.idfconnect.ssorest.plugin.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.idfconnect.ssorest.common.utils.ConfigProperties;
import com.idfconnect.ssorest.common.utils.ConfigPropertiesFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * CommonPluginConfiguration class.
 * </p>
 *
 * @author rsand
 * @since 1.1.1
 */
@Getter
@Setter
@ToString
public class CommonPluginConfiguration implements PluginConfiguration {
    // Logger
    static Logger              logger                       = LoggerFactory.getLogger(CommonPluginConfiguration.class);

    // Configuration parameters as mandated by the interface
    String                     acoName                      = null;
    boolean                    caseInsensitiveUsernames     = false;
    String                     configFileName               = null;
    int                        connectionTimeout            = -1;
    boolean                    enabled                      = true;
    boolean                    enablePublicServices         = true;
    boolean                    enforcePrincipal             = true;
    String                     errorPage                    = null;
    String[]                   gatewayUrls                  = null;
    String                     ignoreExt                    = null;
    String                     ignoreHost                   = null;
    String                     ignoreUrl                    = null;
    String                     keyStore                     = null;
    String                     keyStorePwd                  = null;
    String                     keyStoreType                 = "jks";
    int                        maxConnections               = 200;
    long                       normalInterval               = 300000L;
    String                     pluginId                     = null;
    String                     proxyHost                    = "localhost";
    int                        proxyPort                    = 8080;
    String                     proxyScheme                  = "http";
    boolean                    proxyEnable                  = false;
    boolean                    connectionPoolEnable         = false;
    int                        quorum                       = 50;
    String                     secretKey                    = null;
    int                        socketTimeout                = -1;
    String                     trustStore                   = null;
    String                     trustStorePwd                = "changeit";
    String                     trustStoreType               = "jks";
    long                       urgentInterval               = 60000L;
    boolean                    useFileConfigProvider        = false;
    String                     usernameHeader               = PluginConfiguration.USERNAME_HEADER_NAME_DEFAULT;
    ConfigProperties           rawProperties                = null;

    /**
     * This initialize method will call {@link com.idfconnect.ssorest.common.utils.ConfigPropertiesFactory} <em>initFromEverywhere</em> with no System
     * properties check
     *
     * @param params
     *            a {@link java.util.Map} object.
     * @since 1.1.1
     */
    public final void initialize(Map<String, String> params) {
        initialize(params, null);
    }

    /**
     * This initialize method will call {@link com.idfconnect.ssorest.common.utils.ConfigPropertiesFactory} <em>initFromEverywhere</em>
     *
     * @param params
     *            a {@link java.util.Map} object.
     * @param prefix
     *            a {@link java.lang.String} object.
     */
    public final void initialize(Map<String, String> params, String prefix) {
        ConfigProperties properties = ConfigPropertiesFactory.initFromEverywhere(params, prefix);
        initializeFromProperties(properties);
    }

    /**
     * <p>
     * initializeFromFile.
     * </p>
     *
     * @param filename
     *            a {@link java.lang.String} object.
     * @throws java.io.IOException
     *             if any.
     */
    public final void initializeFromFile(String filename) throws IOException {
        ConfigProperties properties = ConfigPropertiesFactory.initFromFilename(filename);
        initializeFromProperties(properties);
    }

    /**
     * This initialization method is used when the config properties have already been fully loaded by the container and no other configuration sources (file,
     * System, JNDI etc.) need to be checked
     *
     * @param properties
     *            a {@link com.idfconnect.ssorest.common.utils.ConfigProperties} object.
     */
    public void initializeFromProperties(ConfigProperties properties) {
        logger.trace("Internal initialization with properties {}", properties);
        this.rawProperties = properties;
        acoName = properties.getPropertyAsString(PluginConfiguration.SSOREST_ACO_NAME);
        caseInsensitiveUsernames = properties.getPropertyAsBoolean(PluginConfiguration.CASE_INSENSITIVE_USERNAMES, caseInsensitiveUsernames);
        connectionPoolEnable = properties.getPropertyAsBoolean(PluginConfiguration.CONNECTION_POOL_ENABLE, false);
        connectionTimeout = properties.getPropertyAsInt(PluginConfiguration.CONNECTION_TIMEOUT, connectionTimeout);
        enabled = properties.getPropertyAsBoolean(PluginConfiguration.ENABLED, enabled);
        enablePublicServices = properties.getPropertyAsBoolean(PluginConfiguration.ENABLE_PUBLIC_SERVICES, enablePublicServices);
        enforcePrincipal = properties.getPropertyAsBoolean(PluginConfiguration.ENFORCE_PRINCIPAL, enforcePrincipal);
        errorPage = properties.getPropertyAsString(PluginConfiguration.INTERNAL_SERVER_ERROR_PAGE, PluginConfiguration.DEFAULT_500_ERROR_FILE);
        ignoreExt = properties.getPropertyAsString(PluginConfiguration.IGNORE_EXT_NAME);
        ignoreHost = properties.getPropertyAsString(PluginConfiguration.IGNORE_HOST_NAME);
        ignoreUrl = properties.getPropertyAsString(PluginConfiguration.IGNORE_URL_NAME);
        keyStore = properties.getPropertyAsString(PluginConfiguration.KEY_STORE);
        keyStorePwd = properties.getPropertyAsString(PluginConfiguration.KEY_STORE_PWD);
        keyStoreType = properties.getPropertyAsString(PluginConfiguration.KEY_STORE_TYPE, keyStoreType);
        maxConnections = properties.getPropertyAsInt(PluginConfiguration.MAX_CONNECTIONS, maxConnections);
        pluginId = properties.getPropertyAsString(PluginConfiguration.PLUGIN_ID_NAME);
        proxyEnable = properties.getPropertyAsBoolean(PluginConfiguration.PROXY_ENABLE, proxyEnable);
        proxyHost = properties.getPropertyAsString(PluginConfiguration.PROXY_HOST, proxyHost);
        proxyPort = properties.getPropertyAsInt(PluginConfiguration.PROXY_PORT, proxyPort);
        proxyScheme = properties.getPropertyAsString(PluginConfiguration.PROXY_SCHEME, proxyScheme);
        secretKey = properties.getPropertyAsString(PluginConfiguration.SECRET_KEY_NAME);
        socketTimeout = properties.getPropertyAsInt(PluginConfiguration.SOCKET_TIMEOUT, socketTimeout);
        trustStore = properties.getPropertyAsString(PluginConfiguration.TRUST_STORE);
        trustStorePwd = properties.getPropertyAsString(PluginConfiguration.TRUST_STORE_PWD, trustStorePwd);
        trustStoreType = properties.getPropertyAsString(PluginConfiguration.TRUST_STORE_TYPE, trustStoreType);
        usernameHeader = properties.getPropertyAsString(USERNAME_HEADER_NAME, PluginConfiguration.USERNAME_HEADER_NAME_DEFAULT);

        // initialize gateway url
        Object obj = properties.getProperty(PluginConfiguration.SSOREST_BASE_URL);
        if (obj instanceof List) {
            gatewayUrls = properties.getPropertyAsList(PluginConfiguration.SSOREST_BASE_URL).toArray(new String[] {});
        } else {
            String gatewayUrlStr = properties.getPropertyAsString(PluginConfiguration.SSOREST_BASE_URL);
            if (gatewayUrlStr != null) {
                gatewayUrls = gatewayUrlStr.split(",");
            }
        }
        validateUrl();
    }
    /** {@inheritDoc} */
    @Override
	public void initUrlListFromString(String gatewayUrl) {
    	logger.debug("Initializing Gateway URL List from {}", gatewayUrl);
        // initialize gateway url
        gatewayUrls = gatewayUrl.split(",");
        validateUrl();
	}
    
    
    /**
     * <p>validateUrl.</p>
     */
    public void validateUrl() {
    	LinkedList<String> verifiedGatewayUrls = new LinkedList<String>();
        // verify URL here
        for (String url : gatewayUrls) {
            try {
                new URL(url);
                verifiedGatewayUrls.add(url);
            } catch (MalformedURLException e) {
                logger.warn("Malformed URL {}", url);
            }
        }
        verifiedGatewayUrls.toArray(gatewayUrls);
    }
}
