package de.mhus.micro.core.api;

import de.mhus.lib.core.M;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.impl.services.MicroApiServiceProvider;

public class Micro {
    public static final String DATA_UUID = "uuid";
    public static final String DATA_PATH = "path";
    public static final String DATA_VERSION = "version";
    public static final String DATA_TITLE = "title";
    public static final String DATA_LABEL_DOT = "label.";
    public static final String DATA_LABELS = "labels";
    public static final String DATA_FORM = "form";

    public static final String LABEL_PROTO = "@proto";
//	public static final String LABEL_DISCOVER_SOURCE = "_source";

    public static final String LABEL_DEPRECATED = "_deprecated";
    public static final String LABEL_NEW = "_new";
	public static final String LABEL_LOCAL_PREFIX = "_";
	public static final String LABEL_LOCAL = "@local";
	public static final String REST_URL = "@url";
	public static final String REST_METHOD = "@method";
	public static final String PROTO_REST = "rest";
	public static final String REST_HOST = "@host";
	
	public static String getUniqueId(OperationDescription desc) {
		return desc.getPath() + "-" + desc.getUuid() + "-" + desc.getLabels().getString(Micro.LABEL_PROTO, "");
	}

	public MicroApi get() {
		return M.l(MicroApiServiceProvider.class).getApi();
	}
	
}

