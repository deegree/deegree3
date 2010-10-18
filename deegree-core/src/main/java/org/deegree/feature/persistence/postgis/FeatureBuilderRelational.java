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
package org.deegree.feature.persistence.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.JoinChain;
import org.deegree.feature.persistence.mapping.MappingExpression;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.deegree.gml.feature.FeatureReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.ParseException;

/**
 * Builds {@link Feature} instances from SQL result sets for the {@link PostGISFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class FeatureBuilderRelational implements FeatureBuilder {

    static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStore.class );

    private PostGISFeatureStore fs;

    private FeatureType ft;

    private FeatureTypeMapping ftMapping;

    private Connection conn;

    FeatureBuilderRelational( PostGISFeatureStore fs, FeatureType ft, FeatureTypeMapping ftMapping, Connection conn ) {
        this.fs = fs;
        this.ft = ft;
        this.ftMapping = ftMapping;
        this.conn = conn;
    }

    /**
     * Builds a {@link Feature} instance from the current row of the given {@link ResultSet}.
     * <p>
     * The columns in the {@link ResultSet} <b>must</b> correspond to the property mappings of the associated feature
     * type.
     * </p>
     * 
     * @param rs
     *            PostGIS result set, must not be <code>null</code>
     * @return created {@link Feature} instance, never <code>null</code>
     * @throws SQLException
     */
    @Override
    public Feature buildFeature( ResultSet rs )
                            throws SQLException {

        String gmlId = ft.getName().getLocalPart().toUpperCase() + "_" + rs.getString( 1 );
        Feature feature = (Feature) fs.getCache().get( gmlId );
        if ( feature == null ) {
            LOG.debug( "Cache miss. Recreating feature '" + gmlId + "' from relational model." );
            List<Property> props = new ArrayList<Property>();
            int i = 2;
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                // if it is mappable, it has been SELECTed by contract
                MappingExpression propMapping = ftMapping.getMapping( pt.getName() );
                if ( propMapping != null ) {
                    if ( propMapping instanceof JoinChain ) {
                        addProperties( conn, props, pt, (JoinChain) propMapping, rs, i );
                    } else {
                        addProperties( props, pt, rs, i );
                    }
                    i++;
                }
            }
            feature = ft.newFeature( gmlId, props, null );
            fs.getCache().add( feature );
        } else {
            LOG.debug( "Cache hit." );
        }
        return feature;
    }

    private void addProperties( Connection conn, List<Property> props, PropertyType pt, JoinChain propMapping,
                                ResultSet rs, int rsIdx )
                            throws SQLException {

        List<DBField> fields = propMapping.getFields();

        // generate table aliases for all involved tables
        List<String> tableAliases = new ArrayList<String>();
        Map<DBField, String> dbFieldToAlias = new HashMap<DBField, String>();
        for ( int i = 0; i < propMapping.getFields().size(); i++ ) {
            String tableAlias = "X" + ( ( ( i + 1 ) / 2 ) + 1 );
            dbFieldToAlias.put( propMapping.getFields().get( i ), tableAlias );
            if ( i % 2 == 0 ) {
                tableAliases.add( tableAlias );
            }
        }

        StringBuilder sql = new StringBuilder( "SELECT " );
        sql.append( dbFieldToAlias.get( fields.get( fields.size() - 1 ) ) );
        sql.append( "." );
        sql.append( fields.get( fields.size() - 1 ).getColumn() );
        sql.append( " FROM " );
        sql.append( ftMapping.getFtTable() );
        sql.append( " AS " );
        sql.append( dbFieldToAlias.get( fields.get( 0 ) ) );
        for ( int i = 1; i < fields.size(); i += 2 ) {
            DBField field = fields.get( i );
            sql.append( " LEFT OUTER JOIN " );
            sql.append( field.getTable() );
            sql.append( " AS " );
            sql.append( dbFieldToAlias.get( field ) );

            DBField pre = fields.get( i - 1 );
            sql.append( " ON " );
            sql.append( dbFieldToAlias.get( pre ) );
            sql.append( "." );
            sql.append( pre.getColumn() );
            sql.append( "=" );
            sql.append( dbFieldToAlias.get( field ) );
            sql.append( "." );
            sql.append( field.getColumn() );
        }

        sql.append( " WHERE " );
        sql.append( tableAliases.get( 0 ) );
        sql.append( "." );
        sql.append( fields.get( 0 ).getColumn() );
        sql.append( "=?" );

        PreparedStatement stmt = null;
        ResultSet rs2 = null;

        try {
            LOG.debug( "Preparing SELECT: " + sql );
            stmt = conn.prepareStatement( sql.toString() );

            // TODO explicit SQL type handling!?
            stmt.setObject( 1, rs.getObject( 1 ) );

            rs2 = stmt.executeQuery();

            if ( pt instanceof SimplePropertyType ) {
                while ( rs2.next() ) {
                    String value = rs2.getString( 1 );
                    if ( value != null ) {
                        PrimitiveValue pv = new PrimitiveValue( value, ( (SimplePropertyType) pt ).getPrimitiveType() );
                        props.add( new GenericProperty( pt, pv ) );
                    }
                }
            } else if ( pt instanceof GeometryPropertyType ) {
                while ( rs2.next() ) {
                    byte[] wkb = rs2.getBytes( 1 );
                    if ( wkb != null ) {
                        try {
                            Geometry geom = WKBReader.read( wkb );
                            geom.setCoordinateSystem( fs.getStorageSRS() );
                            props.add( new GenericProperty( pt, geom ) );
                        } catch ( ParseException e ) {
                            throw new SQLException( "Error parsing WKB from PostGIS: " + e.getMessage(), e );
                        }
                    }
                }
            } else if ( pt instanceof FeaturePropertyType ) {
                while ( rs2.next() ) {
                    String subFid = rs2.getString( 1 );
                    if ( subFid != null ) {
                        QName valueFtName = ( (FeaturePropertyType) pt ).getFTName();
                        if ( valueFtName != null ) {
                            subFid = valueFtName.getLocalPart().toUpperCase() + "_" + subFid;
                        }
                        String uri = "#" + subFid;
                        FeatureReference ref = new FeatureReference( fs.getResolver(), uri, null );
                        props.add( new GenericProperty( pt, ref ) );
                    }
                }
            } else {
                LOG.warn( "Skipping property '" + pt.getName() + "' -- type '" + pt.getClass()
                          + "' not handled in PostGISFeatureStore." );
            }
        } finally {
            JDBCUtils.close( rs2, stmt, null, LOG );
        }
    }

    private void addProperties( List<Property> props, PropertyType pt, ResultSet rs, int rsIdx )
                            throws SQLException {

        if ( pt instanceof SimplePropertyType ) {
            String value = rs.getString( rsIdx );
            if ( value != null ) {
                PrimitiveValue pv = new PrimitiveValue( value, ( (SimplePropertyType) pt ).getPrimitiveType() );
                props.add( new GenericProperty( pt, pv ) );
            }
        } else if ( pt instanceof GeometryPropertyType ) {
            byte[] wkb = rs.getBytes( rsIdx );
            if ( wkb != null ) {
                try {
                    Geometry geom = WKBReader.read( wkb );
                    geom.setCoordinateSystem( fs.getStorageSRS() );
                    props.add( new GenericProperty( pt, geom ) );
                } catch ( ParseException e ) {
                    throw new SQLException( "Error parsing WKB from PostGIS: " + e.getMessage(), e );
                }
            }
        } else if ( pt instanceof FeaturePropertyType ) {
            String subFid = rs.getString( rsIdx );
            if ( subFid != null ) {
                QName valueFtName = ( (FeaturePropertyType) pt ).getFTName();
                if ( valueFtName != null ) {
                    subFid = valueFtName.getLocalPart().toUpperCase() + "_" + subFid;
                }
                String uri = "#" + subFid;
                FeatureReference ref = new FeatureReference( fs.getResolver(), uri, null );
                props.add( new GenericProperty( pt, ref ) );
            }
        } else {
            LOG.warn( "Skipping property '" + pt.getName() + "' -- type '" + pt.getClass()
                      + "' not handled in PostGISFeatureStore." );
        }
    }
}
