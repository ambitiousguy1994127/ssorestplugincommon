package com.idfconnect.ssorest.plugin.common;

import java.util.Iterator;

import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * A simple round-robin URL manager that does no polling or testing
 *
 * @author Richard Sand
 */
public class SimpleGatewayUrlManager implements GatewayUrlManager {
    // available gateway urls
    Iterator<String>           gatewayUrlPool       = null;
    String                     currentGatewayUrl    = null;

    /**
     * <p>Constructor for SimpleGatewayUrlManager.</p>
     *
     * @param pc a {@link com.idfconnect.ssorest.plugin.common.PluginConfiguration} object.
     */
    public SimpleGatewayUrlManager(PluginConfiguration pc) {
        this.gatewayUrlPool = Iterables.cycle(pc.getGatewayUrls()).iterator();
        getNextGatewayUrl(); // add this to initialie the current gateway url. 
    }

    /** {@inheritDoc} */
    @Override
    public String getCurrentGatewayUrl() {
        return currentGatewayUrl;
    }

    /** {@inheritDoc} */
    @Override
    public String getNextGatewayUrl() {
        currentGatewayUrl = (gatewayUrlPool == null || !gatewayUrlPool.hasNext()) ? null : gatewayUrlPool.next();
        LoggerFactory.getLogger(getClass()).trace("Returning next gateway URL {}", currentGatewayUrl);
        return currentGatewayUrl;
    }
}
