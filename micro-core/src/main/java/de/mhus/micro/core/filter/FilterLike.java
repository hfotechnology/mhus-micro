package de.mhus.micro.core.filter;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.micro.core.api.MicroFilter;

public class FilterLike implements MicroFilter {

	private String[] rules;

	public FilterLike(String[] rules) {
		this.rules = rules;
	}
	
	@Override
	public boolean matches(OperationDescription desc) {
		if (rules == null || rules.length == 0)
			return true;
		
		int cnt = 0;
		for (String rule : rules) {
			if (cnt == 0)
				rule = "p:" + rule;
			
			if (rule.startsWith("path:"))
				return MString.compareFsLikePattern(rule.substring(5), desc.getPath());
			
			if (rule.startsWith("p:"))
				return MString.compareFsLikePattern(rule.substring(2), desc.getPath());
			
			if (rule.startsWith("version:"))
				return new VersionRange(rule.substring(8)).includes(desc.getVersion());
			
			if (rule.startsWith("v:"))
				return new VersionRange(rule.substring(2)).includes(desc.getVersion());

			if (rule.startsWith("label:")) {
				String r = rule.substring(6);
				String[] parts = r.split("=", 2);
				if (parts.length == 2) {
					if (!desc.getLabels().containsKey(parts[0])) return false;
					return MString.compareFsLikePattern(parts[1], desc.getLabels().getString(parts[0], ""));
				}
			}			
			
			if (rule.startsWith("l:")) {
				String r = rule.substring(2);
				String[] parts = r.split("=", 2);
				if (parts.length == 2) {
					if (!desc.getLabels().containsKey(parts[0])) return false;
					return MString.compareFsLikePattern(parts[1], desc.getLabels().getString(parts[0], ""));
				}
			}			

			cnt++;
		}
		
		return false;
	}

}
