package org.deegree.protocol.wps.tools;

import org.deegree.protocol.wps.describeprocess.ComplexData;
import org.deegree.protocol.wps.describeprocess.LiteralOutputData;
import org.deegree.protocol.wps.describeprocess.OutputDescription;
import org.deegree.protocol.wps.describeprocess.ProcessOutput;

/**
 * Encapsulates the structure of a DataOutputParameter
 * 
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataOutputParameter {

    private String title;

    private String _abstract;

    private String identifier;

    private String dataType;

    private String encoding;

    private String mimeType;

    private String uom;

    private String maximumMegaBytes;

    private String schema;

    public DataOutputParameter( OutputDescription outputDescription ) {
        this._abstract = outputDescription.getAbstraCt();
        this.identifier = outputDescription.getIdentifier();
        this.title = outputDescription.getTitle();
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

    /**
     * 
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @return identifier
     */
    public String getAbstraCt() {
        return _abstract;
    }

    /**
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * 
     * @return dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * 
     * @return encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 
     * @return mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 
     * @return uom
     */
    public String getUom() {
        return uom;
    }

    /**
     * 
     * @return maximumMegaBytes
     */
    public String getMaximumMegaBytes() {
        return maximumMegaBytes;
    }

    /**
     * 
     * @return schema
     */
    public String getSchema() {
        return schema;
    }

}
