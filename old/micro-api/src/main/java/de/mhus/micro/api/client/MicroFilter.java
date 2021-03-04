package de.mhus.micro.api.client;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroFilter {

    public static final MicroFilter GET_ALL = new MicroFilter() {
        @Override
        public boolean matches(OperationDescription desc) {
            return true;
        }
    };

     boolean matches(OperationDescription desc);

}
