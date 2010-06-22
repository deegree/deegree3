package org.deegree.protocol.wps.tools;

import org.deegree.protocol.wps.describeprocess.ComplexData;
import org.deegree.protocol.wps.describeprocess.LiteralOutputData;
import org.deegree.protocol.wps.describeprocess.OutputDescription;
import org.deegree.protocol.wps.describeprocess.ProcessOutput;

public class DataOutputParameter {

    private String title;

    private String abstraCt;

    private String identifier;

    private String dataType;

    private String encoding;

    private String mimeType;

    private String uom;

    private String maximumMegaBytes;

    private String schema;

    public DataOutputParameter( OutputDescription outputDescription ) {
        this.abstraCt=outputDescription.getAbstraCt();
        this.identifier=outputDescription.getIdentifier();
        this.title= outputDescription.getTitle();
        if ( outputDescription.getOutputFormChoice().getLiteralOutput() != null ) {
            LiteralOutputData literalOutput = outputDescription.getOutputFormChoice().getLiteralOutput();
            this.dataType = literalOutput.getDataType();
            if ( literalOutput.getUom() != null )
                this.uom = literalOutput.getUom().getDefauLt();
        }
        if ( outputDescription.getOutputFormChoice().getComplexOutput() != null ) {
            ComplexData complexData = outputDescription.getOutputFormChoice().getComplexOutput();
            this.encoding = complexData.getDefaulT().getEncoding();
            this.mimeType = complexData.getDefaulT().getMimeType();
            this.maximumMegaBytes = complexData.getMaximumMegaBytes();
            this.schema = complexData.getDefaulT().getSchema();
        }

    }

    public String getTitle() {
        return title;
    }

    public String getAbstraCt() {
        return abstraCt;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDataType() {
        return dataType;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getUom() {
        return uom;
    }

    public String getMaximumMegaBytes() {
        return maximumMegaBytes;
    }

    public String getSchema() {
        return schema;
    }

}
