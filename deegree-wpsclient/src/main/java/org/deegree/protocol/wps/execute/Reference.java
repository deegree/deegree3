package org.deegree.protocol.wps.execute;

public class Reference {

    String mimeType;

    String encoding;

    String schema;

    String href;

    String method;

    Header header;

    String body;

    BodyReference bodyReference;

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

    public String getHref() {
        return href;
    }

    public void setHref( String href ) {
        this.href = href;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod( String method ) {
        this.method = method;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader( Header header ) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody( String body ) {
        this.body = body;
    }

    public BodyReference getBodyReference() {
        return bodyReference;
    }

    public void setBodyReference( BodyReference bodyReference ) {
        this.bodyReference = bodyReference;
    }

}
