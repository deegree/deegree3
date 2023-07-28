package org.deegree.commons.ows.metadata;

public class MetadataUrl {

	private final String url;

	private final String type;

	private final String format;

	public MetadataUrl(final String url, final String type, final String format) {
		this.url = url;
		this.type = type;
		this.format = format;
	}

	/**
	 * @return the url, never <code>null</code>
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the type, can be <code>null</code> (unspecified)
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the format, can be <code>null</code> (unspecified)
	 */
	public String getFormat() {
		return format;
	}

}
