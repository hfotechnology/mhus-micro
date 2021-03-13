package de.mhus.micro.core.fs;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.Micro;
import de.mhus.micro.core.impl.AbstractPublisher;

public class FsPublisher extends AbstractPublisher {

	private File dir;

	public FsPublisher(File dir) {
		this.dir = dir;
		if (!dir.exists()) dir.mkdirs();
	}
	
	@Override
	public void push(OperationDescription desc) {
		MProperties prop = new MProperties();
		prop.setString(Micro.DATA_UUID, desc.getUuid().toString());
		prop.setString(Micro.DATA_PATH, desc.getPath());
		prop.setString(Micro.DATA_VERSION, desc.getVersionString());
		prop.setString(Micro.DATA_TITLE, desc.getTitle());
		for (Entry<String, Object> label : desc.getLabels())
			if (!label.getKey().startsWith(Micro.LABEL_LOCAL_PREFIX))
				prop.setString(Micro.DATA_LABEL_DOT, MCast.toString( label.getValue() ));
		DefRoot form = desc.getForm();
		if (form != null) {
			try {
				String formStr = MJson.toString( ModelUtil.toJson(form) );
				prop.setString(Micro.DATA_FORM, formStr);
			} catch (Throwable e) {
				log().e(desc,e);
			}
		}
		
		String name = MFile.normalize( Micro.getUniqueId(desc) ) + ".properties";
		
		try {
			prop.save(new File(dir, name));
		} catch (IOException e) {
			log().e(desc,e);
		}
	}

	@Override
	public void remove(OperationDescription desc) {
		String name = MFile.normalize( Micro.getUniqueId(desc) ) + ".properties";
		
		if (!new File(dir, name).delete())
			log().e("@Can't delete file $1",name);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}
