package de.mhus.micro.client.rest;

import java.util.List;

import org.apache.http.client.HttpClient;

import de.mhus.lib.core.io.http.MHttpClientBuilder;
import de.mhus.micro.api.client.MicroClientExecutor;
import de.mhus.micro.api.client.MicroFilter;
import de.mhus.micro.api.client.MicroOperation;

public class MicroClientRest implements MicroClientExecutor {

    HttpClient client = new MHttpClientBuilder().getHttpClient();

    @Override
    public void list(MicroFilter filter, List<MicroOperation> results) {
        
    }


}
