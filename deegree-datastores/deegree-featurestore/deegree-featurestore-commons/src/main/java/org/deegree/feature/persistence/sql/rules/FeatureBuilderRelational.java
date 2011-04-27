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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.sql.AbstractSQLFeatureStore;
import org.deegree.feature.persistence.sql.FeatureBuilder;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.geometry.io.WKBReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.FeatureReference;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.Predicate;
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

    private final GMLVersion gmlVersion;

    private final LinkedHashMap<String, Integer> colToRsIdx = new LinkedHashMap<String, Integer>();

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
        if ( ft.getSchema().getXSModel() != null ) {
            this.gmlVersion = ft.getSchema().getXSModel().getVersion();
        } else {
            this.gmlVersion = GMLVersion.GML_32;
        }
    }

    @Override
    public List<String> getInitialSelectColumns() {
        for ( Pair<String, BaseType> fidColumn : ftMapping.getFidMapping().getColumns() ) {
            addColumn( colToRsIdx, fidColumn.first );
        }
        for ( Mapping mapping : ftMapping.getMappings() ) {
            addSelectColumns( mapping, colToRsIdx, true );
        }
        LOG.debug( "Initial select columns: " + colToRsIdx );
        return new ArrayList<String>( colToRsIdx.keySet() );
    }

    private void addColumn( LinkedHashMap<String, Integer> colToRsIdx, String column ) {
        if ( !colToRsIdx.containsKey( column ) ) {
            colToRsIdx.put( column, colToRsIdx.size() + 1 );
        }
    }

    private LinkedHashMap<String, Integer> getSubsequentSelectColumns( Mapping mapping ) {
        LinkedHashMap<String, Integer> colToRsIdx = new LinkedHashMap<String, Integer>();
        addSelectColumns( mapping, colToRsIdx, false );
        return colToRsIdx;
    }

    private void addSelectColumns( Mapping mapping, LinkedHashMap<String, Integer> colToRsIdx, boolean initial ) {

        List<TableJoin> jc = mapping.getJoinedTable();
        if ( jc != null && initial ) {
            if ( initial ) {
                for ( String column : jc.get( 0 ).getFromColumns() ) {
                    addColumn( colToRsIdx, column );
                }
            }
        } else {
            if ( mapping instanceof PrimitiveMapping ) {
                PrimitiveMapping pm = (PrimitiveMapping) mapping;
                MappingExpression column = pm.getMapping();
                if ( column instanceof DBField ) {
                    addColumn( colToRsIdx, ( (DBField) column ).getColumn() );
                } else {
                    LOG.info( "Omitting mapping '" + mapping + "' from SELECT list. Not mapped to column.'" );
                }
            } else if ( mapping instanceof GeometryMapping ) {
                GeometryMapping gm = (GeometryMapping) mapping;
                MappingExpression column = gm.getMapping();
                if ( column instanceof DBField ) {
                    addColumn( colToRsIdx, fs.getSQLValueMapper().selectGeometry( ( (DBField) column ).getColumn() ) );
                }
            } else if ( mapping instanceof FeatureMapping ) {
                FeatureMapping fm = (FeatureMapping) mapping;
                MappingExpression column = fm.getMapping();
                if ( column instanceof DBField ) {
                    addColumn( colToRsIdx, ( (DBField) column ).getColumn() );
                }
                column = fm.getHrefMapping();
                if ( column instanceof DBField ) {
                    addColumn( colToRsIdx, ( (DBField) column ).getColumn() );
                }
            } else if ( mapping instanceof CompoundMapping ) {
                CompoundMapping cm = (CompoundMapping) mapping;
                for ( Mapping particle : cm.getParticles() ) {
                    addSelectColumns( particle, colToRsIdx, true );
                }
            } else if ( mapping instanceof ConstantMapping<?> ) {
                // nothing to do
            } else {
                LOG.warn( "Mappings of type '" + mapping.getClass() + "' are not handled yet." );
            }
        }
    }

    @Override
    public Feature buildFeature( ResultSet rs )
                            throws SQLException {

        String gmlId = ftMapping.getFidMapping().getPrefix();
        List<Pair<String, BaseType>> fidColumns = ftMapping.getFidMapping().getColumns();
        gmlId += rs.getObject( colToRsIdx.get( fidColumns.get( 0 ).first ) );
        for ( int i = 1; i < fidColumns.size(); i++ ) {
            gmlId += ftMapping.getFidMapping().getDelimiter()
                     + rs.getObject( colToRsIdx.get( fidColumns.get( i ).first ) );
        }
        Feature feature = (Feature) fs.getCache().get( gmlId );
        if ( feature == null ) {
            LOG.debug( "Cache miss. Recreating feature '" + gmlId + "' from db (relational mode)." );
            List<Property> props = new ArrayList<Property>();
            for ( Mapping mapping : ftMapping.getMappings() ) {
                PropertyName propName = mapping.getPath();
                if ( propName.getAsQName() != null ) {
                    PropertyType pt = ft.getPropertyDeclaration( propName.getAsQName(), gmlVersion );
                    addProperties( props, pt, mapping, rs );
                } else {
                    // TODO more complex mappings, e.g. "propname[1]"
                    LOG.warn( "Omitting mapping '" + mapping
                              + "'. Only simple property names (QNames) are currently supported here." );
                }
            }
            feature = ft.newFeature( gmlId, props, null, null );
            fs.getCache().add( feature );
        } else {
            LOG.debug( "Cache hit." );
        }
        return feature;
    }

    private void addProperties( List<Property> props, PropertyType pt, Mapping propMapping, ResultSet rs )
                            throws SQLException {

        List<TypedObjectNode> particles = buildParticles( propMapping, rs, colToRsIdx );
        for ( TypedObjectNode particle : particles ) {
            if ( particle instanceof GenericXMLElement ) {
                GenericXMLElement xmlEl = (GenericXMLElement) particle;
                props.add( new GenericProperty( pt, xmlEl.getName(), null, xmlEl.getAttributes(), xmlEl.getChildren() ) );
            } else {
                props.add( new GenericProperty( pt, pt.getName(), particle ) );
            }
        }
    }

    private List<TypedObjectNode> buildParticles( Mapping mapping, ResultSet rs,
                                                  LinkedHashMap<String, Integer> colToRsIdx )
                            throws SQLException {

        if ( mapping.getJoinedTable() != null ) {
            List<TypedObjectNode> values = new ArrayList<TypedObjectNode>();
            ResultSet rs2 = null;
            try {
                Pair<ResultSet, LinkedHashMap<String, Integer>> p = getJoinedResultSet( mapping.getJoinedTable().get( 0 ),
                                                                                        mapping, rs, colToRsIdx );
                rs2 = p.first;
                while ( rs2.next() ) {
                    TypedObjectNode particle = buildParticle( mapping, rs2, p.second );
                    if ( particle != null ) {
                        values.add( buildParticle( mapping, rs2, p.second ) );
                    }
                }
            } finally {
                if ( rs2 != null ) {
                    rs2.getStatement().close();
                    rs2.close();
                }
            }
            return values;
        }
        TypedObjectNode particle = buildParticle( mapping, rs, colToRsIdx );
        if ( particle != null ) {
            return Collections.singletonList( buildParticle( mapping, rs, colToRsIdx ) );
        }
        return Collections.emptyList();
    }

    private TypedObjectNode buildParticle( Mapping mapping, ResultSet rs, LinkedHashMap<String, Integer> colToRsIdx )
                            throws SQLException {

        TypedObjectNode particle = null;
        if ( mapping instanceof PrimitiveMapping ) {
            PrimitiveMapping pm = (PrimitiveMapping) mapping;
            MappingExpression me = pm.getMapping();
            if ( me instanceof DBField ) {
                Object value = rs.getObject( colToRsIdx.get( ( (DBField) me ).getColumn() ) );
                if ( value != null ) {
                    particle = new PrimitiveValue( value, pm.getType() );
                }
            }
        } else if ( mapping instanceof GeometryMapping ) {
            GeometryMapping pm = (GeometryMapping) mapping;
            MappingExpression me = pm.getMapping();
            if ( me instanceof DBField ) {
                byte[] wkb = rs.getBytes( colToRsIdx.get( fs.getSQLValueMapper().selectGeometry( ( (DBField) me ).getColumn() ) ) );
                if ( wkb != null ) {
                    try {
                        particle = WKBReader.read( wkb, pm.getCRS() );
                    } catch ( ParseException e ) {
                        throw new SQLException( "Error parsing WKB from database: " + e.getMessage(), e );
                    }
                }
            }
        } else if ( mapping instanceof FeatureMapping ) {
            FeatureMapping fm = (FeatureMapping) mapping;
            MappingExpression me = fm.getMapping();
            if ( me instanceof DBField ) {
                Object value = rs.getObject( colToRsIdx.get( ( (DBField) me ).getColumn() ) );
                if ( value != null ) {
                    // TODO
                    String ref = "#" + value;
                    particle = new FeatureReference( fs.getResolver(), ref, null );
                }
            }
            me = fm.getHrefMapping();
            if ( me instanceof DBField ) {
                String value = rs.getString( colToRsIdx.get( ( (DBField) me ).getColumn() ) );
                if ( value != null ) {
                    particle = new FeatureReference( fs.getResolver(), value, null );
                }
            }
        } else if ( mapping instanceof ConstantMapping<?> ) {
            particle = ( (ConstantMapping<?>) mapping ).getValue();
        } else if ( mapping instanceof CompoundMapping ) {
            CompoundMapping cm = (CompoundMapping) mapping;

            Map<QName, PrimitiveValue> attrs = new HashMap<QName, PrimitiveValue>();
            List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();

            for ( Mapping particleMapping : cm.getParticles() ) {

                List<TypedObjectNode> particleValues = buildParticles( particleMapping, rs, colToRsIdx );

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
                        List<?> predicates = step.getPredicates();
                        if ( predicates.size() == 1 ) {
                            Expr predicate = ( (Predicate) predicates.get( 0 ) ).getExpr();
                            if ( predicate instanceof NumberExpr ) {
                                LOG.warn( "Number predicate. Assuming natural ordering." );
                            } else {
                                continue;
                            }
                        } else {
                            LOG.warn( "Unhandled location path: '" + particleMapping.getPath()
                                      + "'. Only unpredicated steps are handled." );
                            continue;
                        }
                    }
                    if ( step instanceof TextNodeStep ) {
                        for ( TypedObjectNode particleValue : particleValues ) {
                            children.add( particleValue );
                        }
                    } else if ( step instanceof NameStep ) {
                        NameStep ns = (NameStep) step;
                        QName name = getQName( ns );
                        if ( step.getAxis() == Axis.ATTRIBUTE ) {
                            for ( TypedObjectNode particleValue : particleValues ) {
                                if ( particleValue instanceof PrimitiveValue ) {
                                    attrs.put( name, (PrimitiveValue) particleValue );
                                } else {
                                    LOG.warn( "Value not suitable for attribute." );
                                }
                            }
                        } else if ( step.getAxis() == Axis.CHILD ) {
                            for ( TypedObjectNode particleValue : particleValues ) {
                                if ( particleValue instanceof PrimitiveValue ) {
                                    // TODO
                                    XSElementDeclaration childType = null;
                                    GenericXMLElement child = new GenericXMLElement(
                                                                                     name,
                                                                                     childType,
                                                                                     Collections.<QName, PrimitiveValue> emptyMap(),
                                                                                     Collections.singletonList( particleValue ) );
                                    children.add( child );
                                } else if ( particleValue != null ) {
                                    children.add( particleValue );
                                }
                            }
                        } else {
                            LOG.warn( "Unhandled axis type '" + step.getAxis() + "' for path: '"
                                      + particleMapping.getPath() + "'" );
                        }
                    } else {
                        // TODO handle other steps as self()
                        for ( TypedObjectNode particleValue : particleValues ) {
                            children.add( particleValue );
                        }
                    }
                } else {
                    LOG.warn( "Unhandled mapping type '" + particleMapping.getClass() + "' for path: '"
                              + particleMapping.getPath() + "'" );
                }
            }

            // TODO
            XSElementDeclaration xsType = null;
            if ( ( !attrs.isEmpty() ) || !children.isEmpty() ) {
                QName elName = getName( mapping.getPath() );
                particle = new GenericXMLElement( elName, xsType, attrs, children );
            }
        } else {
            LOG.warn( "Handling of '" + mapping.getClass() + "' mappings is not implemented yet." );
        }
        return particle;
    }

    private QName getName( PropertyName path ) {
        if ( path.getAsQName() != null ) {
            return path.getAsQName();
        }
        Expr xpath = path.getAsXPath();
        if ( xpath instanceof LocationPath ) {
            LocationPath lp = (LocationPath) xpath;
            if ( lp.getSteps().size() == 1 && !lp.isAbsolute() ) {
                Step step = (Step) lp.getSteps().get( 0 );
                if ( step instanceof NameStep ) {
                    return getQName( (NameStep) step );
                }
            }
        }
        return null;
    }

    private Pair<ResultSet, LinkedHashMap<String, Integer>> getJoinedResultSet( TableJoin jc,
                                                                                Mapping mapping,
                                                                                ResultSet rs,
                                                                                LinkedHashMap<String, Integer> colToRsIdx )
                            throws SQLException {

        LinkedHashMap<String, Integer> rsToIdx = getSubsequentSelectColumns( mapping );

        StringBuilder sql = new StringBuilder( "SELECT " );
        boolean first = true;
        for ( String column : rsToIdx.keySet() ) {
            if ( !first ) {
                sql.append( ',' );
            }
            sql.append( column );
            first = false;
        }
        sql.append( " FROM " );
        sql.append( jc.getToTable() );
        sql.append( " WHERE " );
        first = true;
        for ( String keyColumn : jc.getToColumns() ) {
            if ( !first ) {
                sql.append( " AND " );
            }
            sql.append( keyColumn );
            sql.append( " = ?" );
            first = false;
        }
        LOG.debug( "SQL: {}", sql );

        PreparedStatement stmt = null;
        ResultSet rs2 = null;
        try {
            long begin = System.currentTimeMillis();
            stmt = conn.prepareStatement( sql.toString() );
            LOG.debug( "Preparing subsequent SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            int i = 1;
            for ( String keyColumn : jc.getFromColumns() ) {
                Object key = rs.getObject( colToRsIdx.get( keyColumn ) );
                LOG.debug( "? = '{}' ({})", key, keyColumn );
                stmt.setObject( i++, key );
            }
            begin = System.currentTimeMillis();
            rs2 = stmt.executeQuery();
            LOG.debug( "Executing SELECT took {} [ms] ", System.currentTimeMillis() - begin );
        } catch ( Throwable t ) {
            close( rs2, stmt, null, LOG );
            String msg = "Error performing subsequent SELECT: " + t.getMessage();
            LOG.error( msg, t );
            throw new SQLException( msg, t );
        }
        return new Pair<ResultSet, LinkedHashMap<String, Integer>>( rs2, rsToIdx );
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