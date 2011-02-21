//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/src/main/java/org/deegree/feature/persistence/postgis/PostGISFeatureStoreTransaction.java $
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
package org.deegree.feature.persistence.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.deegree.commons.jdbc.QTableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates columns and values for inserting one row into a database table.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class InsertRow {

    private static Logger LOG = LoggerFactory.getLogger( InsertRow.class );

    private final QTableName table;

    private final LinkedHashMap<String, String> columnToLiteral = new LinkedHashMap<String, String>();

    private final LinkedHashMap<String, Object> columnToObject = new LinkedHashMap<String, Object>();

    private final List<InsertRow> children = new ArrayList<InsertRow>();

    public InsertRow( QTableName table ) {
        this.table = table;
    }

    public InsertRow addChildRow( QTableName table ) {
        InsertRow child = new InsertRow( table );
        children.add( child );
        return child;
    }

    public List<InsertRow> getChildren() {
        return children;
    }

    public void addLiteralValue( String column, String literal ) {
        columnToLiteral.put( column.toLowerCase(), literal );
    }

    public void addPreparedArgument( String column, Object value ) {
        addPreparedArgument( column, value, "?" );
    }

    public void addPreparedArgument( String column, Object value, String literal ) {
        columnToLiteral.put( column.toLowerCase(), literal );
        columnToObject.put( column.toLowerCase(), value );
    }

    public Collection<String> getColumns() {
        return columnToLiteral.keySet();
    }

    public Object get( String column ) {
        return columnToObject.get( column.toLowerCase() );
    }

    public String getInsert() {
        StringBuilder sql = new StringBuilder( "INSERT INTO " + table + "(" );
        boolean first = true;
        for ( String column : columnToLiteral.keySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( column );
        }
        sql.append( ") VALUES(" );
        first = true;
        for ( Entry<String, String> entry : columnToLiteral.entrySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( entry.getValue() );
        }
        sql.append( ")" );
        return sql.toString();
    }

    public int performInsert( Connection conn )
                            throws SQLException {

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Inserting: " + this );
        }

        String sql = getInsert();
        PreparedStatement stmt = conn.prepareStatement( sql );
        int columnId = 1;
        for ( Entry<String, Object> entry : columnToObject.entrySet() ) {
            LOG.debug( "- Argument " + columnId + " = " + entry.getValue() + " (" + entry.getValue().getClass() + ")" );
            stmt.setObject( columnId++, entry.getValue() );
        }
        stmt.execute();

        int internalId = -1;

        // ResultSet rs = null;
        // try {
        // rs = stmt.getGeneratedKeys();
        // if ( rs.next() ) {
        // internalId = rs.getInt( 1 );
        // }
        // } finally {
        // if ( rs != null ) {
        // rs.close();
        // }
        // }
        stmt.close();

        return internalId;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder( "INSERT INTO " + table + "(" );
        boolean first = true;
        for ( String column : columnToLiteral.keySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( column );
        }
        sql.append( ") VALUES(" );
        first = true;
        for ( Entry<String, String> entry : columnToLiteral.entrySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( entry.getValue() );
        }
        sql.append( ")" );
        return sql.toString();
    }
}