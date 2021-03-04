package de.mhus.micro.api.client;

import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.client.MicroFilter;

public class TransportFilter implements MicroFilter {

    private String transport;

    public TransportFilter(String transport) {
        this.transport = transport;
    }
    
    @Override
    public boolean matches(OperationDescription desc) {
        return transport.equals(desc.getLabels().get(MicroConst.DESC_LABEL_TRANSPORT_TYPE));
    }

}
