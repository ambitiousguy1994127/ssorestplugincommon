/**
 * 
 */
package com.idfconnect.ssorest.plugin.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * <p>
 * ConnectionPoolManager class.
 * </p>
 *
 * @author Tony Liang
 */
// TODO in checkConnections, if no state has changed, no need to recreate the iterable
// TODO after checkConnections, the iterable list is restarted - need a better (and threadsafe) way to insert/remove URLs in the cycle w/o disturbing the cycle
// TODO iterables are not thread safe, need something better or some way to ensure thread safety
public class PollingGatewayUrlManager extends Thread implements GatewayUrlManager {
    /** Constant <code>POLLING_URI_DEFAULT="/alive.txt"</code> */
    public static final String POLLING_URI_DEFAULT  = "/alive.txt";

    /** Constant <code>POLLING_URI_PROPERTY="pollingUri"</code> */
    public static final String POLLING_URI_PROPERTY = "pollingUri";                       // TODO check configuration

    Logger                     logger               = LoggerFactory.getLogger(getClass());

    PluginConfiguration        pc                   = null;
    long                       normalInterval       = 300000L;
    long                       urgentInterval       = 60000L;
    int                        quorum               = 50;                                 // 0 to 100
    long                       interval             = normalInterval;
    HttpClientBuilder          httpClientBuilder    = null;
    HttpClient                 httpclient           = null;
    boolean                    isRunning            = false;
    boolean                    isAvailable          = false;                              // if there is no available member at this pool, it will be false.
    SSLContext                 sslCtx               = null;
    String                     pollingUrl           = POLLING_URI_DEFAULT;
    String[]                   gatewayUrls          = null;

    // available gateway urls
    Iterator<String>           gatewayUrlPool       = null;
    String                     currentGatewayUrl    = null;

    /**
     * <p>
     * Constructor for ConnectionPoolManager.
     * </p>
     *
     * @param pc
     *            a {@link com.idfconnect.ssorest.plugin.common.PluginConfiguration} object.
     * @param sslctx
     *            a {@link javax.net.ssl.SSLContext} object.
     * @since 3.0.1
     */
    public PollingGatewayUrlManager(PluginConfiguration pc, SSLContext sslctx) {
        super();
        this.pc = pc;
        this.quorum = pc.getQuorum();
        this.normalInterval = pc.getNormalInterval();
        this.urgentInterval = pc.getUrgentInterval();
        this.gatewayUrls = pc.getGatewayUrls();
        this.sslCtx = sslctx;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        logger.info("Polling thread started");
        isRunning = true;
        while (isRunning) {
            logger.debug("Checking Gateway URL connections...");
            checkConnections();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }
        }
        logger.info("Polling thread stopped");
    }

    /**
     * <p>
     * calculateNextInterval.
     * </p>
     *
     * @param candidateSize
     *            a int.
     * @param availableSize
     *            a int.
     * @return a long.
     * @since 3.0.1
     */
    public long calculateNextInterval(int candidateSize, int availableSize) {
        int d = availableSize * 100 / candidateSize; // calculate the current quorum
        if (d > quorum) { // this will go for normal scanning frequency
            return normalInterval;
        }
        return urgentInterval;

    }

    /**
     * <p>
     * checkConnections.
     * </p>
     *
     * @since 3.0.1
     */
    protected synchronized void checkConnections() {
        if (gatewayUrls == null || gatewayUrls.length == 0) {
            logger.error("No Gateway URL is defined.");
            return;
        }

        List<String> gatewayUrlReady = null;
        gatewayUrlReady = new LinkedList<String>();

        // sleep certain gap here
        logger.debug("Checking all connection pool urls [size={}]", gatewayUrls.length);
        for (String gatewayUrl : gatewayUrls) {
            try {
                String url = gatewayUrl + pollingUrl;
                HttpGet get = new HttpGet(url);
                HttpResponse resp = httpclient.execute(get);
                // HttpEntity entity = resp.getEntity();
                int rc = resp.getStatusLine().getStatusCode();
                if (rc == HttpStatus.SC_OK) {
                    logger.trace("Connection URL {} is UP", gatewayUrl, rc);
                    gatewayUrlReady.add(gatewayUrl);
                } else {
                    logger.warn("Connection URL {} is not ready, rc=", gatewayUrl, rc);
                }
                get.abort();
            } catch (IOException e) {
                logger.warn("Not able to connect to URL {}, reason: {}", gatewayUrl, e.getMessage());
            }
        }
        if (gatewayUrlReady.size() > 0) {
            isAvailable = true;
            logger.info("Connection Pool validation is completed. Size is {}", gatewayUrlReady.size());
        } else {
            isAvailable = false;
            logger.error("No gateway URLs are available at this time");
        }
        interval = calculateNextInterval(gatewayUrls.length, gatewayUrlReady.size());
        gatewayUrlPool = Iterables.cycle(gatewayUrlReady).iterator();
    }

    /**
     * <p>
     * shutdown.
     * </p>
     *
     * @since 3.0.1
     */
    public void shutdown() {
        isRunning = false;
    }

    /**
     * <p>
     * startPoolManager.
     * </p>
     *
     * @since 3.0.1
     */
    public synchronized void lauchManagerThread() {
        // Initialize URLs - start with all
        logger.info("Launching manager thread for URLs: {}", (Object[]) pc.getGatewayUrls());
        this.gatewayUrlPool = Iterables.cycle(gatewayUrls).iterator();
        if (gatewayUrls.length > 1) {
            int random = new Random().nextInt(gatewayUrls.length);
            for (int i = 0; i < random; i++)
                gatewayUrlPool.next();
        }
        currentGatewayUrl = gatewayUrlPool.next();

        // Initialize HTTP client
        httpClientBuilder = HttpClientBuilder.create();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        int timeout = 15000;
        if (pc.getConnectionTimeout() > -1) {
            timeout = pc.getConnectionTimeout();
        }
        logger.info("Setting connection timeout to {} ms", timeout);
        requestConfigBuilder.setConnectTimeout(timeout) // connection timeout
                .setConnectionRequestTimeout(timeout); // timeout waiting for connection from pool
        PoolingHttpClientConnectionManager cm = null;

        if (sslCtx != null) {
            logger.info("Creating custom ConnectionSocketFactory for SSL");
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslCtx))
                    .build();
            cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        } else {
            cm = new PoolingHttpClientConnectionManager();
        }
        httpclient = httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build()).setConnectionManager(cm).build();

        // Start
        start();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Getter for the field <code>currentGatewayUrl</code>.
     * </p>
     */
    @Override
    public synchronized String getCurrentGatewayUrl() {
        return currentGatewayUrl;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * getNextGatewayUrl.
     * </p>
     */
    @Override
    public synchronized String getNextGatewayUrl() {
        // TODO should we return null if the pool does not meet quorum?
        currentGatewayUrl = (gatewayUrlPool == null || !gatewayUrlPool.hasNext()) ? null : gatewayUrlPool.next();
        logger.trace("Returning next gateway URL {}", currentGatewayUrl);
        return currentGatewayUrl;
    }

    /**
     * <p>
     * isReady.
     * </p>
     *
     * @return a boolean.
     * @since 3.0.1
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * <p>
     * isRunning.
     * </p>
     *
     * @return a boolean.
     * @since 3.0.1
     */
    public boolean isRunning() {
        return isRunning;
    }
}
