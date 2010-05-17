package org.deegree.protocol.wps.execute;

import java.util.ArrayList;
import java.util.List;

public class ResponseDocument {

    boolean storeExecuteResponse;

    boolean lineage;

    boolean status;

    List<OutputDefinition> output = new ArrayList();

    public boolean isStoreExecuteResponse() {
        return storeExecuteResponse;
    }

    public void setStoreExecuteResponse( boolean storeExecuteResponse ) {
        this.storeExecuteResponse = storeExecuteResponse;
    }

    public boolean isLineage() {
        return lineage;
    }

    public void setLineage( boolean lineage ) {
        this.lineage = lineage;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus( boolean status ) {
        this.status = status;
    }

    public List<OutputDefinition> getOutput() {
        return output;
    }

    public void setOutput( List<OutputDefinition> output ) {
        this.output = output;
    }

}
