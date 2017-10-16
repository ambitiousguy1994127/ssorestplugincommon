package com.idfconnect.ssorest.plugin.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.idfconnect.ssorest.api.json.JSonGatewayMessageBodyProvider;
import com.idfconnect.ssorest.api.json.JSonGatewayResponse;
import com.idfconnect.ssorest.api.json.JSonHttpRequest;
import com.idfconnect.ssorest.api.json.JSonHttpResponse;
import com.idfconnect.ssorest.common.SSORestException;
import com.idfconnect.ssorest.common.crypto.PropertyValueEncryptionUtil;
import com.idfconnect.ssorest.common.http.FilterHttpRequestWrapper;
import com.idfconnect.ssorest.common.http.WebAgentRequestWrapper;
import com.idfconnect.ssorest.common.http.WebAgentResponseWrapper;
import com.idfconnect.ssorest.common.ssl.SSLUtil;
import com.idfconnect.ssorest.common.utils.ConfigProperties;
import com.idfconnect.ssorest.common.utils.HTMLPageGenerator;
import com.idfconnect.ssorest.common.utils.Signature;
import com.idfconnect.ssorest.plugin.common.b64.Base64Content;
import com.idfconnect.ssorest.plugin.common.b64.Base64ContentCache;
import com.idfconnect.ssorest.plugin.common.b64.Base64ContentProvider;
import com.idfconnect.ssorest.plugin.common.utils.ApacheHttpClientUtils;
import com.idfconnect.ssorest.plugin.common.utils.IgnorePattern;
import com.idfconnect.ssorest.plugin.common.version.PluginVersions;

/**
 * Base class for SSO/Rest Plugin implementations which works on javax.servlet.http request and response objects. Most java plugins (Apache Tomcat, Servlet
 * Filter) are based on extending or wrapping this class.
 *
 * @author Richard Sand
 * @param <T>
 *            an optional context object passed in from the specific application server container
 * @since 1.1.1
 */
public abstract class AbstractHttpServletPlugin<T> implements Base64ContentProvider<T> {
    /** Constant <code>GATEWAY_TOKEN_NAME="gatewayToken"</code> */
    public static final String                 GATEWAY_TOKEN_NAME     = "gatewayToken";

    // This is needed specifically for WAS support or other containers where multiple layers must call the gateway for the same request
    /** Constant <code>GATEWAY_RESPONSE_ATTR="gatewayResponse"</code> */
    public static final String                 GATEWAY_RESPONSE_ATTR  = "gatewayResponse";

    /** Constant to indicate that a signature is needed by the gateway */
    public static final String                 SIGNATURE_NEEDED       = "Signature Needed";

    /** Name of the challenge header */
    public static final String                 CHALLENGE_HEADER_NAME  = "Challenge";

    /** Name of the randomText attribute */
    public static final String                 RANDOMTEXT_ATTR        = "randomText";

    /** Name of the randomTextSigned attribute */
    public static final String                 RANDOMTEXT_SIGNED_ATTR = "randomTextSigned";

    private static final String                REQUESTID              = "requestId";

    private Logger                             logger                 = LoggerFactory.getLogger(getClass());
    private URI                                ssoRestPublicUrl       = null;
    private String                             acoName                = null;
    private Base64ContentCache<T>              b64cache               = null;
    protected SecureRandom                     random                 = null;
    private URI                                currentGatewayUrl      = null;

    // for authentication
    private String                             gatewayToken           = null;
    private String                             pluginID               = null;
    private String                             secretKey              = null;

    // ignore type of parameters
    private IgnorePattern                      ignorePattern          = null;
    private PluginConfiguration                pc                     = null;

    // Apache HTTP Client variables
    private PoolingHttpClientConnectionManager cm                     = null;
    private CloseableHttpClient                httpclient             = null;
    // private RequestConfig requestConfig = null;

    // JAX-RS client
    private static Class<?>[]                  providers              = { JSonGatewayMessageBodyProvider.class };
    private WebTarget                          gatewayWebTarget       = null;

    // URL Manager
    private GatewayUrlManager                  urlManager             = null;

    private RequestConfig                      requestConfig;

    /**
     * Default constructor
     *
     * @since 1.1.1
     */
    public AbstractHttpServletPlugin() {
        super();
    }

    /**
     * <p>
     * shutdown.
     * </p>
     *
     * @since 1.1.1
     */
    public void shutdown() {
        if (httpclient != null)
            try {
                httpclient.close();
            } catch (IOException ioe) {
                logger.error("IOException closing HttpClient", ioe);
            }
        if (cm != null)
            cm.shutdown();
    }

    /**
     * <p>
     * processRequest.
     * </p>
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param containerContext
     *            a T object.
     * @param contextName
     *            a {@link java.lang.String} object.
     * @throws java.io.IOException
     *             if any.
     * @throws javax.servlet.ServletException
     *             if any.
     * @throws java.net.URISyntaxException if any.
     * @since 1.1.1
     */
    public final void processRequest(HttpServletRequest request, HttpServletResponse response, String contextName, T containerContext) throws IOException, ServletException, URISyntaxException {
        String requestId = UUID.randomUUID().toString();
        logger.trace("Starting requestId={}", requestId);
        MDC.put(REQUESTID, requestId);
        // stopped using MDCCloseable because it uses a later version of slf4j than JBoss/Wildfly ship with
        // MDCCloseable mdc = MDC.putCloseable("requestId", requestId);

        // See if we already have processed this request - if so, jump immediately to the handleAlloWContinue
        JSonGatewayResponse gatewayResponse = (JSonGatewayResponse) request.getAttribute(GATEWAY_RESPONSE_ATTR);
        if (gatewayResponse != null && gatewayResponse.getResponse().getStatus() == HttpServletResponse.SC_CONTINUE) {
            logger.debug("Found gatewayResponse attribute in request");
            handleAllowContinue(request, response, gatewayResponse, containerContext);
            return;
        }

        processRequestInt(request, response, contextName, containerContext);
        MDC.remove(REQUESTID);
        // mdc.close();
    }

    /**
     * Enforces that the request principal (as asserted by the container) matches the SSO/Rest username
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param gatewayResponse
     *            a {@link com.idfconnect.ssorest.api.json.JSonGatewayResponse} object.
     * @return true if enforcement has been done (response is committed)
     * @throws java.io.IOException
     *             if any.
     * @throws javax.servlet.ServletException
     *             if any.
     * @since 3.0.1
     */
    protected final boolean enforcePrincipal(HttpServletRequest request, HttpServletResponse response, JSonGatewayResponse gatewayResponse) throws IOException, ServletException {
        Principal p = request.getUserPrincipal();
        if (p == null || p.getName() == null) {
            logger.debug("EnforcePrincipal: No principal in request");
            return false;
        }
        String username = gatewayResponse.getRequest().getHeader(pc.getUsernameHeader());
        if (username == null) {
            logger.debug("EnforcePrincipal: No username header {} in request", pc.getUsernameHeader());
            return false;
        }

        if (pc.isCaseInsensitiveUsernames() && username.equalsIgnoreCase(p.getName())
                || (!pc.isCaseInsensitiveUsernames() && username.equals(p.getName()))) {
            logger.debug("EnforcePrincipal check passed");
            return false;
        }

        // TODO - add check for SSO/Rest User Principal, and if so, check session IDs as well
        // TODO add similar enforce method for HttpSession
        logger.debug("Principal {} does not match username {}, calling logout and redirecting", p.getName(), username);

        // NOTE - this is Servlet 3.0 compatible only
        // TODO - do we need to also call session.invalidate()?
        request.logout();

        // Transfer any new cookies to the response
        for (Cookie c : gatewayResponse.getResponse().getCookies()) {
            logger.debug("Transferring gateway cookie to response {}", c);
            response.addCookie(c);
        }

        // TODO add ability to configure custom URL for this use case
        response.sendRedirect(request.getRequestURL().toString());
        return true;
    }

    private final void processRequestInt(HttpServletRequest request, HttpServletResponse response, String contextName, T containerContext) throws IOException, ServletException, URISyntaxException {
        // Do nothing if we're not enabled
        if (!pc.isEnabled()) {
            passRequestToContainer(request, response, containerContext);
            return;
        }

        // Do nothing if we match an ignore pattern
        String requestURI = request.getRequestURI();
        if (ignorePattern.isIgnored(requestURI)) {
            logger.trace("Auto-allowing ignored pattern {}", requestURI);
            passRequestToContainer(request, response, containerContext);
            return;
        }

        // Handle special SSORest public endpoint URLs
        if (pc.isEnablePublicServices() && requestURI.startsWith(ssoRestPublicUrl.getPath())) {
            handleSsoRestPublicEndpoints(request, response);
            return;
        }

        // Starting time
        long start = 0;
        if (logger.isDebugEnabled())
            start = System.currentTimeMillis();

        // Log the inbound request
        logger.info("Processing new request {} {} {} from {}", request.getProtocol(), request.getMethod(), requestURI, request.getRemoteAddr());

        // Marshall the JSon request for the gateway
        // Send aco name anyway even if it is null
        request.setAttribute(PluginConfiguration.SSOREST_ACO_NAME, acoName);
        request.setAttribute(PluginConfiguration.PLUGIN_ID_NAME, pluginID);
        request.setAttribute(GATEWAY_TOKEN_NAME, gatewayToken);
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        JSonHttpRequest jsonRequest = new JSonHttpRequest(request);
        logger.debug("Request details {}", jsonRequest);

        // Talk to the gateway
        JSonGatewayResponse gatewayResponse = null;
        Entity<JSonHttpRequest> entity = Entity.entity(jsonRequest, MediaType.APPLICATION_JSON_TYPE);
        Response clientResponse = postJson(entity, 0);

        gatewayResponse = clientResponse.readEntity(JSonGatewayResponse.class);
        if (logger.isDebugEnabled()) {
            // Compute processing time
            long time = System.currentTimeMillis() - start;
            logger.debug("Gateway request took {} ms", time);
        }
        if (logger.isTraceEnabled())
            logger.trace("Response received, post-processing request JSon {}", gatewayResponse.getRequest());

        // Expose the JSon response for wonky application servers like WAS
        request.setAttribute(GATEWAY_RESPONSE_ATTR, gatewayResponse);

        // Immediate error handling - explicitly handle 500 and 502 errors only, re-throw others
        if ((clientResponse.getStatusInfo() == Status.BAD_GATEWAY) || (clientResponse.getStatusInfo() == Status.INTERNAL_SERVER_ERROR)) {
            String file = pc.getErrorPage();
            if (file == null)
                file = PluginConfiguration.DEFAULT_500_ERROR_FILE;
            try {
                HTMLPageGenerator.write500ResponseStream(response, file, "Unable to communicate with the gateway server", null, HTMLPageGenerator.DO_NOT_BASE64_ENCODE);
            } catch (SSORestException sre) {
                logger.error("Unable to render error page {}", file);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to render error page");
            }
            return;
        }

        // Retrieve the JSon response
        JSonHttpResponse jsonResponse = gatewayResponse.getResponse();
        logger.debug("Received JSon response {}", jsonResponse);

        // Remember the gateway token
        String token = jsonResponse.getHeader(GATEWAY_TOKEN_NAME); // this is important to save the token
        if (token != null)
            gatewayToken = token;

        // If request is allowed to continue (SC_CONTINUE), then do so
        if (jsonResponse.getStatus() == HttpServletResponse.SC_CONTINUE) {
            if (pc.isEnforcePrincipal() && enforcePrincipal(request, response, gatewayResponse))
                return;
            handleAllowContinue(request, response, gatewayResponse, containerContext);
            return;
        }

        // Handle response SC_NOT_EXTENDED
        if (jsonResponse.getStatus() == WebAgentResponseWrapper.SC_NOT_EXTENDED) {
            String bodyContent = jsonResponse.getBodyContent();

            // Handle a response (SC_NOT_EXTENDED) asking for a signature
            if (bodyContent != null && bodyContent.indexOf(SIGNATURE_NEEDED) >= 0) // for plugin validation only
                handleSignatureRequired(jsonResponse, request, response, contextName, containerContext);

            // Handle a response (SC_NOT_EXTENDED) asking for a local file (i.e. an FCC)
            else
                handleSendLocalFile(request, response, contextName, containerContext);
            return;
        }

        // For all other response codes, send along back to the browser
        response.setStatus(jsonResponse.getStatus());
        if (jsonResponse.getContentType() != null)
            response.setContentType(jsonResponse.getContentType());

        // Transfer response cookies
        for (Cookie c : jsonResponse.getCookies()) {
            logger.debug("Transferring cookie to response {}", c);
            response.addCookie(c);
        }

        // Transfer headers
        for (String name : jsonResponse.getHeaders().keySet()) {
            if (name.equals(GATEWAY_TOKEN_NAME))
                continue;
            for (String value : jsonResponse.getHeaders().get(name)) {
                logger.trace("Transferring header to response {} = {}", name, value);
                response.addHeader(name, value);
            }
        }

        // Transfer content - tliang here is the fix for utf-8 encoding issue, performance wise?
        if (jsonResponse.getBodyContent() != null && jsonResponse.getBodyContent().length() > 0) {
            byte[] decodedBytes = Base64.decodeBase64(jsonResponse.getBodyContent());
            response.getOutputStream().write(decodedBytes);
            logger.debug("Transferring {} bytes to response body", decodedBytes.length);
        }
    }

    /**
     * <p>
     * handleAllowContinue.
     * </p>
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param gatewayResponse
     *            a {@link com.idfconnect.ssorest.api.json.JSonGatewayResponse} object.
     * @param containerContext
     *            a T object.
     * @throws javax.servlet.ServletException
     *             if any.
     * @throws java.io.IOException
     *             if any.
     * @since 1.1.1
     */
    protected final void handleAllowContinue(HttpServletRequest request, HttpServletResponse response, JSonGatewayResponse gatewayResponse, T containerContext) throws ServletException, IOException {
        logger.trace("Entering handleAllowContinue");

        // Create a new request wrapper
        FilterHttpRequestWrapper postgwRequest = new FilterHttpRequestWrapper(request);

        // Transfer request headers
        postgwRequest.setHeaders(gatewayResponse.getRequest().getAllHeaders());

        // Transfer request cookies
        Cookie[] cookies = gatewayResponse.getRequest().getCookies();
        logger.debug("Transferring gateway cookies to request {}", (Object) cookies);
        postgwRequest.setCookies(cookies);

        // Transfer any new cookies to the response
        for (Cookie c : gatewayResponse.getResponse().getCookies()) {
            logger.debug("Transferring gateway cookie to response {}", c);
            response.addCookie(c);
        }

        // Pass request to container
        logger.debug("Request may continue, passing request to container");
        passRequestToContainer(postgwRequest, response, containerContext);
    }

    private Response postJson(Entity<JSonHttpRequest> entity, int retryCount) throws ServletException {
        Response resp = null;
        if (gatewayWebTarget == null)
            gatewayWebTarget = createJaxRSWebTarget(currentGatewayUrl, cm, requestConfig, providers);

        try {
            resp = gatewayWebTarget.request(MediaType.APPLICATION_JSON_TYPE).post(entity);
            int status = resp.getStatus();
            if (status == HttpServletResponse.SC_NOT_FOUND || status == HttpServletResponse.SC_SERVICE_UNAVAILABLE || status == HttpServletResponse.SC_BAD_GATEWAY
                    || status == HttpServletResponse.SC_GATEWAY_TIMEOUT) { // time out or other reasons, retry another url
                throw new SSORestException("Error response from request to gateway (" + status + ")");
            }
        } catch (Exception e) {
            logger.warn("Gateway URL not available {}. Reason is: {}", currentGatewayUrl, e.getMessage());
            if (++retryCount >= pc.getGatewayUrls().length) {
                logger.error("Failover Max retry:{} reached. Exiting...", retryCount);
                throw new ServletException("Failover Max retry reached, exiting...");
            }
            logger.debug("Trying to find next available URL");

            initializeGatewayUrl(true); // reinitialize the gateway url with the next available gateway url
            gatewayWebTarget = createJaxRSWebTarget(currentGatewayUrl, cm, requestConfig, providers);
            return postJson(entity, retryCount);
        }
        return resp;
    }

    /**
     * <p>
     * handleSendLocalFile.
     * </p>
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param containerContext
     *            a T object.
     * @param contextName
     *            a {@link java.lang.String} object.
     * @throws javax.servlet.ServletException
     *             if any.
     * @throws java.io.IOException
     *             if any.
     * @throws java.net.URISyntaxException if any.
     * @since 1.1.1
     */
    protected final void handleSendLocalFile(HttpServletRequest request, HttpServletResponse response, String contextName, T containerContext) throws ServletException, IOException, URISyntaxException {
        logger.debug("Gateway response stated content required, looking for local content to supply");
        if (b64cache == null)
            b64cache = new Base64ContentCache<T>(this);
        Base64Content content = null;
        try {
            content = b64cache.getContentWithoutCache(request.getRequestURI().substring(request.getContextPath().length()), contextName, containerContext);
        } catch (FileNotFoundException fnfe) {
            logger.warn("Gateway requested content for {} but no file was found", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
            return;
        } catch (Exception e) {
            logger.error("Gateway requested content for {} but an exception occurred: {}", request.getRequestURI(), e.toString());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
            return;
        }

        logger.debug("Local content found, resubmitting request to gateway");
        logger.debug("Content: {}", content.getContent());
        request.setAttribute(WebAgentRequestWrapper.CONTENT_REQUEST_ATTRIBUTE, content.getContent());
        request.setAttribute(WebAgentRequestWrapper.CONTENT_TIMESTAMP_REQUEST_ATTRIBUTE, Long.toString(content.getLastModified()));

        // Re-invoke
        processRequest(request, response, contextName, containerContext);
    }

    /**
     * <p>
     * handleSignatureRequired.
     * </p>
     *
     * @param jsonResponse
     *            a {@link com.idfconnect.ssorest.api.json.JSonHttpResponse} object.
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param containerContext
     *            a T object.
     * @param contextName
     *            a {@link java.lang.String} object.
     * @throws javax.servlet.ServletException
     *             if any.
     * @throws java.io.IOException
     *             if any.
     * @since 1.1.1
     */
    protected final void handleSignatureRequired(JSonHttpResponse jsonResponse, HttpServletRequest request, HttpServletResponse response, String contextName, T containerContext)
            throws ServletException, IOException {
        try {
            String challenge = jsonResponse.getHeader(CHALLENGE_HEADER_NAME);
            if (challenge == null) {
                logger.warn("Not able to get challenge from Gateway. No challenge response will be sent!");
                return;
            }
            String challengeSigned = Signature.calculateRFC2104HMAC(challenge, secretKey); // algorithm to sign the randomtext
            logger.trace("Random text generated and to be signed with the secret key");
            request.setAttribute(RANDOMTEXT_ATTR, challenge);
            request.setAttribute(RANDOMTEXT_SIGNED_ATTR, URLEncoder.encode(challengeSigned, StandardCharsets.UTF_8.name())); // remember to URL encode

            // Re-process
            processRequest(request, response, contextName, containerContext);
        } catch (Exception e) {
            logger.error("Exception generating signature", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * <p>
     * handleSsoRestPublicEndpoints.
     * </p>
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @throws org.apache.http.client.ClientProtocolException
     *             if any.
     * @throws java.io.IOException
     *             if any.
     * @throws java.net.URISyntaxException if any.
     * @since 1.1.1
     */
    protected final void handleSsoRestPublicEndpoints(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException, URISyntaxException {
        logger.debug("Processing public endpoint request {}", request.getRequestURI());

        if (request.getMethod().equalsIgnoreCase("post")) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String key = params.nextElement();
                String value = request.getParameter(key);
                if (value != null) {
                    nameValuePairs.add(new BasicNameValuePair(key, value));
                }
            }
            postToEndpoints(request.getRequestURI(), new UrlEncodedFormEntity(nameValuePairs), response);
            return;
        }
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        // Pass local context as a parameter
        HttpGet get = new HttpGet(request.getRequestURI());
        HttpResponse gwResponse = httpclient.execute(get, localContext);

        logger.debug("Committing response here {}", gwResponse);
        // transfer response status
        response.setStatus(gwResponse.getStatusLine().getStatusCode());

        // transfer headers
        for (Header header : gwResponse.getAllHeaders()) {
            // TODO (61) block cookies?
            response.addHeader(header.getName(), header.getValue());
        }

        // transfer response cookies
        for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
            logger.debug("Transferring cookie to response {}", c);
            response.addCookie(ApacheHttpClientUtils.convertApacheCookieToServletCookie(c));
        }

        // transfer content
        HttpEntity entity = gwResponse.getEntity();
        if (entity != null) {
            if (entity.getContentType() != null)
                response.setContentType(entity.getContentType().getValue());
            if (entity.getContent() != null) {
                InputStream is = entity.getContent();
                OutputStream os = response.getOutputStream();
                int next;
                while ((next = is.read()) != -1)
                    os.write(next);
                os.flush();
            }
        }
    }

    /**
     * <p>
     * startup.
     * </p>
     *
     * @param configProps
     *            a {@link com.idfconnect.ssorest.common.utils.ConfigProperties} object.
     * @throws javax.servlet.ServletException
     *             if any.
     * @since 1.1.1
     */
    public void startup(ConfigProperties configProps) throws ServletException {
        pc = new CommonPluginConfiguration();
        logger.trace("Calling startup with ConfigProperties {}", configProps);
        ((CommonPluginConfiguration) pc).initializeFromProperties(configProps);
        startup(pc);
    }

    /**
     * <p>
     * startup.
     * </p>
     *
     * @param config
     *            a {@link java.util.Map} object.
     * @throws javax.servlet.ServletException
     *             if any.
     * @since 1.1.1
     */
    public void startup(Map<String, String> config) throws ServletException {
        pc = new CommonPluginConfiguration();
        logger.trace("Calling startup with Map {}", config);
        ((CommonPluginConfiguration) pc).initialize(config);
        startup(pc);
    }

    /**
     * <p>
     * startup.
     * </p>
     *
     * @param pc
     *            a {@link com.idfconnect.ssorest.plugin.common.PluginConfiguration} object.
     * @throws javax.servlet.ServletException
     *             if any.
     * @since 1.1.1
     */
    public void startup(PluginConfiguration pc) throws ServletException {
        logger.info("Initializing SSO/Rest plugin");
        PluginVersions.logVersions(logger);

        this.pc = pc;

        // Check if we're enabled
        if (!pc.isEnabled()) {
            logger.warn("SSO/Rest plugin is disabled");
            return;
        }

        logger.debug("PluginConfiguration: {}", pc.toString());

        // Retrieve agent config object name, if any
        acoName = pc.getAcoName();
        if (acoName != null)
            logger.info("Agent Config Object defined: {}", acoName);
        else
            logger.info("Agent Config Object not defined, using gateway default agent config");

        // Retrieve plugin ID
        pluginID = pc.getPluginId();
        if (pluginID != null)
            logger.info("Plugin ID: {} ", pluginID);
        else
            logger.warn("Plugin ID is not defined!");

        // Retrieve secret key
        secretKey = pc.getSecretKey();
        if (secretKey == null)
            logger.warn("No secret key defined");

        // decrypt secret value e.g {AES}xxxxxx
        secretKey = PropertyValueEncryptionUtil.getValue(secretKey);

        // retrieve ignore parameter settings
        ignorePattern = new IgnorePattern();
        if (pc.getIgnoreExt() != null) {
            logger.info("IgnoreExt: {}", pc.getIgnoreExt());
            ignorePattern.setIgnoreExt(pc.getIgnoreExt());
        }
        if (pc.getIgnoreHost() != null) {
            logger.info("IgnoreHost: {}", pc.getIgnoreHost());
            ignorePattern.setIgnoreHost(pc.getIgnoreHost());
        }
        if (pc.getIgnoreUrl() != null) {
            logger.info("IgnoreUrl: {}", pc.getIgnoreUrl());
            ignorePattern.setIgnoreUrl(pc.getIgnoreUrl());
        }

        // Randomizer for signature
        random = new SecureRandom();

        // Initialize the Apache HTTP and Jersey clients
        SSLContext sslCtx = startupSSL();
        startupApacheHttpClient(sslCtx);

        // Intialize the gateway URL provider
        if (pc.isConnectionPoolEnable()) {
            // initialize connection pool
            PollingGatewayUrlManager um = new PollingGatewayUrlManager(pc, sslCtx);
            this.urlManager = um;
            um.lauchManagerThread();

            // need to wait for pool manager to be ready
            int retry = 0;
            int retryLimit = 5; // TODO make configurable
            while (retry++ < retryLimit) {
                if (um.isAvailable())
                    break;
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    logger.error("Connection Pool error. {}", e.getMessage());
                }
            }
            if (retry >= retryLimit) { // retry exceeded, not ready
                logger.error("Retry limit exceeded. No Gateway URL is available. Please verify your gateway URLs");
            }
        } else
            urlManager = new SimpleGatewayUrlManager(pc);

        // Finally initialize the gateway URL
        initializeGatewayUrl();
        gatewayWebTarget = createJaxRSWebTarget(currentGatewayUrl, cm, requestConfig, providers);
    }

    /**
     * Start the Apache HTTP Client
     *
     * @param sslCtx
     *            a {@link javax.net.ssl.SSLContext} object.
     * @throws javax.servlet.ServletException
     *             if any.
     * @since 1.1.1
     */
    protected void startupApacheHttpClient(SSLContext sslCtx) throws ServletException {
        HttpHost proxy = null;
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // Create our SSL handler for the gw URL - if necessary
        if (sslCtx != null) {
            logger.info("Creating custom ConnectionSocketFactory for SSL");
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslCtx))
                    .build();
            cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        } else
            cm = new PoolingHttpClientConnectionManager();

        // Configure Pooling Connection Manager
        // TODO (67) implement monitor threads for eviction and keepalive as per https://hc.apache.org/httpcomponents-client-4.5.x/tutorial/html/connmgmt.html
        cm.setDefaultMaxPerRoute(pc.getMaxConnections());
        cm.setMaxTotal(pc.getMaxConnections());

        httpClientBuilder.setConnectionManager(cm);

        // Start to configure RequestConfiguration
        // TODO decide default values
        if (pc.getConnectionTimeout() > -1) {
            logger.info("Setting connection timeout to {} ms", pc.getConnectionTimeout());
            requestConfigBuilder.setConnectTimeout(pc.getConnectionTimeout()) // connection timeout
                    .setConnectionRequestTimeout(pc.getConnectionTimeout()); // timeout waiting for connection from pool
        }
        if (pc.getSocketTimeout() > -1) {
            logger.info("Setting socket read timeout to {} ms", pc.getSocketTimeout());
            requestConfigBuilder.setSocketTimeout(pc.getSocketTimeout());
        }
        // Configure the proxy if specified
        if (pc.isProxyEnable()) {
            try {
                new URL(pc.getProxyScheme() + "://" + pc.getProxyHost() + ":" + pc.getProxyPort());
                proxy = new HttpHost(pc.getProxyHost(), pc.getProxyPort(), pc.getProxyScheme());
            } catch (MalformedURLException me) {
                logger.error("Invalid parameters were provided for the proxy, no proxy will be used: {}", me.toString());
            }
        }
        if (proxy != null) {
            logger.info("Setting proxy URL to {}", proxy);
            requestConfigBuilder.setProxy(proxy);
            // DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            // httpClientBuilder.setRoutePlanner(routePlanner);
        } else
            logger.debug("Not using a proxy");

        // Build the final HTTP client
        requestConfig = requestConfigBuilder.build();
        httpclient = httpClientBuilder.setDefaultRequestConfig(requestConfig).build();
    }

    /**
     * Configure the JAX-RS WebTarget for the gateway endpoint
     *
     * @param hc
     *            a {@link org.apache.http.client.HttpClient} object.
     * @param requestConfig a {@link org.apache.http.client.config.RequestConfig} object.
     * @param providers
     *            an array of {@link java.lang.Class} objects.
     * @param gatewayUri a {@link java.net.URI} object.
     * @return a {@link javax.ws.rs.client.WebTarget} object.
     * @throws javax.servlet.ServletException if any.
     * @since 3.0.3
     */
    protected abstract WebTarget createJaxRSWebTarget(URI gatewayUri, HttpClientConnectionManager hc, RequestConfig requestConfig, Class<?>[] providers) throws ServletException;

    /**
     * @throws ServletException
     */
    private void initializeGatewayUrl() throws ServletException {
        initializeGatewayUrl(false);
    }

    /**
     * @throws ServletException
     *             This will always give you a result, no matter what
     */
    private void initializeGatewayUrl(boolean isNext) throws ServletException {
        // Make sure we have a well formed gateway url
        String gatewayUrl = null;
        if (isNext) {
            gatewayUrl = urlManager.getNextGatewayUrl();
            if (gatewayUrl == null)
                throw new ServletException("getNextGatewayUrl returned null, no gateway endpoints are available");
        } else {
            gatewayUrl = urlManager.getCurrentGatewayUrl();
            if (gatewayUrl == null)
                throw new ServletException("getCurrentGatewayUrl returned null, no gateway endpoint is defined"); // TODO should never happen
        }

        currentGatewayUrl = null;
        try {
            currentGatewayUrl = new URI(gatewayUrl + "/" + PluginConfiguration.SSOREST_SERVICES_URI + "/" + PluginConfiguration.SSOREST_GATEWAY_SERVICES_URI + "/evaluate");
            logger.info("Using agent gateway URL {}", currentGatewayUrl);

            ssoRestPublicUrl = new URI(gatewayUrl + "/" + PluginConfiguration.SSOREST_SERVICES_URI + "/" + PluginConfiguration.SSOREST_PUBLIC_SERVICES_URI + "/");
            logger.info("Using SSORest public services URL {}", ssoRestPublicUrl);
        } catch (URISyntaxException e) {
            throw new ServletException("A malformed gatewayurl parameter was specified: " + gatewayUrl);
        }
    }

    /**
     * <p>
     * startupSSL.
     * </p>
     *
     * @return a {@link javax.net.ssl.SSLContext} object.
     * @since 1.1.1
     */
    protected SSLContext startupSSL() {
        String truststore = pc.getTrustStore();
        String truststorePwd = pc.getTrustStorePwd();
        String truststoreType = pc.getTrustStoreType();
        String keystore = pc.getKeyStore();
        String keystorePwd = pc.getKeyStorePwd();
        String keystoreType = pc.getKeyStoreType();

        try {
            TrustManager[] trustManagers = null;
            KeyManager[] keyManagers = null;
            InputStream is;
            if (truststore != null) {
                is = openLocalFile(truststore);
                if (is != null) {
                    trustManagers = SSLUtil.getTrustManagers(truststoreType, is, truststorePwd.toCharArray());
                    is.close();
                    logger.info("Loaded trust store {}", truststore);
                } else
                    logger.warn("Null input stream loading trust store {}", truststore);
            }
            if ((keystore != null) && (keystorePwd != null)) {
                is = openLocalFile(keystore);
                if (is != null) {
                    keyManagers = SSLUtil.getKeyManagers(keystoreType, is, keystorePwd.toCharArray());
                    is.close();
                    logger.info("Loaded key store {}", keystore);
                } else
                    logger.warn("Null input stream loading key store {}", keystore);
            }

            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(keyManagers, trustManagers, new SecureRandom());
            return sslCtx;
        } catch (GeneralSecurityException gse) {
            logger.error("Exception configuring SSL context, SSL client will be disabled", gse);
        } catch (IOException ioe) {
            logger.error("Exception reading files for SSL context, SSL client will be disabled", ioe);
        }

        return null;
    }

    /**
     * Reads a file from the file system
     *
     * @param filename
     *            a {@link java.lang.String} object.
     * @return an InputStream to the file
     * @throws java.io.IOException
     *             if any.
     * @since 1.1.1
     */
    protected abstract InputStream openLocalFile(String filename) throws IOException;

    /**
     * Passes a request that is allowed to continue back to the underlying container
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param containerContext
     *            an object such as a FilterChain that the container uses for request processing
     * @throws javax.servlet.ServletException
     *             if any.
     * @throws java.io.IOException
     *             if any.
     * @since 1.1.1
     */
    protected abstract void passRequestToContainer(HttpServletRequest request, HttpServletResponse response, T containerContext) throws ServletException, IOException;

    /**
     * Returns the active plugin configuration
     *
     * @return a {@link com.idfconnect.ssorest.plugin.common.PluginConfiguration} object.
     * @since 3.0.1
     */
    public PluginConfiguration getPluginConfiguration() {
        // TODO make this immutable
        return pc;
    }

    /**
     * <p>
     * postToEndpoints.
     * </p>
     *
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param targetUri
     *            a {@link java.net.URI} object.
     * @param entity
     *            a {@link org.apache.http.HttpEntity} object.
     * @throws org.apache.http.client.ClientProtocolException
     *             if any.
     * @throws java.io.IOException
     *             if any.
     * @throws java.net.URISyntaxException if any.
     * @since 1.2.9
     */
    public final void postToEndpoints(String targetUri, HttpEntity entity, HttpServletResponse response) throws ClientProtocolException, IOException, URISyntaxException {
        logger.debug("Processing endpoint request {}", targetUri);

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        URI target = new URI(ssoRestPublicUrl + targetUri.substring(ssoRestPublicUrl.getPath().length()));
        
        HttpResponse gwResponse = null;
        HttpPost post = new HttpPost(target);
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        post.setEntity(entity);
        gwResponse = httpclient.execute(post, localContext);

        logger.debug("Committing response here {}", gwResponse);
        // transfer response status
        response.setStatus(gwResponse.getStatusLine().getStatusCode());

        // transfer headers
        for (Header header : gwResponse.getAllHeaders()) {
            // TODO (61) block cookies?
            response.addHeader(header.getName(), header.getValue());
        }

        // transfer response cookies
        for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
            logger.debug("Transferring cookie to response {}", c);
            response.addCookie(ApacheHttpClientUtils.convertApacheCookieToServletCookie(c));
        }

        // transfer content
        HttpEntity resEntity = gwResponse.getEntity();
        if (resEntity != null) {
            if (resEntity.getContentType() != null)
                response.setContentType(resEntity.getContentType().getValue());
            if (resEntity.getContent() != null) {
                InputStream is = resEntity.getContent();
                OutputStream os = response.getOutputStream();
                int next;
                while ((next = is.read()) != -1)
                    os.write(next);
                os.flush();
            }
        }
    }

    /**
     * <p>
     * Getter for the field <code>currentGatewayUrl</code>.
     * </p>
     *
     * @return a {@link java.net.URL} object.
     */
    protected final URI getCurrentGatewayUrl() {
        return currentGatewayUrl;
    }
}
