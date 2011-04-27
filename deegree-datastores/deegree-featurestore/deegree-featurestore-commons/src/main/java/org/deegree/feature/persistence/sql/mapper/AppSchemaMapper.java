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
package org.deegree.feature.persistence.sql.mapper;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.feature.persistence.sql.blob.BlobCodec.Compression.NONE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.feature.persistence.sql.BBoxTableMapping;
import org.deegree.feature.persistence.sql.DataTypeMapping;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.UUIDGenerator;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
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

    private List<DataTypeMapping> dtMappings = new ArrayList<DataTypeMapping>();

    private final MappedApplicationSchema mappedSchema;

    private final HashMap<String, String> nsToPrefix;

    private final GeometryStorageParams geometryParams;

    /**
     * Creates a new {@link AppSchemaMapper} instance for the given schema.
     * 
     * @param appSchema
     *            application schema to be mapped, must not be <code>null</code>
     * @param createBlobMapping
     *            true, if BLOB mapping should be performed, false otherwise
     * @param createRelationalMapping
     *            true, if relational mapping should be performed, false otherwise
     * @param geometryParams
     *            parameters for storing geometries, must not be <code>null</code>
     */
    public AppSchemaMapper( ApplicationSchema appSchema, boolean createBlobMapping, boolean createRelationalMapping,
                            GeometryStorageParams geometryParams, int maxLength ) {

        this.appSchema = appSchema;
        this.geometryParams = geometryParams;

        List<FeatureType> ftList = appSchema.getFeatureTypes( null, false, false );
        FeatureType[] fts = appSchema.getFeatureTypes( null, false, false ).toArray( new FeatureType[ftList.size()] );
        Map<FeatureType, FeatureType> ftToSuperFt = appSchema.getFtToSuperFt();
        Map<String, String> prefixToNs = appSchema.getNamespaceBindings();
        GMLSchemaInfoSet xsModel = appSchema.getXSModel();

        FeatureTypeMapping[] ftMappings = null;

        nsToPrefix = new HashMap<String, String>();
        Iterator<String> nsIter = CommonNamespaces.getNamespaceContext().getNamespaceURIs();
        while ( nsIter.hasNext() ) {
            String ns = nsIter.next();
            nsToPrefix.put( ns, CommonNamespaces.getNamespaceContext().getPrefix( ns ) );
        }
        nsToPrefix.putAll( xsModel.getNamespacePrefixes() );

        mcManager = new MappingContextManager( nsToPrefix, maxLength );
        if ( createRelationalMapping ) {
            ftMappings = generateFtMappings( fts );
        }

        BBoxTableMapping bboxMapping = createBlobMapping ? generateBBoxMapping() : null;
        BlobMapping blobMapping = createBlobMapping ? generateBlobMapping() : null;

        DataTypeMapping[] dtMappings = this.dtMappings.toArray( new DataTypeMapping[this.dtMappings.size()] );

        this.mappedSchema = new MappedApplicationSchema( fts, ftToSuperFt, prefixToNs, xsModel, ftMappings, dtMappings,
                                                         bboxMapping, blobMapping, geometryParams );
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
        BlobCodec codec = new BlobCodec( appSchema.getXSModel().getVersion(), NONE );
        return new BlobMapping( table, geometryParams.getCrs(), codec );
    }

    private BBoxTableMapping generateBBoxMapping() {
        // TODO
        String ftTable = "FEATURE_TYPES";
        return new BBoxTableMapping( ftTable, geometryParams.getCrs() );
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
        MappingContext mc = mcManager.newContext( ft.getName(), "attr_gml_id" );

        // TODO
        QTableName table = new QTableName( mc.getTable() );
        // TODO
        IDGenerator generator = new UUIDGenerator();
        String prefix = ft.getName().getPrefix().toUpperCase() + "_" + ft.getName().getLocalPart().toUpperCase() + "_";
        Pair<String, BaseType> fidColumn = new Pair<String, BaseType>( "attr_gml_id", STRING );
        FIDMapping fidMapping = new FIDMapping( prefix, "_", Collections.singletonList( fidColumn ), generator );

        List<Mapping> mappings = new ArrayList<Mapping>();
        for ( PropertyType pt : ft.getPropertyDeclarations( appSchema.getXSModel().getVersion() ) ) {
            mappings.add( generatePropMapping( pt, mc ) );
        }
        return new FeatureTypeMapping( ft.getName(), table, fidMapping, mappings );
    }

    private Mapping generatePropMapping( PropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping property '" + pt.getName() + "'" );

        XSElementDeclaration elDecl = pt.getElementDecl();
        if ( elDecl != null && elDecl.getTypeDefinition() instanceof XSComplexTypeDefinition ) {
            PropertyName path = getPropName( pt.getName() );

            MappingContext propMc = null;
            List<TableJoin> jc = null;
            if ( pt.getMaxOccurs() == 1 ) {
                propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
            } else {
                propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
                jc = generateJoinChain( mc, propMc );
            }
            List<Mapping> particles = null;
            if ( pt instanceof ObjectPropertyType ) {
                particles = generateMapping( (XSComplexTypeDefinition) elDecl.getTypeDefinition(), propMc,
                                             new HashMap<QName, QName>(), (ObjectPropertyType) pt );
            } else {
                particles = generateMapping( (XSComplexTypeDefinition) elDecl.getTypeDefinition(), propMc,
                                             new HashMap<QName, QName>(), pt.isNillable() );
            }
            return new CompoundMapping( path, particles, jc );
        }

        Mapping mapping = null;
        if ( pt instanceof SimplePropertyType ) {
            mapping = generatePropMapping( (SimplePropertyType) pt, mc );
        } else if ( pt instanceof GeometryPropertyType ) {
            mapping = generatePropMapping( (GeometryPropertyType) pt, mc );
        } else if ( pt instanceof FeaturePropertyType ) {
            mapping = generatePropMapping( (FeaturePropertyType) pt, mc );
        } else if ( pt instanceof CustomPropertyType ) {
            mapping = generatePropMapping( (CustomPropertyType) pt, mc );
        } else if ( pt instanceof CodePropertyType ) {
            mapping = generatePropMapping( (CodePropertyType) pt, mc );
        } else {
            LOG.warn( "Unhandled property type '" + pt.getName() + "': " + pt.getClass().getName() );
        }
        return mapping;
    }

    private PrimitiveMapping generatePropMapping( SimplePropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping simple property '" + pt.getName() + "'" );
        PropertyName path = getPropName( pt.getName() );
        MappingContext propMc = null;
        List<TableJoin> jc = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            LOG.warn( "TODO: Build JoinChain" );
        }
        MappingExpression mapping = new DBField( propMc.getColumn() );
        return new PrimitiveMapping( path, mapping, pt.getPrimitiveType(), jc );
    }

    private DBField getNilMapping( MappingContext ctx ) {
        QName nilAttrName = new QName( CommonNamespaces.XSINS, "nil", "xsi" );
        return new DBField( mcManager.mapOneToOneAttribute( ctx, nilAttrName ).getColumn() );
    }

    private GeometryMapping generatePropMapping( GeometryPropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping geometry property '" + pt.getName() + "'" );
        PropertyName path = getPropName( pt.getName() );
        MappingContext propMc = null;
        List<TableJoin> jc = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            LOG.warn( "TODO: Build JoinChain" );
        }
        MappingExpression mapping = new DBField( propMc.getColumn() );
        return new GeometryMapping( path, mapping, pt.getGeometryType(), geometryParams, jc );
    }

    private Mapping generatePropMapping( FeaturePropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping feature property '" + pt.getName() + "'" );
        PropertyName path = getPropName( pt.getName() );
        List<TableJoin> jc = null;
        MappingContext fkMC = null;
        MappingContext hrefMC = null;
        if ( pt.getMaxOccurs() == 1 ) {
            fkMC = mcManager.mapOneToOneElement( mc, pt.getName() );
            String ns = pt.getName().getNamespaceURI();
            String prefix = pt.getName().getPrefix();
            String localName = pt.getName().getLocalPart() + "_href";
            hrefMC = mcManager.mapOneToOneElement( mc, new QName( ns, localName, prefix ) );
        } else {
            MappingContext mc2 = mcManager.mapOneToManyElements( mc, pt.getName() );
            jc = generateJoinChain( mc, mc2 );
            fkMC = mcManager.mapOneToOneElement( mc, new QName( "fk" ) );
            hrefMC = mcManager.mapOneToOneElement( mc, new QName( "href" ) );
        }
        return new FeatureMapping( path, new DBField( fkMC.getColumn() ), new DBField( hrefMC.getColumn() ),
                                   pt.getFTName(), jc );
    }

    private CompoundMapping generatePropMapping( CustomPropertyType pt, MappingContext mc ) {

        LOG.debug( "Mapping custom property '" + pt.getName() + "'" );

        XSComplexTypeDefinition xsTypeDef = pt.getXSDValueType();
        if ( xsTypeDef == null ) {
            LOG.warn( "No XSD type definition available for custom property '" + pt.getName() + "'. Skipping it." );
            return null;
        }

        PropertyName path = getPropName( pt.getName() );

        MappingContext propMc = null;
        List<TableJoin> jc = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            jc = generateJoinChain( mc, propMc );
        }
        List<Mapping> particles = generateMapping( pt.getXSDValueType(), propMc, new HashMap<QName, QName>(),
                                                   pt.isNillable() );
        return new CompoundMapping( path, particles, jc );
    }

    private CompoundMapping generatePropMapping( CodePropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping code property '" + pt.getName() + "'" );
        PropertyName path = getPropName( pt.getName() );
        MappingContext propMc = null;
        MappingContext codeSpaceMc = null;
        List<TableJoin> jc = null;
        MappingExpression mapping = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
            codeSpaceMc = mcManager.mapOneToOneAttribute( propMc, new QName( "codeSpace" ) );
            mapping = new DBField( propMc.getColumn() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            codeSpaceMc = mcManager.mapOneToOneAttribute( propMc, new QName( "codeSpace" ) );
            jc = generateJoinChain( mc, propMc );
            mapping = new DBField( "value" );
        }
        MappingExpression csMapping = new DBField( codeSpaceMc.getColumn() );
        List<Mapping> particles = new ArrayList<Mapping>();
        particles.add( new PrimitiveMapping( new PropertyName( "text()", null ), mapping, STRING, null ) );
        particles.add( new PrimitiveMapping( new PropertyName( "@codeSpace", null ), csMapping, BaseType.STRING, null ) );
        return new CompoundMapping( path, particles, jc );
    }

    private List<TableJoin> generateJoinChain( MappingContext from, MappingContext to ) {
        QTableName fromTable = new QTableName( from.getTable() );
        QTableName toTable = new QTableName( to.getTable() );
        List<String> fromColumns = Collections.singletonList( from.getIdColumn() );
        List<String> toColumns = Collections.singletonList( "parentfk" );
        List<String> orderColumns = Collections.singletonList( "num" );
        TableJoin join = new TableJoin( fromTable, toTable, fromColumns, toColumns, orderColumns, true );
        return Collections.singletonList( join );
    }

    private List<Mapping> generateMapping( XSComplexTypeDefinition typeDef, MappingContext mc,
                                           Map<QName, QName> elements, boolean isNillable ) {

        List<Mapping> particles = new ArrayList<Mapping>();

        // text node
        if ( typeDef.getContentType() != CONTENTTYPE_EMPTY && typeDef.getContentType() != CONTENTTYPE_ELEMENT ) {
            // TODO
            NamespaceContext nsContext = null;
            PropertyName path = new PropertyName( "text()", nsContext );
            String column = mc.getColumn();
            if ( column == null || column.isEmpty() ) {
                column = "value";
            }
            DBField dbField = new DBField( mc.getTable(), column );
            BaseType pt = BaseType.STRING;
            if ( typeDef.getSimpleType() != null ) {
                pt = BaseType.valueOf( typeDef.getSimpleType() );
            }
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
            BaseType pt = BaseType.valueOf( attrDecl.getTypeDefinition() );
            particles.add( new PrimitiveMapping( path, dbField, pt, null ) );
        }

        // xsi:nil attribute
        if ( isNillable ) {
            QName attrName = new QName( XSINS, "nil", "xsi" );
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            PropertyName path = new PropertyName( "@" + getName( attrName ), null );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            particles.add( new PrimitiveMapping( path, dbField, BOOLEAN, null ) );
        }

        // child elements
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            List<Mapping> childElMappings = generateMapping( particle, 1, mc, elements );
            particles.addAll( childElMappings );
        }
        return particles;
    }

    private List<Mapping> generateMapping( XSComplexTypeDefinition typeDef, MappingContext mc,
                                           Map<QName, QName> elements, ObjectPropertyType opt ) {

        List<Mapping> particles = new ArrayList<Mapping>();

        // attributes
        XSObjectList attributeUses = typeDef.getAttributeUses();
        for ( int i = 0; i < attributeUses.getLength(); i++ ) {
            XSAttributeDeclaration attrDecl = ( (XSAttributeUse) attributeUses.item( i ) ).getAttrDeclaration();
            QName attrName = new QName( attrDecl.getName() );
            if ( XLNNS.equals( attrDecl.getNamespace() ) ) {
                // TODO should all xlink attributes be skipped?
                continue;
            }
            if ( attrDecl.getNamespace() != null ) {
                attrName = new QName( attrDecl.getNamespace(), attrDecl.getName() );
            }
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            // TODO
            NamespaceContext nsContext = null;
            PropertyName path = new PropertyName( "@" + getName( attrName ), nsContext );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            BaseType pt = BaseType.valueOf( attrDecl.getTypeDefinition() );
            particles.add( new PrimitiveMapping( path, dbField, pt, null ) );
        }

        // xsi:nil attribute
        if ( opt.isNillable() ) {
            QName attrName = new QName( XSINS, "nil", "xsi" );
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            PropertyName path = new PropertyName( "@" + getName( attrName ), null );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            particles.add( new PrimitiveMapping( path, dbField, BOOLEAN, null ) );
        }

        PropertyName path = new PropertyName( ".", null );
        if ( opt instanceof GeometryPropertyType ) {
            GeometryType gt = GeometryType.GEOMETRY;
            // TODO
            CoordinateDimension dim = CoordinateDimension.DIM_2;
            // TODO
            String srid = "-1";
            MappingContext elMC = mcManager.mapOneToOneElement( mc, new QName( "value" ) );
            particles.add( new GeometryMapping( path, new DBField( elMC.getColumn() ), gt, geometryParams, null ) );
        } else if ( opt instanceof FeaturePropertyType ) {
            QName valueFtName = ( (FeaturePropertyType) opt ).getFTName();
            MappingContext fkMC = mcManager.mapOneToOneElement( mc, new QName( "fk" ) );
            MappingContext hrefMC = mcManager.mapOneToOneElement( mc, new QName( "href" ) );
            particles.add( new FeatureMapping( path, new DBField( fkMC.getColumn() ),
                                               new DBField( hrefMC.getColumn() ), valueFtName, null ) );
        } else {
            LOG.warn( "Unhandled object property type '" + opt.getClass() + "'." );
        }

        return particles;
    }

    private List<Mapping> generateMapping( XSParticle particle, int maxOccurs, MappingContext mc,
                                           Map<QName, QName> elements ) {

        List<Mapping> childElMappings = new ArrayList<Mapping>();

        // // check if the particle term defines a GMLObjectPropertyType
        // if ( particle.getTerm() instanceof XSElementDeclaration ) {
        // XSElementDeclaration elDecl = (XSElementDeclaration) particle.getTerm();
        // QName elName = new QName( elDecl.getNamespace(), elDecl.getName() );
        // int minOccurs = particle.getMinOccurs();
        // maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
        // // TODO
        // List<PropertyType> ptSubstitutions = null;
        // ObjectPropertyType pt = appSchema.getXSModel().getGMLPropertyDecl( elDecl, elName, minOccurs, maxOccurs,
        // ptSubstitutions );
        // if ( pt != null ) {
        // if ( pt instanceof GeometryPropertyType ) {
        // childElMappings.add( generatePropMapping( (GeometryPropertyType) pt, mc ) );
        // } else if ( pt instanceof FeaturePropertyType ) {
        // childElMappings.add( generatePropMapping( (FeaturePropertyType) pt, mc ) );
        // } else {
        // LOG.warn( "TODO: Generic object property type " + pt );
        // }
        // }
        // }
        if ( childElMappings.isEmpty() ) {
            if ( particle.getMaxOccursUnbounded() ) {
                childElMappings.addAll( generateMapping( particle.getTerm(), -1, mc, elements ) );
            } else {
                for ( int i = 1; i <= particle.getMaxOccurs(); i++ ) {
                    childElMappings.addAll( generateMapping( particle.getTerm(), i, mc, elements ) );
                }
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

        QName eName = new QName( elDecl.getNamespace(), elDecl.getName() );
        if ( eName.equals( new QName( "http://www.opengis.net/gml/3.2", "AbstractCRS" ) ) ) {
            LOG.warn( "Skipping mapping of AbstractCRS element" );
            return mappings;
        }
        if ( eName.equals( new QName( "http://www.opengis.net/gml/3.2", "TimeOrdinalEra" ) ) ) {
            LOG.warn( "Skipping mapping of TimeOrdinalEra element" );
            return mappings;
        }

        if ( eName.equals( new QName( "http://www.opengis.net/gml/3.2", "TimePeriod" ) ) ) {
            LOG.warn( "Skipping mapping of TimePeriod element" );
            return mappings;
        }

        if ( eName.equals( new QName( "http://www.isotc211.org/2005/gmd", "EX_GeographicDescription" ) ) ) {
            LOG.warn( "Skipping mapping of EX_GeographicDescription element" );
        }

        // consider every concrete element substitution
        List<XSElementDeclaration> substitutions = appSchema.getXSModel().getSubstitutions( elDecl, null, true, true );
        if ( eName.equals( new QName( "http://www.isotc211.org/2005/gco", "CharacterString" ) ) ) {
            substitutions.clear();
            substitutions.add( elDecl );
        }

        if ( eName.equals( new QName( "http://www.isotc211.org/2005/gmd", "MD_Identifier" ) ) ) {
            substitutions.clear();
            substitutions.add( elDecl );
        }

        NamespaceContext nsContext = null;

        for ( XSElementDeclaration substitution : substitutions ) {
            ObjectPropertyType opt = appSchema.getCustomElDecl( substitution );
            if ( opt != null ) {
                mappings.add( generatePropMapping( opt, mc ) );
            } else {
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

                List<TableJoin> jc = null;
                if ( occurence == -1 ) {
                    jc = generateJoinChain( mc, elMC );
                }

                PropertyName path = new PropertyName( getName( elName ), nsContext );
                if ( typeDef instanceof XSComplexTypeDefinition ) {
                    List<Mapping> particles = generateMapping( (XSComplexTypeDefinition) typeDef, elMC, elements2,
                                                               substitution.getNillable() );
                    mappings.add( new CompoundMapping( path, particles, jc ) );
                } else {
                    MappingExpression mapping = new DBField( elMC.getColumn() );
                    BaseType pt = BaseType.valueOf( (XSSimpleTypeDefinition) typeDef );
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
        LOG.debug( "Handling of wild cards not implemented yet." );
        StringBuffer sb = new StringBuffer( "Path: " );
        for ( QName qName : elements.keySet() ) {
            sb.append( qName );
            sb.append( " -> " );
        }
        sb.append( "wildcard" );
        LOG.debug( "Skipping wildcard at path: " + sb );
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
            String prefix = nsToPrefix.get( name.getNamespaceURI() );
            return prefix + ":" + name.getLocalPart();
        }
        return name.getLocalPart();
    }

    private PropertyName getPropName( QName name ) {
        if ( name.getNamespaceURI() != null && !name.getNamespaceURI().equals( NULL_NS_URI ) ) {
            String prefix = name.getPrefix();
            if ( prefix == null || prefix.isEmpty() ) {
                prefix = nsToPrefix.get( name.getNamespaceURI() );
            }
            name = new QName( name.getNamespaceURI(), name.getLocalPart(), prefix );
        }
        return new PropertyName( name );
    }
}