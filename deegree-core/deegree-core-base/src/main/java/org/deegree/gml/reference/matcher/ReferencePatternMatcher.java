package org.deegree.gml.reference.matcher;

/**
 * A {@link ReferencePatternMatcher} checks if a passed reference matches the specified
 * pattern.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface ReferencePatternMatcher {

	/**
	 * Checks if the passed url matches the pattern of the {@link ReferencePatternMatcher}
	 * @param url the url to check, may be <code>null</code>
	 * @return <code>true</code> if the url matches, <code>false</code> otherwise
	 */
	boolean isMatching(String url);

}
