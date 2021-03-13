package de.mhus.micro.osgi;

import java.io.File;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.MLog;
import de.mhus.micro.core.api.MicroProvider;
import de.mhus.micro.core.fs.ConfigProvider;
import de.mhus.micro.core.impl.services.MicroProviderServiceProvider;

@ServiceComponent(service = MicroProviderServiceProvider.class)
public class ConfigProviderService extends MLog implements MicroProviderServiceProvider {

	private ConfigProvider service;
	private File file;

	@Override
	public synchronized MicroProvider getService() {
		if (service == null) {
			try {
				service = new ConfigProvider(file);
			} catch (Exception e) {
				log().e(e);
			}
		}
		return service;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setFilePath(String path) {
		this.file = new File(path);
	}
	
}
