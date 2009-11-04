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
package org.deegree.commons.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.deegree.commons.utils.CloseableIterator;
import org.deegree.commons.utils.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for easy implementation of {@link CloseableIterator}s that are backed by an SQL result set.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @param <T>
 *            type of the iterated objects
 */
public abstract class ResultSetIterator<T> implements CloseableIterator<T> {

    private static final Logger LOG = LoggerFactory.getLogger( ResultSetIterator.class );

    private boolean currentRowRead = true;

    private final ResultSet rs;

    private final Connection conn;

    private final Statement stmt;

    /**
     * Creates a new {@link ResultSetIterator} instance.
     * 
     * @param rs
     *            result set that the iterator uses to build the elements, must not be <code>null</code>
     * @param conn
     *            connection that was used to obtain the result set, must not be <code>null</code>
     * @param stmt
     *            statement that was used to obtain the result set, must not be <code>null</code>
     */
    protected ResultSetIterator( ResultSet rs, Connection conn, Statement stmt ) {
        this.rs = rs;
        this.conn = conn;
        this.stmt = stmt;
    }

    @Override
    public void close() {
        JDBCUtils.close( rs, stmt, conn, LOG );
    }

    @Override
    public boolean hasNext() {
        if ( !currentRowRead ) {
            return true;
        }
        try {
            if ( rs.next() ) {
                currentRowRead = false;
                return true;
            }
        } catch ( SQLException e ) {
            // try to close everything
            close();
            // wrap as unchecked exception
            throw new RuntimeException( e.getMessage(), e );
        }
        return false;
    }

    @Override
    public T next() {
        if ( !hasNext() ) {
            throw new NoSuchElementException();
        }
        currentRowRead = true;
        T element;
        try {
            element = createElement( rs );
        } catch ( SQLException e ) {
            // try to close everything
            close();
            // wrap as unchecked exception
            throw new RuntimeException( e.getMessage(), e );
        }
        return element;
    }

    /**
     * Invoked to create the next element in the iteration sequence from the {@link ResultSet} (usually from one row).
     * 
     * @param rs
     *            result set that is used to build the element, this is never <code>null</code>
     * @return new element from the iteration sequence
     * @throws SQLException
     *             if the accessing of the result set or element creation fails
     */
    protected abstract T createElement( ResultSet rs )
                            throws SQLException;

    @Override
    public void remove() {
        try {
            rs.deleteRow();
        } catch ( SQLException e ) {
            // try to close everything
            close();
            // wrap as unchecked exception
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public List<T> getAsListAndClose() {
        LinkedList<T> list = new LinkedList<T>();
        while ( hasNext() ) {
            list.add( next() );
        }
        close();
        return list;
    }

    @Override
    public Collection<T> getAsCollectionAndClose( Collection<T> collection ) {
        while ( hasNext() ) {
            collection.add( next() );
        }
        close();
        return collection;
    }
}
