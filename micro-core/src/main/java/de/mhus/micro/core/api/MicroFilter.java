package de.mhus.micro.core.api;

import de.mhus.lib.core.operation.OperationDescription;

public interface MicroFilter {

    public static final MicroFilter ALL = new MicroFilter() {
        @Override
        public boolean matches(OperationDescription desc) {
            return true;
        }
    };

     boolean matches(OperationDescription desc);

}
