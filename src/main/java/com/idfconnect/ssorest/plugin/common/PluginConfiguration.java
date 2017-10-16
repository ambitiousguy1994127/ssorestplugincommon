package com.idfconnect.ssorest.plugin.common;

/**
 * Interface for all plugin configuration classes
 *
 * @author Richard Sand
 * @since 1.1.1
 */
public interface PluginConfiguration {
    /** Constant <code>INTERNAL_SERVER_ERROR_PAGE="internalServerErrorPage"</code> */
    String INTERNAL_SERVER_ERROR_PAGE    = "internalServerErrorPage";

    /** Constant <code>DEFAULT_500_ERROR_FILE="500error.html"</code> */
    String DEFAULT_500_ERROR_FILE        = "500error.html";

    /** Constant <code>SSOREST_ACO_NAME="acoName"</code> */
    String SSOREST_ACO_NAME              = "acoName";

    /** Constant <code>CONNECTION_TIMEOUT="connectionTimeout"</code> */
    String CONNECTION_TIMEOUT            = "connectionTimeout";

    /** Constant <code>ENABLED="enabled"</code> */
    String ENABLED                       = "enabled";

    /** Constant <code>ENABLE_PUBLIC_SERVICES="enablePublicServices"</code> */
    String ENABLE_PUBLIC_SERVICES        = "enablePublicServices";

    /** Constant <code>SSOREST_GATEWAY_SERVICES_URI="gateway"</code> */
    String SSOREST_GATEWAY_SERVICES_URI  = "gateway";

    /** Constant <code>SSOREST_SERVICES_URI="service"</code> */
    String SSOREST_SERVICES_URI          = "service";

    /** Constant <code>IGNORE_EXT_NAME="ignoreExt"</code> */
    String IGNORE_EXT_NAME               = "ignoreExt";

    /** Constant <code>IGNORE_HOST_NAME="ignoreHost"</code> */
    String IGNORE_HOST_NAME              = "ignoreHost";

    /** Constant <code>IGNORE_URL_NAME="ignoreUrl"</code> */
    String IGNORE_URL_NAME               = "ignoreUrl";

    /** Constant <code>KEY_STORE="keyStore"</code> */
    String KEY_STORE                     = "keyStore";

    /** Constant <code>KEY_STORE_PWD="keystorepass"</code> */
    String KEY_STORE_PWD                 = "keystorepass";

    /** Constant <code>KEY_STORE_TYPE="keystoretype"</code> */
    String KEY_STORE_TYPE                = "keystoretype";

    /** Constant <code>MAX_CONNECTIONS="maxConnections"</code> */
    String MAX_CONNECTIONS               = "maxConnections";

    /** Constant <code>MAX_CONNECTIONS_PER_ROUTE="maxConnectionsPerRoute"</code> */
    String MAX_CONNECTIONS_PER_ROUTE     = "maxConnectionsPerRoute";

    /** Constant <code>PLUGIN_ID_NAME="pluginID"</code> */
    String PLUGIN_ID_NAME                = "pluginID";

    /** Constant <code>PROXY_ENABLE="proxyEnable"</code> */
    String PROXY_ENABLE                  = "proxyEnable";

    /** Constant <code>PROXY_HOST="proxyHost"</code> */
    String PROXY_HOST                    = "proxyHost";

    /** Constant <code>PROXY_PORT="proxyPort"</code> */
    String PROXY_PORT                    = "proxyPort";

    /** Constant <code>PROXY_SCHEME="proxyScheme"</code> */
    String PROXY_SCHEME                  = "proxyScheme";

    /** Constant <code>SSOREST_BASE_URL="gatewayUrl"</code> */
    String SSOREST_BASE_URL              = "gatewayUrl";

    /** Constant <code>SSOREST_PUBLIC_SERVICES_URI="public"</code> */
    String SSOREST_PUBLIC_SERVICES_URI   = "public";

    /** Constant <code>SSOREST_INTERNAL_SERVICES_URI="sso"</code> */
    String SSOREST_INTERNAL_SERVICES_URI = "sso";

    /** Constant <code>SECRET_KEY_NAME="secretKey"</code> */
    String SECRET_KEY_NAME               = "secretKey";

    /** Constant <code>SOCKET_TIMEOUT="socketTimeout"</code> */
    String SOCKET_TIMEOUT                = "socketTimeout";

    /** Constant <code>TRUST_STORE="trustStore"</code> */
    String TRUST_STORE                   = "trustStore";

    /** Constant <code>TRUST_STORE_PWD="truststorepass"</code> */
    String TRUST_STORE_PWD               = "truststorepass";

    /** Constant <code>TRUST_STORE_TYPE="truststoretype"</code> */
    String TRUST_STORE_TYPE              = "truststoretype";

    /** Constant <code>NORMAL_INTERVAL="normalInterval"</code> */
    String NORMAL_INTERVAL               = "normalInterval";

    /** Constant <code>URGENT_INTERVAL="urgentInterval"</code> */
    String URGENT_INTERVAL               = "urgentInterval";

    /** Constant <code>QUORUM="quorum"</code> */
    String QUORUM                        = "quorum";

    /** Constant <code>QUORUM="quorum"</code> */
    String CONNECTION_POOL_ENABLE        = "connectionPoolEnable";

    /** Constant <code>ENFORCE_PRINCIPAL="enforcePrincipal"</code> */
    String ENFORCE_PRINCIPAL             = "enforcePrincipal";

    /** Name of the parameter specifying the username header */
    String USERNAME_HEADER_NAME          = "usernameHeader";

    /** Constant <code>CASE_INSENSITIVE_USERNAMES="caseInsensitiveUsernames"</code> */
    String CASE_INSENSITIVE_USERNAMES    = "caseInsensitiveUsernames";

    /** Config property constant for use with providing the configuration file name */
    String CONFIG_FILENAME_PROP          = "configFileName";

    /** Config property constant for use with enabling the file configuration capability */
    String USE_FILE_CONFIG_PROVIDER_PROP = "useFileConfigProvider";
    
    /** Constant for indicating prefix for System properties containing configuration parameters */
    String SYSTEM_PROPERTY_PREFIX = "com.idfconnect.ssorest.plugin";

    /** Default name for the username header */
    String USERNAME_HEADER_NAME_DEFAULT = "SMUSER";
    
    //////
    //
    // configFileName
    //
    //////
    
    /**
     * The name of the configuration file to use for this plugin configuration, if any
     *
     * @return the name of the configuration file, or null if none
     * @since 3.0.1
     */
    String getConfigFileName();

    /**
     * Sets the name of the configuration file to use for this plugin configuration, if any
     *
     * @param name
     *            a {@link java.lang.String} object.
     * @since 3.0.1
     */
    void setConfigFileName(String name);

    //////
    //
    // useFileConfigProvider
    //
    //////
    
    /**
     * Returns true if the file-based configuration provider should be used
     *
     * @return true if the file-based configuration provider should be used
     * @since 3.0.1
     */
    boolean isUseFileConfigProvider();

    /**
     * Set to true i the file-based configuration provider should be used
     *
     * @param useFileConfigProvider
     *            a boolean.
     * @since 3.0.1
     */
    void setUseFileConfigProvider(boolean useFileConfigProvider);

    //////
    //
    // gatewayUrl
    //
    //////
    
    /**
     * <p>
     * getGatewayUrl.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String[] getGatewayUrls();

    //////
    //
    // errorPage
    //
    //////
    
    /**
     * <p>
     * getErrorPage.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getErrorPage();

    /**
     * <p>
     * setErrorPage.
     * </p>
     *
     * @param errorPage
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setErrorPage(String errorPage);

    //////
    //
    // acoName
    //
    //////

    /**
     * <p>
     * getAcoName.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getAcoName();

    /**
     * <p>
     * setAcoName.
     * </p>
     *
     * @param acoName
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setAcoName(String acoName);

    //////
    //
    // secretKey
    //
    //////
    
    /**
     * <p>
     * getSecretKey.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getSecretKey();

    //////
    //
    // pluginId
    //
    //////

    /**
     * <p>
     * getPluginId.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getPluginId();

    //////
    //
    // ignoreExt
    //
    //////

    /**
     * <p>
     * getIgnoreExt.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getIgnoreExt();

    //////
    //
    // ignoreHost
    //
    //////

    /**
     * <p>
     * getIgnoreHost.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getIgnoreHost();

    //////
    //
    // ignoreUrl
    //
    //////

    /**
     * <p>
     * getIgnoreUrl.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getIgnoreUrl();

    //////
    //
    // enabled
    //
    //////

    /**
     * <p>
     * isEnabled.
     * </p>
     *
     * @return a boolean.
     * @since 1.1.1
     */
    boolean isEnabled();
    
    /**
     * <p>
     * setEnabled.
     * </p>
     *
     * @param enabled
     *            a boolean.
     * @since 1.1.1
     */
    void setEnabled(boolean enabled);

    //////
    //
    // maxConnections
    //
    //////

    /**
     * <p>
     * getMaxConnections.
     * </p>
     *
     * @return a int.
     * @since 1.1.1
     */
    int getMaxConnections();

    //////
    //
    // trustStore settings
    //
    //////

    /**
     * <p>
     * getTrustStore.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getTrustStore();

    /**
     * <p>
     * getTrustStorePwd.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getTrustStorePwd();
    
    /**
     * <p>
     * getTrustStoreType.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getTrustStoreType();
    
    /**
     * <p>
     * setTrustStore.
     * </p>
     *
     * @param trustStore
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setTrustStore(String trustStore);

    /**
     * <p>
     * setTrustStorePwd.
     * </p>
     *
     * @param trustStorePwd
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setTrustStorePwd(String trustStorePwd);

    /**
     * <p>
     * setTrustStoreType.
     * </p>
     *
     * @param trustStoreType
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setTrustStoreType(String trustStoreType);

    //////
    //
    // keyStore settings
    //
    //////

    /**
     * <p>
     * getKeyStore.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getKeyStore();

    /**
     * <p>
     * getKeyStorePwd.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getKeyStorePwd();

    /**
     * <p>
     * getKeyStoreType.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getKeyStoreType();

    /**
     * <p>
     * setKeyStore.
     * </p>
     *
     * @param keyStore
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setKeyStore(String keyStore);

    /**
     * <p>
     * setKeyStorePwd.
     * </p>
     *
     * @param keyStorePwd
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setKeyStorePwd(String keyStorePwd);

    /**
     * <p>
     * setKeyStoreType.
     * </p>
     *
     * @param keyStoreType
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setKeyStoreType(String keyStoreType);

    //////
    //
    // proxy settings
    //
    //////

    /**
     * <p>
     * getProxyHost.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getProxyHost();

    /**
     * <p>
     * getProxyPort.
     * </p>
     *
     * @return a int.
     * @since 1.1.1
     */
    int getProxyPort();

    /**
     * <p>
     * getProxyScheme.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.1.1
     */
    String getProxyScheme();

    /**
     * <p>
     * isProxyEnable.
     * </p>
     *
     * @return a boolean.
     * @since 1.1.1
     */
    boolean isProxyEnable();

    //////
    //
    // enablePublicServices
    //
    //////

    /**
     * <p>
     * isEnablePublicServices.
     * </p>
     *
     * @return a boolean.
     * @since 1.1.1
     */
    boolean isEnablePublicServices();

    //////
    //
    // gatewayUrl
    //
    //////

    /**
     * <p>
     * setGatewayUrl.
     * </p>
     *
     * @param gatewayUrl
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setGatewayUrls(String[] gatewayUrl);

    //////
    //
    // connectionPool settings
    //
    //////

    /**
     * <p>
     * setConnectionPoolEnable.
     * </p>
     *
     * @param isEnabled
     *            a boolean.
     * @since 3.0.1
     */
    void setConnectionPoolEnable(boolean isEnabled);

    /**
     * <p>
     * isConnectionPoolEnable.
     * </p>
     *
     * @return a boolean.
     * @since 3.0.1
     */
    boolean isConnectionPoolEnable();

    //////
    //
    // publicServices
    //
    //////

    /**
     * <p>
     * setEnablePublicServices.
     * </p>
     *
     * @param enablePublicServices
     *            a boolean.
     * @since 1.1.1
     */
    void setEnablePublicServices(boolean enablePublicServices);

    /**
     * <p>
     * setSecretKey.
     * </p>
     *
     * @param secretKey
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setSecretKey(String secretKey);

    /**
     * <p>
     * setPluginId.
     * </p>
     *
     * @param pluginId
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setPluginId(String pluginId);

    /**
     * <p>
     * setProxyHost.
     * </p>
     *
     * @param proxyHost
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setProxyHost(String proxyHost);

    /**
     * <p>
     * setProxyPort.
     * </p>
     *
     * @param proxyPort
     *            a int.
     * @since 1.1.1
     */
    void setProxyPort(int proxyPort);

    /**
     * <p>
     * setProxyScheme.
     * </p>
     *
     * @param proxyScheme
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setProxyScheme(String proxyScheme);

    /**
     * <p>
     * setProxyEnable.
     * </p>
     *
     * @param proxyEnable
     *            a boolean.
     * @since 1.1.1
     */
    void setProxyEnable(boolean proxyEnable);

    /**
     * <p>
     * setIgnoreExt.
     * </p>
     *
     * @param ignoreExt
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setIgnoreExt(String ignoreExt);

    /**
     * <p>
     * setIgnoreHost.
     * </p>
     *
     * @param ignoreHost
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setIgnoreHost(String ignoreHost);

    /**
     * <p>
     * setIgnoreUrl.
     * </p>
     *
     * @param ignoreUrl
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    void setIgnoreUrl(String ignoreUrl);

    /**
     * <p>
     * setMaxConnections.
     * </p>
     *
     * @param maxConnections
     *            a int.
     * @since 1.1.1
     */
    void setMaxConnections(int maxConnections);

    /**
     * <p>
     * getConnectionTimeout.
     * </p>
     *
     * @return a int.
     * @since 1.1.1
     */
    int getConnectionTimeout();

    /**
     * <p>
     * getSocketTimeout.
     * </p>
     *
     * @return a int.
     * @since 1.1.1
     */
    int getSocketTimeout();

    /**
     * <p>
     * setConnectionTimeout.
     * </p>
     *
     * @param timeout
     *            a int.
     * @since 1.1.1
     */
    void setConnectionTimeout(int timeout);

    /**
     * <p>
     * setSocketTimeout.
     * </p>
     *
     * @param timeout
     *            a int.
     * @since 1.1.1
     */
    void setSocketTimeout(int timeout);

    /**
     * <p>
     * getNormalInterval.
     * </p>
     *
     * @return a long.
     * @since 3.0.1
     */
    public long getNormalInterval();

    /**
     * <p>
     * getUrgentInterval.
     * </p>
     *
     * @return a long.
     * @since 3.0.1
     */
    public long getUrgentInterval();

    /**
     * <p>
     * getQuorum.
     * </p>
     *
     * @return a int.
     * @since 3.0.1
     */
    public int getQuorum();
    
    /**
     * <p>
     * setQuorum.
     * </p>
     *
     * @param quorum
     *            a int.
     * @since 3.0.1
     */
    public void setQuorum(int quorum);

    /**
     * <p>
     * setNormalInterval.
     * </p>
     *
     * @param normalInterval
     *            a long.
     * @since 3.0.1
     */
    public void setNormalInterval(long normalInterval);

    /**
     * <p>
     * setUrgentInterval.
     * </p>
     *
     * @param urgentInterval
     *            a long.
     * @since 3.0.1
     */
    public void setUrgentInterval(long urgentInterval);

    /**
     * <p>
     * setEnforcePrincipal.
     * </p>
     *
     * @param enforcePrincipal
     *            a boolean.
     * @since 3.0.1
     */
    public void setEnforcePrincipal(boolean enforcePrincipal);

    /**
     * <p>
     * isEnforcePrincipal.
     * </p>
     *
     * @return a boolean.
     * @since 3.0.1
     */
    public boolean isEnforcePrincipal();

    /**
     * <p>
     * setUsernameHeader.
     * </p>
     *
     * @param usernameHeader
     *            a {@link java.lang.String} object.
     * @since 3.0.1
     */
    public void setUsernameHeader(String usernameHeader);

    /**
     * <p>
     * getUsernameHeader.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @since 3.0.1
     */
    public String getUsernameHeader();

    /**
     * <p>
     * setCaseInsensitiveUsernames.
     * </p>
     *
     * @param caseInsensitiveUsernames
     *            a boolean.
     * @since 3.0.1
     */
    public void setCaseInsensitiveUsernames(boolean caseInsensitiveUsernames);

    /**
     * <p>
     * isCaseInsensitiveUsernames.
     * </p>
     *
     * @return a boolean.
     * @since 3.0.1
     */
    public boolean isCaseInsensitiveUsernames();


	/**
	 * <p>initUrlListFromString.</p>
	 *
	 * @param gatewayUrl a {@link java.lang.String} object.
	 */
	void initUrlListFromString(String gatewayUrl);
}
