package org.deegree.gml.reference.matcher;

/**
 * Matches if the URL starts with the passed pattern (baseUrl).
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class BaseUrlReferencePatternMatcher implements ReferencePatternMatcher {

	private final String baseUrl;

	public BaseUrlReferencePatternMatcher(String baseUrl) {
		if (baseUrl == null || "".equals(baseUrl))
			throw new IllegalArgumentException("baseUrl must never be null or empty!");
		this.baseUrl = baseUrl;
	}

	@Override
	public boolean isMatching(String url) {
		if (url == null)
			return false;
		return url.startsWith(baseUrl);
	}

}