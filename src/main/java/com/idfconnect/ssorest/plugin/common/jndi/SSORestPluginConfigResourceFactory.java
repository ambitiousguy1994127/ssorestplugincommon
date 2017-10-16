package com.idfconnect.ssorest.plugin.common.jndi;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.idfconnect.ssorest.common.utils.JndiMapperUtil;
import com.idfconnect.ssorest.plugin.common.CommonPluginConfiguration;

/**
 * Factory for defining plugin configuration as a Resource Environment Provider
 *
 * @author Nghia
 */
public class SSORestPluginConfigResourceFactory implements ObjectFactory {
    /** Default JNDI resource name */
    public static final String SSO_CONFIG_RESOURCE_JNDI_DEFAULT = "sso/SSORestResourceReference";

    /** Config property constant for use with providing the JNDI name */
    public static final String JNDI_CONFIG_PROVIDER_NAME_PROP   = "jndiConfigProviderName";

    /** Config property constant for use with enabling the JNDI configuration capability */
    public static final String USE_JNDI_CONFIG_PROVIDER_PROP    = "useJndiConfigProvider";

    static Logger              logger                           = LoggerFactory.getLogger(SSORestPluginConfigResourceFactory.class);

    /**
     * Returns the configuration resource using the default JNDI name
     *
     * @return the default configuration resource
     * @throws javax.naming.NamingException
     *             if any.
     * @since 3.0.1
     */
    public static CommonPluginConfiguration getConfiguration() throws NamingException {
        return getConfiguration(SSO_CONFIG_RESOURCE_JNDI_DEFAULT);
    }

    /**
     * Returns the configuration resource using the specified JNDI name
     *
     * @param jndiName
     *            a {@link java.lang.String} object.
     * @return a {@link com.idfconnect.ssorest.plugin.common.CommonPluginConfiguration} object.
     * @throws javax.naming.NamingException
     *             if any.
     * @since 3.0.1
     */
    public static CommonPluginConfiguration getConfiguration(String jndiName) throws NamingException {
        Context ctx = new InitialContext();
        if (logger.isTraceEnabled()) {
            logger.trace("Entering getConfiguration for JNDI name {}", jndiName);

            try {
                int found = 0;
                Hashtable<?, ?> env = ctx.getEnvironment();
                for (Entry<?, ?> entry : env.entrySet())
                    logger.trace("Environment entry [{}] {}={}", found++, entry.getKey(), entry.getValue());
                logger.trace("Total environnment entries found: " + found);

                logger.trace(JndiMapperUtil.dumpContext(ctx).toString());
            } catch (Exception e) {
                logger.warn("We were trying to tell you all of the JNDI names and all we got was this exception", e);
            }
        }
        SSORestPluginConfigResource config = (SSORestPluginConfigResource) ctx.lookup(jndiName);
        return config;
    }

    static class SSORestPluginConfigResource extends CommonPluginConfiguration {
        private Map<String, String> attributes = null;

        SSORestPluginConfigResource() {
            attributes = new HashMap<String, String>();
        }

        void setAttribute(String attributeName, String attributeValue) {
            attributes.put(attributeName, attributeValue);
        }

        String getAttribute(String attributeName) {
            return attributes.get(attributeName);
        }

        CommonPluginConfiguration initialize() {
            initialize(attributes);
            return this;
        }
    }

    /** {@inheritDoc} */
    @Override
    public CommonPluginConfiguration getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        SSORestPluginConfigResource config = new SSORestPluginConfigResource();
        Reference ref = (Reference) obj;
        Enumeration<?> addrs = ref.getAll();
        RefAddr addr = null;
        String entryName = null;
        String value = null;
        while (addrs.hasMoreElements()) {
            addr = (RefAddr) addrs.nextElement();
            entryName = addr.getType();
            value = (String) addr.getContent();
            config.setAttribute(entryName, value);
        }
        return config.initialize();
    }
}
