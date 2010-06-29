//$HeadURL: https://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wps.tools;

import org.deegree.protocol.wps.describeprocess.DataInputDescribeProcess;
import org.deegree.protocol.wps.describeprocess.Format;
import org.deegree.protocol.wps.describeprocess.InputFormChoiceDescribeProcess;
import org.deegree.protocol.wps.describeprocess.LiteralInputData;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;

/**
 * Encapsulates the structure of a DataInputParameter
 * 
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataInputParameter {

    private String identifier;

    private String _abstract;

    private String maxOccurs;

    private String minOccurs;

    private String[] valueList;

    private String title;

    private String metaData;

    private String dataFormat;

    private String encoding;

    private String mimeType;

    private String[] allowedValues;

    /**
     * Constructor that builds object in reference to the given DescribeProcess object
     * 
     * @param dataInputDescribeProcess
     * 
     */
    public DataInputParameter( DataInputDescribeProcess dataInputDescribeProcess ) {
        this.identifier = dataInputDescribeProcess.getIdentifier();
        this._abstract = dataInputDescribeProcess.getAbstraCt();
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

    /**
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * 
     * @return _abstract
     */
    public String getAbstraCt() {
        return _abstract;
    }

    /**
     * 
     * @return maxOccurs
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * 
     * @return minOccurs
     */
    public String getMinOccurs() {
        return minOccurs;
    }

    /**
     * 
     * @return valueList[]
     */
    public String[] getValueList() {
        return valueList;
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
     * @return metaData
     */
    public String getMetaData() {
        return metaData;
    }

    /**
     * 
     * @return dataFormat
     */
    public String getDataFormat() {
        return dataFormat;
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
     * @return allowedValues[]
     */
    public String[] getAllowedValues() {
        return allowedValues;
    }

}
