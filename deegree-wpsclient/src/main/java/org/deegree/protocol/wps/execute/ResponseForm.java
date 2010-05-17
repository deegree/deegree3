package org.deegree.protocol.wps.execute;

public class ResponseForm {

    ResponseDocument responseDocument;

    RawOutputData rawOutputData;

    public ResponseDocument getResponseDocument() {
        return responseDocument;
    }

    public void setResponseDocument( ResponseDocument responseDocument ) {
        this.responseDocument = responseDocument;
    }

    public RawOutputData getRawOutputData() {
        return rawOutputData;
    }

    public void setRawOutputData( RawOutputData rawOutputData ) {
        this.rawOutputData = rawOutputData;
    }

}
