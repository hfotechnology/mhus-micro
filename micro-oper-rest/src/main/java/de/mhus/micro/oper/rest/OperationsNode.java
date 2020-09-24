
package de.mhus.micro.oper.rest;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.JsonConfigBuilder;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.TaskContext;
import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.micro.api.MicroConst;
import de.mhus.micro.api.MicroUtil;
import de.mhus.micro.api.operation.OperationsAdmin;
import de.mhus.micro.api.server.MicroProvider;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestException;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.api.RestResult;
import de.mhus.rest.core.node.AbstractNode;
import de.mhus.rest.core.result.ErrorJsonResult;
import de.mhus.rest.core.result.JsonResult;
import de.mhus.rest.osgi.PublicRestNode;

@RestNode(name = "operation", parentNode = PublicRestNode.class)
@Component(immediate = true,property = {
        OperationsAdmin.EVENT_TOPICS
        },
        service = {RestNodeService.class, EventHandler.class, MicroProvider.class})
public class OperationsNode extends AbstractNode implements EventHandler, MicroProvider {

    private CfgString CFG_HOST = new CfgString(OperationsNode.class, "host", MSystem.getHostname());
    private CfgInt CFG_PORT = new CfgInt(OperationsNode.class, "port", 8181);
    private CfgString CFG_URL = new CfgString(OperationsNode.class, "url", "http://${host}:${port}/rest/public/operation");
    private Map<UUID,OperationDescription> descriptions = Collections.synchronizedMap(new HashMap<>());
    
    @Reference
    private OperationsAdmin admin;
    
    @Activate
    public void doActivate() {
        reload();
    }
    
    @Override
    public void reload() {
        for (Operation item : admin.list()) {
            OperationDescription desc = item.getDescription();
            OperationDescription desc2 = prepareDesc(desc);
            descriptions.put(desc2.getUuid(), desc2);
            MicroUtil.firePushAdd(desc2);
        }
    }

    private String prepareUrlPrefix() {
        try {
            String url = CFG_URL.value();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("host", CFG_HOST.value());
            attributes.put("port", CFG_PORT.value());
            return StringCompiler.compile(url).execute(attributes);
        } catch (Throwable t) {
            log().e(t);
            return CFG_URL.value();
        }
    }

    @Override
    public Node lookup(List<String> parts, CallContext callContext) throws Exception {
        if (parts.size() < 1)
            throw new NotSupportedException();
        callContext.put("path", parts.remove(0));
        if (parts.size() > 0)
            callContext.put("version", parts.remove(0));

        callContext.put("parts", parts);
        return this;
    }

    @Override
    public RestResult doRead(CallContext context) throws Exception {
        String load = context.getParameter("load");
        return doOperation(context, load);
    }

    @Override
    public RestResult doCreate(CallContext context) throws Exception {
        InputStream loadStream = context.getLoadContent();
        String load = null;
        if (loadStream == null)
            load = context.getParameter("load");
        else
            load = MFile.readFile(loadStream);
        return doOperation(context, load);
    }

    private RestResult doOperation(CallContext context, String load) throws Exception {

        Operation oper = findOperation(context);
        if (oper == null)
            throw new RestException(404, "not found (1)");
        
        TaskContext taskContext = new RestTaskContext(context, load);
        
        if (!oper.hasAccess(taskContext))
            throw new RestException(404, "not found (2)");
        
        if (!oper.canExecute(taskContext))
            throw new RestException(404, "not found (3)");
            
        OperationResult res = oper.doExecute(taskContext);
        
        Object r = res.getResult();
        
        IConfig outter = new MConfig();
        outter.setBoolean("successful", res.isSuccessful());
        outter.setString("msg", res.getMsg());
        outter.setInt("rc", res.getReturnCode());

        if (r != null) {
            // map result to rest result
            if (r instanceof Map) {
                IConfig cfg = IConfig.readFromMap((Map<?, ?>) r);
                outter.addObject("result", cfg);
            } else
            if (r instanceof IConfig) {
                IConfig cfg = (IConfig)r;
                outter.addObject("result", cfg);
            } else
            if (r instanceof ObjectNode) {
                IConfig cfg = new JsonConfigBuilder().fromJson((JsonNode)r);
                outter.addObject("result", cfg);
            } else
            if (r instanceof String) {
                outter.setString("result", (String)r);
            } else {
                JsonNode json = MJson.pojoToJson(r);
                IConfig cfg = new JsonConfigBuilder().fromJson(json);
                outter.addObject("result", cfg);
            }

            String content = IConfig.toPrettyJsonString(outter);
            JsonNode json = MJson.load(content);
            JsonResult out = new JsonResult();
            out.setJson(json);
            return out;
        }

        return new ErrorJsonResult(res.getReturnCode(), res.getMsg());
    }

    private Operation findOperation(CallContext context) {
        String path = (String) context.get("path");
        String version = (String) context.get("version");

        OperationsAdmin api = M.l(OperationsAdmin.class);

        if (MValidator.isUUID(version)) {
            return api.getOperation(UUID.fromString(version));
        }

        return api.getOperation(path, version);
    }

    @Override
    public RestResult doDelete(CallContext context) throws Exception {
        throw new NotSupportedException();
    }

    @Override
    public RestResult doUpdate(CallContext context) throws Exception {
        throw new NotSupportedException();
    }

    @Override
    public RestResult doAction(CallContext context) throws Exception {
        throw new NotSupportedException();
    }

    @Override
    public void handleEvent(Event event) {
        OperationDescription desc = (OperationDescription) event.getProperty(OperationsAdmin.EVENT_PROPERTY_DESCRIPTION);
        if (desc == null) return;
        
        String topic = event.getTopic();
        log().i("event",event); //XXX
        if (OperationsAdmin.EVENT_TOPIC_ADD.equals(topic)) {
            OperationDescription desc2 = prepareDesc(desc);
            descriptions.put(desc2.getUuid(), desc2);
            MicroUtil.firePushAdd(desc2);
        } else
        if (OperationsAdmin.EVENT_TOPIC_REMOVE.equals(topic)) {
            OperationDescription desc2 = descriptions.get(desc.getUuid());
            if (desc2 == null) 
                desc2 = prepareDesc(desc2);
            MicroUtil.firePushRemove(desc2);
            descriptions.remove(desc2.getUuid());
        }

    }

    private OperationDescription prepareDesc(OperationDescription desc) {
        OperationDescription desc2 = new OperationDescription(desc);
        desc2.putLabel(MicroConst.DESC_LABEL_TRANSPORT_TYPE, MicroConst.REST_TRANSPORT);
        String urlPrefix = prepareUrlPrefix();
        desc2.putLabel(MicroConst.REST_URL, urlPrefix + "/" + desc.getPath() + "/" + desc.getVersionString());
        desc2.putLabel(MicroConst.REST_METHOD, "POST");
        return desc2;
    }

    @Override
    public void provided(List<OperationDescription> list) {
        descriptions.values().forEach(d -> list.add(d));
    }

}
