package org.deegree.protocol.wps.execute;

public class OutputDefinition {

    String mimeType;

    String encoding;

    String schema;

    String uom;

    boolean asReference;

    String identifier;

    String title;

    String abstraCt;

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

    public boolean isAsReference() {
        return asReference;
    }

    public void setAsReference( boolean asReference ) {
        this.asReference = asReference;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getAbstraCt() {
        return abstraCt;
    }

    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

}
