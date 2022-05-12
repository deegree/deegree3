//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.shape;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.legacy.LegacyConnectionProvider;
import org.deegree.feature.persistence.shape.ShapeFeatureStoreProvider.Mapping;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.property.SimplePropertyType;
import org.slf4j.Logger;

/**
 * Copies the dbf contents into the h2 db.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 *
 * @version $Revision: $, $Date: $
 */
class DbfIndexImporter {

    private static final Logger LOG = getLogger( DbfIndexImporter.class );

    private DBFReader dbf;

    private Pair<ArrayList<Pair<float[], Long>>, Boolean> envelopes;

    private ArrayList<String> fields = new ArrayList<>();

    private List<Mapping> mappings;

    private File file;

    private Map<String, Mapping> fieldMap;

    private LegacyConnectionProvider connProvider;

    DbfIndexImporter( DBFReader dbf, File file, Pair<ArrayList<Pair<float[], Long>>, Boolean> envelopes,
                      List<Mapping> mappings ) {
        this.dbf = dbf;
        this.file = file.getAbsoluteFile();
        this.envelopes = envelopes;
        this.mappings = mappings;
    }

    private void createTable( StringBuilder sb ) {
        sb.append( "create table dbf_index (record_number integer,file_index bigint" );
    }

    private Map<String, Mapping> createMappingMap( List<Mapping> mappings ) {
        Map<String, Mapping> fieldMap = null;
        if ( mappings != null ) {
            fieldMap = new HashMap<>();
            for ( Mapping m : mappings ) {
                if ( m.propname != null ) {
                    fieldMap.put( m.propname, m );
                }
            }
        }
        return fieldMap;
    }

    private void appendFields( StringBuilder create ) {
        for ( PropertyType pt : dbf.getFields() ) {
            if ( pt instanceof SimplePropertyType ) {
                SimplePropertyType spt = (SimplePropertyType) pt;
                if ( fieldMap != null && !fieldMap.containsKey( spt.getName().getLocalPart() ) ) {
                    continue;
                }

                create.append( ", " );
                String sqlType = null;
                switch ( spt.getPrimitiveType().getBaseType() ) {
                case BOOLEAN:
                    sqlType = "boolean";
                    break;
                case DATE:
                case DATE_TIME:
                case TIME:
                    sqlType = "timestamp";
                    break;
                case DECIMAL:
                case DOUBLE:
                    sqlType = "double";
                    break;
                case INTEGER:
                    sqlType = "integer";
                    break;
                case STRING:
                    sqlType = "varchar";
                    break;
                }
                String field = pt.getName().getLocalPart().toLowerCase();
                fields.add( field );
                create.append( field + " " + sqlType );
            }
        }
        create.append( ")" );
    }

    private String createInsertStatement( Map<SimplePropertyType, Property> entry ) {
        StringBuilder sb = new StringBuilder();
        StringBuilder qms = new StringBuilder();
        sb.setLength( 0 );
        qms.setLength( 0 );
        qms.append( "?,?" );
        sb.append( "insert into dbf_index (record_number,file_index" );
        for ( SimplePropertyType spt : entry.keySet() ) {
            if ( fieldMap != null && !fieldMap.containsKey( spt.getName().getLocalPart() ) ) {
                continue;
            }
            sb.append( "," );
            qms.append( ",?" );
            sb.append( spt.getName().getLocalPart().toLowerCase() );
        }
        sb.append( ") values (" ).append( qms ).append( ")" );
        return sb.toString();
    }

    private void setValue( PrimitiveValue primVal, SimplePropertyType spt, int idx, PreparedStatement stmt )
                            throws SQLException {
        if ( primVal.getValue() == null ) {
            switch ( spt.getPrimitiveType().getBaseType() ) {
            case BOOLEAN:
                stmt.setNull( idx, Types.BOOLEAN );
                break;
            case DATE:
            case DATE_TIME:
            case TIME:
                stmt.setNull( idx, Types.DATE );
                break;
            case DECIMAL:
            case DOUBLE:
                stmt.setNull( idx, Types.DECIMAL );
                break;
            case INTEGER:
                stmt.setNull( idx, Types.INTEGER );
                break;
            case STRING:
                stmt.setNull( idx, Types.VARCHAR );
                break;
            }
            return;
        }
        switch ( spt.getPrimitiveType().getBaseType() ) {
        case BOOLEAN:
            stmt.setBoolean( idx, (Boolean) primVal.getValue() );
            break;
        case DATE:
        case DATE_TIME:
        case TIME:
            // TODO should this use the corresponding subclass of java.sql.Date?
            stmt.setDate( idx, new java.sql.Date( ( (Date) primVal.getValue() ).getTimeInMilliseconds() ) );
            break;
        case DECIMAL:
        case DOUBLE:
            stmt.setBigDecimal( idx, (BigDecimal) primVal.getValue() );
            break;
        case INTEGER:
            stmt.setInt( idx, ( (BigInteger) primVal.getValue() ).intValue() );
            break;
        case STRING:
            stmt.setString( idx, (String) primVal.getValue() );
            break;
        }
    }

    private void insertRow( Connection conn, Iterator<Pair<float[], Long>> iter, int recNum )
                            throws SQLException, IOException {
        HashMap<SimplePropertyType, Property> entry = dbf.getEntry( recNum );
        PreparedStatement stmt = conn.prepareStatement( createInsertStatement( entry ) );

        int idx = 2;
        stmt.setInt( 1, recNum );
        Pair<float[], Long> p = iter.next();
        stmt.setLong( 2, p.second );
        for ( SimplePropertyType spt : entry.keySet() ) {
            if ( fieldMap != null && !fieldMap.containsKey( spt.getName().getLocalPart() ) ) {
                continue;
            }
            PrimitiveValue primVal = ( (SimpleProperty) entry.get( spt ) ).getValue();
            setValue( primVal, spt, ++idx, stmt );
        }

        stmt.executeUpdate();
        stmt.close();
    }

    private void createIndexes( Connection conn )
                            throws SQLException {
        for ( String field : fields ) {
            if ( fieldMap != null && !fieldMap.get( field ).index ) {
                continue;
            }
            PreparedStatement stmt = conn.prepareStatement( "create index " + field + "_index on dbf_index (" + field
                                                            + ")" );
            stmt.executeUpdate();
            stmt.close();
        }
    }

    private void importDbf( StringBuilder create )
                            throws IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connProvider.getConnection();
            stmt = conn.prepareStatement( create.toString() );
            stmt.executeUpdate();
            stmt.close();

            createIndexes( conn );

            conn.setAutoCommit( false );
            Iterator<Pair<float[], Long>> iter = envelopes.first.iterator();
            for ( int i = 0; i < dbf.size(); ++i ) {
                insertRow( conn, iter, i );
            }

            conn.commit();

        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close( stmt );
            JDBCUtils.close( conn );
        }
    }

    ConnectionProvider createIndex()
                            throws IOException {
        LOG.debug( "Creating h2 db index..." );

        StringBuilder create = new StringBuilder();

        createTable( create );
        fieldMap = createMappingMap( mappings );

        appendFields( create );

        File dbfile = new File( file.toString().substring( 0, file.toString().lastIndexOf( '.' ) ) );

        connProvider = new LegacyConnectionProvider( "jdbc:h2:" + dbfile, "SA", "", false, null );

        if ( new File( dbfile.toString() + ".mv.db" ).exists() ) {
            // TODO proper check for database consistency
            return connProvider;
        }

        importDbf( create );

        LOG.debug( "Done creating h2 db index." );
        return connProvider;
    }

}
