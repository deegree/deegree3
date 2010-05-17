package org.deegree.protocol.wps.execute;

public class RawOutputData {

    String identifier;

    String mimeType;

    String encoding;

    String schema;

    String uom;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType( String mimeType ) {
        this.mimeType = mimeType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding( String encoding ) {
        this.encoding = encoding;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema( String schema ) {
        this.schema = schema;
    }

    public String getUom() {
        return uom;
    }

    public void setUom( String uom ) {
        this.uom = uom;
    }

}
