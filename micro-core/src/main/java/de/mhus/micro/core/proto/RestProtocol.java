package de.mhus.micro.core.proto;

import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.JsonConfigBuilder;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.io.http.MHttpClientBuilder;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.impl.AbstractProtocol;

public class RestProtocol extends AbstractProtocol {

	private static final String[] PROTOCOLS = new String[] {C.PROTO_REST,"http","https"};
	private HttpClient client = new MHttpClientBuilder().getHttpClient();
	
	@Override
	public MicroResult execute(OperationDescription desc, IConfig arguments, IReadProperties properties) {

		try {
	        String uri = desc.getLabels().getString(C.REST_URL);
	        String host = desc.getLabels().getString(C.REST_HOST, null);
	        String method = desc.getLabels().getString(C.REST_METHOD, MHttp.METHOD_POST);
	        
	        if (host != null) {
	        	host = hostMapping(host);
	        	arguments.setString("host", host);
	        }
            if (uri.contains("{"))
                uri = StringCompiler.compile(uri).execute(arguments);

            Subject subject = Aaa.getSubject();
            
            HttpResponse res = null;
            if (MHttp.METHOD_POST.equals(method)) {
                HttpPost post = new HttpPost(uri);
                if (subject.isAuthenticated()) {
                    String jwt = Aaa.createBearerToken(subject, null);
                    if (jwt != null)
                        post.addHeader("Authorization", "Bearer " + jwt);
                }
                // TODO different transport types - xml, form properties - pluggable?
                String argJson = IConfig.toPrettyJsonString(arguments);
                post.setEntity(new StringEntity(argJson, MString.CHARSET_UTF_8));
                res = client.execute(post);
            } else
            if (MHttp.METHOD_PUT.equals(method)) {
                HttpPut post = new HttpPut(uri);
                if (subject.isAuthenticated()) {
                    String jwt = Aaa.createBearerToken(subject, null);
                    if (jwt != null)
                        post.addHeader("Authorization", "Bearer " + jwt);
                }
                // TODO different transport types - xml, form properties - pluggable?
                String argJson = IConfig.toPrettyJsonString(arguments);
                post.setEntity(new StringEntity(argJson, MString.CHARSET_UTF_8));
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
                return new MicroResult(false, -500, "no result", desc, null, null);

            boolean successful = res.getStatusLine().getStatusCode() == 200;
            int rc = res.getStatusLine().getStatusCode() == 200 ? 0 : -res.getStatusLine().getStatusCode();
            String msg = null;
            MProperties config = new MProperties();
            
            for (Header header : res.getAllHeaders()) {
            	switch (header.getName()) {
            	case "successful":
            		successful = MCast.toboolean(header.getValue(), successful);
            		break;
            	case "rc":
            		rc = MCast.toint(header.getValue(), rc);
            		break;
            	case "msg":
            		msg = header.getValue();
            		break;
        		default:
            	}
            	config.setString(header.getName(), header.getValue());
            }

            IConfig resC = null;
            
            HttpEntity entry = res.getEntity();
            Header type = entry.getContentType();
            // TODO different return formats - xml, plain, stream - pluggable?
            if (type.getValue().startsWith(MHttp.CONTENT_TYPE_JSON)) {
                InputStream is = entry.getContent();
                resC = new JsonConfigBuilder().read(is);
            } else
            if (MHttp.CONTENT_TYPE_TEXT.equals(type.getValue())) {
                InputStream is = entry.getContent();
                String resText = MFile.readFile(is);
                resC = new MConfig();
                resC.setString(IConfig.NAMELESS_VALUE, resText);
            }

            return new MicroResult(successful, rc, msg, desc, resC, config);

		} catch (Throwable t) {
			return new MicroResult(desc, t);
		}
	}

	protected String hostMapping(String host) {
		// TODO implement host mapping
		return host;
	}

	@Override
	public String[] getNames() {
		return PROTOCOLS;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}
}
