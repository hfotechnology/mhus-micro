package de.mhus.micro.osgi;

import java.io.File;

import de.mhus.micro.core.api.MicroDiscovery;
import de.mhus.micro.core.fs.FsDiscovery;
import de.mhus.micro.core.impl.services.MicroDiscoveryServiceProvider;

public class FsDiscoveryService implements MicroDiscoveryServiceProvider {

	private FsDiscovery service;
	private File dir;

	@Override
	public synchronized MicroDiscovery getService() {
		if (service == null) {
			service = new FsDiscovery(dir);
		}
		return service;
	}

	public File getDir() {
		return dir;
	}

	public void setDir(File dir) {
		this.dir = dir;
	}
	
	public void setDirPath(String path) {
		dir = new File(path);
	}

}
