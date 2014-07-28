package org.deegree.commons.ows.metadata.layer;

public class ExternalIdentifier {

    private final String id;

    private final String authorityCode;

    private final String authorityUrl;

    public ExternalIdentifier( final String id, final String authorityCode, final String authorityUrl ) {
        this.id = id;
        this.authorityCode = authorityCode;
        this.authorityUrl = authorityUrl;
    }

    public String getId() {
        return id;
    }

    public String getAuthorityCode() {
        return authorityCode;
    }

    public String getAuthorityUrl() {
        return authorityUrl;
    }

}
