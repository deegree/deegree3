package org.deegree.gml.reference.matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches if at least one of the encapsulated {@link ReferencePatternMatcher} matches.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MultipleReferencePatternMatcher implements ReferencePatternMatcher {

	private final List<ReferencePatternMatcher> matchersToApply = new ArrayList();

	@Override
	public boolean isMatching(String url) {
		for (ReferencePatternMatcher matcherToApply : matchersToApply) {
			if (matcherToApply.isMatching(url))
				return true;
		}
		return false;
	}

	public void addMatcherToApply(ReferencePatternMatcher matcherToAdd) {
		matchersToApply.add(matcherToAdd);
	}

	public void removeMatcherToApply(ReferencePatternMatcher matcherToAdd) {
		matchersToApply.remove(matcherToAdd);
	}

}
