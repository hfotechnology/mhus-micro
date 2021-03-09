package de.mhus.micro.core.filter;

import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.micro.core.api.MicroFilter;

public class FilterPathVersion implements MicroFilter {

	private String path;
	private VersionRange version;

	public FilterPathVersion(String pathVersion) {
		int pos = pathVersion.indexOf(':');
		if (pos < 0) {
			path = pathVersion;
			version = null;
		} else {
			path = pathVersion.substring(0, pos);
			version = new VersionRange(pathVersion.substring(pos+1));
		}
	}
	
	public FilterPathVersion(String path, String versionRange) {
		this.path = path;
		if (versionRange !=null)
			version = new VersionRange(versionRange);
	}

	public FilterPathVersion(String path, VersionRange versionRange) {
		this.path = path;
		version = versionRange;
	}

	@Override
	public boolean matches(OperationDescription desc) {
		if (path.equals(desc.getPath()) && (version == null || version.includes(desc.getVersion())) )
				return true;
		return false;
	}

}
