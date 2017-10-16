/**
 * 
 */
package com.idfconnect.ssorest.plugin.common.utils;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is created to assert if the incoming URL can be ignored
 *
 * @author Tony
 * @since 1.1.1
 */
public class IgnorePattern implements Serializable {
    private static final long   serialVersionUID = 284064937832081969L;
    private static final String DELIMETER        = ",";

    // we might add more in future
    private Set<String>         ignoreExt;
    private Set<String>         ignoreHost;
    private Set<String>         ignoreUrl;

    private Set<String> toSet(String str) {
        if (str == null)
            return null;
        // we use hashset here because it is fast!
        Set<String> set = new HashSet<String>();
        try {
            String[] s = str.split(DELIMETER);
            for (String string : s) {
                set.add(string.toLowerCase());
            }
        } catch (Exception e) {
            return null;
        }
        return set;
    }

    /**
     * <p>
     * Setter for the field <code>ignoreExt</code>.
     * </p>
     *
     * @param ingoreExt
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    public void setIgnoreExt(String ingoreExt) {
        this.ignoreExt = toSet(ingoreExt);
    }

    /**
     * <p>
     * Setter for the field <code>ignoreHost</code>.
     * </p>
     *
     * @param ingoreHost
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    public void setIgnoreHost(String ingoreHost) {
        this.ignoreHost = toSet(ingoreHost);
        ;
    }

    /**
     * <p>
     * Setter for the field <code>ignoreUrl</code>.
     * </p>
     *
     * @param ingoreUrl
     *            a {@link java.lang.String} object.
     * @since 1.1.1
     */
    public void setIgnoreUrl(String ingoreUrl) {
        this.ignoreUrl = toSet(ingoreUrl);
        ;
    }

    /**
     * This is the method to find out if the URL will need to be processed by the filter based on the conditions
     *
     * @param urlIn
     *            a {@link java.lang.String} object.
     * @return a boolean.
     * @since 1.1.1
     */
    public boolean isIgnored(String urlIn) {
        URI uri = null;
        try {
            uri = new URI(urlIn);

            if (ignoreExt != null && ignoreExt.size() != 0) {
                // simple logic to parse and get the file ext
                String path = uri.getPath();
                // need to make everything lower case?
                String ext = path.substring(path.lastIndexOf(".")).toLowerCase();
                if (ignoreExt.contains(ext))
                    return true;
            }
            if (ignoreHost != null && ignoreHost.size() != 0) {
                String host = uri.getHost().toLowerCase() + ":" + uri.getPort();
                if (ignoreHost.contains(host))
                    return true;
            }
            if (ignoreUrl != null && ignoreUrl.size() != 0) {
                String path = uri.getPath().toLowerCase();
                if (ignoreUrl.contains(path))
                    return true;
            }

        } catch (Exception e) {
            // url cannot be parsed, cannot ignore
            // TODO - handle exception here
        }
        return false;
    }

}
