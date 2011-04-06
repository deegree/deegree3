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

import static org.deegree.commons.utils.JDBCUtils.close;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.sql.AbstractSQLFeatureStore;
import org.deegree.feature.persistence.sql.FeatureBuilder;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.expressions.JoinChain;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.deegree.gml.feature.FeatureReference;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Step;
import org.jaxen.expr.TextNodeStep;
import org.jaxen.saxpath.Axis;
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

    private final NamespaceBindings nsBindings;

    private static final QName XSI_NIL = new QName( CommonNamespaces.XSINS, "nil", "xsi" );

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
        this.nsBindings = new NamespaceBindings();
        for ( String prefix : fs.getNamespaceContext().keySet() ) {
            String ns = fs.getNamespaceContext().get( prefix );
            nsBindings.addNamespace( prefix, ns );
        }
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
                addSelectColumns( mapping, columns, true );
            }
        }
        return columns;
    }

    private List<String> getSelectColumns( Mapping mapping ) {
        List<String> columns = new ArrayList<String>();
        addSelectColumns( mapping, columns, false );
        return columns;
    }

    private void addSelectColumns( Mapping mapping, List<String> columns, boolean handleJoin ) {

        JoinChain jc = mapping.getJoinedTable();
        if ( handleJoin && jc != null ) {
            columns.add( jc.getFields().get( 0 ).getColumn() );
        } else {
            if ( mapping instanceof PrimitiveMapping ) {
                PrimitiveMapping pm = (PrimitiveMapping) mapping;
                MappingExpression column = pm.getMapping();
                if ( column instanceof DBField ) {
                    columns.add( ( (DBField) column ).getColumn() );
                }
            } else if ( mapping instanceof GeometryMapping ) {
                GeometryMapping gm = (GeometryMapping) mapping;
                MappingExpression column = gm.getMapping();
                if ( column instanceof DBField ) {
                    // TODO
                    columns.add( "AsBinary(" + ( (DBField) column ).getColumn() + ")" );
                }
            } else if ( mapping instanceof FeatureMapping ) {
                FeatureMapping fm = (FeatureMapping) mapping;
                MappingExpression column = fm.getMapping();
                if ( column instanceof DBField ) {
                    columns.add( ( (DBField) column ).getColumn() );
                }
            } else if ( mapping instanceof CompoundMapping ) {
                CompoundMapping cm = (CompoundMapping) mapping;
                for ( Mapping particle : cm.getParticles() ) {
                    addSelectColumns( particle, columns, true );
                }
            } else {
                LOG.warn( "Mappings of type '" + mapping.getClass() + "' are not handled yet." );
            }
        }
    }

    @Override
    public Feature buildFeature( ResultSet rs )
                            throws SQLException {

        String gmlId = "" + rs.getObject( 1 );
        Feature feature = (Feature) fs.getCache().get( gmlId );
        if ( feature == null ) {
            LOG.debug( "Cache miss. Recreating feature '" + gmlId + "' from db (relational mode)." );
            List<Property> props = new ArrayList<Property>();
            int i = 2;
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                Mapping propMapping = ftMapping.getMapping( pt.getName() );
                if ( propMapping != null ) {
                    i = addProperties( props, pt, propMapping, rs, i, gmlId );
                }
            }
            feature = ft.newFeature( gmlId, props, null, null );
            fs.getCache().add( feature );
        } else {
            LOG.debug( "Cache hit." );
        }
        return feature;
    }

    private int addProperties( List<Property> props, PropertyType pt, Mapping propMapping, ResultSet rs, int i,
                               Object pk )
                            throws SQLException {

        Pair<List<Pair<TypedObjectNode, Boolean>>, Integer> pair = buildParticles( propMapping, rs, i, pk );
        for ( Pair<TypedObjectNode, Boolean> value : pair.first ) {
            if ( value.first instanceof GenericXMLElementContent ) {
                GenericXMLElementContent ec = (GenericXMLElementContent) value.first;
                ec.getAttributes().remove( XSI_NIL );
            }
            props.add( new GenericProperty( pt, pt.getName(), value.first, value.second ) );
        }
        return pair.second;
    }

    private Pair<List<Pair<TypedObjectNode, Boolean>>, Integer> buildParticles( Mapping mapping, ResultSet rs, int i,
                                                                                Object pk )
                            throws SQLException {

        if ( mapping.getJoinedTable() != null ) {
            List<Pair<TypedObjectNode, Boolean>> values = new ArrayList<Pair<TypedObjectNode, Boolean>>();
            ResultSet rs2 = null;
            try {
                rs2 = getJoinedResultSet( mapping.getJoinedTable(), mapping, pk );
                while ( rs2.next() ) {
                    Object newPk = rs2.getObject( 1 );
                    values.addAll( buildParticle( mapping, rs2, 2, newPk ).first );
                }
            } finally {
                if ( rs2 != null ) {
                    rs2.close();
                }
            }
            return new Pair<List<Pair<TypedObjectNode, Boolean>>, Integer>( values, ++i );
        }
        return buildParticle( mapping, rs, i, pk );
    }

    private Pair<List<Pair<TypedObjectNode, Boolean>>, Integer> buildParticle( Mapping mapping, ResultSet rs, int i,
                                                                               Object pk )
                            throws SQLException {

        List<Pair<TypedObjectNode, Boolean>> values = new ArrayList<Pair<TypedObjectNode, Boolean>>();
        boolean isNil = false;
        if ( mapping instanceof PrimitiveMapping ) {
            PrimitiveMapping pm = (PrimitiveMapping) mapping;
            MappingExpression me = pm.getMapping();
            if ( me instanceof DBField ) {
                Object value = rs.getObject( i++ );
                if ( value != null ) {
                    values.add( new Pair<TypedObjectNode, Boolean>( new PrimitiveValue( value, pm.getType() ), isNil ) );
                }
            }
        } else if ( mapping instanceof GeometryMapping ) {
            GeometryMapping pm = (GeometryMapping) mapping;
            MappingExpression me = pm.getMapping();
            if ( me instanceof DBField ) {
                byte[] wkb = rs.getBytes( i++ );
                if ( wkb != null ) {
                    try {
                        Geometry geom = WKBReader.read( wkb, pm.getCRS() );
                        values.add( new Pair<TypedObjectNode, Boolean>( geom, isNil ) );
                    } catch ( ParseException e ) {
                        throw new SQLException( "Error parsing WKB from database: " + e.getMessage(), e );
                    }
                }
            }
        } else if ( mapping instanceof FeatureMapping ) {
            Object value = rs.getObject( i++ );
            if ( value != null ) {
                // TODO
                String ref = "#" + value;
                values.add( new Pair<TypedObjectNode, Boolean>( new FeatureReference( fs.getResolver(), ref, null ),
                                                                isNil ) );
            } else {
                values.add( new Pair<TypedObjectNode, Boolean>( null, isNil ) );
            }
        } else if ( mapping instanceof CompoundMapping ) {
            CompoundMapping cm = (CompoundMapping) mapping;

            Map<QName, PrimitiveValue> attrs = new HashMap<QName, PrimitiveValue>();
            if ( isNil ) {
                attrs.put( XSI_NIL, new PrimitiveValue( isNil ) );
            }

            List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();

            for ( Mapping particleMapping : cm.getParticles() ) {

                Pair<List<Pair<TypedObjectNode, Boolean>>, Integer> particleValues = buildParticles( particleMapping,
                                                                                                     rs, i, pk );
                i = particleValues.second;

                Expr xpath = particleMapping.getPath().getAsXPath();
                if ( xpath instanceof LocationPath ) {
                    LocationPath lp = (LocationPath) xpath;
                    if ( lp.getSteps().size() != 1 ) {
                        LOG.warn( "Unhandled location path: '" + particleMapping.getPath()
                                  + "'. Only single step paths are handled." );
                        continue;
                    }
                    if ( lp.isAbsolute() ) {
                        LOG.warn( "Unhandled location path: '" + particleMapping.getPath()
                                  + "'. Only relative paths are handled." );
                        continue;
                    }
                    Step step = (Step) lp.getSteps().get( 0 );
                    if ( !step.getPredicates().isEmpty() ) {
                        LOG.warn( "Unhandled location path: '" + particleMapping.getPath()
                                  + "'. Only unpredicated steps are handled." );
                        continue;
                    }
                    if ( step instanceof TextNodeStep ) {
                        for ( Pair<TypedObjectNode, Boolean> particleValue : particleValues.first ) {
                            children.add( particleValue.first );
                        }
                    } else if ( step instanceof NameStep ) {
                        NameStep ns = (NameStep) step;
                        QName name = getQName( ns );
                        if ( step.getAxis() == Axis.ATTRIBUTE ) {
                            for ( Pair<TypedObjectNode, Boolean> particleValue : particleValues.first ) {
                                if ( particleValue.first instanceof PrimitiveValue ) {
                                    attrs.put( name, (PrimitiveValue) particleValue.first );
                                } else {
                                    LOG.warn( "Value not suitable for attribute." );
                                }
                            }
                        } else if ( step.getAxis() == Axis.CHILD ) {
                            for ( Pair<TypedObjectNode, Boolean> particleValue : particleValues.first ) {
                                if ( particleValue.first != null ) {
                                    XSTypeDefinition childType = null;
                                    GenericXMLElement child = new GenericXMLElement(
                                                                                     name,
                                                                                     childType,
                                                                                     Collections.<QName, PrimitiveValue> emptyMap(),
                                                                                     Collections.singletonList( particleValue.first ) );
                                    children.add( child );
                                }
                            }
                        } else {
                            LOG.warn( "Unhandled path: '" + particleMapping.getPath() + "'" );
                        }
                    } else {
                        LOG.warn( "Unhandled path: '" + particleMapping.getPath() + "'" );
                    }
                } else {
                    LOG.warn( "Unhandled path: '" + particleMapping.getPath() + "'" );
                }
            }

            // TODO
            XSTypeDefinition xsType = null;
            if ( ( !attrs.isEmpty() ) || !children.isEmpty() ) {
                values.add( new Pair<TypedObjectNode, Boolean>(
                                                                new GenericXMLElementContent( xsType, attrs, children ),
                                                                isNil ) );
            } else if ( isNil ) {
                values.add( new Pair<TypedObjectNode, Boolean>(
                                                                new GenericXMLElementContent( xsType, attrs, children ),
                                                                isNil ) );
            }
        } else {
            LOG.warn( "Handling of '" + mapping.getClass() + "' mappings is not implemented yet." );
        }
        return new Pair<List<Pair<TypedObjectNode, Boolean>>, Integer>( values, i );
    }

    private ResultSet getJoinedResultSet( JoinChain jc, Mapping mapping, Object pk )
                            throws SQLException {

        List<String> columns = getSelectColumns( mapping );

        StringBuilder sql = new StringBuilder( "SELECT " );
        // TODO
        sql.append( "id" );
        for ( String column : columns ) {
            sql.append( ',' );
            sql.append( column );
        }
        sql.append( " FROM " );
        sql.append( jc.getFields().get( 1 ).getTable() );
        sql.append( " WHERE " );
        sql.append( jc.getFields().get( 1 ).getColumn() );
        sql.append( " = ?" );
        LOG.warn( "SQL: {}", sql );

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            long begin = System.currentTimeMillis();
            stmt = conn.prepareStatement( sql.toString() );
            LOG.debug( "Preparing subsequent SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            stmt.setObject( 1, pk );
            begin = System.currentTimeMillis();
            rs = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
        } catch ( Throwable t ) {
            close( rs, stmt, null, LOG );
            String msg = "Error performing subsequent SELECT: " + t.getMessage();
            LOG.error( msg, t );
            throw new SQLException( msg, t );
        }
        return rs;
    }

    private QName getQName( NameStep step ) {
        String prefix = step.getPrefix();
        QName qName;
        if ( prefix.isEmpty() ) {
            qName = new QName( step.getLocalName() );
        } else {
            String ns = nsBindings.translateNamespacePrefixToUri( prefix );
            qName = new QName( ns, step.getLocalName(), prefix );
        }
        return qName;
    }
}