//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.ogcwebservices.wfs.operation.transaction;

import java.util.List;

import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;

/**
 * Encapsulates a TransactionResponse element according to WFS Specification OGC 04-094 (#12.3 Pg.72).
 * <p>
 * Because deegree supports atomic transactions, there is no need for the optional TransactionResults child element.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TransactionResponse extends DefaultOGCWebServiceResponse {

    private int totalInserted;

    private int totalUpdated;

    private int totalDeleted;

    private List<InsertResults> insertResults;

    private List<Exception> exceptions;

    /**
     * Creates a new <code>TransactionResponse</code> instance from the given parameters.
     *
     * @param transaction
     *            request caused a response
     * @param totalInserted
     * @param totalUpdated
     * @param totalDeleted
     * @param insertResults
     */
    public TransactionResponse( Transaction transaction, int totalInserted, int totalUpdated, int totalDeleted,
                                List<InsertResults> insertResults ) {
        super( transaction );
        this.totalInserted = totalInserted;
        this.totalUpdated = totalUpdated;
        this.totalDeleted = totalDeleted;
        this.insertResults = insertResults;
    }

    /**
     * @param transaction
     * @param totalInserted
     * @param totalUpdated
     * @param totalDeleted
     * @param insertResults
     * @param exceptions
     */
    public TransactionResponse( Transaction transaction, int totalInserted, int totalUpdated, int totalDeleted,
                                List<InsertResults> insertResults, List<Exception> exceptions ) {
        this( transaction, totalInserted, totalUpdated, totalDeleted, insertResults );
        this.exceptions = exceptions;
    }

    /**
     * Returns the number of features that have been deleted in the transaction.
     *
     * @return number of features that have been deleted in the transaction.
     */
    public int getTotalDeleted() {
        return totalDeleted;
    }

    /**
     * Returns the number of features that have been inserted in the transaction.
     *
     * @return number of features that have been inserted in the transaction.
     */
    public int getTotalInserted() {
        return totalInserted;
    }

    /**
     * Returns the number of features that have been updated in the transaction.
     *
     * @return number of features that have been updated in the transaction.
     */
    public int getTotalUpdated() {
        return totalUpdated;
    }

    /**
     * Returns the insert results, i.e. the feature ids of the features that have been inserted for every insert
     * operation of the transaction.
     *
     * @return the insert results.
     */
    public List<InsertResults> getInsertResults() {
        return insertResults;
    }

    /**
     * @return a list of exceptions that occurred during the execution of the transaction operations.
     */
    public List<Exception> getExceptions() {
        return exceptions;
    }

}
