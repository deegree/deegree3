//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.gml.feature.schema;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericAppSchema;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.ArrayPropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the {@link FeatureType} hierarchy defined in a GML schema document.
 * <p>
 * Note that the generated {@link AppSchema} only contains user-defined feature types, i.e. all feature base
 * types from the GML namespace (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>) are ignored. This
 * follows the idea that working with {@link AppSchema} objects should not involve GML (and GML-version)
 * specific details (such as the mentioned GML feature types).
 * </p>
 * 
 * @see AppSchema
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AppSchemaXSDDecoder {

    private Logger LOG = LoggerFactory.getLogger( AppSchemaXSDDecoder.class );

    private GMLSchemaInfoSet analyzer;

    // key: ft name, value: element declaration
    private Map<QName, XSElementDeclaration> ftNameToFtElement = new HashMap<QName, XSElementDeclaration>();

    // key: geometry name, value: element declaration
    private Map<QName, XSElementDeclaration> geometryNameToGeometryElement = new HashMap<QName, XSElementDeclaration>();

    // key: name of feature type, value: feature type
    private Map<QName, FeatureType> ftNameToFt = new HashMap<QName, FeatureType>();

    // key: name of feature type, value: GML core feature type
    private Map<QName, FeatureType> ftNameToCoreFt = new HashMap<QName, FeatureType>();

    // key: name of ft A, value: name of ft B (A is in substitionGroup B)
    private Map<QName, QName> ftNameToSubstitutionGroupName = new HashMap<QName, QName>();

    private Map<QName, PropertyType> propNameToGlobalDecl = new HashMap<QName, PropertyType>();

    // stores all feature property types, so the reference to the contained
    // FeatureType can be resolved, after all FeatureTypes have been created
    private List<FeaturePropertyType> featurePropertyTypes = new ArrayList<FeaturePropertyType>();

    private final Map<String, String> prefixToNs = new HashMap<String, String>();

    private final Map<String, String> nsToPrefix = new HashMap<String, String>();

    private int prefixIndex = 0;

    private final GMLVersion gmlVersion;

    private final String gmlNs;

    /**
     * Creates a new {@link AppSchemaXSDDecoder} from the given schema URL(s).
     * 
     * @param gmlVersion
     *            gml version of the schema files, can be null (auto-detect GML version)
     * @param namespaceHints
     *            optional hints (key: prefix, value: namespaces) for generating 'nice' qualified feature type and
     *            property type names, may be null
     * @param schemaUrls
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public AppSchemaXSDDecoder( GMLVersion gmlVersion, Map<String, String> namespaceHints, String... schemaUrls )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        analyzer = new GMLSchemaInfoSet( gmlVersion, schemaUrls );
        this.gmlVersion = analyzer.getVersion();
        gmlNs = this.gmlVersion.getNamespace();

        for ( Entry<String, String> nsToPrefix : analyzer.getNamespacePrefixes().entrySet() ) {
            this.nsToPrefix.put( nsToPrefix.getKey(), nsToPrefix.getValue() );
            this.prefixToNs.put( nsToPrefix.getValue(), nsToPrefix.getKey() );
        }

        if ( namespaceHints != null ) {
            for ( Entry<String, String> prefixToNs : namespaceHints.entrySet() ) {
                nsToPrefix.put( prefixToNs.getValue(), prefixToNs.getKey() );
                this.prefixToNs.put( prefixToNs.getKey(), prefixToNs.getValue() );
            }
        }

        List<XSElementDeclaration> featureElementDecls = analyzer.getFeatureElementDeclarations( null, false );

        // feature element declarations
        for ( XSElementDeclaration elementDecl : featureElementDecls ) {
            QName ftName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
            ftNameToFtElement.put( ftName, elementDecl );
            XSElementDeclaration substitutionElement = elementDecl.getSubstitutionGroupAffiliation();
            if ( substitutionElement != null ) {
                QName substitutionElementName = createQName( substitutionElement.getNamespace(),
                                                             substitutionElement.getName() );
                ftNameToSubstitutionGroupName.put( ftName, substitutionElementName );
            }
        }

        // geometry element declarations
        List<XSElementDeclaration> geometryElementDecls = analyzer.getGeometryElementDeclarations( null, false );
        for ( XSElementDeclaration elementDecl : geometryElementDecls ) {
            QName elName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
            geometryNameToGeometryElement.put( elName, elementDecl );
            LOG.debug( "Geometry element " + elName );
        }
    }

    /**
     * Creates a new {@link AppSchemaXSDDecoder} from the given schema file (which may be a directory).
     * 
     * @param gmlVersion
     *            gml version of the schema files, can be null (auto-detect GML version)
     * @param namespaceHints
     *            optional hints (key: prefix, value: namespaces) for generating 'nice' qualified feature type and
     *            property type names, may be null
     * @param schemaFile
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public AppSchemaXSDDecoder( GMLVersion gmlVersion, Map<String, String> namespaceHints, File schemaFile )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, MalformedURLException, UnsupportedEncodingException {
        this( gmlVersion, namespaceHints, getSchemaURLs( schemaFile ) );
    }

    private static String[] getSchemaURLs( File schemaFile )
                            throws MalformedURLException, UnsupportedEncodingException {

        List<String> schemaUrls = new ArrayList<String>();

        if ( !schemaFile.exists() ) {
            throw new IllegalArgumentException( "File/directory '" + schemaFile + "' does not exist." );
        }
        if ( schemaFile.isDirectory() ) {
            String[] inputFiles = schemaFile.list( new FilenameFilter() {
                @Override
                public boolean accept( File dir, String name ) {
                    return name.toLowerCase().endsWith( ".xsd" );
                }
            } );
            for ( String file : inputFiles ) {
                schemaUrls.add( new URL( schemaFile.toURI().toURL(), URLEncoder.encode( file, "UTF-8" ) ).toExternalForm() );
            }
        } else if ( schemaFile.isFile() ) {
            schemaUrls.add( schemaFile.toURI().toURL().toExternalForm() );
        } else {
            throw new IllegalArgumentException( "'" + schemaFile + "' is neither a file nor a directory." );
        }
        return schemaUrls.toArray( new String[schemaUrls.size()] );
    }

    public AppSchema extractFeatureTypeSchema() {

        for ( QName ftName : ftNameToFtElement.keySet() ) {
            FeatureType ft = buildFeatureType( ftNameToFtElement.get( ftName ) );
            if ( gmlNs.equals( ft.getName().getNamespaceURI() ) ) {
                ftNameToCoreFt.put( ftName, ft );
            } else {
                ftNameToFt.put( ftName, ft );
            }
        }

        // resolveFtReferences();

        FeatureType[] fts = ftNameToFt.values().toArray( new FeatureType[ftNameToFt.size()] );

        Map<FeatureType, FeatureType> ftSubstitution = new HashMap<FeatureType, FeatureType>();
        for ( QName ftName : ftNameToSubstitutionGroupName.keySet() ) {
            QName substitutionFtName = ftNameToSubstitutionGroupName.get( ftName );
            if ( ftName.getNamespaceURI().equals( gmlNs ) || substitutionFtName.getNamespaceURI().equals( gmlNs ) ) {
                LOG.trace( "Skipping substitution relation: '" + ftName + "' -> '" + substitutionFtName
                           + "' (involves GML internal feature type declaration)." );
                continue;
            }
            ftSubstitution.put( ftNameToFt.get( ftName ), ftNameToFt.get( substitutionFtName ) );
        }
        return new GenericAppSchema( fts, ftSubstitution, prefixToNs, analyzer );
    }

    private FeatureType buildFeatureType( XSElementDeclaration featureElementDecl ) {

        QName ftName = createQName( featureElementDecl.getNamespace(), featureElementDecl.getName() );
        LOG.debug( "Building feature type declaration: '" + ftName + "'" );

        if ( featureElementDecl.getTypeDefinition().getType() == XSTypeDefinition.SIMPLE_TYPE ) {
            String msg = "The schema type of feature element '" + ftName
                         + "' is simple, but feature elements must always have a complex type.";
            throw new IllegalArgumentException( msg );
        }

        // extract property types from type definition
        List<PropertyType> pts = new ArrayList<PropertyType>();
        XSComplexTypeDefinition typeDef = (XSComplexTypeDefinition) featureElementDecl.getTypeDefinition();

        // element contents
        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            XSParticle particle = typeDef.getParticle();
            XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case XSConstants.MODEL_GROUP: {
                XSModelGroup modelGroup = (XSModelGroup) term;
                switch ( modelGroup.getCompositor() ) {
                case XSModelGroup.COMPOSITOR_ALL: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_CHOICE: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_CHOICE" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_SEQUENCE: {
                    XSObjectList sequence = modelGroup.getParticles();
                    for ( int i = 0; i < sequence.getLength(); i++ ) {
                        XSParticle particle2 = (XSParticle) sequence.item( i );
                        switch ( particle2.getTerm().getType() ) {
                        case XSConstants.ELEMENT_DECLARATION: {
                            XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                            int minOccurs = particle2.getMinOccurs();
                            int maxOccurs = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                            PropertyType pt = buildPropertyType( elementDecl2, minOccurs, maxOccurs );
                            if ( pt != null ) {
                                pts.add( pt );
                            }
                        }
                        case XSConstants.WILDCARD: {
                            LOG.debug( "Unhandled particle: WILDCARD" );
                            break;
                        }
                        case XSConstants.MODEL_GROUP: {
                            XSModelGroup modelGroup2 = (XSModelGroup) particle2.getTerm();
                            addPropertyTypes( pts, modelGroup2 );
                            break;
                        }
                        }
                    }
                    break;
                }
                default: {
                    assert false;
                }
                }
                break;
            }
            case XSConstants.WILDCARD: {
                LOG.debug( "Unhandled particle: WILDCARD" );
                break;
            }
            case XSConstants.ELEMENT_DECLARATION: {
                LOG.debug( "Unhandled particle: ELEMENT_DECLARATION" );
                break;
            }
            default: {
                assert false;
            }
            }
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
            LOG.debug( "Unhandled content type: EMPTY" );
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_MIXED: {
            LOG.debug( "Unhandled content type: MIXED" );
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE: {
            XSSimpleTypeDefinition simpleType = typeDef.getSimpleType();
            QName simpleTypeName = createQName( simpleType.getNamespace(), simpleType.getName() );
            // contents = new SimpleContent( simpleTypeName );
            break;
        }
        default: {
            assert false;
        }
        }

        List<XSElementDeclaration> fcDecls = analyzer.getFeatureCollectionElementDeclarations( null, false );
        if ( fcDecls.contains( featureElementDecl ) ) {
            return new GenericFeatureCollectionType( ftName, pts, featureElementDecl.getAbstract() );
        }

        return new GenericFeatureType( ftName, pts, featureElementDecl.getAbstract() );
    }

    private void addPropertyTypes( List<PropertyType> pts, XSModelGroup modelGroup ) {
        LOG.trace( " - processing model group..." );
        switch ( modelGroup.getCompositor() ) {
        case XSModelGroup.COMPOSITOR_ALL: {
            LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
            break;
        }
        case XSModelGroup.COMPOSITOR_CHOICE: {
            LOG.debug( "Unhandled model group: COMPOSITOR_CHOICE" );
            break;
        }
        case XSModelGroup.COMPOSITOR_SEQUENCE: {
            XSObjectList sequence = modelGroup.getParticles();
            for ( int i = 0; i < sequence.getLength(); i++ ) {
                XSParticle particle = (XSParticle) sequence.item( i );
                switch ( particle.getTerm().getType() ) {
                case XSConstants.ELEMENT_DECLARATION: {
                    XSElementDeclaration elementDecl = (XSElementDeclaration) particle.getTerm();
                    int minOccurs = particle.getMinOccurs();
                    int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
                    PropertyType pt = buildPropertyType( elementDecl, minOccurs, maxOccurs );
                    if ( pt != null ) {
                        pts.add( pt );
                    }
                    break;
                }
                case XSConstants.WILDCARD: {
                    LOG.debug( "Unhandled particle: WILDCARD" );
                    break;
                }
                case XSConstants.MODEL_GROUP: {
                    XSModelGroup modelGroup2 = (XSModelGroup) particle.getTerm();
                    addPropertyTypes( pts, modelGroup2 );
                    break;
                }
                }
            }
            break;
        }
        default: {
            assert false;
        }
        }
    }

    private PropertyType buildPropertyType( XSElementDeclaration elementDecl, int minOccurs, int maxOccurs ) {

        PropertyType pt = null;
        QName ptName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.trace( "*** Found property declaration: '" + elementDecl.getName() + "'." );

        // HACK HACK HACK
        if ( gmlNs.equals( elementDecl.getNamespace() )
             && ( "boundedBy".equals( elementDecl.getName() ) || "name".equals( elementDecl.getName() )
                  || "description".equals( elementDecl.getName() ) || "metaDataProperty".equals( elementDecl.getName() )
                  || "descriptionReference".equals( elementDecl.getName() )
                  || "identifier".equals( elementDecl.getName() ) || ( "location".equals( elementDecl.getName() ) && gmlVersion != GMLVersion.GML_2 ) ) ) {
            LOG.trace( "Omitting from feature type -- GML standard property." );
        } else {
            // parse substitutable property declarations (e.g. genericProperty in CityGML)
            List<PropertyType> ptSubstitutions = new ArrayList<PropertyType>();
            XSObjectList list = analyzer.getXSModel().getSubstitutionGroup( elementDecl );
            if ( list != null ) {
                for ( int i = 0; i < list.getLength(); i++ ) {
                    XSElementDeclaration substitution = (XSElementDeclaration) list.item( i );
                    QName declName = new QName( substitution.getNamespace(), substitution.getName() );
                    PropertyType globalDecl = propNameToGlobalDecl.get( declName );
                    if ( globalDecl == null ) {
                        globalDecl = buildPropertyType( substitution, minOccurs, maxOccurs );
                        propNameToGlobalDecl.put( declName, globalDecl );
                    }
                    ptSubstitutions.add( globalDecl );
                }
            }

            XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
            switch ( typeDef.getTypeCategory() ) {
            case XSTypeDefinition.SIMPLE_TYPE: {
                pt = new SimplePropertyType( ptName, minOccurs, maxOccurs, getPrimitiveType( (XSSimpleType) typeDef ),
                                             elementDecl, ptSubstitutions, (XSSimpleTypeDefinition) typeDef );
                ( (SimplePropertyType) pt ).setCodeList( getCodeListId( elementDecl ) );
                break;
            }
            case XSTypeDefinition.COMPLEX_TYPE: {
                pt = buildPropertyType( elementDecl, (XSComplexTypeDefinition) typeDef, minOccurs, maxOccurs,
                                        ptSubstitutions );
                break;
            }
            }
        }
        return pt;
    }

    private PropertyType buildPropertyType( XSElementDeclaration elementDecl, XSComplexTypeDefinition typeDef,
                                            int minOccurs, int maxOccurs, List<PropertyType> ptSubstitutions ) {

        PropertyType pt = null;
        QName ptName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.trace( "- Property definition '" + ptName + "' uses a complex type for content definition." );
        boolean isNillable = elementDecl.getNillable();

        // check for well known GML types first
        if ( typeDef.getName() != null ) {
            QName typeName = createQName( typeDef.getNamespace(), typeDef.getName() );
            // if ( typeName.equals( new QName( gmlVersion.getNamespace(), "CodeType" ) ) ) {
            // LOG.trace( "Identified a CodePropertyType." );
            // pt = new CodePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(), isNillable,
            // ptSubstitutions );
            // }
            if ( typeName.equals( new QName( gmlVersion.getNamespace(), "BoundingShapeType" ) ) ) {
                LOG.trace( "Identified an EnvelopePropertyType." );
                pt = new EnvelopePropertyType( ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions );
                // } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "MeasureType" ) ) ) {
                // LOG.trace( "Identified a MeasurePropertyType (GENERIC)." );
                // pt = new MeasurePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(), isNillable,
                // ptSubstitutions );
                // } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "LengthType" ) ) ) {
                // LOG.trace( "Identified a MeasurePropertyType (LENGTH)." );
                // pt = new MeasurePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(), isNillable,
                // ptSubstitutions );
                // } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "AngleType" ) ) ) {
                // LOG.trace( "Identified a MeasurePropertyType (ANGLE)." );
                // pt = new MeasurePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(), isNillable,
                // ptSubstitutions );
            } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "FeatureArrayPropertyType" ) ) ) {
                LOG.trace( "Identified a FeatureArrayPropertyType" );
                pt = new ArrayPropertyType( ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions );
            }
        }

        // no success -> check if it is a GML object property declaration (feature, geometry, ...)
        if ( pt == null ) {
            pt = analyzer.getGMLPropertyDecl( elementDecl, ptName, minOccurs, maxOccurs, ptSubstitutions );
            if ( pt != null && pt instanceof FeaturePropertyType ) {
                featurePropertyTypes.add( (FeaturePropertyType) pt );
            }
        }

        // no success -> build custom property declaration
        if ( pt == null ) {
            pt = new CustomPropertyType( ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions );
        }
        return pt;
    }

    private String getCodeListId( XSElementDeclaration elementDecl ) {
        String codeListId = null;
        // handle adv schemas (referenced code list id inside annotation
        // element)
        XSObjectList annotations = elementDecl.getAnnotations();
        if ( annotations.getLength() > 0 ) {
            XSAnnotation annotation = (XSAnnotation) annotations.item( 0 );
            String s = annotation.getAnnotationString();
            XMLAdapter adapter = new XMLAdapter( new StringReader( s ) );
            NamespaceBindings nsContext = new NamespaceBindings();
            nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
            nsContext.addNamespace( "adv", "http://www.adv-online.de/nas" );
            codeListId = adapter.getNodeAsString( adapter.getRootElement(),
                                                  new XPath( "xs:appinfo/adv:referenzierteCodeList/text()", nsContext ),
                                                  null );
            if ( codeListId != null ) {
                codeListId = codeListId.trim();
            }
        }
        return codeListId;
    }

    private BaseType getPrimitiveType( XSSimpleType typeDef ) {

        BaseType pt = null;
        if ( typeDef.getName() != null ) {
            encounteredTypes.add( createQName( typeDef.getNamespace(), typeDef.getName() ) );
        }
        pt = BaseType.valueOf( typeDef );
        LOG.trace( "Mapped '" + typeDef.getName() + "' (base type: '" + typeDef.getBaseType() + "') -> '" + pt + "'" );
        return pt;
    }

    private Set<QName> encounteredTypes = new HashSet<QName>();

    /**
     * After parsing, this method can be called to find out all referenced types that have been encountered (for
     * debugging).
     * 
     * @return
     */
    public Set<QName> getAllEncounteredTypes() {
        return encounteredTypes;
    }

    private QName createQName( String namespace, String localPart ) {
        String prefix = nsToPrefix.get( namespace );
        if ( prefix == null ) {
            prefix = generatePrefix();
            nsToPrefix.put( namespace, prefix );
        }
        return new QName( namespace, localPart, prefix );
    }

    private String generatePrefix() {
        return "app" + prefixIndex++;
    }
}