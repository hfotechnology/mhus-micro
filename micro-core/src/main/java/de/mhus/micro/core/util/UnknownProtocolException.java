package de.mhus.micro.core.util;

import de.mhus.lib.errors.MRuntimeException;

public class UnknownProtocolException extends MRuntimeException {

	private static final long serialVersionUID = 1L;
	
	public UnknownProtocolException(String proto) {
		super(proto);
	}

}
