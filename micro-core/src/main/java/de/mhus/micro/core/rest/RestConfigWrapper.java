package de.mhus.micro.core.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.mhus.lib.core.AbstractProperties;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.rest.core.CallContext;

public class RestConfigWrapper extends AbstractProperties {

	private static final long serialVersionUID = 1L;
	private CallContext context;
	private Map<String,Object> overlay = new HashMap<>();

	public RestConfigWrapper(CallContext context) {
		this.context = context;
	}

	@Override
	public void clear() {
		overlay.clear();
	}

	@Override
	public boolean containsValue(Object value) {
		throw new NotSupportedException();
	}

	@Override
	public Collection<Object> values() {
		throw new NotSupportedException();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return overlay.entrySet();
	}

	@Override
	public Object getProperty(String key) {
		return context.getRequest().getHeader(key);
	}

	@Override
	public boolean isProperty(String key) {
		return context.getRequest().getHeader(key) != null;
	}

	@Override
	public void removeProperty(String key) {
		overlay.remove(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		overlay.put(key, value);
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public Set<String> keys() {
		return overlay.keySet();
	}

	@Override
	public int size() {
		return overlay.size();
	}

}
