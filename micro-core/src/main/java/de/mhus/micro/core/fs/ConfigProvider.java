package de.mhus.micro.core.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.core.node.INodeFactory;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.MException;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.Micro;
import de.mhus.micro.core.impl.AbstractProvider;

public class ConfigProvider extends AbstractProvider {

	private ArrayList<OperationDescription> list = new ArrayList<>();
	private File file;
	private long modifyDate;
	
	public ConfigProvider(INode config) {
		reload(config);
	}
	
	public ConfigProvider(File file) throws MException {
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
		INode config = M.l(INodeFactory.class).read(file);
		reload(config);
		modifyDate = file.lastModified();
	}

	public synchronized void reload(INode config) {
		ArrayList<OperationDescription> l = new ArrayList<>();
		Set<UUID> uuids = new HashSet<>();
		
		for (INode entry : config.getArrayOrCreate(INode.NAMELESS_VALUE)) {
			try {
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
				for (Entry<String, Object> label : entry.getObject(Micro.DATA_LABELS).entrySet()) {
					if (labels == null) labels = new MProperties();
					labels.put(label.getKey(), String.valueOf( label.getValue() ));
				}
				
				OperationDescription desc = new OperationDescription(uuid, path, version, null, title, labels, form);
								
				l.add(desc);
				uuids.add(desc.getUuid());
				
				if (api != null)
					api.updateDescription(desc);
			} catch (Throwable t) {
				log().e(entry,t);
			}
			
		}
		
		if (api != null)
			list.forEach(desc -> {
				if (!uuids.contains(desc.getUuid()))
					api.removeDescription(desc);
			});
		
		list = l;
		
	}
	
	@Override
	public Boolean discover(Function<OperationDescription,Boolean> action) {
		for ( OperationDescription desc : list)
			if (!action.apply(desc) )
				return Boolean.FALSE;
		return Boolean.TRUE;
	}

}
