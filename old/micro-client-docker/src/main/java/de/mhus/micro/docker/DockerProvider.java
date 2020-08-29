package de.mhus.micro.docker;

import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroClientProvider;

// https://github.com/spotify/docker-client

@Component(immediate = true)
public class DockerProvider extends MLog implements MicroClientProvider {

    private static DockerProvider instance;

    public static DockerProvider instance() {
        return instance;
    }

    
    @Activate
    public void doActivate(BundleContext ctx) {
        log().i("doActivate");
        instance = this;
    }

    @Deactivate
    public void doDeactivate() {
        instance = null;
    }
    
    @Override
    public IConfig execute(String path, IConfig arguments) {
        return null;
    }

    @Override
    public void list(String filter, List<OperationDescription> results) {
        
    }

}
