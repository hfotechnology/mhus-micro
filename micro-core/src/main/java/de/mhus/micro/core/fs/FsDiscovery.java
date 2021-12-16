package de.mhus.micro.core.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.INodeFactory;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.Micro;
import de.mhus.micro.core.impl.AbstractDiscovery;

public class FsDiscovery extends AbstractDiscovery {

    private volatile Map<String,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());
    private volatile List<OperationDescription> values = Collections.synchronizedList(new ArrayList<>());
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
			OperationDescription x = descriptions.remove(v);
			if (x != null)
			    values.remove(x);
		});
		
	}

	@Override
	public synchronized void reload() {
		descriptions.forEach((k,v) -> ((MProperties)v.getLabels()).put(Micro.LABEL_DEPRECATED, "true") );
		for (File file : dir.listFiles())
			if (file.isFile())
				load(file);
		descriptions.values().removeIf(v -> v.getLabels().containsKey(Micro.LABEL_DEPRECATED));
		values.removeIf(v -> v.getLabels().containsKey(Micro.LABEL_DEPRECATED));
		orderValues();
	}

	private void orderValues() {
        values.sort(Micro.DESCRIPTION_COMPERATOR);
    }

    private void load(File file) {
		INodeFactory factory = M.l(INodeFactory.class);
		try {
			INode entry = factory.read(file);
			
			UUID uuid = UUID.fromString( entry.getStringOrCreate(Micro.DATA_UUID, x -> UUID.randomUUID().toString()) );
			String path = entry.getString(Micro.DATA_PATH);
			String title = entry.getString(Micro.DATA_TITLE, path);
			Version version = new Version( entry.getStringOrCreate(Micro.DATA_VERSION, x -> Version.V_0_0_0.toString()));
			
			DefRoot form = null;
			String formStr = entry.getString(Micro.DATA_FORM, null);
			if (formStr != null) {
				form = ModelUtil.fromJson(formStr);
			}
			
			MProperties labels = null;
			
			// as sub config
			if (entry.isObject(Micro.DATA_LABELS))
				for (Entry<String, Object> label : entry.getObject(Micro.DATA_LABELS).entrySet()) {
					if (labels == null) labels = new MProperties();
					labels.put(label.getKey(), String.valueOf( label.getValue() ));
				}
				
			// or as parameters
			for (Entry<String, Object> label : entry.entrySet()) {
				if (label.getKey().startsWith(Micro.DATA_LABEL_DOT)) {
					labels.put(label.getKey().substring(Micro.DATA_LABEL_DOT.length()), label.getValue());
				}
			}
			OperationDescription desc = new OperationDescription(uuid, path, version, null, title, labels, form );
			
			descriptions.put(file.getName(),desc);
			values.add(desc);
			
		} catch (Exception e) {
			log().e(file,e);
		}
		modifyDates.put(file.getName(), file.lastModified());
	}

	@Override
	public Boolean discover(Function<OperationDescription,Boolean> action) {
		for ( OperationDescription desc : descriptions.values())
			if (!action.apply(desc) )
				return Boolean.FALSE;
		return Boolean.TRUE;
	}

}
