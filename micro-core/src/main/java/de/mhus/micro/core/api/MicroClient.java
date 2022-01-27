package de.mhus.micro.core.api;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;

public class MicroClient extends MLog {

    protected MProperties defaultProperties;

    public MicroClient(String ... defaultProperties) {
        if (defaultProperties != null) {
            this.defaultProperties = IProperties.to(defaultProperties);
        }
    }

    protected MicroApi api() {
        return M.l(MicroApi.class);
    }

}
