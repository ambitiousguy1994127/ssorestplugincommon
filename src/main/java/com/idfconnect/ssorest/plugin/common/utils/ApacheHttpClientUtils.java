package com.idfconnect.ssorest.plugin.common.utils;

import java.util.Date;

import javax.servlet.http.Cookie;

import com.idfconnect.ssorest.common.http.FriendlyCookie;

/**
 * <p>ApacheHttpClientUtils class.</p>
 *
 * @author rsand
 * @since 1.1.1
 */
public class ApacheHttpClientUtils {
    private ApacheHttpClientUtils() {
    }

    /**
     * Method to convert an Apache HttpClient cookie to a Java Servlet cookie
     *
     * @param apacheCookie
     *            the source apache cookie
     * @return a java servlet cookie
     * @since 1.1.1
     */
    public static Cookie convertApacheCookieToServletCookie(org.apache.http.cookie.Cookie apacheCookie) {
        if (apacheCookie == null) {
            return null;
        }

        String name = apacheCookie.getName();
        String value = apacheCookie.getValue();

        FriendlyCookie cookie = new FriendlyCookie(name, value);

        // set the domain
        value = apacheCookie.getDomain();
        if (value != null) {
            cookie.setDomain(value);
        }

        // path
        value = apacheCookie.getPath();
        if (value != null) {
            cookie.setPath(value);
        }

        // secure
        cookie.setSecure(apacheCookie.isSecure());

        // comment
        value = apacheCookie.getComment();
        if (value != null) {
            cookie.setComment(value);
        }

        // version
        cookie.setVersion(apacheCookie.getVersion());

        // From the Apache source code, maxAge is converted to expiry date using the following formula:
        // if (maxAge >= 0) {
        // setExpiryDate(new Date(System.currentTimeMillis() + maxAge * 1000L));
        // }
        // We reverse this to get the actual max age
        Date expiryDate = apacheCookie.getExpiryDate();
        if (expiryDate != null) {
            long maxAge = (expiryDate.getTime() - System.currentTimeMillis()) / 1000;
            // we have to cast down, no other option
            cookie.setMaxAge((int) maxAge);
        }

        // return the servlet cookie
        return cookie;
    }
}
