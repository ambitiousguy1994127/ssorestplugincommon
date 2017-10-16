package com.idfconnect.ssorest.plugin.common.jersey;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;

import com.idfconnect.ssorest.plugin.common.AbstractHttpServletPlugin;

/**
 * Extension of {@link com.idfconnect.ssorest.plugin.common.AbstractHttpServletPlugin} that uses a Jersey client
 *
 * @author Richard Sand
 * @param <T>
 */
public abstract class AbstractHttpServletPluginJersey<T> extends AbstractHttpServletPlugin<T> {
    /**
     * {@inheritDoc}
     *
     * Configure the JAX-RS Client Configuration for the gateway endpoint. This default implementation uses Jersey client.
     * @since 3.0.3
     */
    @Override
    protected WebTarget createJaxRSWebTarget(final URI gatewayUri, final HttpClientConnectionManager ccm, final RequestConfig requestConfig, final Class<?>[] providers) {
        // Configure the Jersey client connection handler with the Apache client
        // All Apache custom configuration is maintained within the connection manager and requestconfig
        // These must be conveyed to the Jersey client

        org.glassfish.jersey.client.ClientConfig cc = new org.glassfish.jersey.client.ClientConfig()
                .connectorProvider(new org.glassfish.jersey.apache.connector.ApacheConnectorProvider())
                .property(org.glassfish.jersey.apache.connector.ApacheClientProperties.CONNECTION_MANAGER, ccm)
                .property(org.glassfish.jersey.apache.connector.ApacheClientProperties.REQUEST_CONFIG, requestConfig);
        for (Class<?> providerClass : providers)
            cc.register(providerClass);
        
        Client jerseyClient = ClientBuilder.newClient(cc);
        return jerseyClient.target(gatewayUri);
    }
}
