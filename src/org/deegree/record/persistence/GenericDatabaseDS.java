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
package org.deegree.record.persistence;

import java.io.Writer;
import java.util.Set;

import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;

/**
 * This class holds all the necessary information that is needed for the database request. It is an abstraction to a
 * specific database export. <br/>
 * The request itself is encapsulated in the expression{@link Writer}.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GenericDatabaseDS {

    private Writer expressionWriter;

    private ResultType resultType;

    private SetOfReturnableElements setOfReturnableElements;

    private int maxRecords;
    
    private int startPosition;

    private Set<String> table;

    private Set<String> column;

    public GenericDatabaseDS( Writer expressionWriter, ResultType resultType,
                              SetOfReturnableElements setOfReturnableElements, int maxRecords, int startPosition, Set<String> table,
                              Set<String> column ) {
        this.expressionWriter = expressionWriter;
        this.resultType = resultType;
        this.setOfReturnableElements = setOfReturnableElements;
        this.maxRecords = maxRecords;
        this.startPosition = startPosition;
        this.table = table;
        this.column = column;

    }

    /**
     * @return the expressionWriter
     */
    public Writer getExpressionWriter() {
        return expressionWriter;
    }

    /**
     * @param expressionWriter
     *            the expressionWriter to set
     */
    public void setExpressionWriter( Writer expressionWriter ) {
        this.expressionWriter = expressionWriter;
    }

    /**
     * @return the resultType
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @param resultType
     *            the resultType to set
     */
    public void setResultType( ResultType resultType ) {
        this.resultType = resultType;
    }

    /**
     * @return the setOfReturnableElements
     */
    public SetOfReturnableElements getSetOfReturnableElements() {
        return setOfReturnableElements;
    }

    /**
     * @param setOfReturnableElements
     *            the setOfReturnableElements to set
     */
    public void setSetOfReturnableElements( SetOfReturnableElements setOfReturnableElements ) {
        this.setOfReturnableElements = setOfReturnableElements;
    }

    /**
     * @return the maxRecords
     */
    public int getMaxRecords() {
        return maxRecords;
    }

    /**
     * @param maxRecords
     *            the maxRecords to set
     */
    public void setMaxRecords( int maxRecords ) {
        this.maxRecords = maxRecords;
    }
    
    

    /**
     * @return the startPosition
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * @param startPosition the startPosition to set
     */
    public void setStartPosition( int startPosition ) {
        this.startPosition = startPosition;
    }

    /**
     * @return the table
     */
    public Set<String> getTable() {
        return table;
    }

    /**
     * @param table
     *            the table to set
     */
    public void setTable( Set<String> table ) {
        this.table = table;
    }

    /**
     * @return the column
     */
    public Set<String> getColumn() {
        return column;
    }

    /**
     * @param column
     *            the column to set
     */
    public void setColumn( Set<String> column ) {
        this.column = column;
    }

}
