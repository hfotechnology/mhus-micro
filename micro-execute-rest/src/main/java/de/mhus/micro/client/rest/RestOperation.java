package de.mhus.micro.client.rest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.client.MicroOperation;

public class RestOperation extends MLog  implements MicroOperation {

    private OperationDescription description;
    private HttpClient client;
    private String uri;
    

    public RestOperation(OperationDescription description, HttpClient client, String uri) {
        this.description = description;
        this.client = client;
        this.uri = uri;
    }

    @Override
    public IConfig execute(IConfig arguments) {
        HttpPost post = new HttpPost(uri);
        try {
            post.setEntity(new StringEntity("test post"));
            HttpResponse res = client.execute(post);
            HttpEntity entry = res.getEntity();
            InputStream is = entry.getContent();
            
        } catch (IOException e) {

        }
        return null;
    }

    @Override
    public OperationDescription getDescription() {
        return description;
    }

}
