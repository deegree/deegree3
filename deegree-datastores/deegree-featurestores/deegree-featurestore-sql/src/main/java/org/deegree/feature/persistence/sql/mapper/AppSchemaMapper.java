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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.feature.persistence.sql.BBoxTableMapping;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.UUIDGenerator;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.expression.ValueReference;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.jaxen.NamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link MappedAppSchema} instances from {@link AppSchema}s by inferring a canonical database mapping.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AppSchemaMapper {

    private static Logger LOG = LoggerFactory.getLogger( AppSchemaMapper.class );

    private final AppSchema appSchema;

    private final MappingContextManager mcManager;

    private final MappedAppSchema mappedSchema;

    private final HashMap<String, String> nsToPrefix;

    private final GeometryStorageParams geometryParams;

    private final boolean useIntegerFids;

    private final int MAX_COMPLEXITY_INDEX = 100;

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
    public AppSchemaMapper( AppSchema appSchema, boolean createBlobMapping, boolean createRelationalMapping,
                            GeometryStorageParams geometryParams, int maxLength, boolean usePrefixedSQLIdentifiers,
                            boolean useIntegerFids ) {

        this.appSchema = appSchema;
        this.geometryParams = geometryParams;
        this.useIntegerFids = useIntegerFids;

        List<FeatureType> ftList = appSchema.getFeatureTypes( null, false, false );
        List<FeatureType> blackList = new ArrayList<FeatureType>();
        for ( FeatureType ft : ftList ) {
            if ( ft.getName().getNamespaceURI().equals( appSchema.getGMLSchema().getVersion().getNamespace() ) ) {
                blackList.add( ft );
            }
        }

        ftList.removeAll( blackList );
        FeatureType[] fts = ftList.toArray( new FeatureType[ftList.size()] );
        Map<FeatureType, FeatureType> ftToSuperFt = new HashMap<FeatureType, FeatureType>( appSchema.getFtToSuperFt() );
        for ( FeatureType ft : blackList ) {
            ftToSuperFt.remove( ft );
        }
        Map<String, String> prefixToNs = appSchema.getNamespaceBindings();
        GMLSchemaInfoSet xsModel = appSchema.getGMLSchema();

        FeatureTypeMapping[] ftMappings = null;

        nsToPrefix = new HashMap<String, String>();
        Iterator<String> nsIter = CommonNamespaces.getNamespaceContext().getNamespaceURIs();
        while ( nsIter.hasNext() ) {
            String ns = nsIter.next();
            nsToPrefix.put( ns, CommonNamespaces.getNamespaceContext().getPrefix( ns ) );
        }
        nsToPrefix.putAll( xsModel.getNamespacePrefixes() );

        mcManager = new MappingContextManager( nsToPrefix, maxLength, usePrefixedSQLIdentifiers );
        if ( createRelationalMapping ) {
            ftMappings = generateFtMappings( fts );
        }

        BBoxTableMapping bboxMapping = createBlobMapping ? generateBBoxMapping() : null;
        BlobMapping blobMapping = createBlobMapping ? generateBlobMapping() : null;

        this.mappedSchema = new MappedAppSchema( fts, ftToSuperFt, prefixToNs, xsModel, ftMappings, bboxMapping,
                                                 blobMapping, geometryParams, true, null, appSchema.getGeometryTypes(),
                                                 appSchema.getGeometryToSuperType() );
    }

    /**
     * Returns the {@link MappedAppSchema} instance.
     * 
     * @return mapped schema, never <code>null</code>
     */
    public MappedAppSchema getMappedSchema() {
        return mappedSchema;
    }

    private BlobMapping generateBlobMapping() {
        // TODO
        String table = "GML_OBJECTS";
        // TODO
        BlobCodec codec = new BlobCodec( appSchema.getGMLSchema().getVersion(), NONE );
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
        TableName table = new TableName( mc.getTable() );
        // TODO

        FIDMapping fidMapping = null;
        String prefix = ft.getName().getPrefix().toUpperCase() + "_" + ft.getName().getLocalPart().toUpperCase() + "_";
        if ( useIntegerFids ) {
            IDGenerator generator = new AutoIDGenerator();
            Pair<SQLIdentifier, BaseType> fidColumn = new Pair<SQLIdentifier, BaseType>( new SQLIdentifier( "gid" ),
                                                                                         BaseType.INTEGER );
            fidMapping = new FIDMapping( prefix, "_", Collections.singletonList( fidColumn ), generator );
        } else {
            IDGenerator generator = new UUIDGenerator();
            Pair<SQLIdentifier, BaseType> fidColumn = new Pair<SQLIdentifier, BaseType>(
                                                                                         new SQLIdentifier(
                                                                                                            "attr_gml_id" ),
                                                                                         STRING );
            fidMapping = new FIDMapping( prefix, "_", Collections.singletonList( fidColumn ), generator );
        }

        List<Mapping> mappings = new ArrayList<Mapping>();
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            if ( !pt.getName().getNamespaceURI().equals( appSchema.getGMLSchema().getVersion().getNamespace() ) ) {
                mappings.addAll( generatePropMapping( pt, mc ) );
            } else if ( pt.getName().getLocalPart().equals( "identifier" ) ) {
                mappings.addAll( generatePropMapping( pt, mc ) );
            }
        }

        return new FeatureTypeMapping( ft.getName(), table, fidMapping, mappings );
    }

    private List<Mapping> generatePropMapping( PropertyType pt, MappingContext mc ) {

        LOG.debug( "Mapping property '" + pt.getName() + "'" );

        List<Mapping> mappings = new ArrayList<Mapping>();

        XSElementDeclaration elDecl = pt.getElementDecl();

        int before = mcManager.getContextCount();

        try {
            if ( elDecl != null && elDecl.getTypeDefinition() instanceof XSComplexTypeDefinition ) {

                // consider every concrete element substitution
                List<XSElementDeclaration> substitutions = appSchema.getGMLSchema().getSubstitutions( elDecl, null,
                                                                                                      true, true );
                for ( XSElementDeclaration substitution : substitutions ) {
                    try {
                        QName eName = new QName( substitution.getNamespace(), substitution.getName() );
                        ValueReference path = getPropName( eName );

                        MappingContext propMc = null;
                        List<TableJoin> jc = null;
                        if ( pt.getMaxOccurs() == 1 ) {
                            propMc = mcManager.mapOneToOneElement( mc, eName );
                        } else {
                            propMc = mcManager.mapOneToManyElements( mc, eName );
                            jc = generateJoinChain( mc, propMc );
                        }
                        List<Mapping> particles = null;
                        ObjectPropertyType opt = appSchema.getCustomElDecl( substitution );

                        before = mcManager.getContextCount();

                        if ( opt != null ) {
                            particles = generateMapping( (XSComplexTypeDefinition) substitution.getTypeDefinition(),
                                                         propMc, new ArrayList<XSElementDeclaration>(),
                                                         new ArrayList<XSComplexTypeDefinition>(), opt );
                        } else {
                            particles = generateMapping( (XSComplexTypeDefinition) substitution.getTypeDefinition(),
                                                         propMc, new ArrayList<XSElementDeclaration>(),
                                                         new ArrayList<XSComplexTypeDefinition>(),
                                                         substitution.getNillable() );
                        }

                        int complexity = mcManager.getContextCount() - before;
                        LOG.info( "Mapping complexity index of property type '" + eName + "': " + complexity );
                        if ( complexity > MAX_COMPLEXITY_INDEX ) {
                            LOG.warn( "Mapping property type '" + eName + "' exceeds complexity limit: " + complexity );
                            mappings.clear();
                        } else {
                            mappings.add( new CompoundMapping( path, pt.getMinOccurs() == 0, particles, jc, elDecl ) );
                        }
                    } catch ( Throwable t ) {
                        LOG.warn( "Unable to create relational mapping for property type '" + pt.getName() + "': "
                                  + t.getMessage() );
                    }
                }
                return mappings;
            }

            if ( pt instanceof SimplePropertyType ) {
                mappings.add( generatePropMapping( (SimplePropertyType) pt, mc ) );
            } else if ( pt instanceof GeometryPropertyType ) {
                mappings.add( generatePropMapping( (GeometryPropertyType) pt, mc ) );
            } else if ( pt instanceof FeaturePropertyType ) {
                mappings.add( generatePropMapping( (FeaturePropertyType) pt, mc ) );
            } else if ( pt instanceof CustomPropertyType ) {
                mappings.add( generatePropMapping( (CustomPropertyType) pt, mc ) );
            } else if ( pt instanceof CodePropertyType ) {
                mappings.add( generatePropMapping( (CodePropertyType) pt, mc ) );
            } else {
                LOG.warn( "Unhandled property type '" + pt.getName() + "': " + pt.getClass().getName() );
            }
        } catch ( Throwable t ) {
            LOG.warn( "Unable to create relational mapping for property type '" + pt.getName() + "': " + t.getMessage() );
        }

        int complexity = mcManager.getContextCount() - before;
        LOG.debug( "Mapping complexity index of property type '" + pt.getName() + "': " + complexity );
        if ( complexity > MAX_COMPLEXITY_INDEX ) {
            LOG.warn( "Mapping property type '" + pt.getName() + "' exceeds complexity limit: " + complexity );
            mappings.clear();
        }

        return mappings;
    }

    private PrimitiveMapping generatePropMapping( SimplePropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping simple property '" + pt.getName() + "'" );
        ValueReference path = getPropName( pt.getName() );
        MappingContext propMc = null;
        List<TableJoin> jc = null;
        MappingExpression mapping = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
            mapping = new DBField( propMc.getColumn() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            jc = generateJoinChain( mc, propMc );
            mapping = new DBField( "value" );
        }
        return new PrimitiveMapping( path, false, mapping, pt.getPrimitiveType(), jc, null );
    }

    private GeometryMapping generatePropMapping( GeometryPropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping geometry property '" + pt.getName() + "'" );
        ValueReference path = getPropName( pt.getName() );
        MappingContext propMc = null;
        List<TableJoin> jc = null;
        MappingExpression mapping = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
            mapping = new DBField( propMc.getColumn() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            jc = generateJoinChain( mc, propMc );
            mapping = new DBField( "value" );
        }
        return new GeometryMapping( path, pt.getMinOccurs() == 0, mapping, pt.getGeometryType(), geometryParams, jc );
    }

    private Mapping generatePropMapping( FeaturePropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping feature property '" + pt.getName() + "'" );
        ValueReference path = getPropName( pt.getName() );
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

        FeatureType valueFt = pt.getValueFt();
        if ( valueFt != null && valueFt.getSchema().getSubtypes( valueFt ).length == 1 ) {

            TableJoin ftJoin = generateFtJoin( fkMC, valueFt );
            if ( ftJoin != null ) {
                jc = new ArrayList<TableJoin>( jc );
                jc.add( ftJoin );
            } else {
                jc = Collections.singletonList( ftJoin );
            }
        } else {
            LOG.warn( "Ambigous feature property type '" + pt.getName() + "'. Not creating a Join mapping." );
        }

        return new FeatureMapping( path, pt.getMinOccurs() == 0, new DBField( hrefMC.getColumn() ), pt.getFTName(), jc );
    }

    private CompoundMapping generatePropMapping( CustomPropertyType pt, MappingContext mc ) {

        LOG.debug( "Mapping custom property '" + pt.getName() + "'" );

        XSComplexTypeDefinition xsTypeDef = pt.getXSDValueType();
        if ( xsTypeDef == null ) {
            LOG.warn( "No XSD type definition available for custom property '" + pt.getName() + "'. Skipping it." );
            return null;
        }

        ValueReference path = getPropName( pt.getName() );

        MappingContext propMc = null;
        List<TableJoin> jc = null;
        if ( pt.getMaxOccurs() == 1 ) {
            propMc = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            propMc = mcManager.mapOneToManyElements( mc, pt.getName() );
            jc = generateJoinChain( mc, propMc );
        }

        List<XSElementDeclaration> parentEls = new ArrayList<XSElementDeclaration>();
        parentEls.add( pt.getElementDecl() );

        List<XSComplexTypeDefinition> parentCts = new ArrayList<XSComplexTypeDefinition>();

        List<Mapping> particles = new ArrayList<Mapping>();
        try {
            particles = generateMapping( pt.getXSDValueType(), propMc, parentEls, parentCts, pt.isNillable() );
            return new CompoundMapping( path, pt.getMinOccurs() == 0, particles, jc, pt.getElementDecl() );
        } catch ( Throwable t ) {
            LOG.warn( "Full relational mapping of property '" + pt.getName() + "' failed: " + t.getMessage() );
        }
        return new CompoundMapping( path, pt.getMinOccurs() == 0, particles, jc, pt.getElementDecl() );
    }

    private CompoundMapping generatePropMapping( CodePropertyType pt, MappingContext mc ) {
        LOG.debug( "Mapping code property '" + pt.getName() + "'" );
        ValueReference path = getPropName( pt.getName() );
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
        particles.add( new PrimitiveMapping( new ValueReference( "text()", null ), false, mapping,
                                             new PrimitiveType( STRING ), null, null ) );
        particles.add( new PrimitiveMapping( new ValueReference( "@codeSpace", null ), true, csMapping,
                                             new PrimitiveType( STRING ), null, null ) );
        return new CompoundMapping( path, pt.getMinOccurs() == 0, particles, jc, pt.getElementDecl() );
    }

    private List<TableJoin> generateJoinChain( MappingContext from, MappingContext to ) {
        TableName fromTable = new TableName( from.getTable() );
        TableName toTable = new TableName( to.getTable() );
        List<String> fromColumns = Collections.singletonList( from.getIdColumn() );
        List<String> toColumns = Collections.singletonList( "parentfk" );
        List<String> orderColumns = Collections.singletonList( "num" );
        Map<SQLIdentifier, IDGenerator> keyColumnToIdGenerator = new HashMap<SQLIdentifier, IDGenerator>();
        keyColumnToIdGenerator.put( new SQLIdentifier( "id" ), new AutoIDGenerator() );
        TableJoin join = new TableJoin( fromTable, toTable, fromColumns, toColumns, orderColumns, true,
                                        keyColumnToIdGenerator );
        return Collections.singletonList( join );
    }

    private TableJoin generateFtJoin( MappingContext from, FeatureType valueFt ) {
        TableJoin ftJoin = null;
        if ( valueFt != null && valueFt.getSchema().getSubtypes( valueFt ).length == 1 ) {
            LOG.warn( "Ambigous feature join." );
        }
        TableName fromTable = new TableName( from.getTable() );
        TableName toTable = new TableName( "?" );
        List<String> fromColumns = Collections.singletonList( from.getColumn() );
        List<String> toColumns = Collections.singletonList( "attr_gml_id" );
        Map<SQLIdentifier, IDGenerator> keyColumnToIdGenerator = new HashMap<SQLIdentifier, IDGenerator>();
        keyColumnToIdGenerator.put( new SQLIdentifier( "id" ), new AutoIDGenerator() );
        ftJoin = new TableJoin( fromTable, toTable, fromColumns, toColumns, Collections.EMPTY_LIST, false,
                                keyColumnToIdGenerator );
        return ftJoin;
    }

    private List<Mapping> generateMapping( XSComplexTypeDefinition typeDef, MappingContext mc,
                                           List<XSElementDeclaration> parentEls,
                                           List<XSComplexTypeDefinition> parentCTs, boolean isNillable ) {

        if ( typeDef.getName() != null ) {
            for ( XSComplexTypeDefinition ct : parentCTs ) {
                if ( ct.getName() != null ) {
                    if ( typeDef.getName().equals( ct.getName() ) && typeDef.getNamespace().equals( ct.getNamespace() ) ) {
                        handleCycle( parentEls, parentCTs );
                        return Collections.emptyList();
                    }
                }
            }
        }
        parentCTs.add( typeDef );

        List<Mapping> particles = new ArrayList<Mapping>();

        // text node
        if ( typeDef.getContentType() != CONTENTTYPE_EMPTY && typeDef.getContentType() != CONTENTTYPE_ELEMENT ) {
            // TODO
            NamespaceContext nsContext = null;
            ValueReference path = new ValueReference( "text()", nsContext );
            String column = mc.getColumn();
            if ( column == null || column.isEmpty() ) {
                column = "value";
            }
            DBField dbField = new DBField( mc.getTable(), column );
            PrimitiveType pt = new PrimitiveType( BaseType.STRING );
            if ( typeDef.getSimpleType() != null ) {
                pt = new PrimitiveType( typeDef.getSimpleType() );
            }
            particles.add( new PrimitiveMapping( path, false, dbField, pt, null, null ) );
        }

        // attributes
        XSObjectList attributeUses = typeDef.getAttributeUses();
        for ( int i = 0; i < attributeUses.getLength(); i++ ) {
            XSAttributeUse attrUse = ( (XSAttributeUse) attributeUses.item( i ) );
            XSAttributeDeclaration attrDecl = attrUse.getAttrDeclaration();
            QName attrName = new QName( attrDecl.getName() );
            if ( attrDecl.getNamespace() != null ) {
                attrName = new QName( attrDecl.getNamespace(), attrDecl.getName() );
            }
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            // TODO
            NamespaceContext nsContext = null;
            ValueReference path = new ValueReference( "@" + getName( attrName ), nsContext );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            PrimitiveType pt = new PrimitiveType( attrDecl.getTypeDefinition() );
            particles.add( new PrimitiveMapping( path, !attrUse.getRequired(), dbField, pt, null, null ) );
        }

        // xsi:nil attribute
        if ( isNillable ) {
            QName attrName = new QName( XSINS, "nil", "xsi" );
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            ValueReference path = new ValueReference( "@" + getName( attrName ), null );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            particles.add( new PrimitiveMapping( path, true, dbField, new PrimitiveType( BOOLEAN ), null, null ) );
        }

        // child elements
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            List<Mapping> childElMappings = generateMapping( particle, 1, mc, parentEls, parentCTs );
            particles.addAll( childElMappings );
        }
        return particles;
    }

    private void handleCycle( List<XSElementDeclaration> parentEls, List<XSComplexTypeDefinition> parentCTs ) {
        StringBuffer sb = new StringBuffer( "Path: " );
        for ( XSElementDeclaration qName : parentEls ) {
            sb.append( "{" );
            sb.append( qName.getNamespace() );
            sb.append( "}" );
            sb.append( qName.getName() );
            sb.append( " -> " );
        }
        throw new RuntimeException( "Detected unhandled cycle '" + sb + "'." );
    }

    private List<Mapping> generateMapping( XSComplexTypeDefinition typeDef, MappingContext mc,
                                           List<XSElementDeclaration> parentEls,
                                           List<XSComplexTypeDefinition> parentCTs, ObjectPropertyType opt ) {

        List<Mapping> particles = new ArrayList<Mapping>();

        // attributes
        XSObjectList attributeUses = typeDef.getAttributeUses();
        for ( int i = 0; i < attributeUses.getLength(); i++ ) {
            XSAttributeUse attrUse = ( (XSAttributeUse) attributeUses.item( i ) );
            XSAttributeDeclaration attrDecl = attrUse.getAttrDeclaration();
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
            ValueReference path = new ValueReference( "@" + getName( attrName ), nsContext );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            PrimitiveType pt = new PrimitiveType( attrDecl.getTypeDefinition() );
            particles.add( new PrimitiveMapping( path, !attrUse.getRequired(), dbField, pt, null, null ) );
        }

        // xsi:nil attribute
        if ( opt.isNillable() ) {
            QName attrName = new QName( XSINS, "nil", "xsi" );
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            ValueReference path = new ValueReference( "@" + getName( attrName ), null );
            DBField dbField = new DBField( attrMc.getTable(), attrMc.getColumn() );
            particles.add( new PrimitiveMapping( path, true, dbField, new PrimitiveType( BOOLEAN ), null, null ) );
        }

        ValueReference path = new ValueReference( ".", null );
        if ( opt instanceof GeometryPropertyType ) {
            GeometryType gt = GeometryType.GEOMETRY;
            // TODO
            CoordinateDimension dim = CoordinateDimension.DIM_2;
            // TODO
            String srid = "-1";
            MappingContext elMC = mcManager.mapOneToOneElement( mc, new QName( "value" ) );
            particles.add( new GeometryMapping( path, true, new DBField( elMC.getColumn() ), gt, geometryParams, null ) );
        } else if ( opt instanceof FeaturePropertyType ) {
            QName valueFtName = ( (FeaturePropertyType) opt ).getFTName();
            MappingContext fkMC = mcManager.mapOneToOneElement( mc, new QName( "fk" ) );
            List<TableJoin> jc = Collections.emptyList();
            TableJoin ftJoin = generateFtJoin( fkMC, ( (FeaturePropertyType) opt ).getValueFt() );
            if ( ftJoin != null ) {
                jc = new ArrayList<TableJoin>( jc );
                jc.add( ftJoin );
            }
            MappingContext hrefMC = mcManager.mapOneToOneElement( mc, new QName( "href" ) );
            particles.add( new FeatureMapping( path, true, new DBField( hrefMC.getColumn() ), valueFtName, jc ) );
        } else {
            LOG.warn( "Unhandled object property type '" + opt.getClass() + "'." );
        }

        return particles;
    }

    private List<Mapping> generateMapping( XSParticle particle, int maxOccurs, MappingContext mc,
                                           List<XSElementDeclaration> parentEls, List<XSComplexTypeDefinition> parentCTs ) {

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
                childElMappings.addAll( generateMapping( particle.getTerm(), -1, mc, parentEls, parentCTs ) );
            } else {
                for ( int i = 1; i <= particle.getMaxOccurs(); i++ ) {
                    childElMappings.addAll( generateMapping( particle.getTerm(), i, mc,
                                                             new ArrayList<XSElementDeclaration>( parentEls ),
                                                             new ArrayList<XSComplexTypeDefinition>( parentCTs ) ) );
                }
            }
        }
        return childElMappings;
    }

    private List<Mapping> generateMapping( XSTerm term, int occurence, MappingContext mc,
                                           List<XSElementDeclaration> parentEls, List<XSComplexTypeDefinition> parentCTs ) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        if ( term instanceof XSElementDeclaration ) {
            mappings.addAll( generateMapping( (XSElementDeclaration) term, occurence, mc, parentEls, parentCTs ) );
        } else if ( term instanceof XSModelGroup ) {
            mappings.addAll( generateMapping( (XSModelGroup) term, occurence, mc, parentEls, parentCTs ) );
        } else {
            mappings.addAll( generateMapping( (XSWildcard) term, occurence, mc, parentEls, parentCTs ) );
        }
        return mappings;
    }

    private List<Mapping> generateMapping( XSElementDeclaration elDecl, int occurence, MappingContext mc,
                                           List<XSElementDeclaration> parentEls, List<XSComplexTypeDefinition> parentCTs ) {

        if ( elDecl.getScope() == XSConstants.SCOPE_GLOBAL ) {
            for ( XSElementDeclaration el : parentEls ) {
                if ( elDecl.getName().equals( el.getName() ) && elDecl.getNamespace().equals( el.getNamespace() ) ) {
                    handleCycle( parentEls, parentCTs );
                    return Collections.emptyList();
                }
            }
        }
        parentEls.add( elDecl );

        List<Mapping> mappings = new ArrayList<Mapping>();

        QName eName = new QName( elDecl.getNamespace(), elDecl.getName() );
        // if ( eName.equals( new QName( "http://www.opengis.net/gml/3.2", "AbstractCRS" ) ) ) {
        // LOG.warn( "Skipping mapping of AbstractCRS element" );
        // return mappings;
        // }
        // if ( eName.equals( new QName( "http://www.opengis.net/gml/3.2", "TimeOrdinalEra" ) ) ) {
        // LOG.warn( "Skipping mapping of TimeOrdinalEra element" );
        // return mappings;
        // }
        //
        // if ( eName.equals( new QName( "http://www.opengis.net/gml/3.2", "TimePeriod" ) ) ) {
        // LOG.warn( "Skipping mapping of TimePeriod element" );
        // return mappings;
        // }
        //
        // if ( eName.equals( new QName( "http://www.isotc211.org/2005/gmd", "EX_GeographicDescription" ) ) ) {
        // LOG.warn( "Skipping mapping of EX_GeographicDescription element" );
        // }

        // consider every concrete element substitution
        List<XSElementDeclaration> substitutions = appSchema.getGMLSchema().getSubstitutions( elDecl, null, true, true );
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
                mappings.addAll( generatePropMapping( opt, mc ) );
            } else {
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

                List<TableJoin> jc = null;
                if ( occurence == -1 ) {
                    jc = generateJoinChain( mc, elMC );
                }

                ValueReference path = new ValueReference( getName( elName ), nsContext );
                if ( typeDef instanceof XSComplexTypeDefinition ) {
                    List<Mapping> particles = generateMapping( (XSComplexTypeDefinition) typeDef, elMC,
                                                               new ArrayList<XSElementDeclaration>( parentEls ),
                                                               new ArrayList<XSComplexTypeDefinition>( parentCTs ),
                                                               substitution.getNillable() );
                    // TODO
                    mappings.add( new CompoundMapping( path, false, particles, jc, substitution ) );
                } else {
                    MappingExpression mapping = new DBField( elMC.getColumn() );
                    PrimitiveType pt = new PrimitiveType( (XSSimpleTypeDefinition) typeDef );
                    mappings.add( new PrimitiveMapping( path, false, mapping, pt, jc, null ) );
                }
            }
        }
        return mappings;
    }

    private List<Mapping> generateMapping( XSModelGroup modelGroup, int occurrence, MappingContext mc,
                                           List<XSElementDeclaration> parentEls, List<XSComplexTypeDefinition> parentCTs ) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        XSObjectList particles = modelGroup.getParticles();
        for ( int i = 0; i < particles.getLength(); i++ ) {
            XSParticle particle = (XSParticle) particles.item( i );
            mappings.addAll( generateMapping( particle, occurrence, mc,
                                              new ArrayList<XSElementDeclaration>( parentEls ),
                                              new ArrayList<XSComplexTypeDefinition>( parentCTs ) ) );
        }
        return mappings;
    }

    private List<Mapping> generateMapping( XSWildcard wildCard, int occurrence, MappingContext mc,
                                           List<XSElementDeclaration> parentEls, List<XSComplexTypeDefinition> parentCTs ) {
        LOG.debug( "Handling of wild cards not implemented yet." );
        StringBuffer sb = new StringBuffer( "Path: " );
        for ( XSElementDeclaration parentEl : parentEls ) {
            sb.append( parentEl.getName() );
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
        if ( name.getNamespaceURI() != null && !name.getNamespaceURI().equals( "" ) ) {
            String prefix = nsToPrefix.get( name.getNamespaceURI() );
            return prefix + ":" + name.getLocalPart();
        }
        return name.getLocalPart();
    }

    private ValueReference getPropName( QName name ) {
        if ( name.getNamespaceURI() != null && !name.getNamespaceURI().equals( "" ) ) {
            String prefix = name.getPrefix();
            if ( prefix == null || prefix.isEmpty() ) {
                prefix = nsToPrefix.get( name.getNamespaceURI() );
            }
            name = new QName( name.getNamespaceURI(), name.getLocalPart(), prefix );
        }
        return new ValueReference( name );
    }
}