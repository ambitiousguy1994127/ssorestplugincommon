package com.idfconnect.ssorest.plugin.common.b64;

import java.io.IOException;

/**
 * An interface that is capable of loading and returning a Base64Content object based upon a provided URI.
 * This interface is used by the Base64ContentCache to request content by the app container to be loaded that isn't present in the cache
 *
 * @author Richard Sand
 * @since 1.1.1
 */
public interface Base64ContentProvider<T> {
    /**
     * getBase64Content
     *
     * @param uri the URI of the content to provide
     * @param contextName the name of the application container context
     * @param containerContext the context of the application container
     * @return Base64Content
     * @throws java.io.IOException if any.
     * @since 1.1.1
     */
    Base64Content getBase64Content(String uri, String contextName, T containerContext) throws IOException;
}
