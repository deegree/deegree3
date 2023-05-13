package org.deegree.commons.ows.metadata.layer;

public class UrlWithFormat {

	private final String url;

	private final String format;

	public UrlWithFormat(final String url, final String format) {
		this.url = url;
		this.format = format;
	}

	public String getUrl() {
		return url;
	}

	public String getFormat() {
		return format;
	}

}
