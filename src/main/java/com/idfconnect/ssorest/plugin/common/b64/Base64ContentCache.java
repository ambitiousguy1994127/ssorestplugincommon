package com.idfconnect.ssorest.plugin.common.b64;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple content cache storing the contents in memory as Base64 encoded Strings
 *
 * @author Richard Sand
 * @since 1.1.1
 */
public class Base64ContentCache<T> {
    HashMap<String, Base64Content> contentMap = new HashMap<String, Base64Content>();
    Base64ContentProvider<T>          provider   = null;
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * <p>Constructor for Base64ContentCache.</p>
     *
     * @param cp a {@link com.idfconnect.ssorest.plugin.common.b64.Base64ContentProvider} object.
     * @since 1.1.1
     */
    public Base64ContentCache(Base64ContentProvider<T> cp) {
        this.provider = cp;
    }

    /**
     * <p>getContent.</p>
     *
     * @param uri a {@link java.lang.String} object.
     * @param contextName a {@link java.lang.String} object.
     * @param containerContext a T object.
     * @return a {@link com.idfconnect.ssorest.plugin.common.b64.Base64Content} object.
     * @throws java.io.IOException if any.
     * @since 1.1.1
     */
    public Base64Content getContent(String uri, String contextName, T containerContext) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (contextName != null)
            sb.append(contextName).append(":");
        sb.append(uri);
        String fullKey = sb.toString();
        logger.trace("Cache request for key {}", fullKey);

        // Determine the content to use, and refresh if necessary
        Base64Content entry = null;
        if (contentMap.containsKey(fullKey)) {
            logger.trace("Content for {} found in cache", fullKey);
            entry = contentMap.get(fullKey);
        }
        else {
            entry = provider.getBase64Content(uri, contextName, containerContext);
            if (entry == null) {
                logger.trace("Provider returned null for {}", fullKey);
                return null;
            }
            logger.trace("Adding entry to cache for {}", fullKey);
            contentMap.put(fullKey, entry);
        }
        
        // If the entry is stale, reload
        if (entry.isStale()) {
            logger.trace("Reloading stale content {}", uri);
            entry.reload();
        }
        
        return entry;
    }
    
    /**
     * This method will directly load the content from the file instead of checking the cahce.
     *
     * @param uri a {@link java.lang.String} object.
     * @param contextName a {@link java.lang.String} object.
     * @param containerContext a T object.
     * @return a {@link com.idfconnect.ssorest.plugin.common.b64.Base64Content} object.
     * @throws java.io.IOException if any.
     * @since 1.1.1
     */
    public Base64Content getContentWithoutCache(String uri, String contextName, T containerContext) throws IOException {
        Base64Content entry = null;
     
        entry = provider.getBase64Content(uri, contextName, containerContext);
        if (entry == null) {
            logger.trace("Provider returned null for {}", uri);
            return null;
        }
        // If the entry is stale, reload
        if (entry.isStale()) {
            logger.trace("Reloading stale content {}", uri);
            entry.reload();
        }

        return entry;
    }
}
