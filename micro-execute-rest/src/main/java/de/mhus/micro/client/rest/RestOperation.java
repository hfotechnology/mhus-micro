package de.mhus.micro.client.rest;

import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.JsonConfigBuilder;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.io.http.MHttpClientBuilder;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.client.MicroOperation;

public class RestOperation extends MLog  implements MicroOperation {

    private OperationDescription description;
    private HttpClient client;
    private String uri;
    private Object method;
    

    public RestOperation(OperationDescription description, HttpClient client) {
        this.description = description;
        this.client = client;
        this.uri = description.getLabels().get(MicroConst.REST_URL);
        this.method = description.getLabels().getOrDefault(MicroConst.REST_METHOD, MHttp.METHOD_POST);
    }

    @Override
    public IConfig execute(IConfig arguments, IProperties properties) {
        HttpResponse res = null;
        try {
            if (uri.contains("{"))
                uri = StringCompiler.compile(uri).execute(arguments);
            // TODO different methods - GET POST PUT DELETE
            if (MHttp.METHOD_POST.equals(method)) {
                HttpPost post = new HttpPost(uri);
            
                // TODO different transport types - xml, form properties - pluggable?
                String argJson = IConfig.toPrettyJsonString(arguments);
                post.setEntity(new StringEntity(argJson, MHttp.CONTENT_TYPE_JSON));
                res = client.execute(post);
            } else
            if (MHttp.METHOD_PUT.equals(method)) {
                HttpPut post = new HttpPut(uri);
            
                // TODO different transport types - xml, form properties - pluggable?
                String argJson = IConfig.toPrettyJsonString(arguments);
                post.setEntity(new StringEntity(argJson, MHttp.CONTENT_TYPE_JSON));
                res = client.execute(post);
            } 
//            else
//            if (MHttp.METHOD_DELETE.equals(method)) {
//                HttpDelete post = new HttpDelete(uri);
//            
//                // TODO different transport types - xml, form properties - pluggable?
//                String argJson = IConfig.toPrettyJsonString(arguments);
//                post.setEntity(new StringEntity(argJson, MHttp.CONTENT_TYPE_JSON));
//                res = client.execute(post);
//            } else
//            if (MHttp.METHOD_GET.equals(method)) {
//                HttpGet post = new HttpGet(uri);
//
//                // TODO different transport types - xml, form properties - pluggable?
//                String argJson = IConfig.toPrettyJsonString(arguments);
//                tEntity(new StringEntity(argJson, MHttp.CONTENT_TYPE_JSON));
//                res = client.execute(post);
//            }

            if (res == null)
                return null;

            HttpEntity entry = res.getEntity();
            Header type = entry.getContentType();
            // TODO different return formats - xml, plain, stream - pluggable?
            if (MHttp.CONTENT_TYPE_JSON.equals(type.getValue())) {
                InputStream is = entry.getContent();
                IConfig resJson = new JsonConfigBuilder().read(is);
                return resJson;
            }
            if (MHttp.CONTENT_TYPE_TEXT.equals(type.getValue())) {
                InputStream is = entry.getContent();
                IConfig resCfg = new MConfig();
                String resText = MFile.readFile(is);
                resCfg.setString(IConfig.NAMELESS_VALUE, resText);
                return resCfg;
            }
            
        } catch (Exception e) {
            log().e(e);
        } finally {
            MHttpClientBuilder.close(res); // no NPE
        }
        return null;
    }

    @Override
    public OperationDescription getDescription() {
        return description;
    }

}
