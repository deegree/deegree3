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
package org.deegree.metadata.persistence;

import java.io.Writer;

import org.deegree.filter.Filter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.protocol.csw.CSWConstants.ResultType;

/**
 * This class holds all the necessary information that is needed for the database request. <br>
 * The request itself is encapsulated in the expression{@link Writer}.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MetadataQuery {

    // ?
    private final ResultType resultType;

    // ?
    private final int maxRecords;

    private final int startPosition;

    private final Filter filter;

    private final SortProperty[] sorting;

    /**
     * Creates a new {@link MetadataQuery} instance with all attributes that can be declared.
     * 
     * @param filter
     *            the parsed filter expression
     * @param resultType
     *            {@link ResultType}
     * @param maxRecords
     *            the maximum number of records that shall be presented in the response
     * @param startPosition
     *            at which record position should start the response}
     */
    public MetadataQuery( Filter filter, SortProperty[] sorting, ResultType resultType, int maxRecords,
                          int startPosition ) {

        this.filter = filter;
        this.sorting = sorting;
        this.resultType = resultType;
        this.maxRecords = maxRecords;
        this.startPosition = startPosition;

    }

    /**
     * @return the filter
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * @return the resultType
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @return the maxRecords
     */
    public int getMaxRecords() {
        return maxRecords;
    }

    /**
     * @return the startPosition
     */
    public int getStartPosition() {
        return startPosition;
    }

    public SortProperty[] getSorting() {
        return sorting;
    }

}
