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
package org.deegree.feature.persistence.sql.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.sql.AbstractSQLFeatureStore;
import org.deegree.feature.persistence.sql.FeatureBuilder;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.expressions.JoinChain;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.deegree.gml.feature.FeatureReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.ParseException;

/**
 * Builds {@link Feature} instances from SQL result set rows (relational mode).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureBuilderRelational implements FeatureBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureBuilderRelational.class );

    private final AbstractSQLFeatureStore fs;

    private final FeatureType ft;

    private final FeatureTypeMapping ftMapping;

    private final Connection conn;

    /**
     * Creates a new {@link FeatureBuilderRelational} instance.
     * 
     * @param fs
     *            feature store, must not be <code>null</code>
     * @param ft
     *            feature type, must not be <code>null</code>
     * @param ftMapping
     *            feature type mapping, must not be <code>null</code>
     * @param conn
     *            JDBC connection (used for performing subsequent SELECTs), must not be <code>null</code>
     */
    public FeatureBuilderRelational( AbstractSQLFeatureStore fs, FeatureType ft, FeatureTypeMapping ftMapping,
                                     Connection conn ) {
        this.fs = fs;
        this.ft = ft;
        this.ftMapping = ftMapping;
        this.conn = conn;
    }

    @Override
    public List<String> getSelectColumns() {
        List<String> columns = new ArrayList<String>();
        columns.add( ftMapping.getFidMapping().getColumn() );
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            Mapping mapping = ftMapping.getMapping( pt.getName() );
            if ( mapping == null ) {
                LOG.warn( "No mapping for property '" + pt.getName() + "' -- omitting from SELECT list." );
            } else {
                addSelectColumns( mapping, columns );
            }
        }
        return columns;
    }

    private void addSelectColumns( Mapping mapping, List<String> columns ) {

        JoinChain jc = mapping.getJoinedTable();
        if ( jc != null ) {
            columns.add( jc.getFields().get( 0 ).getColumn() );
        } else {
            if ( mapping instanceof PrimitiveMapping ) {
                PrimitiveMapping pm = (PrimitiveMapping) mapping;
                MappingExpression column = pm.getMapping();
                if ( column instanceof DBField ) {
                    columns.add( ( (DBField) column ).getColumn() );
                } else {
                    LOG.warn( "Skipping mapping '" + column + "'. Not mapped to a column." );
                }
            } else if ( mapping instanceof GeometryMapping ) {
                GeometryMapping gm = (GeometryMapping) mapping;
                MappingExpression column = gm.getMapping();
                if ( column instanceof DBField ) {
                    // TODO
                    columns.add( "AsBinary(" + ( (DBField) column ).getColumn() + ")" );
                } else {
                    LOG.warn( "Skipping mapping '" + column + "'. Not mapped to a column." );
                }
            } else if ( mapping instanceof CompoundMapping ) {
                CompoundMapping cm = (CompoundMapping) mapping;
                for ( Mapping particle : cm.getParticles() ) {
                    addSelectColumns( particle, columns );
                }
            } else {
                LOG.warn( "Mappings of type '" + mapping.getClass() + "' are not handled yet." );
            }
        }
    }

    @Override
    public Feature buildFeature( ResultSet rs )
                            throws SQLException {

        String gmlId = ftMapping.getFidMapping().getPrefix() + rs.getObject( 1 );
        Feature feature = (Feature) fs.getCache().get( gmlId );
        if ( feature == null ) {
            LOG.debug( "Cache miss. Recreating feature '" + gmlId + "' from db (relational mode)." );
            List<Property> props = new ArrayList<Property>();
            int i = 2;
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                Mapping propMapping = ftMapping.getMapping( pt.getName() );
                if ( propMapping != null ) {
                    i = addProperties( props, pt, propMapping, rs, i );
                }
            }
            feature = ft.newFeature( gmlId, props, null, null );
            fs.getCache().add( feature );
        } else {
            LOG.debug( "Cache hit." );
        }
        return feature;
    }

    private int addProperties( List<Property> props, PropertyType pt, Mapping propMapping, ResultSet rs, int i )
                            throws SQLException {

        Pair<List<TypedObjectNode>, Integer> pair = buildParticles( propMapping, rs, i );
        for ( TypedObjectNode value : pair.first ) {
            props.add( new GenericProperty( pt, value ) );
        }
        return pair.second;
    }

    private Pair<List<TypedObjectNode>, Integer> buildParticles( Mapping mapping, ResultSet rs, int i )
                            throws SQLException {

        LOG.warn( "JoinChain handling not implemented yet." );

        List<TypedObjectNode> values = new ArrayList<TypedObjectNode>();
        if ( mapping instanceof PrimitiveMapping ) {
            PrimitiveMapping pm = (PrimitiveMapping) mapping;
            MappingExpression me = pm.getMapping();
            if ( me instanceof DBField ) {
                Object value = rs.getObject( i++ );
                if ( value != null ) {
                    values.add( new PrimitiveValue( value, pm.getType() ) );
                }
            } else {
                LOG.warn( "Skipping." );
            }
        } else if ( mapping instanceof GeometryMapping ) {
            GeometryMapping pm = (GeometryMapping) mapping;
            MappingExpression me = pm.getMapping();
            if ( me instanceof DBField ) {
                byte[] wkb = rs.getBytes( i++ );
                if ( wkb != null ) {
                    try {
                        Geometry geom = WKBReader.read( wkb, pm.getCRS() );
                        values.add( geom );
                    } catch ( ParseException e ) {
                        throw new SQLException( "Error parsing WKB from database: " + e.getMessage(), e );
                    }
                }
            } else {
                LOG.warn( "Skipping." );
            }
        } else if ( mapping instanceof CompoundMapping ) {
            // TODO
        } else {
            LOG.warn( "Handling of '" + mapping.getClass() + "' mappings is not implemented yet." );
        }
        return new Pair<List<TypedObjectNode>, Integer>( values, i );
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
                GeometryMapping mapping = (GeometryMapping) ftMapping.getMapping( pt.getName() );
                while ( rs2.next() ) {
                    byte[] wkb = rs2.getBytes( 1 );
                    if ( wkb != null ) {
                        try {
                            Geometry geom = WKBReader.read( wkb, mapping.getCRS() );
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
            PrimitiveValue value = null;
            value = SQLValueMangler.sqlToInternal( rs, rsIdx, ( (SimplePropertyType) pt ).getPrimitiveType() );
            if ( value != null ) {
                props.add( new GenericProperty( pt, value ) );
            }
        } else if ( pt instanceof GeometryPropertyType ) {
            GeometryMapping mapping = (GeometryMapping) ftMapping.getMapping( pt.getName() );
            byte[] wkb = rs.getBytes( rsIdx );
            if ( wkb != null ) {
                try {
                    Geometry geom = WKBReader.read( wkb, mapping.getCRS() );
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