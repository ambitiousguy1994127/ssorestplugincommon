package com.idfconnect.ssorest.plugin.common.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.idfconnect.ssorest.plugin.common.PluginConfiguration;

public class URIHandlingTests {
    String gw1 = "http://www.idfconnect.net/ssorest";
    String gw1Public = gw1 + "/" + PluginConfiguration.SSOREST_SERVICES_URI + "/" + PluginConfiguration.SSOREST_PUBLIC_SERVICES_URI;
    String uri1 = "/ssorest/service/public/idlettl";
    
    public URIHandlingTests() {
    }
    
    @Test
    public void test1() throws URISyntaxException {
        URI gwUri = new URI(gw1 + "/");
        
        System.out.println("gwUri: " + gwUri);
        
        assertTrue(uri1.startsWith(gwUri.getPath()));
        
        URI gwPub = new URI(gw1Public + "/");
        
        System.out.println("gwPub: " + gwPub);
        
        assertTrue(uri1.startsWith(gwPub.getPath()));
        
        URI target = new URI(gwPub + uri1.substring(gwPub.getPath().length()));
        
        System.out.println("target: " + target);

        assertEquals(uri1, target.getPath());
    }

}
