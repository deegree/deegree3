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

import static java.lang.Boolean.TRUE;
import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.jaxen.saxpath.Axis.CHILD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSObjectList;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.sql.FeatureBuilder;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.AppSchemaGeometryHierarchy;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
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

    private final SQLFeatureStore fs;

    private final FeatureType ft;

    private final FeatureTypeMapping ftMapping;

    private final Connection conn;

    private final String tableAlias;

    private final NamespaceBindings nsBindings;

    // private final GMLVersion gmlVersion;

    private final LinkedHashMap<String, Integer> qualifiedSqlExprToRsIdx = new LinkedHashMap<String, Integer>();

    private final boolean nullEscalation;

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
     * @param escalationPolicy
     *            the void escalation policy, must not be <code>null</code>
     */
    public FeatureBuilderRelational( SQLFeatureStore fs, FeatureType ft, FeatureTypeMapping ftMapping, Connection conn,
                                     String ftTableAlias, boolean nullEscalation ) {
        this.fs = fs;
        this.ft = ft;
        this.ftMapping = ftMapping;
        this.conn = conn;
        this.tableAlias = ftTableAlias;
        this.nullEscalation = nullEscalation;
        this.nsBindings = new NamespaceBindings();
        for ( String prefix : fs.getNamespaceContext().keySet() ) {
            String ns = fs.getNamespaceContext().get( prefix );
            nsBindings.addNamespace( prefix, ns );
        }
        // if ( ft.getSchema().getGMLSchema() != null ) {
        // this.gmlVersion = ft.getSchema().getGMLSchema().getVersion();
        // } else {
        // this.gmlVersion = GMLVersion.GML_32;
        // }
    }

    @Override
    public List<String> getInitialSelectList() {
        for ( Pair<SQLIdentifier, BaseType> fidColumn : ftMapping.getFidMapping().getColumns() ) {
            addColumn( qualifiedSqlExprToRsIdx, tableAlias + "." + fidColumn.first.getName() );
        }
        for ( Mapping mapping : ftMapping.getMappings() ) {
            addSelectColumns( mapping, qualifiedSqlExprToRsIdx, true );
        }
        LOG.debug( "Initial select columns: " + qualifiedSqlExprToRsIdx );
        return new ArrayList<String>( qualifiedSqlExprToRsIdx.keySet() );
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
            if ( mapping instanceof FeatureMapping ) {
                ParticleConverter<?> particleConverter = fs.getConverter( mapping );
                if ( particleConverter != null ) {
                    addColumn( colToRsIdx, particleConverter.getSelectSnippet( tableAlias ) );
                } else {
                    LOG.info( "Omitting mapping '" + mapping + "' from SELECT list. Not mapped to column.'" );
                }
            } else {
                for ( SQLIdentifier column : jc.get( 0 ).getFromColumns() ) {
                    addColumn( colToRsIdx, tableAlias + "." + column );
                }
            }
        } else {
            ParticleConverter<?> particleConverter = fs.getConverter( mapping );
            if ( mapping instanceof PrimitiveMapping ) {
                if ( particleConverter != null ) {
                    addColumn( colToRsIdx, particleConverter.getSelectSnippet( tableAlias ) );
                } else {
                    LOG.info( "Omitting mapping '" + mapping + "' from SELECT list. Not mapped to column.'" );
                }
            } else if ( mapping instanceof GeometryMapping ) {
                if ( particleConverter != null ) {
                    addColumn( colToRsIdx, particleConverter.getSelectSnippet( tableAlias ) );
                } else {
                    LOG.info( "Omitting mapping '" + mapping + "' from SELECT list. Not mapped to column.'" );
                }
            } else if ( mapping instanceof FeatureMapping ) {
                if ( particleConverter != null ) {
                    addColumn( colToRsIdx, particleConverter.getSelectSnippet( tableAlias ) );
                } else {
                    LOG.info( "Omitting mapping '" + mapping + "' from SELECT list. Not mapped to column.'" );
                }
            } else if ( mapping instanceof BlobParticleMapping ) {
                if ( particleConverter != null ) {
                    addColumn( colToRsIdx, particleConverter.getSelectSnippet( tableAlias ) );
                } else {
                    LOG.info( "Omitting mapping '" + mapping + "' from SELECT list. Not mapped to column.'" );
                }
            } else if ( mapping instanceof CompoundMapping ) {
                CompoundMapping cm = (CompoundMapping) mapping;
                for ( Mapping particle : cm.getParticles() ) {
                    addSelectColumns( particle, colToRsIdx, true );
                }
            } else if ( mapping instanceof SqlExpressionMapping<?> ) {
                // nothing to do
            } else {
                LOG.warn( "Mappings of type '" + mapping.getClass() + "' are not handled yet." );
            }
        }
    }

    @Override
    public Feature buildFeature( ResultSet rs )
                            throws SQLException {

        Feature feature = null;
        try {
            String gmlId = ftMapping.getFidMapping().getPrefix();
            List<Pair<SQLIdentifier, BaseType>> fidColumns = ftMapping.getFidMapping().getColumns();
            gmlId += rs.getObject( qualifiedSqlExprToRsIdx.get( tableAlias + "." + fidColumns.get( 0 ).first ) );
            for ( int i = 1; i < fidColumns.size(); i++ ) {
                gmlId += ftMapping.getFidMapping().getDelimiter()
                         + rs.getObject( qualifiedSqlExprToRsIdx.get( tableAlias + "." + fidColumns.get( i ).first ) );
            }
            if ( fs.getCache() != null ) {
                feature = (Feature) fs.getCache().get( gmlId );
            }
            if ( feature == null ) {
                LOG.debug( "Recreating feature '" + gmlId + "' from db (relational mode)." );
                List<Property> props = new ArrayList<Property>();
                for ( Mapping mapping : ftMapping.getMappings() ) {
                    ValueReference propName = mapping.getPath();
                    QName childEl = getChildElementStepAsQName( propName );
                    if ( childEl != null ) {
                        PropertyType pt = ft.getPropertyDeclaration( childEl );
                        String idPrefix = gmlId + "_" + toIdPrefix( propName );
                        addProperties( props, pt, mapping, rs, idPrefix );
                    } else {
                        LOG.warn( "Omitting mapping '" + mapping
                                  + "'. Only single child element steps (optionally with number predicate)"
                                  + " are currently supported." );
                    }
                }
                feature = ft.newFeature( gmlId, props, null );
                if ( fs.getCache() != null ) {
                    fs.getCache().add( feature );
                }
            } else {
                LOG.debug( "Cache hit." );
            }
        } catch ( Throwable t ) {
            LOG.error( t.getMessage(), t );
            throw new SQLException( t.getMessage(), t );
        }
        return feature;
    }

    private String toIdPrefix( ValueReference propName ) {
        String s = propName.getAsText();
        s = s.replace( "/", "_" );
        s = s.replace( ":", "_" );
        s = s.replace( "[", "_" );
        s = s.replace( "]", "_" );
        s = s.toUpperCase();
        return s;
    }

    private void addProperties( List<Property> props, PropertyType pt, Mapping propMapping, ResultSet rs,
                                String idPrefix )
                            throws SQLException {
        List<TypedObjectNode> particles = buildParticles( propMapping, rs, qualifiedSqlExprToRsIdx, idPrefix );
        if ( particles.isEmpty() && pt.getMinOccurs() > 0 ) {
            if ( pt.isNillable() ) {
                Map<QName, PrimitiveValue> attrs = Collections.singletonMap( new QName( CommonNamespaces.XSINS, "nil" ),
                                                                             new PrimitiveValue( Boolean.TRUE ) );
                props.add( new GenericProperty( pt, propMapping.getPath().getAsQName(), null, attrs,
                                                Collections.<TypedObjectNode> emptyList() ) );
            } else {
                LOG.warn( "Unable to map NULL value for mapping '" + propMapping.getPath().getAsText()
                          + "' to output. This will result in schema violations." );
            }
        }
        for ( final TypedObjectNode particle : particles ) {
            if ( particle instanceof Property ) {
                props.add( (Property) particle );
            } else if ( particle instanceof GenericXMLElement ) {
                props.add( convertToProperty( (GenericXMLElement) particle, pt ) );
            } else {
                props.add( new GenericProperty( pt, pt.getName(), particle ) );
            }
        }
    }

    private Property convertToProperty( final GenericXMLElement particle, final PropertyType pt ) {
        if ( pt instanceof ObjectPropertyType && !isChildElementGmlObject( particle ) ) {
            LOG.warn( "Recreating GML object-valued property: "
                      + ( (ObjectPropertyType) pt ).getAllowedRepresentation() );
            return recreatePropertyFromGml( pt, particle );
        }
        return new GenericProperty( pt, particle.getName(), null, particle.getAttributes(), particle.getChildren() );
    }

    private boolean isChildElementGmlObject( final GenericXMLElement particle ) {
        if ( particle.getChildren().size() != 1 ) {
            return false;
        }
        final TypedObjectNode child = particle.getChildren().get( 0 );
        if ( child instanceof GMLObject ) {
            return true;
        }
        return false;
    }

    // private GMLObject buildGmlObject( final ObjectPropertyType pt, final CompoundMapping propMapping,
    // final ResultSet rs, final String idPrefix ) {
    // LOG.debug( "Recreating GML object from db (relational mode)." );
    // final List<Property> props = new ArrayList<Property>();
    // for ( final Mapping mapping : propMapping.getParticles()) {
    // ValueReference propName = mapping.getPath();
    // QName childEl = getChildElementStepAsQName( propName );
    // if ( childEl != null ) {
    // PropertyType pt = ft.getPropertyDeclaration( childEl );
    // String idPrefix = gmlId + "_" + toIdPrefix( propName );
    // addProperties( props, pt, mapping, rs, idPrefix );
    // } else {
    // LOG.warn( "Omitting mapping '" + mapping
    // + "'. Only single child element steps (optionally with number predicate)"
    // + " are currently supported." );
    // }
    // }
    // switch (pt.getCategory()) {
    // case TIME_SLICE: {
    // return new GenericTimeSlice( id, type, props );
    // }
    // default: {
    //
    // }
    // }
    // }

    private Property recreatePropertyFromGml( final PropertyType pt, final GenericXMLElement particle ) {
        try {
            final GMLSchemaInfoSet gmlSchema = ft.getSchema().getGMLSchema();
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
            final GMLVersion version = ft.getSchema().getGMLSchema().getVersion();
            final GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( version, xmlWriter );
            gmlWriter.setNamespaceBindings( gmlSchema.getNamespacePrefixes() );
            final GmlXlinkOptions resolveState = new GmlXlinkOptions();
            gmlWriter.getFeatureWriter().export( particle, resolveState );
            gmlWriter.close();
            xmlWriter.close();
            bos.close();
            final InputStream is = new ByteArrayInputStream( bos.toByteArray() );
            final XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( is );
            final GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( version, xmlReader );
            gmlReader.setApplicationSchema( ft.getSchema() );
            gmlReader.setLaxMode( true );
            final Property property = gmlReader.getFeatureReader().parseProperty( new XMLStreamReaderWrapper(
                                                                                                              xmlReader,
                                                                                                              null ),
                                                                                  pt, null );
            return property;
        } catch ( final Exception e ) {
            LOG.error( e.getMessage(), e );
        }
        return new GenericProperty( pt, particle.getName(), null, particle.getAttributes(), particle.getChildren() );
    }

    private List<TypedObjectNode> buildParticles( Mapping mapping, ResultSet rs,
                                                  LinkedHashMap<String, Integer> colToRsIdx, String idPrefix )
                            throws SQLException {

        if ( !( mapping instanceof FeatureMapping ) && mapping.getJoinedTable() != null ) {
            List<TypedObjectNode> values = new ArrayList<TypedObjectNode>();
            ResultSet rs2 = null;
            try {
                Pair<ResultSet, LinkedHashMap<String, Integer>> p = getJoinedResultSet( mapping.getJoinedTable().get( 0 ),
                                                                                        mapping, rs, colToRsIdx );
                rs2 = p.first;
                int i = 0;
                while ( rs2.next() ) {
                    TypedObjectNode particle = buildParticle( mapping, rs2, p.second, idPrefix + "_" + ( i++ ) );
                    if ( particle != null ) {
                        values.add( particle );
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
        TypedObjectNode particle = buildParticle( mapping, rs, colToRsIdx, idPrefix );
        if ( particle != null ) {
            return Collections.singletonList( particle );
        }
        return Collections.emptyList();
    }

    private TypedObjectNode buildParticle( Mapping mapping, ResultSet rs, LinkedHashMap<String, Integer> colToRsIdx,
                                           String idPrefix )
                            throws SQLException {

        LOG.debug( "Trying to build particle with path {}.", mapping.getPath() );

        TypedObjectNode particle = null;
        ParticleConverter<?> converter = fs.getConverter( mapping );

        if ( mapping instanceof PrimitiveMapping ) {
            PrimitiveMapping pm = (PrimitiveMapping) mapping;
            MappingExpression me = pm.getMapping();
            String col = converter.getSelectSnippet( tableAlias );
            int colIndex = colToRsIdx.get( col );
            particle = converter.toParticle( rs, colIndex );
        } else if ( mapping instanceof GeometryMapping ) {
            GeometryMapping pm = (GeometryMapping) mapping;
            MappingExpression me = pm.getMapping();
            if ( me instanceof DBField ) {
                String col = converter.getSelectSnippet( tableAlias );
                int colIndex = colToRsIdx.get( col );
                particle = converter.toParticle( rs, colIndex );
                Geometry geom = ( (Geometry) particle );
                if ( geom != null ) {
                    geom.setId( idPrefix );
                }
            }
        } else if ( mapping instanceof FeatureMapping ) {
            FeatureMapping fm = (FeatureMapping) mapping;
            // if ( fm.getJoinedTable() != null && !fm.getJoinedTable().isEmpty() ) {
            String col = converter.getSelectSnippet( tableAlias );
            int colIndex = colToRsIdx.get( col );
            particle = converter.toParticle( rs, colIndex );
            // }
        } else if ( mapping instanceof BlobParticleMapping ) {
            final BlobParticleMapping bm = (BlobParticleMapping) mapping;
            final MappingExpression me = bm.getMapping();
            if ( me instanceof DBField ) {
                final String col = converter.getSelectSnippet( tableAlias );
                final int colIndex = colToRsIdx.get( col );
                particle = converter.toParticle( rs, colIndex );
            }
        } else if ( mapping instanceof CompoundMapping ) {
            CompoundMapping cm = (CompoundMapping) mapping;
            if ( converter != null ) {
                final String col = converter.getSelectSnippet( tableAlias );
                final int colIndex = colToRsIdx.get( col );
                particle = converter.toParticle( rs, colIndex );
            } else {
                particle = buildParticleRecursively( cm, rs, colToRsIdx, idPrefix );
            }
        } else {
            LOG.warn( "Handling of '" + mapping.getClass() + "' mappings is not implemented yet." );
        }

        if ( particle == null ) {
            LOG.debug( "Building of particle with path {} resulted in NULL.", mapping.getPath() );
        } else {
            LOG.debug( "Built particle with path {}.", mapping.getPath() );
        }

        return particle;
    }

    private TypedObjectNode buildParticleRecursively( final CompoundMapping cm, final ResultSet rs,
                                                      final LinkedHashMap<String, Integer> colToRsIdx,
                                                      final String idPrefix )
                            throws SQLException {

        Map<QName, PrimitiveValue> attrs = new HashMap<QName, PrimitiveValue>();
        List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();

        boolean escalateVoid = false;

        for ( Mapping particleMapping : cm.getParticles() ) {

            // TODO idPrefix
            List<TypedObjectNode> particleValues = buildParticles( particleMapping, rs, colToRsIdx, idPrefix );

            if ( !particleMapping.isVoidable() ) {
                boolean found = false;
                for ( TypedObjectNode particleValue : particleValues ) {
                    if ( particleValue != null ) {
                        found = true;
                    }
                }
                if ( !found && this.nullEscalation ) {
                    escalateVoid = true;
                }
            }

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
                            LOG.debug( "Number predicate. Assuming natural ordering." );
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

        TypedObjectNode particle = null;
        PrimitiveValue nilled = attrs.get( new QName( CommonNamespaces.XSINS, "nil" ) );
        if ( nilled != null && nilled.getValue().equals( TRUE ) ) {
            QName elName = getName( cm.getPath() );
            particle = new GenericXMLElement( elName, cm.getElementDecl(), attrs, null );
        } else if ( escalateVoid ) {
            if ( cm.isVoidable() ) {
                LOG.debug( "Materializing void by omitting particle for path {}.", cm.getPath() );
            } else if ( cm.getElementDecl() != null && cm.getElementDecl().getNillable() ) {
                LOG.debug( "Materializing void by nilling particle for path {}.", cm.getPath() );
                QName elName = getName( cm.getPath() );
                // required attributes must still be present even if element is nilled...
                Map<QName, PrimitiveValue> nilAttrs = new HashMap<QName, PrimitiveValue>();
                if ( cm.getElementDecl().getTypeDefinition() instanceof XSComplexTypeDefinition ) {
                    XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) cm.getElementDecl().getTypeDefinition();
                    XSObjectList attrUses = complexType.getAttributeUses();
                    for ( int i = 0; i < attrUses.getLength(); i++ ) {
                        XSAttributeUse attrUse = (XSAttributeUse) attrUses.item( i );
                        if ( attrUse.getRequired() ) {
                            QName attrName = null;
                            XSAttributeDeclaration attrDecl = attrUse.getAttrDeclaration();
                            if ( attrDecl.getNamespace() == null || attrDecl.getNamespace().isEmpty() ) {
                                attrName = new QName( attrDecl.getName() );
                            } else {
                                attrName = new QName( attrDecl.getNamespace(), attrDecl.getName() );
                            }
                            PrimitiveValue attrValue = attrs.get( attrName );
                            if ( attrValue == null ) {
                                LOG.debug( "Required attribute " + attrName
                                           + "not present. Cannot void using xsi:nil. Escalating void value." );
                                return null;
                            }
                            nilAttrs.put( attrName, attrValue );
                        }
                    }
                }
                nilAttrs.put( new QName( XSINS, "nil", XSI_PREFIX ), new PrimitiveValue( TRUE ) );
                particle = new GenericXMLElement( elName, cm.getElementDecl(), nilAttrs, null );
            }
        } else {
            if ( ( !attrs.isEmpty() ) || !children.isEmpty() ) {
                QName elName = getName( cm.getPath() );
                particle = new GenericXMLElement( elName, cm.getElementDecl(), attrs, children );
            }
        }

        QName elName = getName( cm.getPath() );
        if ( particle instanceof GenericXMLElement && fs.getSchema().getGeometryType( elName ) != null ) {
            particle = unwrapCustomGeometry( (GenericXMLElement) particle );
        }
        return particle;
    }

    // TODO where should this happen in the end?
    private TypedObjectNode unwrapCustomGeometry( GenericXMLElement particle ) {

        GMLObjectType ot = fs.getSchema().getGeometryType( particle.getName() );
        Geometry geom = null;
        List<Property> props = new ArrayList<Property>();
        for ( TypedObjectNode child : particle.getChildren() ) {
            if ( child instanceof Geometry ) {
                geom = (Geometry) child;
            } else if ( child instanceof GenericXMLElement ) {
                GenericXMLElement xmlEl = (GenericXMLElement) child;
                PropertyType pt = ot.getPropertyDeclaration( xmlEl.getName() );
                props.add( new GenericProperty( pt, xmlEl.getName(), null, xmlEl.getAttributes(), xmlEl.getChildren() ) );
            } else {
                LOG.warn( "Unhandled particle: " + child );
            }
        }
        if ( geom == null ) {
            return null;
        }
        AppSchemaGeometryHierarchy hierarchy = fs.getSchema().getGeometryHierarchy();

        if ( hierarchy != null ) {
            if ( hierarchy.getSurfaceSubstitutions().contains( particle.getName() ) && geom instanceof Polygon ) {
                // constructed as Polygon, but needs to become a Surface
                Polygon p = (Polygon) geom;
                GeometryFactory geomFac = new GeometryFactory();
                List<SurfacePatch> patches = new ArrayList<SurfacePatch>();
                patches.add( geomFac.createPolygonPatch( p.getExteriorRing(), p.getInteriorRings() ) );
                geom = geomFac.createSurface( geom.getId(), patches, geom.getCoordinateSystem() );
            } else if ( hierarchy.getCurveSubstitutions().contains( particle.getName() ) && geom instanceof LineString ) {
                // constructed as LineString, but needs to become a Curve
                LineString p = (LineString) geom;
                GeometryFactory geomFac = new GeometryFactory();
                CurveSegment[] segments = new CurveSegment[1];
                segments[0] = geomFac.createLineStringSegment( p.getControlPoints() );
                geom = geomFac.createCurve( geom.getId(), geom.getCoordinateSystem(), segments );
            }
            geom.setType( fs.getSchema().getGeometryType( particle.getName() ) );
            geom.setProperties( props );
        }
        return geom;
    }

    private QName getName( ValueReference path ) {
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
        sql.append( ' ' );
        sql.append( tableAlias );
        sql.append( " WHERE " );
        first = true;
        for ( SQLIdentifier keyColumn : jc.getToColumns() ) {
            if ( !first ) {
                sql.append( " AND " );
            }
            sql.append( keyColumn );
            sql.append( " = ?" );
            first = false;
        }
        if ( jc.getOrderColumns() != null && !jc.getOrderColumns().isEmpty() ) {
            sql.append( " ORDER BY " );
            first = true;
            for ( SQLIdentifier orderColumn : jc.getOrderColumns() ) {
                if ( !first ) {
                    sql.append( "," );
                }
                if ( orderColumn.toString().endsWith( "-" ) ) {
                    sql.append( orderColumn.toString().substring( 0, orderColumn.toString().length() - 1 ) );
                    sql.append( " DESC" );
                } else {
                    sql.append( orderColumn );
                }
                first = false;
            }
        }
        LOG.debug( "SQL: {}", sql );

        PreparedStatement stmt = null;
        ResultSet rs2 = null;
        try {
            long begin = System.currentTimeMillis();
            stmt = conn.prepareStatement( sql.toString() );

            LOG.debug( "Preparing subsequent SELECT took {} [ms] ", System.currentTimeMillis() - begin );
            int i = 1;
            for ( SQLIdentifier keyColumn : jc.getFromColumns() ) {
                Object key = rs.getObject( colToRsIdx.get( tableAlias + "." + keyColumn ) );
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

    private QName getChildElementStepAsQName( ValueReference ref ) {
        QName qName = null;
        Expr xpath = ref.getAsXPath();
        if ( xpath instanceof LocationPath ) {
            LocationPath lpath = (LocationPath) xpath;
            if ( lpath.getSteps().size() == 1 ) {
                if ( lpath.getSteps().get( 0 ) instanceof NameStep ) {
                    NameStep step = (NameStep) lpath.getSteps().get( 0 );
                    if ( isChildElementStepWithoutPredicateOrWithNumberPredicate( step ) ) {
                        String prefix = step.getPrefix();
                        if ( prefix.isEmpty() ) {
                            qName = new QName( step.getLocalName() );
                        } else {
                            String ns = ref.getNsContext().translateNamespacePrefixToUri( prefix );
                            qName = new QName( ns, step.getLocalName(), prefix );
                        }
                        LOG.debug( "QName: " + qName );
                    }
                }
            }
        }
        return qName;
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

    private boolean isChildElementStepWithoutPredicateOrWithNumberPredicate( NameStep step ) {
        if ( step.getAxis() == CHILD && !step.getLocalName().equals( "*" ) ) {
            if ( step.getPredicates().isEmpty() ) {
                return true;
            } else if ( step.getPredicates().size() == 1 ) {
                Predicate predicate = (Predicate) step.getPredicates().get( 0 );
                Expr expr = predicate.getExpr();
                if ( expr instanceof NumberExpr ) {
                    return true;
                }
            }
        }
        return false;
    }
}