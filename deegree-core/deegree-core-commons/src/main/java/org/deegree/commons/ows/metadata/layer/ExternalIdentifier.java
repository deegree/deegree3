package org.deegree.commons.ows.metadata.layer;

public class ExternalIdentifier {

	private final String id;

	private final String authorityCode;

	public ExternalIdentifier(final String id, final String authorityCode) {
		this.id = id;
		this.authorityCode = authorityCode;
	}

	public String getId() {
		return id;
	}

	public String getAuthorityCode() {
		return authorityCode;
	}

}
