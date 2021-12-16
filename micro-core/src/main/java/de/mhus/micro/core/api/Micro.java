package de.mhus.micro.core.api;

import java.util.Comparator;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.fs.FsDiscovery;
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
    public static final String NEED_ANSWER = "needAnswer";
	
	public static String getUniqueId(OperationDescription desc) {
		return desc.getPath() + "-" + desc.getUuid() + "-" + desc.getLabels().getString(Micro.LABEL_PROTO, "");
	}

	public MicroApi get() {
		return M.l(MicroApiServiceProvider.class).getApi();
	}

    private static String[] CFG_PROTO_ORDER_ARRAY = null;
    @SuppressWarnings("unused")
    private static CfgString CFG_PROTO_ORDER = new CfgString(MicroApi.class, "protoOrder", "local,rest,jms").updateAction(v -> CFG_PROTO_ORDER_ARRAY = v.split(",") );
    public static final Comparator<OperationDescription> DESCRIPTION_COMPERATOR = new Comparator<OperationDescription>() {
        
        @Override
        public int compare(OperationDescription o1, OperationDescription o2) {
            if (CFG_PROTO_ORDER_ARRAY != null) {
                String proto1 = o1.getLabels().getString(Micro.LABEL_PROTO, "");
                String proto2 = o2.getLabels().getString(Micro.LABEL_PROTO, "");
                int index1 = MCollection.indexOf(CFG_PROTO_ORDER_ARRAY, proto1);
                int index2 = MCollection.indexOf(CFG_PROTO_ORDER_ARRAY, proto2);
                if (index1 == -1 && index2 == -1) return 0;
                if (index1 == -1) return 1;
                if (index2 == -1) return -1;
                return Integer.compare(index1, index2);
            }
            return 0;
        }
    };

}

