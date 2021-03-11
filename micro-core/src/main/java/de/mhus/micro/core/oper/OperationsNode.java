
package de.mhus.micro.core.oper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.VoidNode;
import de.mhus.rest.core.result.JsonResult;

@RestNode(name = "operation", parent = Node.PUBLIC_NODE_NAME)
@ServiceComponent(service = RestNodeService.class)
public class OperationsNode extends VoidNode {

    private CfgString CFG_HOST = new CfgString(OperationsNode.class, "host", MSystem.getHostname());
    private CfgInt CFG_PORT = new CfgInt(OperationsNode.class, "port", 8181);
    private CfgString CFG_URL = new CfgString(OperationsNode.class, "url", "http://${host}:${port}/rest/public/operation");
    private Map<UUID,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());
    
    @ServiceActivate
    public void doActivate() {

    }

    @Override
    public Node lookup(List<String> parts, CallContext callContext) throws Exception {

        if (parts.size() < 1) return this;
        return callContext.lookup(parts, getClass());
    }

    @Override
    public void doRead(JsonResult result, CallContext callContext) throws Exception {}

}
