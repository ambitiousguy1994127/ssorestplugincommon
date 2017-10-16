package com.idfconnect.ssorest.plugin.common.b64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * <p>
 * LocalBase64Content class.
 * </p>
 *
 * @author rsand
 * @since 1.1.1
 */
public class LocalBase64Content implements Base64Content {
    String path         = null;
    long   lastModified = -1;
    String content      = null;

    /**
     * Creates a new LocalBase64Content
     *
     * @param path
     *            filesystem path
     * @throws java.io.IOException
     *             if any.
     * @since 1.1.1
     */
    public LocalBase64Content(String path) throws IOException {
        this.path = path;
        reload();
    }

    /**
     * Creates a new LocalBase64Content
     *
     * @param is
     *            input stream
     * @throws java.io.IOException
     *             if any.
     * @since 1.1.1
     */
    public LocalBase64Content(InputStream is) throws IOException {
        loadInputStream(is);
    }

    /** {@inheritDoc} */
    @Override
    public void reload() throws IOException {
        File f = new File(path);
        lastModified = f.lastModified();
        FileInputStream is = new FileInputStream(f);
        try {
            loadInputStream(is);
        } finally {
            is.close();
        }
    }

    private void loadInputStream(InputStream is) throws IOException {
        byte[] bytes = IOUtils.toByteArray(is);
        content = Base64.encodeBase64URLSafeString(bytes);
        is.close();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStale() {
        if (path == null)
            return false;
        File f = new File(path);
        return (f.lastModified() > lastModified);
    }

    /** {@inheritDoc} */
    @Override
    public String getContent() {
        return content;
    }

    /** {@inheritDoc} */
    @Override
    public long getLastModified() {
        return lastModified;
    }
}
