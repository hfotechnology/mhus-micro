package de.mhus.micro.api.client;

import java.util.List;

public interface MicroClientExecutor {
    
    void list(MicroFilter filter, List<MicroOperation> results);

}
