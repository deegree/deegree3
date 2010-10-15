//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.csw.getrecords;

import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.filter.Filter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.services.csw.AbstractCSWRequest;

/**
 * Represents a <Code>GetRecords</Code> request to a CSW.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecords extends AbstractCSWRequest {

    private String requestId;

    private URI outputSchema;

    private int startPosition;

    private int maxRecords;

    private String[] elementName;

    private ReturnableElement elementSetName;

    private Filter constraint;

    private SortProperty[] sortBy;

    private boolean distributedSearch;

    private int hopCount;

    private String responseHandler;

    private ResultType resultType;

    private ConstraintLanguage constraintLanguage;

    private OMElement holeRequest;

    /**
     * Creates a new {@link GetRecords} request.
     * 
     * @param version
     *            protocol version
     * @param namespaces
     * @param typeNames
     *            one or more names of queryable entities
     * @param outputFormat
     *            controls the format of the output regarding to a MIME-type (default: application/xml)
     * @param resultType
     *            mode of the response that is requested
     * @param requestId
     *            UUID
     * @param outputSchema
     *            indicates the schema of the output (default: http://www.opengis.net/cat/csw/2.0.2)
     * @param startPosition
     *            used to specify at which position should be started
     * @param maxRecords
     *            defines the maximum number of records that should be returned
     * @param elementName
     * @param elementSetName
     * @param constraintLanguage
     * @param constraint
     * @param sortBy
     * @param distributedSearch
     * @param hopCount
     * @param responseHandler
     */
    public GetRecords( Version version, NamespaceContext namespaces, QName[] typeNames, String outputFormat,
                       ResultType resultType, String requestId, URI outputSchema, int startPosition, int maxRecords,
                       String[] elementName, ReturnableElement elementSetName, ConstraintLanguage constraintLanguage,
                       Filter constraint, SortProperty[] sortBy, boolean distributedSearch, int hopCount,
                       String responseHandler, OMElement holeRequest ) {
        super( version, namespaces, typeNames, outputFormat );
        this.resultType = resultType;
        this.requestId = requestId;
        this.outputSchema = outputSchema;
        this.startPosition = startPosition;
        this.maxRecords = maxRecords;
        this.elementName = elementName;
        this.elementSetName = elementSetName;
        this.constraintLanguage = constraintLanguage;
        this.constraint = constraint;
        this.sortBy = sortBy;
        this.distributedSearch = distributedSearch;
        this.hopCount = hopCount;
        this.responseHandler = responseHandler;
        this.holeRequest = holeRequest;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @return the outputSchema
     */
    public URI getOutputSchema() {
        return outputSchema;
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
     * @return the elementName
     */
    public String[] getElementName() {
        return elementName;
    }

    /**
     * @return the elementSetName
     */
    public ReturnableElement getElementSetName() {
        return elementSetName;
    }

    /**
     * @return the constraint
     */
    public Filter getConstraint() {
        return constraint;
    }

    /**
     * @return the sortBy
     */
    public SortProperty[] getSortBy() {
        return sortBy;
    }

    /**
     * @return the distributedSearch
     */
    public boolean isDistributedSearch() {
        return distributedSearch;
    }

    /**
     * @return the hopCount
     */
    public int getHopCount() {
        return hopCount;
    }

    /**
     * @return the responseHandler
     */
    public String getResponseHandler() {
        return responseHandler;
    }

    /**
     * @return the resultType
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @return the constraintLanguage
     */
    public ConstraintLanguage getConstraintLanguage() {
        return constraintLanguage;
    }

    /**
     * @return the holeRequest
     */
    public OMElement getHoleRequest() {
        return holeRequest;
    }

}
