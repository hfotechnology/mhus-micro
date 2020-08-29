package de.mhus.micro.api.client;

import java.util.List;

import de.mhus.lib.core.config.IConfig;

public interface MicroApi {

    MicroResult execute(MicroFilter filter, IConfig arguments);
    
    void list(MicroFilter filter, List<MicroOperation> results);

    List<MicroResult> executeAll(MicroFilter filter, IConfig arguments);
    
    void discover(MicroFilter filter, List<MicroOperation> results);
    
}
