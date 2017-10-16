package com.idfconnect.ssorest.plugin.common.b64;

import java.io.IOException;

/**
 * Holds base64-encoded content for use in the content cache
 *
 * @author Richard Sand
 * @since 1.1.1
 */
public interface Base64Content {
    /**
     * Reload the content
     *
     * @throws java.io.IOException if any.
     */
    void reload() throws IOException;

    /**
     * Returns true if the content has been modified since being read into the cache
     *
     * @return boolean
     */
    boolean isStale();

    /**
     * Returns the base64 encoded content
     *
     * @return String
     */
    String getContent();

    /**
     * Timestamp when the content was last modified
     *
     * @return long
     */
    long getLastModified();
}
