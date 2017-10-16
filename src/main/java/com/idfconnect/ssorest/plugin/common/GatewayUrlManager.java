package com.idfconnect.ssorest.plugin.common;

/**
 * Interface for classes providing Gateway URLs to the plugins
 *
 * @author Richard Sand
 */
public interface GatewayUrlManager {
    /**
     * <p>
     * Getter for the field <code>currentGatewayUrl</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getCurrentGatewayUrl();

    /**
     * <p>
     * getNextGatewayUrl.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getNextGatewayUrl();
}
