package de.mhus.micro.core.oper;

import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.micro.core.api.Micro;

public class LocalOperationProvider extends OperationProvider {

	@Override
	protected OperationDescription cloneDescription(OperationDescription description) {
		OperationDescription out = new OperationDescription(description);
		((MProperties)out.getLabels()).setString(Micro.LABEL_PROTO, "operation");
		((MProperties)out.getLabels()).setBoolean(Micro.LABEL_LOCAL, true);
		return out;
	}

}
