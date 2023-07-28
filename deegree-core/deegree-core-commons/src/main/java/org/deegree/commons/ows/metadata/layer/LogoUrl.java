package org.deegree.commons.ows.metadata.layer;

public class LogoUrl extends UrlWithFormat {

	private final int width;

	private final int height;

	public LogoUrl(final String url, final String format, final Integer width, final Integer height) {
		super(url, format);
		this.width = width;
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

}
