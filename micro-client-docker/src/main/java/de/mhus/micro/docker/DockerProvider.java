package de.mhus.micro.docker;

import java.util.List;

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.api.MicroClientProvider;

@Component
public class DockerProvider extends MLog implements MicroClientProvider {

    @Override
    public IConfig execute(String path, IConfig arguments) {

        return null;
    }

    @Override
    public void list(String filter, List<OperationDescription> results) {
        
    }

}
