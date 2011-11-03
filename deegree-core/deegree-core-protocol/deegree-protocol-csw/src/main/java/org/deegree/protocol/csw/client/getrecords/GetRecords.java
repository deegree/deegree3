//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.csw.client.getrecords;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.filter.Filter;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecords {

    private int startPosition = 1;

    private int maxRecords = 10;

    private String outputFormat = "application/xml";

    private String outputSchema = "http://www.opengis.net/cat/csw/2.0.2.";

    private ResultType resultType = ResultType.hits;

    private List<QName> typeNames = Collections.singletonList( new QName( CSWConstants.CSW_202_NS, "Record",
                                                                          CSWConstants.CSW_202_PREFIX ) );

    private final Filter constraint;

    private final ReturnableElement elementSetName;

    /**
     * @param startPosition
     * @param maxRecords
     * @param outputFormat
     * @param outputSchema
     * @param typeNames
     * @param resultType
     * @param constraint
     */
    public GetRecords( int startPosition, int maxRecords, String outputFormat, String outputSchema,
                       List<QName> typeNames, ResultType resultType, ReturnableElement elementSetName, Filter constraint ) {
        this( resultType, elementSetName, constraint );
        this.startPosition = startPosition;
        this.maxRecords = maxRecords;
        this.outputFormat = outputFormat;
        this.outputSchema = outputSchema;
        this.typeNames = typeNames;
    }

    /**
     * @param resultType2
     * @param elementSetName
     * @param constraint2
     */
    public GetRecords( ResultType resultType, ReturnableElement elementSetName, Filter constraint ) {
        this.elementSetName = elementSetName;
        this.resultType = resultType;
        this.constraint = constraint;
    }

    /**
     * @return the startPosition
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * @return the maxRecords
     */
    public int getMaxRecords() {
        return maxRecords;
    }

    /**
     * @return the outputFormat
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @return the outputSchema
     */
    public String getOutputSchema() {
        return outputSchema;
    }

    /**
     * @return the typeNames
     */
    public List<QName> getTypeNames() {
        return typeNames;
    }

    /**
     * @return the resultType
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @return the constraint
     */
    public Filter getConstraint() {
        return constraint;
    }

    /**
     * 
     * @return
     */
    public ReturnableElement getElementSetName() {
        return elementSetName;
    }

}
