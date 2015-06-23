package org.apache.sling.contrib.pipes;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * Pipe outputing binding related to a json stream
 */
public class JsonPipe extends BasePipe {
    private static Logger logger = LoggerFactory.getLogger(JsonPipe.class);
    public static final String RESOURCE_TYPE = "slingPipes/json";

    HttpClient client;

    public JsonPipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
        configureHttpClient();
    }

    /**
     * Configure http client
     */
    private void configureHttpClient(){
        HttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        manager.setParams(params);
        client = new HttpClient(manager);
        client.getParams().setAuthenticationPreemptive(false);
    }

    @Override
    public Object getOutputBinding() {
        GetMethod method = null;
        HttpState httpState = new HttpState();
        InputStream responseInputStream = null;
        try {
            String url = getExpr();
            method = new GetMethod(url);
            logger.debug("Executing GET {}", url);
            int status = client.executeMethod(null,method,httpState);
            if (status == HttpStatus.SC_OK){
                logger.debug("200 received, streaming content");
                responseInputStream = method.getResponseBodyAsStream();
                StringWriter writer = new StringWriter();
                IOUtils.copy(responseInputStream, writer, "utf-8");
                String jsonString = writer.toString();
                JSONTokener tokener = new JSONTokener(jsonString);
                if (tokener.next() == '['){
                    return new JSONArray(jsonString);
                } else {
                    return new JSONObject(jsonString);
                }
            }
        }
        catch(Exception e) {
            logger.error("unable to retrieve the data");
        } finally {
            if (method != null){
                method.releaseConnection();
            }
            IOUtils.closeQuietly(responseInputStream);
        }
        return null;
    }
}
