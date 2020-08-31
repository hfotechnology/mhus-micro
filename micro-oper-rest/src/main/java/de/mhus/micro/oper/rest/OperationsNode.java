package de.mhus.micro.oper.rest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.io.http.MHttp;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.TaskContext;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.micro.oper.impl.OperationsAdmin;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.annotation.RestNode;
import de.mhus.rest.core.api.Node;
import de.mhus.rest.core.api.RestException;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.api.RestResult;
import de.mhus.rest.core.node.AbstractNode;
import de.mhus.rest.core.result.ErrorJsonResult;
import de.mhus.rest.core.result.JsonResult;
import de.mhus.rest.core.result.PlainTextResult;
import de.mhus.rest.osgi.PublicRestNode;

@RestNode(name = "operation", parentNode = PublicRestNode.class)
@Component(immediate = true, service = RestNodeService.class)
public class OperationsNode extends AbstractNode {

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
        if (r != null) {
            // map result to rest result
            if (r instanceof RestResult)
                return (RestResult)r;
            if (r instanceof ObjectNode) {
                JsonResult out = new JsonResult();
                out.setJson((JsonNode)r);
                return out;
            }
            if (r instanceof String)
                return new PlainTextResult((String)r, MHttp.findMimeType((String)r, MFile.DEFAULT_MIME));
            
            JsonNode json = MJson.pojoToJson(r);
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

}
