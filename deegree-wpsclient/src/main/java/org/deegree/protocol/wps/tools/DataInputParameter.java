package org.deegree.protocol.wps.tools;

import org.deegree.protocol.wps.describeprocess.DataInputDescribeProcess;
import org.deegree.protocol.wps.describeprocess.Format;
import org.deegree.protocol.wps.describeprocess.InputFormChoiceDescribeProcess;
import org.deegree.protocol.wps.describeprocess.LiteralInputData;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;

public class DataInputParameter {

    private String identifier;

    private String abstraCt;

    private String maxOccurs;

    private String minOccurs;

    private String[] valueList;

    private String title;

    private String metaData;

    private String dataFormat;

    private String encoding;

    private String mimeType;

    private String[] allowedValues;

    public DataInputParameter( DataInputDescribeProcess dataInputDescribeProcess ) {
        this.identifier = dataInputDescribeProcess.getIdentifier();
        this.abstraCt = dataInputDescribeProcess.getAbstraCt();
        this.maxOccurs = dataInputDescribeProcess.getMaxOccurs();
        this.minOccurs = dataInputDescribeProcess.getMinOccurs();
        this.title = dataInputDescribeProcess.getTitle();
        this.metaData = dataInputDescribeProcess.getMetadata();
        if ( dataInputDescribeProcess.getValueList() != null ) {
            valueList = new String[dataInputDescribeProcess.getValueList().size()];
            for ( int i = 0; i < dataInputDescribeProcess.getValueList().size(); i++ ) {
                valueList[i] = String.valueOf( dataInputDescribeProcess.getValueList().get( i ) );
            }
        }
        InputFormChoiceDescribeProcess inputFormChoiceDescribeProcess = dataInputDescribeProcess.getInputFormChoice();
        if ( inputFormChoiceDescribeProcess.getComplexData() != null ) {
            Format defaultComplexData = inputFormChoiceDescribeProcess.getComplexData().getDefaulT();
            this.dataFormat = defaultComplexData.getSchema();
            this.encoding = defaultComplexData.getEncoding();
            this.mimeType = defaultComplexData.getMimeType();
        }
        if ( inputFormChoiceDescribeProcess.getLiteralData() != null ) {
            LiteralInputData literalData = inputFormChoiceDescribeProcess.getLiteralData();
            this.dataFormat = literalData.getDataType();

            if ( literalData.getLiteralValuesChoice() != null ) {
                if ( literalData.getLiteralValuesChoice().getAllowedValues() != null )
                    allowedValues = new String[literalData.getLiteralValuesChoice().getAllowedValues().size()];
                {
                    for ( int i = 0; i < literalData.getLiteralValuesChoice().getAllowedValues().size(); i++ ) {
                        allowedValues[i] = literalData.getLiteralValuesChoice().getAllowedValues().get( i );

                    }
                }
            }
        }

    }

    public String getIdentifier() {
        return identifier;
    }

    public String getAbstraCt() {
        return abstraCt;
    }

    public String getMaxOccurs() {
        return maxOccurs;
    }

    public String getMinOccurs() {
        return minOccurs;
    }

    public String[] getValueList() {
        return valueList;
    }

    public String getTitle() {
        return title;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String[] getAllowedValues() {
        return allowedValues;
    }

}
