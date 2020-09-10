package de.mhus.micro.client.rest;

import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.client.MicroFilter;

public class FilterTransport implements MicroFilter {

    private String transport;

    public FilterTransport(String transport) {
        this.transport = transport;
    }
    
    @Override
    public boolean matches(OperationDescription desc) {
        return transport.equals(desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE));
    }

}
