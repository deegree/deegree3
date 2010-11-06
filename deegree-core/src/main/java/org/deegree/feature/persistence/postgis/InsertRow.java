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

import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.ParseException;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class InsertRow {

    private static Logger LOG = LoggerFactory.getLogger( InsertRow.class );

    private final QTableName table;

    private final LinkedHashMap<String, Object> columnsToValues;

    public InsertRow( QTableName table ) {
        this.table = table;
        this.columnsToValues = new LinkedHashMap<String, Object>();
    }

    public void add( String column, Object value ) {
        columnsToValues.put( column.toLowerCase(), value );
    }

    public Object get( String column ) {
        return columnsToValues.get( column.toLowerCase() );
    }

    public int performInsert( Connection conn )
                            throws SQLException {

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Inserting: " + this );
        }

        StringBuilder sql = new StringBuilder( "INSERT INTO " + table + "(" );
        boolean first = true;
        for ( String column : columnsToValues.keySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( column );
        }
        sql.append( ") VALUES(" );
        first = true;
        for ( Entry<String, Object> entry : columnsToValues.entrySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            if ( entry.getValue() instanceof Geometry ) {
                sql.append( "GeomFromWKB(?," );
                // TODO
                sql.append( "-1)" );
            } else {
                sql.append( "?" );
            }
        }
        sql.append( ")" );

        PreparedStatement stmt = conn.prepareStatement( sql.toString(), RETURN_GENERATED_KEYS );
        int columnId = 1;
        for ( Entry<String, Object> entry : columnsToValues.entrySet() ) {
            Object pgValue = entry.getValue();
            if ( entry.getValue() instanceof Geometry ) {
                try {
                    pgValue = WKBWriter.write( (Geometry) entry.getValue() );
                } catch ( ParseException e ) {
                    throw new SQLException( e.getMessage(), e );
                }
            } else {

            }
            stmt.setObject( columnId++, pgValue );
        }
        stmt.execute();

        int internalId = -1;

        ResultSet rs = null;
        try {
            rs = stmt.getGeneratedKeys();
            if ( rs.next() ) {
                internalId = rs.getInt( 1 );
            }
        } finally {
            if ( rs != null ) {
                rs.close();
            }
        }
        stmt.close();

        return internalId;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder( "INSERT INTO " + table + "(" );
        boolean first = true;
        for ( String column : columnsToValues.keySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( column );
        }
        sql.append( ") VALUES(" );
        first = true;
        for ( Entry<String, Object> entry : columnsToValues.entrySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            if ( entry.getValue() instanceof Geometry ) {
                sql.append( "GeomFromWKB(?," );
                // TODO
                sql.append( "-1)" );
            } else {
                sql.append( "'" );
                sql.append( entry.getValue() );
                sql.append( "'" );
            }
        }
        sql.append( ")" );
        return sql.toString();
    }
}