package edu.uci.ics.crawler4j.robotstxt;

import java.util.SortedSet;
import java.util.TreeSet;

public class RuleSet extends TreeSet<String> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean add(String str) {
		SortedSet<String> sub = headSet(str);
		if (!sub.isEmpty() && str.startsWith(sub.last())) {
			// no need to add; prefix is already present
			return false;
		}
		boolean retVal = super.add(str);
		sub = tailSet(str + "\0");
		while (!sub.isEmpty() && sub.first().startsWith(str)) {
			// remove redundant entries
			sub.remove(sub.first());
		}
		return retVal;
	}
	
	public boolean containsPrefixOf(String s) {
		SortedSet<String> sub = headSet(s);
		// because redundant prefixes have been eliminated,
		// only a test against last item in headSet is necessary
		if (!sub.isEmpty() && s.startsWith(sub.last())) {
			return true; // prefix substring exists
		} 
		// might still exist exactly (headSet does not contain boundary)
		return contains(s); 
	}
}
