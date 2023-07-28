package org.deegree.commons.ows.metadata.layer;

public class Attribution {

	public final String title;

	public final String url;

	public final LogoUrl logoUrl;

	public Attribution(final String title, final String url, final LogoUrl logoUrl) {
		this.title = title;
		this.url = url;
		this.logoUrl = logoUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public LogoUrl getLogoUrl() {
		return logoUrl;
	}

}
