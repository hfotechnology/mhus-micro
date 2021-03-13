
package de.mhus.micro.core.rest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.JsonConfigBuilder;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.micro.core.api.MicroApi;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.filter.FilterPathVersion;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestException;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.api.RestResult;
import de.mhus.rest.core.node.AbstractNode;
import de.mhus.rest.core.nodes.PublicRestNode;
import de.mhus.rest.core.result.ErrorJsonResult;
import de.mhus.rest.core.result.JsonResult;
import de.mhus.rest.core.result.PlainTextResult;

@RestNode(name = "operation", parentNode = PublicRestNode.class)
@ServiceComponent(service = RestNodeService.class)
public class OperationsNode extends AbstractNode {

    @Override
    public Node lookup(List<String> parts, CallContext callContext) throws Exception {
        if (parts.size() < 1)
            throw new NotSupportedException();
        callContext.put("path", parts.remove(0));
        
        callContext.put("parts", parts);
        return this;
    }

    @Override
    public RestResult doRead(CallContext context) throws Exception {
        String load = context.getParameter("_load");
        return doOperation(context, load == null ? IConfig.wrap(context.getParameters()) : IConfig.readConfigFromString(load));
    }

    // POST
    @Override
    public RestResult doCreate(CallContext context) throws Exception {
        InputStream loadStream = context.getLoadContent();
        String load = null;
        if (loadStream == null)
            load = context.getParameter("load");
        else
            load = MFile.readFile(loadStream);
        return doOperation(context, IConfig.readConfigFromString(load));
    }

    private RestResult doOperation(CallContext context, IConfig load) throws Exception {

    	OperationDescription oper = findOperation(context);
        if (oper == null)
            throw new RestException(404, "not found (1)");
        
        MicroApi api = M.l(MicroApi.class);
        String path = (String) context.get("path");
        IReadProperties config = new RestConfigWrapper(context);
        
        MicroResult res = api.execute(path, load, config);

        Object r = res.getResult();
        
        context.setResponseHeader("successful", ""+res.isTransportSuccessful());
        context.setResponseHeader("rc", res.getReturnCode());
        context.setResponseHeader("msg", res.getMessage());
        
        IConfig cfg = null;
        
        if (r != null) {
            // map result to rest result
            if (r instanceof Map) {
                cfg = IConfig.readFromMap((Map<?, ?>) r);
            } else
            if (r instanceof IConfig) {
                cfg = (IConfig)r;
            } else
            if (r instanceof ObjectNode) {
                cfg = new JsonConfigBuilder().fromJson((JsonNode)r);
            } else
            if (r instanceof String) {
            	return new PlainTextResult((String)r, MHttp.CONTENT_TYPE_TEXT);
            } else {
                JsonNode json = MJson.pojoToJson(r);
                cfg = new JsonConfigBuilder().fromJson(json);
            }

            JsonNode json = new JsonConfigBuilder().writeToJsonNode(cfg);
            JsonResult out = new JsonResult();
            out.setJson(json);
            return out;
        }

        return new ErrorJsonResult(res.getReturnCode(), res.getMessage());
    }

    private OperationDescription findOperation(CallContext context) {
        String path = (String) context.get("path");

        MicroApi api = M.l(MicroApi.class);
        return api.first(new FilterPathVersion(path));
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

}
