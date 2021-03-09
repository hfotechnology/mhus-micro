package de.mhus.micro.core.proto;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.io.http.MHttpClientBuilder;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.util.AbstractProtocol;

public class RestProtocol extends AbstractProtocol {

	private static final String[] PROTOCOLS = new String[] {"rest","http","https"};
	private HttpClient client = new MHttpClientBuilder().getHttpClient();
	
	@Override
	public MicroResult execute(OperationDescription desc, IConfig arguments, IProperties properties) {

		try {
	        String uri = desc.getLabels().getString(C.REST_URL);
	        String method = desc.getLabels().getString(C.REST_METHOD, MHttp.METHOD_POST);
	        
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
                return new MicroResult(false, -500, "no result", desc, null);

			return null;
		} catch (Throwable t) {
			return new MicroResult(desc, t);
		}
	}

	@Override
	public String[] getNames() {
		return PROTOCOLS;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}
}
