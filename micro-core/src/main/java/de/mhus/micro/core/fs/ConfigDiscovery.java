package de.mhus.micro.core.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;

import de.mhus.lib.core.M;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.IConfigFactory;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.MException;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.MicroFilter;
import de.mhus.micro.core.util.AbstractDiscovery;

public class ConfigDiscovery extends AbstractDiscovery {

	private ArrayList<OperationDescription> list = new ArrayList<>();
	private File file;
	private long modifyDate;
	
	public ConfigDiscovery(IConfig config) {
		reload(config);
	}
	
	public ConfigDiscovery(File file) throws MException {
		this.file = file;
		reload(file);
	}
	
	@Override
	public void check() {
		if (modifyDate > 0 && file != null) {
			if (file.lastModified() != modifyDate)
				reload();
		}
	}

	@Override
	public void reload() {
		modifyDate = 0;
		if (file != null) {
			try {
				reload(file);
			} catch (MException e) {
				log().e(file,e);
			}
		}
	}

	private void reload(File file) throws MException {
		IConfig config = M.l(IConfigFactory.class).read(file);
		reload(config);
		modifyDate = file.lastModified();
	}

	public synchronized void reload(IConfig config) {
		ArrayList<OperationDescription> l = new ArrayList<>();
		for (IConfig entry : config.getArrayOrCreate(IConfig.NAMELESS_VALUE)) {
			try {
				UUID uuid = UUID.fromString( entry.getStringOrCreate("uuid", x -> UUID.randomUUID().toString()) );
				String path = entry.getString("path");
				String title = entry.getString("title", path);
				Version version = new Version( entry.getStringOrCreate("version", x -> Version.V_0_0_0.toString()));
				
				OperationDescription desc = new OperationDescription(uuid, path, version, null, title);
				
				String formStr = entry.getString("form", null);
				if (formStr != null) {
					DefRoot form = ModelUtil.fromJson(formStr);
					desc.setForm(form);
				}
				for (Entry<String, Object> label : entry.getObject("labels").entrySet()) {
					desc.getLabels().put(label.getKey(), String.valueOf( label.getValue() ));
				}
				
				l.add(desc);
				
			} catch (Throwable t) {
				log().e(entry,t);
			}
			
		}
		
		list = l;
		
	}
	
	@Override
	public void discover(MicroFilter filter, Consumer<OperationDescription> action) {
		list.forEach( v -> action.accept(v) );
	}

}
