package de.mhus.micro.core.impl;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.aaa.SubjectEnvironment;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.micro.core.api.MicroApi;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.filter.FilterPathVersion;
import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

public class Executor extends MLog {

	private MicroApi api;
	
	public MicroResult doExecute(
			Subject subject, 
			Format<TextMap> traceFormat, 
			TextMap traceMapper, 
			String pathVersion, 
			IConfig arguments) {
		
		IProperties properties = new MProperties();
		OperationDescription description = null;
		Scope scope = null;
		try (SubjectEnvironment access = Aaa.asSubject(subject)) {
			
			// Init traceing
			SpanContext parentSpanCtx = null;
			if (traceFormat != null && traceMapper != null) {
				parentSpanCtx = ITracer.get().tracer().extract(traceFormat, traceMapper);
			}
			if (parentSpanCtx == null) {
                scope = ITracer.get().start("execute", pathVersion);
            } else if (parentSpanCtx != null) {
                scope =
                        ITracer.get()
                                .tracer()
                                .buildSpan("execute")
                                .asChildOf(parentSpanCtx)
                                .startActive(true);
                ITracer.get().activate(pathVersion);
            }
			
			// execute
	    	description = api.first(new FilterPathVersion(pathVersion));
	    	if (description == null) throw new NotFoundException("@Operation for path $1 not found",pathVersion);
	    	return api.execute(description, arguments, properties);
			
		} catch (Throwable t) {
			return new MicroResult(description, t);
		} finally {
			if (scope != null)
				scope.close();
		}
	}

	public MicroApi getApi() {
		return api;
	}

	public void setApi(MicroApi api) {
		this.api = api;
	}
	
	
}
