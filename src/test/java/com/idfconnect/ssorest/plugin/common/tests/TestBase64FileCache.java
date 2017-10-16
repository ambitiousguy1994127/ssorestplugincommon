package com.idfconnect.ssorest.plugin.common.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.idfconnect.ssorest.plugin.common.b64.Base64Content;
import com.idfconnect.ssorest.plugin.common.b64.Base64ContentCache;
import com.idfconnect.ssorest.plugin.common.b64.Base64ContentProvider;
import com.idfconnect.ssorest.plugin.common.b64.LocalBase64Content;

public class TestBase64FileCache {
    private static Logger logger = LoggerFactory.getLogger(TestBase64FileCache.class);
    private static final String contentUri = "./login.fcc";

    Base64ContentProvider<Object> provider = new Base64ContentProvider<Object>() {
        @Override
        public Base64Content getBase64Content(String uri, String contextName, Object containerContext) throws IOException {
            Base64Content content = new LocalBase64Content(uri);
            return content;
        }
    };
    
    @Test
    public void test() throws Exception {
        Base64ContentCache<Object> cache = new Base64ContentCache<Object>(provider);
        URL resource = getClass().getClassLoader().getResource(contentUri);
        logger.info("Test resource: {}", resource);
        File resourceFile = new File(resource.toURI());
        logger.info("Test resource file: {}", resourceFile);
        Base64Content entry = cache.getContent(resourceFile.getAbsolutePath(), null, null);
        
        logger.debug("Cache Entry: {}", entry.getContent());
        
        String content = new String(Base64.decodeBase64(entry.getContent()));
        logger.debug("Cache Content: {}", content);

        Base64InputStream b64is = new Base64InputStream(new ByteArrayInputStream(entry.getContent().getBytes()));
        LineNumberReader lnr64 = new LineNumberReader(new InputStreamReader(b64is));
        LineNumberReader lnr = new LineNumberReader(new FileReader(resourceFile));
        String nextline = null;
        while ((nextline = lnr.readLine()) != null) {
            String nextline2 = lnr64.readLine();
            logger.info("{}: {}", lnr.getLineNumber(), nextline);
            assertEquals("Line " + lnr.getLineNumber() + " values are equal", nextline, nextline2);
        }
        assertNull("No more content in 64-linereader", lnr64.readLine());
        lnr.close();
        lnr64.close();
    }
}
