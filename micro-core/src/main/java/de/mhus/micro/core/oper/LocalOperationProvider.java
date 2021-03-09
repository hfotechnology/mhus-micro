package de.mhus.micro.core.oper;

import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.api.C;

public class LocalOperationProvider extends OperationProvider {

	@Override
	protected OperationDescription cloneDescription(OperationDescription description) {
		OperationDescription out = new OperationDescription(description);
		((MProperties)out.getLabels()).setString(C.LABEL_PROTO, "operation");
		((MProperties)out.getLabels()).setBoolean(C.LABEL_LOCAL, true);
		return out;
	}

}
