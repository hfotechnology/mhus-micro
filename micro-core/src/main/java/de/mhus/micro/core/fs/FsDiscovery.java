package de.mhus.micro.core.fs;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.IConfigFactory;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.util.AbstractDiscovery;

public class FsDiscovery extends AbstractDiscovery {

    private volatile Map<String,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());
    private volatile Map<String,Long> modifyDates = Collections.synchronizedMap(new HashMap<>());
	private File dir;

	public FsDiscovery(File dir) {
		this.dir = dir;
		reload();
	}
	
	@Override
	public void check() {
		
		Set<String> done = new HashSet<>(modifyDates.keySet());
		for (File file : dir.listFiles())
			if (file.isFile()) {
				done.remove(file.getName());
				if (modifyDates.containsKey(file.getName())) {
					if (file.lastModified() != modifyDates.get(file.getName()))
						load(file);
				} else {
					load(file);
				}
			}
		
		done.forEach( v -> {
			modifyDates.remove(v);
			descriptions.remove(v);
		});
		
	}

	@Override
	public synchronized void reload() {
		descriptions.forEach((k,v) -> ((MProperties)v.getLabels()).put(C.LABEL_DEPRECATED, "true") );
		for (File file : dir.listFiles())
			if (file.isFile())
				load(file);
		descriptions.values().removeIf(v -> v.getLabels().containsKey(C.LABEL_DEPRECATED));
	}

	private void load(File file) {
		IConfigFactory factory = M.l(IConfigFactory.class);
		try {
			IConfig entry = factory.read(file);
			
			UUID uuid = UUID.fromString( entry.getStringOrCreate(C.DATA_UUID, x -> UUID.randomUUID().toString()) );
			String path = entry.getString(C.DATA_PATH);
			String title = entry.getString(C.DATA_TITLE, path);
			Version version = new Version( entry.getStringOrCreate(C.DATA_VERSION, x -> Version.V_0_0_0.toString()));
			
			DefRoot form = null;
			String formStr = entry.getString(C.DATA_FORM, null);
			if (formStr != null) {
				form = ModelUtil.fromJson(formStr);
			}
			
			MProperties labels = null;
			for (Entry<String, Object> label : entry.getObject(C.DATA_LABELS).entrySet()) {
				if (labels == null) labels = new MProperties();
				labels.put(label.getKey(), String.valueOf( label.getValue() ));
			}
						
			OperationDescription desc = new OperationDescription(uuid, path, version, null, title, labels, form );
			
			descriptions.put(file.getName(),desc);
			
		} catch (Exception e) {
			log().e(file,e);
		}
		modifyDates.put(file.getName(), file.lastModified());
	}

	@Override
	public void discover( Consumer<OperationDescription> action) {
		descriptions.values().forEach(d -> action.accept(d));
	}

}
