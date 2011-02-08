//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;
import static org.deegree.feature.persistence.BlobCodec.Compression.NONE;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.cs.CRS;
import org.deegree.feature.persistence.BlobCodec;
import org.deegree.feature.persistence.mapping.BBoxTableMapping;
import org.deegree.feature.persistence.mapping.BlobMapping;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.JoinChain;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.id.FIDMapping;
import org.deegree.feature.persistence.mapping.id.IDGenerator;
import org.deegree.feature.persistence.mapping.id.UUIDGenerator;
import org.deegree.feature.persistence.mapping.property.CompoundMapping;
import org.deegree.feature.persistence.mapping.property.FeatureMapping;
import org.deegree.feature.persistence.mapping.property.GeometryMapping;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.persistence.mapping.property.PrimitiveMapping;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.jaxen.NamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link MappedApplicationSchema} instances from {@link ApplicationSchema}s by inferring a canonical database
 * mapping.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AppSchemaMapper {

    private static Logger LOG = LoggerFactory.getLogger( AppSchemaMapper.class );

    private final ApplicationSchema appSchema;

    private final MappingContextManager mcManager;

    private final CRS storageCrs;

    private final String storageSrid;

    // TODO
    private final CoordinateDimension storageDim = DIM_2;

    private final MappedApplicationSchema mappedSchema;

    /**
     * Creates a new {@link AppSchemaMapper} instance for the given schema.
     * 
     * @param appSchema
     *            application schema to be mapped, must not be <code>null</code>
     * @param createBlobMapping
     *            true, if BLOB mapping should be performed, false otherwise
     * @param createRelationalMapping
     *            true, if relational mapping should be performed, false otherwise
     * @param storageCrs
     *            CRS to use for geometry properties, must not be <code>null</code>
     * @param srid
     *            native DB-SRS identifier, must not be <code>null</code>
     */
    public AppSchemaMapper( ApplicationSchema appSchema, boolean createBlobMapping, boolean createRelationalMapping,
                            CRS storageCrs, String srid ) {
        this.appSchema = appSchema;
        this.storageCrs = storageCrs;
        this.storageSrid = srid;

        FeatureType[] fts = appSchema.getFeatureTypes();
        Map<FeatureType, FeatureType> ftToSuperFt = appSchema.getFtToSuperFt();
        Map<String, String> prefixToNs = appSchema.getNamespaceBindings();
        GMLSchemaInfoSet xsModel = appSchema.getXSModel();
        FeatureTypeMapping[] ftMappings = null;
        mcManager = new MappingContextManager( xsModel.getNamespacePrefixes() );
        if ( createRelationalMapping ) {
            ftMappings = generateFtMappings( fts );
        }

        BBoxTableMapping bboxMapping = createBlobMapping ? generateBBoxMapping() : null;
        BlobMapping blobMapping = createBlobMapping ? generateBlobMapping() : null;

        this.mappedSchema = new MappedApplicationSchema( fts, ftToSuperFt, prefixToNs, xsModel, ftMappings, storageCrs,
                                                         bboxMapping, blobMapping );
    }

    /**
     * Returns the {@link MappedApplicationSchema} instance.
     * 
     * @return mapped schema, never <code>null</code>
     */
    public MappedApplicationSchema getMappedSchema() {
        return mappedSchema;
    }

    private BlobMapping generateBlobMapping() {
        // TODO
        String table = "GML_OBJECTS";
        // TODO
        BlobCodec codec = new BlobCodec( GMLVersion.GML_32, NONE );
        return new BlobMapping( table, storageCrs, codec );
    }

    private BBoxTableMapping generateBBoxMapping() {
        // TODO
        String ftTable = "FEATURE_TYPES";
        return new BBoxTableMapping( ftTable, storageCrs );
    }

    private FeatureTypeMapping[] generateFtMappings( FeatureType[] fts ) {
        FeatureTypeMapping[] ftMappings = new FeatureTypeMapping[fts.length];
        for ( int i = 0; i < fts.length; i++ ) {
            ftMappings[i] = generateFtMapping( fts[i] );
        }
        return ftMappings;
    }

    private FeatureTypeMapping generateFtMapping( FeatureType ft ) {
        LOG.info( "Mapping feature type '" + ft.getName() + "'" );
        MappingContext mc = mcManager.newContext( ft.getName() );

        // TODO
        QTableName table = new QTableName( mc.getTable() );
        // TODO
        IDGenerator generator = new UUIDGenerator();
        // TODO
        FIDMapping fidMapping = new FIDMapping( "", "attr_gml_id", STRING, generator );

        Map<QName, Mapping> propToMapping = new HashMap<QName, Mapping>();
        // TODO: gml properties
        for ( PropertyType pt : ft.getPropertyDeclarations( GML_32 ) ) {
            Mapping propMapping = generatePropMapping( pt, mc );
            propToMapping.put( pt.getName(), propMapping );
        }
        return new FeatureTypeMapping( ft.getName(), table, fidMapping, propToMapping );
    }

    private Mapping generatePropMapping( PropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping property '" + pt.getName() + "'" );
        Mapping mapping = null;
        if ( pt instanceof SimplePropertyType ) {
            mapping = generatePropMapping( (SimplePropertyType) pt, mc );
        } else if ( pt instanceof GeometryPropertyType ) {
            mapping = generatePropMapping( (GeometryPropertyType) pt, mc );
        } else if ( pt instanceof FeaturePropertyType ) {
            mapping = generatePropMapping( (FeaturePropertyType) pt, mc );
        } else if ( pt instanceof CustomPropertyType ) {
            mapping = generatePropMapping( (CustomPropertyType) pt, mc );
        } else {
            LOG.warn( "Unhandled property type '" + pt.getClass() + "'" );
        }
        return mapping;
    }

    private PrimitiveMapping generatePropMapping( SimplePropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping simple property '" + pt.getName() + "'" );
        PropertyName path = new PropertyName( pt.getName() );
        // TODO
        String column = pt.getName().getLocalPart().toLowerCase();
        MappingExpression mapping = new DBField( column );
        // TODO
        JoinChain jc = null;
        return new PrimitiveMapping( path, mapping, pt.getPrimitiveType(), jc );
    }

    private GeometryMapping generatePropMapping( GeometryPropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping geometry property '" + pt.getName() + "'" );
        PropertyName path = new PropertyName( pt.getName() );

        MappingContext propMc = null;
        JoinChain jc = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            // TODO
            // jc = new JoinChain( dbf1, dbf2 );
        }
        MappingExpression mapping = new DBField( propMc.getColumn() );
        return new GeometryMapping( path, mapping, pt.getGeometryType(), storageDim, storageCrs, storageSrid, jc );
    }

    private FeatureMapping generatePropMapping( FeaturePropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping feature property '" + pt.getName() + "'" );
        PropertyName path = new PropertyName( pt.getName() );
        MappingExpression mapping = null;
        JoinChain jc = null;
        MappingContext mc2 = null;
        if ( pt.getMaxOccurs() == 1 ) {
            mc2 = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            mc2 = mcManager.mapOneToManyElements( mc, pt.getName() );
            jc = new JoinChain( new DBField( mc.getTable(), "id" ), new DBField( mc2.getTable(), "parentfk" ) );
        }
        mapping = new DBField( mc2.getColumn() );
        return new FeatureMapping( path, mapping, pt.getFTName(), jc );
    }

    private CompoundMapping generatePropMapping( CustomPropertyType pt, MappingContext mc ) {

        LOG.info( "Mapping custom property '" + pt.getName() + "'" );

        XSComplexTypeDefinition xsTypeDef = pt.getXSDValueType();
        if ( xsTypeDef == null ) {
            LOG.warn( "No XSD type definition available." );
            return null;
        }

        PropertyName path = new PropertyName( pt.getName() );

        MappingContext propMc = null;
        JoinChain jc = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            // TODO
            // jc =
        }
        MappingExpression mapping = new DBField( propMc.getColumn() );
        List<Mapping> particles = generateMapping( pt.getXSDValueType(), propMc, new HashMap<QName, QName>() );
        return new CompoundMapping( path, mapping, particles, jc );
    }

    private List<Mapping> generateMapping( XSComplexTypeDefinition typeDef, MappingContext mc, Map<QName, QName> elements ) {
        List<Mapping> particles = new ArrayList<Mapping>();

        // text node
        if ( typeDef.getContentType() != CONTENTTYPE_EMPTY && typeDef.getContentType() != CONTENTTYPE_ELEMENT ) {
            // TODO
            NamespaceContext nsContext = null;
            PropertyName path = new PropertyName( "text()", nsContext );
            DBField dbField = new DBField( mc.getTable(), mc.getColumn() );
            PrimitiveType pt = XMLValueMangler.getPrimitiveType( typeDef.getSimpleType() );
            particles.add( new PrimitiveMapping( path, dbField, pt, null ) );
        }

        // attributes
        XSObjectList attributeUses = typeDef.getAttributeUses();
        for ( int i = 0; i < attributeUses.getLength(); i++ ) {
            XSAttributeDeclaration attrDecl = ( (XSAttributeUse) attributeUses.item( i ) ).getAttrDeclaration();
            QName attrName = new QName( attrDecl.getName() );
            if ( attrDecl.getNamespace() != null ) {
                attrName = new QName( attrDecl.getNamespace(), attrDecl.getName() );
            }
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            // TODO
            NamespaceContext nsContext = null;
            PropertyName path = new PropertyName( "@" + getName( attrName ), nsContext );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            PrimitiveType pt = XMLValueMangler.getPrimitiveType( attrDecl.getTypeDefinition() );
            particles.add( new PrimitiveMapping( path, dbField, pt, null ) );
        }

        // child elements
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            List<Mapping> childElMappings = generateMapping( particle, 1, mc, elements );
            particles.addAll( childElMappings );
        }
        return particles;
    }

    private List<Mapping> generateMapping( XSParticle particle, int maxOccurs, MappingContext mc,
                                         Map<QName, QName> elements ) {
        List<Mapping> childElMappings = new ArrayList<Mapping>();
        if ( particle.getMaxOccursUnbounded() ) {
            childElMappings.addAll( generateMapping( particle.getTerm(), -1, mc, elements ) );
        } else {
            for ( int i = 1; i <= particle.getMaxOccurs(); i++ ) {
                childElMappings.addAll( generateMapping( particle.getTerm(), i, mc, elements ) );
            }
        }
        return childElMappings;
    }

    private List<Mapping> generateMapping( XSTerm term, int occurence, MappingContext mc, Map<QName, QName> elements ) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        if ( term instanceof XSElementDeclaration ) {
            mappings.addAll( generateMapping( (XSElementDeclaration) term, occurence, mc, elements ) );
        } else if ( term instanceof XSModelGroup ) {
            mappings.addAll( generateMapping( (XSModelGroup) term, occurence, mc, elements ) );
        } else {
            mappings.addAll( generateMapping( (XSWildcard) term, occurence, mc, elements ) );
        }
        return mappings;
    }

    private List<Mapping> generateMapping( XSElementDeclaration elDecl, int occurence, MappingContext mc,
                                         Map<QName, QName> elements ) {

        List<Mapping> mappings = new ArrayList<Mapping>();

        // consider every concrete element substitution
        List<XSElementDeclaration> substitutions = appSchema.getXSModel().getSubstitutions( elDecl, null, true, true );

        for ( XSElementDeclaration substitution : substitutions ) {

            Map<QName, QName> elements2 = new LinkedHashMap<QName, QName>( elements );

            QName elName = new QName( substitution.getName() );
            if ( substitution.getNamespace() != null ) {
                elName = new QName( substitution.getNamespace(), substitution.getName() );
            }

            MappingContext elMC = null;
            if ( occurence == 1 ) {
                elMC = mcManager.mapOneToOneElement( mc, elName );
            } else {
                elMC = mcManager.mapOneToManyElements( mc, elName );
            }

            NamespaceContext nsContext = null;
            PropertyName path = new PropertyName( getName( elName ), nsContext );

            if ( appSchema.getFeatureType( elName ) != null ) {
                QName valueFtName = elName;
                JoinChain jc = null;
                DBField mapping = null;
                if ( occurence == -1 ) {
                    // TODO
                } else {
                    mapping = new DBField( elMC.getColumn() );
                }
                mappings.add( new FeatureMapping( path, mapping, valueFtName, jc ) );
            } else if ( appSchema.getXSModel().getGeometryElement( elName ) != null ) {
                JoinChain jc = null;
                DBField mapping = null;
                // TODO
                GeometryType gt = GeometryType.GEOMETRY;
                // TODO
                CoordinateDimension dim = CoordinateDimension.DIM_2;
                // TODO
                String srid = "-1";
                if ( occurence == -1 ) {
                    // TODO
                    // writeJoinedTable( writer, elMC.getTable() );
                } else {
                    mapping = new DBField( elMC.getColumn() );
                }
                mappings.add( new GeometryMapping( path, mapping, gt, dim, storageCrs, srid, jc ) );
            } else {
                XSTypeDefinition typeDef = substitution.getTypeDefinition();
                QName complexTypeName = getQName( typeDef );
                // TODO multiple elements with same name?
                QName complexTypeName2 = elements2.get( elName );
                if ( complexTypeName2 != null && complexTypeName2.equals( complexTypeName ) ) {
                    // during this mapping traversal, there already has been an element with this name and type
                    StringBuffer sb = new StringBuffer( "Path: " );
                    for ( QName qName : elements2.keySet() ) {
                        sb.append( qName );
                        sb.append( " -> " );
                    }
                    sb.append( elName );
                    LOG.info( "Skipping complex element '" + elName + "' -- detected recursion: " + sb );
                    continue;
                }
                elements2.put( elName, getQName( typeDef ) );

                MappingExpression mapping = new DBField( elMC.getColumn() );

                JoinChain jc = null;

                if ( occurence == -1 ) {
                    // TODO
                    // writeJoinedTable( writer, elMC.getTable() );
                }

                if ( typeDef instanceof XSComplexTypeDefinition ) {
                    List<Mapping> particles = generateMapping( (XSComplexTypeDefinition) typeDef, elMC, elements2 );
                    mappings.add( new CompoundMapping( path, mapping, particles, jc ) );
                } else {
                    PrimitiveType pt = XMLValueMangler.getPrimitiveType( (XSSimpleTypeDefinition) typeDef );
                    mappings.add( new PrimitiveMapping( path, mapping, pt, jc ) );
                }
            }
        }
        return mappings;
    }

    private List<Mapping> generateMapping( XSModelGroup modelGroup, int occurrence, MappingContext mc,
                                         Map<QName, QName> elements ) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        XSObjectList particles = modelGroup.getParticles();
        for ( int i = 0; i < particles.getLength(); i++ ) {
            XSParticle particle = (XSParticle) particles.item( i );
            mappings.addAll( generateMapping( particle, occurrence, mc, elements ) );
        }
        return mappings;
    }

    private List<Mapping> generateMapping( XSWildcard wildCard, int occurrence, MappingContext mc,
                                         Map<QName, QName> elements ) {
        LOG.warn( "Handling of wild cards not implemented yet." );
        StringBuffer sb = new StringBuffer( "Path: " );
        for ( QName qName : elements.keySet() ) {
            sb.append( qName );
            sb.append( " -> " );
        }
        sb.append( "wildcard" );
        LOG.info( "Skipping wildcard at path: " + sb );
        return new ArrayList<Mapping>();
    }

    //
    // private String getPrimitiveTypeName( XSSimpleTypeDefinition typeDef ) {
    // if ( typeDef == null ) {
    // return "string";
    // }
    // return XMLValueMangler.getPrimitiveType( typeDef ).getXSTypeName();
    // }
    //

    private QName getQName( XSTypeDefinition xsType ) {
        QName name = null;
        if ( !xsType.getAnonymous() ) {
            name = new QName( xsType.getNamespace(), xsType.getName() );
        }
        return name;
    }

    private String getName( QName name ) {
        if ( name.getNamespaceURI() != null && !name.getNamespaceURI().equals( NULL_NS_URI ) ) {
            String prefix = appSchema.getXSModel().getNamespacePrefixes().get( name.getNamespaceURI() );
            return prefix + ":" + name.getLocalPart();
        }
        return name.getLocalPart();
    }
}