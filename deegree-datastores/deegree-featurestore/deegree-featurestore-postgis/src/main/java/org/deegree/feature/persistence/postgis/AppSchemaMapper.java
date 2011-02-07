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
 * Creates {@link MappedApplicationSchema} instances from {@link ApplicationSchema}s by inferring a canonical relational
 * schema.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AppSchemaMapper {

    private static Logger LOG = LoggerFactory.getLogger( AppSchemaMapper.class );

    private final MappingContextManager mcManager;

    private final ApplicationSchema appSchema;

    private final MappedApplicationSchema mappedSchema;

    /**
     * Creates a new {@link AppSchemaMapper} instance for the given schema.
     * 
     * @param appSchema
     *            application schema to be mapped, must not be <code>null</code>
     */
    public AppSchemaMapper( ApplicationSchema appSchema ) {
        this.appSchema = appSchema;

        FeatureType[] fts = appSchema.getFeatureTypes();
        Map<FeatureType, FeatureType> ftToSuperFt = appSchema.getFtToSuperFt();
        Map<String, String> prefixToNs = appSchema.getNamespaceBindings();
        GMLSchemaInfoSet xsModel = appSchema.getXSModel();

        mcManager = new MappingContextManager( xsModel.getNamespacePrefixes() );

        FeatureTypeMapping[] ftMappings = generateFtMappings( fts );
        // TODO
        CRS storageCRS = CRS.EPSG_4326;
        BBoxTableMapping bboxMapping = generateBBoxMapping();
        BlobMapping blobMapping = generateBlobMapping();

        this.mappedSchema = new MappedApplicationSchema( fts, ftToSuperFt, prefixToNs, xsModel, ftMappings, storageCRS,
                                                         bboxMapping, blobMapping );
    }

    public MappedApplicationSchema getMappedSchema() {
        return mappedSchema;
    }

    private BlobMapping generateBlobMapping() {
        // TODO
        String table = "GML_OBJECTS";
        // TODO
        CRS storageCRS = CRS.EPSG_4326;
        // TODO
        BlobCodec codec = new BlobCodec( GMLVersion.GML_32, NONE );
        return new BlobMapping( table, storageCRS, codec );
    }

    private BBoxTableMapping generateBBoxMapping() {
        // TODO
        String ftTable = "FEATURE_TYPES";
        // TODO
        CRS crs = CRS.EPSG_4326;
        return new BBoxTableMapping( ftTable, crs );
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
        FIDMapping fidMapping = new FIDMapping( "", "gml_id", STRING, generator );

        Map<QName, Mapping> propToMapping = new HashMap<QName, Mapping>();
        // TODO: gml properties
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
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
        // TODO
        String column = pt.getName().getLocalPart().toLowerCase();
        MappingExpression mapping = new DBField( column );
        // TODO
        CoordinateDimension dim = DIM_2;
        // TODO
        CRS crs = CRS.EPSG_4326;
        // TODO
        String srid = "4326";
        // TODO
        JoinChain jc = null;
        return new GeometryMapping( path, mapping, pt.getGeometryType(), dim, crs, srid, jc );
    }

    private FeatureMapping generatePropMapping( FeaturePropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping feature property '" + pt.getName() + "'" );
        PropertyName path = new PropertyName( pt.getName() );
        // TODO
        String column = pt.getName().getLocalPart().toLowerCase();
        MappingExpression mapping = new DBField( column );
        // TODO
        JoinChain jc = null;
        return new FeatureMapping( path, mapping, pt.getFTName(), jc );
    }

    private CompoundMapping generatePropMapping( CustomPropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping custom property '" + pt.getName() + "'" );
        PropertyName path = new PropertyName( pt.getName() );
        // TODO
        String column = pt.getName().getLocalPart().toLowerCase();
        MappingExpression mapping = new DBField( column );
        List<Mapping> particles = createMapping( pt.getXSDValueType(), mc, new HashMap<QName, QName>() );
        // TODO
        JoinChain jc = null;
        return new CompoundMapping( path, mapping, particles, jc );
    }

    private List<Mapping> createMapping( XSComplexTypeDefinition typeDef, MappingContext mc, Map<QName, QName> elements ) {
        List<Mapping> particles = new ArrayList<Mapping>();

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
            DBField dbField = new DBField( attrMc.getColumn(), attrMc.getTable() );
            PrimitiveType pt = XMLValueMangler.getPrimitiveType( attrDecl.getTypeDefinition() );
            particles.add( new PrimitiveMapping( path, dbField, pt, null ) );
        }

        // text node
        if ( typeDef.getContentType() != CONTENTTYPE_EMPTY && typeDef.getContentType() != CONTENTTYPE_ELEMENT ) {
            MappingContext primitiveMc = mcManager.mapOneToOneElement( mc, new QName( "value" ) );
            // TODO
            NamespaceContext nsContext = null;
            PropertyName path = new PropertyName( "text()", nsContext );
            DBField dbField = new DBField( primitiveMc.getColumn(), primitiveMc.getTable() );
            PrimitiveType pt = XMLValueMangler.getPrimitiveType( typeDef.getSimpleType() );
            particles.add( new PrimitiveMapping( path, dbField, pt, null ) );
        }

        // child elements
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            List<Mapping> childElMappings = createMapping( particle, 1, mc, elements );
            particles.addAll( childElMappings );
        }
        return particles;
    }

    private List<Mapping> createMapping( XSParticle particle, int maxOccurs, MappingContext mc,
                                         Map<QName, QName> elements ) {
        List<Mapping> childElMappings = new ArrayList<Mapping>();
        if ( particle.getMaxOccursUnbounded() ) {
            childElMappings.addAll( createMapping( particle.getTerm(), -1, mc, elements ) );
        } else {
            for ( int i = 1; i <= particle.getMaxOccurs(); i++ ) {
                childElMappings.addAll( createMapping( particle.getTerm(), i, mc, elements ) );
            }
        }
        return childElMappings;
    }

    private List<Mapping> createMapping( XSTerm term, int occurence, MappingContext mc, Map<QName, QName> elements ) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        if ( term instanceof XSElementDeclaration ) {
            mappings.addAll( createMapping( (XSElementDeclaration) term, occurence, mc, elements ) );
        } else if ( term instanceof XSModelGroup ) {
            mappings.addAll( createMapping( (XSModelGroup) term, occurence, mc, elements ) );
        } else {
            mappings.addAll( createMapping( (XSWildcard) term, occurence, mc, elements ) );
        }
        return mappings;
    }

    private List<Mapping> createMapping( XSElementDeclaration elDecl, int occurence, MappingContext mc,
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
                CRS crs = CRS.EPSG_4326;
                // TODO
                String srid = "-1";
                if ( occurence == -1 ) {
                    // TODO
                    // writeJoinedTable( writer, elMC.getTable() );
                } else {
                    mapping = new DBField( elMC.getColumn() );
                }
                mappings.add( new GeometryMapping( path, mapping, gt, dim, crs, srid, jc ) );
            } else {
//                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "CI_Citation" ) ) ) {
//                    LOG.warn( "Skipping CI_Citation!!!" );
//                    continue;
//                }
//
//                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "CI_Contact" ) ) ) {
//                    LOG.warn( "Skipping CI_Contact!!!" );
//                    continue;
//                }
//
//                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "CI_ResponsibleParty" ) ) ) {
//                    LOG.warn( "Skipping CI_ResponsibleParty!!!" );
//                    continue;
//                }
//
//                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "MD_Resolution" ) ) ) {
//                    LOG.warn( "Skipping MD_Resolution!!!" );
//                    continue;
//                }
//
//                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "EX_Extent" ) ) ) {
//                    LOG.warn( "Skipping EX_Extent!!!" );
//                    continue;
//                }
//
//                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "MD_PixelOrientationCode" ) ) ) {
//                    LOG.warn( "Skipping EX_Extent!!!" );
//                    continue;
//                }
//
//                if ( elName.equals( new QName( CommonNamespaces.GML3_2_NS, "TimeOrdinalEra" ) ) ) {
//                    LOG.warn( "Skipping TimeOrdinalEra!!!" );
//                    continue;
//                }
//
//                if ( elName.equals( new QName( CommonNamespaces.GML3_2_NS, "TimePeriod" ) ) ) {
//                    LOG.warn( "Skipping TimePeriod!!!" );
//                    continue;
//                }
//
//                if ( elName.getNamespaceURI().equals( CommonNamespaces.GML3_2_NS ) ) {
//                    if ( elName.getLocalPart().endsWith( "CRS" ) ) {
//                        LOG.warn( "Skipping " + elName.getLocalPart() + "!!!" );
//                    }
//                    continue;
//                }

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
                    //  TODO
//                    writeJoinedTable( writer, elMC.getTable() );
                }

                if ( typeDef instanceof XSComplexTypeDefinition ) {
                    List<Mapping> particles = createMapping( (XSComplexTypeDefinition) typeDef, elMC, elements2 );
                    mappings.add( new CompoundMapping( path, mapping, particles, jc ) );
                } else {
                    PrimitiveType pt = XMLValueMangler.getPrimitiveType( (XSSimpleTypeDefinition) typeDef );
                    mappings.add( new PrimitiveMapping( path, mapping, pt, jc ) );
                }
            }
        }
        return mappings;
    }

    private List<Mapping> createMapping( XSModelGroup modelGroup, int occurrence, MappingContext mc,
                                         Map<QName, QName> elements ) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        XSObjectList particles = modelGroup.getParticles();
        for ( int i = 0; i < particles.getLength(); i++ ) {
            XSParticle particle = (XSParticle) particles.item( i );
            mappings.addAll( createMapping( particle, occurrence, mc, elements ) );
        }
        return mappings;
    }

    private List<Mapping> createMapping( XSWildcard wildCard, int occurrence, MappingContext mc,
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