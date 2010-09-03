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

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.deegree.feature.types.property.ValueRepresentation.INLINE;

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
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSAnnotation;
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
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.ValueRepresentation;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the {@link FeatureType} hierarchy defined in a GML schema document.
 * <p>
 * Note that the generated {@link ApplicationSchema} only contains user-defined feature types, i.e. all feature base
 * types from the GML namespace (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>) are ignored. This
 * follows the idea that working with {@link ApplicationSchema} objects should not involve GML (and GML-version)
 * specific details (such as the mentioned GML feature types).
 * </p>
 * 
 * @see ApplicationSchema
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ApplicationSchemaXSDDecoder {

    private Logger LOG = LoggerFactory.getLogger( ApplicationSchemaXSDDecoder.class );

    private GMLSchemaAnalyzer analyzer;

    // key: ft name, value: element declaration
    private Map<QName, XSElementDeclaration> ftNameToFtElement = new HashMap<QName, XSElementDeclaration>();

    // key: geometry name, value: element declaration
    private Map<QName, XSElementDeclaration> geometryNameToGeometryElement = new HashMap<QName, XSElementDeclaration>();

    // key: name of feature type, value: feature type
    private Map<QName, FeatureType> ftNameToFt = new HashMap<QName, FeatureType>();

    // key: name of ft A, value: name of ft B (A is in substitionGroup B)
    private Map<QName, QName> ftNameToSubstitutionGroupName = new HashMap<QName, QName>();

    // stores all feature property types, so the reference to the contained
    // FeatureType can be resolved,
    // after all FeatureTypes have been created
    private List<FeaturePropertyType> featurePropertyTypes = new ArrayList<FeaturePropertyType>();

    private final Map<String, String> nsToPrefix = new HashMap<String, String>();

    private int prefixIndex = 0;

    private final GMLVersion gmlVersion;

    private final String gmlNs;

    /**
     * Creates a new {@link ApplicationSchemaXSDDecoder} from the given schema URL(s).
     * 
     * @param gmlVersion
     * @param namespaceHints
     *            optional hints (key: prefix, value: namespaces) for generating 'nice' qualified feature type and
     *            property type names, may be null
     * @param schemaUrls
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ApplicationSchemaXSDDecoder( GMLVersion gmlVersion, Map<String, String> namespaceHints, String... schemaUrls )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {
        if ( namespaceHints != null ) {
            for ( Entry<String, String> prefixToNs : namespaceHints.entrySet() ) {
                nsToPrefix.put( prefixToNs.getValue(), prefixToNs.getKey() );
            }
        }

        this.gmlVersion = gmlVersion;
        gmlNs = gmlVersion.getNamespace();
        analyzer = new GMLSchemaAnalyzer( gmlVersion, schemaUrls );
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
            QName ftName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
            geometryNameToGeometryElement.put( ftName, elementDecl );
        }
    }

    /**
     * Creates a new {@link ApplicationSchemaXSDDecoder} from the given schema file (which may be a directory).
     * 
     * @param gmlVersion
     * @param namespaceHints
     * @param schemaFile
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException 
     */
    public ApplicationSchemaXSDDecoder( GMLVersion gmlVersion, Map<String, String> namespaceHints, File schemaFile )
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

    public ApplicationSchema extractFeatureTypeSchema() {

        for ( QName ftName : ftNameToFtElement.keySet() ) {
            FeatureType ft = buildFeatureType( ftNameToFtElement.get( ftName ) );
            if ( gmlNs.equals( ft.getName().getNamespaceURI() ) ) {
                LOG.trace( "Skipping GML internal feature type declaration: '" + ftName + "'." );
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
        return new ApplicationSchema( fts, ftSubstitution, analyzer );
    }

    private void resolveFtReferences() {
        for ( FeaturePropertyType pt : featurePropertyTypes ) {
            LOG.trace( "Resolving reference to feature type: '" + pt.getFTName() + "'" );
            pt.resolve( ftNameToFt.get( pt.getFTName() ) );
        }
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
                            int minOccurs = particle.getMinOccurs();
                            int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
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
            // contents = new Sequence( elements );
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

        // parse substitutable properties (e.g. genericProperty in CityGML)
        List<PropertyType> ptSubstitutions = new ArrayList<PropertyType>();
        XSObjectList list = analyzer.getXSModel().getSubstitutionGroup( elementDecl );
        if ( list != null ) {
            for ( int i = 0; i < list.getLength(); i++ ) {
                XSElementDeclaration substitution = (XSElementDeclaration) list.item( i );
                ptSubstitutions.add( buildPropertyType( substitution, minOccurs, maxOccurs ) );
            }
        }

        // HACK HACK HACK
        if ( gmlNs.equals( elementDecl.getNamespace() )
             && ( "boundedBy".equals( elementDecl.getName() ) || "name".equals( elementDecl.getName() )
                  || "description".equals( elementDecl.getName() ) || "metaDataProperty".equals( elementDecl.getName() )
                  || "descriptionReference".equals( elementDecl.getName() )
                  || "identifier".equals( elementDecl.getName() ) || ( "location".equals( elementDecl.getName() ) && gmlVersion != GMLVersion.GML_2 ) ) ) {
            LOG.trace( "Omitting from feature type -- GML standard property." );
        } else {
            XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
            switch ( typeDef.getTypeCategory() ) {
            case XSTypeDefinition.SIMPLE_TYPE: {
                QName typeName = typeDef.getName() != null ? new QName( typeDef.getNamespace(), typeDef.getName() )
                                                          : null;
                pt = new SimplePropertyType( ptName, minOccurs, maxOccurs, getPrimitiveType( (XSSimpleType) typeDef ),
                                             elementDecl.getAbstract(), ptSubstitutions,
                                             (XSSimpleTypeDefinition) typeDef );
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
        pt = buildFeaturePropertyType( elementDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions );
        if ( pt == null ) {
            pt = buildGeometryPropertyType( elementDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions );
            if ( pt == null ) {
                if ( typeDef.getName() != null ) {
                    // TODO improve detection of property types
                    QName typeName = createQName( typeDef.getNamespace(), typeDef.getName() );
                    if ( typeName.equals( new QName( gmlVersion.getNamespace(), "CodeType" ) ) ) {
                        LOG.trace( "Identified a CodePropertyType." );
                        pt = new CodePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(),
                                                   ptSubstitutions );
                    } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "BoundingShapeType" ) ) ) {
                        LOG.trace( "Identified an EnvelopePropertyType." );
                        pt = new EnvelopePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(),
                                                       ptSubstitutions );
                    } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "MeasureType" ) ) ) {
                        LOG.trace( "Identified a MeasurePropertyType (GENERIC)." );
                        pt = new MeasurePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(),
                                                      ptSubstitutions );
                    } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "LengthType" ) ) ) {
                        LOG.trace( "Identified a MeasurePropertyType (LENGTH)." );
                        pt = new MeasurePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(),
                                                      ptSubstitutions );
                    } else if ( typeName.equals( new QName( gmlVersion.getNamespace(), "AngleType" ) ) ) {
                        LOG.trace( "Identified a MeasurePropertyType (ANGLE)." );
                        pt = new MeasurePropertyType( ptName, minOccurs, maxOccurs, elementDecl.getAbstract(),
                                                      ptSubstitutions );
                    } else if ( typeName.equals( QName.valueOf( "{http://www.xplanung.de/xplangml}XP_VariableGeometrieType" ) )
                                || typeName.equals( QName.valueOf( "{http://www.xplanung.de/xplangml/3/0}XP_FlaechengeometrieType" ) )
                                || typeName.equals( QName.valueOf( "{http://www.xplanung.de/xplangml/3/0}XP_LiniengeometrieType" ) )
                                || typeName.equals( QName.valueOf( "{http://www.xplanung.de/xplangml/3/0}XP_PunktgeometrieType" ) )
                                || typeName.equals( QName.valueOf( "{http://www.xplanung.de/xplangml/3/0}XP_VariableGeometrieType" ) ) ) {
                        // TODO remove xplan hack!!!
                        pt = new GeometryPropertyType( ptName, minOccurs, maxOccurs,
                                                       GeometryPropertyType.GeometryType.GEOMETRY,
                                                       GeometryPropertyType.CoordinateDimension.DIM_2,
                                                       elementDecl.getAbstract(), ptSubstitutions, INLINE );
                    } else {
                        pt = new CustomPropertyType( ptName, minOccurs, maxOccurs, typeDef, elementDecl.getAbstract(),
                                                     ptSubstitutions );
                    }
                } else {
                    pt = new CustomPropertyType( ptName, minOccurs, maxOccurs, typeDef, elementDecl.getAbstract(),
                                                 ptSubstitutions );
                }
            }
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
            NamespaceContext nsContext = new NamespaceContext();
            nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
            nsContext.addNamespace( "adv", "http://www.adv-online.de/nas" );
            codeListId = adapter.getNodeAsString(
                                                  adapter.getRootElement(),
                                                  new XPath( "xs:appinfo/adv:referenzierteCodeList/text()", nsContext ),
                                                  null );
            if ( codeListId != null ) {
                codeListId = codeListId.trim();
            }
        }
        return codeListId;
    }

    /**
     * Analyzes the given complex type definition and returns a {@link FeaturePropertyType} if it defines a feature
     * property.
     * 
     * @param elementDecl
     * @param typeDef
     * @param minOccurs
     * @param maxOccurs
     * @return corresponding {@link FeaturePropertyType} or null, if declaration does not define a feature property
     */
    private FeaturePropertyType buildFeaturePropertyType( XSElementDeclaration elementDecl,
                                                          XSComplexTypeDefinition typeDef, int minOccurs,
                                                          int maxOccurs, List<PropertyType> ptSubstitutions ) {

        QName ptName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.trace( "Checking if element declaration '" + ptName + "' defines a feature property type." );

        FeaturePropertyType pt = buildFeaturePropertyTypeXGml( elementDecl, typeDef, minOccurs, maxOccurs,
                                                               ptSubstitutions );
        if ( pt != null ) {
            return pt;
        }
        pt = buildFeaturePropertyTypeAdv( elementDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions );
        if ( pt != null ) {
            return pt;
        }
        pt = buildFeaturePropertyTypeGML32( elementDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions );
        if ( pt != null ) {
            return pt;
        }

        boolean allowsXLink = allowsXLink( typeDef );

        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
            pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, null, elementDecl.getAbstract(),
                                          ptSubstitutions, ValueRepresentation.REMOTE );
            return pt;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            LOG.trace( "CONTENTTYPE_ELEMENT" );
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
                    LOG.trace( "Found sequence." );
                    XSObjectList sequence = modelGroup.getParticles();
                    if ( sequence.getLength() != 1 ) {
                        LOG.trace( "Length = '" + sequence.getLength() + "' -> cannot be a feature property." );
                        return null;
                    }
                    XSParticle particle2 = (XSParticle) sequence.item( 0 );
                    switch ( particle2.getTerm().getType() ) {
                    case XSConstants.ELEMENT_DECLARATION: {
                        XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                        int minOccurs2 = particle2.getMinOccurs();
                        int maxOccurs2 = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                        QName elementName = createQName( elementDecl2.getNamespace(), elementDecl2.getName() );
                        if ( ftNameToFtElement.get( elementName ) != null ) {
                            LOG.trace( "Identified a feature property." );
                            pt = null;
                            if ( gmlNs.equals( elementName.getNamespaceURI() ) ) {
                                if ( allowsXLink ) {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, null,
                                                                  elementDecl.getAbstract(), ptSubstitutions,
                                                                  ValueRepresentation.BOTH );
                                } else {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, null,
                                                                  elementDecl.getAbstract(), ptSubstitutions,
                                                                  ValueRepresentation.INLINE );
                                }
                            } else {
                                if ( allowsXLink ) {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementName,
                                                                  elementDecl.getAbstract(), ptSubstitutions,
                                                                  ValueRepresentation.BOTH );
                                } else {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, null,
                                                                  elementDecl.getAbstract(), ptSubstitutions,
                                                                  ValueRepresentation.INLINE );
                                }
                            }
                            featurePropertyTypes.add( pt );
                            return pt;
                        }
                    }
                    case XSConstants.WILDCARD: {
                        LOG.debug( "Unhandled particle: WILDCARD" );
                        break;
                    }
                    case XSConstants.MODEL_GROUP: {
                        LOG.debug( "Unhandled particle: MODEL_GROUP" );
                        break;
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
        default: {
            LOG.debug( "Unhandled content type in buildFeaturePropertyType(...) encountered." );
        }
        }
        return null;
    }

    private boolean allowsXLink( XSComplexTypeDefinition typeDef ) {
        XSObjectList xsObjectList = typeDef.getAttributeUses();
        for ( int i = 0; i < xsObjectList.getLength(); i++ ) {
            XSAttributeDeclaration attr = ( (XSAttributeUse) xsObjectList.item( i ) ).getAttrDeclaration();
            if ( "href".equals( attr.getName() ) && XLNNS.equals( attr.getNamespace() ) ) {
                return true;
            }
        }
        return false;
    }

    private FeaturePropertyType buildFeaturePropertyTypeXGml( XSElementDeclaration elementDecl,
                                                              XSComplexTypeDefinition typeDef, int minOccurs,
                                                              int maxOccurs, List<PropertyType> ptSubstitutions ) {
        // handle schemas that use a source="urn:x-gml:targetElement" attribute
        // for defining the referenced feature type
        // inside the annotation element (e.g. CITE examples for WFS 1.1.0)
        XSObjectList annotations = elementDecl.getAnnotations();
        if ( annotations.getLength() > 0 ) {
            XSAnnotation annotation = (XSAnnotation) annotations.item( 0 );
            String s = annotation.getAnnotationString();
            XMLAdapter adapter = new XMLAdapter( new StringReader( s ) );
            NamespaceContext nsContext = new NamespaceContext();
            nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
            QName refElement = adapter.getNodeAsQName(
                                                       adapter.getRootElement(),
                                                       new XPath(
                                                                  "xs:appinfo[@source='urn:x-gml:targetElement']/text()",
                                                                  nsContext ), null );
            if ( refElement != null ) {
                LOG.debug( "Identified a feature property (urn:x-gml:targetElement)." );
                QName elementName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
                FeaturePropertyType pt = new FeaturePropertyType( elementName, minOccurs, maxOccurs, refElement,
                                                                  elementDecl.getAbstract(), ptSubstitutions,
                                                                  ValueRepresentation.BOTH );
                featurePropertyTypes.add( pt );
                return pt;
            }
        }
        return null;
    }

    private FeaturePropertyType buildFeaturePropertyTypeAdv( XSElementDeclaration elementDecl,
                                                             XSComplexTypeDefinition typeDef, int minOccurs,
                                                             int maxOccurs, List<PropertyType> ptSubstitutions ) {
        // handle adv schemas (referenced feature type inside annotation
        // element)
        XSObjectList annotations = elementDecl.getAnnotations();
        if ( annotations.getLength() > 0 ) {
            XSAnnotation annotation = (XSAnnotation) annotations.item( 0 );
            String s = annotation.getAnnotationString();
            XMLAdapter adapter = new XMLAdapter( new StringReader( s ) );
            NamespaceContext nsContext = new NamespaceContext();
            nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
            nsContext.addNamespace( "adv", "http://www.adv-online.de/nas" );
            QName refElement = adapter.getNodeAsQName( adapter.getRootElement(),
                                                       new XPath( "xs:appinfo/adv:referenziertesElement/text()",
                                                                  nsContext ), null );
            if ( refElement != null ) {
                LOG.trace( "Identified a feature property (adv style)." );
                QName elementName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
                FeaturePropertyType pt = new FeaturePropertyType( elementName, minOccurs, maxOccurs, refElement,
                                                                  elementDecl.getAbstract(), ptSubstitutions,
                                                                  ValueRepresentation.BOTH );
                featurePropertyTypes.add( pt );
                return pt;
            }
        }
        return null;
    }

    private FeaturePropertyType buildFeaturePropertyTypeGML32( XSElementDeclaration elementDecl,
                                                               XSComplexTypeDefinition typeDef, int minOccurs,
                                                               int maxOccurs, List<PropertyType> ptSubstitutions ) {
        // handle GML 3.2 schemas (referenced feature type inside annotation
        // element)
        XSObjectList annotations = elementDecl.getAnnotations();
        if ( annotations.getLength() > 0 ) {
            XSAnnotation annotation = (XSAnnotation) annotations.item( 0 );
            String s = annotation.getAnnotationString();
            XMLAdapter adapter = new XMLAdapter( new StringReader( s ) );
            NamespaceContext nsContext = new NamespaceContext();
            nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
            nsContext.addNamespace( "gml", gmlNs );
            QName refElement = adapter.getNodeAsQName( adapter.getRootElement(),
                                                       new XPath( "xs:appinfo/gml:targetElement/text()", nsContext ),
                                                       null );
            if ( refElement != null ) {
                LOG.trace( "Identified a feature property (GML 3.2 style)." );
                QName elementName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
                FeaturePropertyType pt = new FeaturePropertyType( elementName, minOccurs, maxOccurs, refElement,
                                                                  elementDecl.getAbstract(), ptSubstitutions,
                                                                  ValueRepresentation.REMOTE );
                featurePropertyTypes.add( pt );
                return pt;
            }
        }
        return null;
    }

    /**
     * Analyzes the given complex type definition and returns a {@link GeometryPropertyType} if it defines a geometry
     * property.
     * 
     * @param elementDecl
     * @param typeDef
     * @param minOccurs
     * @param maxOccurs
     * @return corresponding {@link GeometryPropertyType} or null, if declaration does not define a geometry property
     */
    private GeometryPropertyType buildGeometryPropertyType( XSElementDeclaration elementDecl,
                                                            XSComplexTypeDefinition typeDef, int minOccurs,
                                                            int maxOccurs, List<PropertyType> ptSubstitutions ) {

        QName ptName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            LOG.trace( "CONTENTTYPE_ELEMENT" );
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
                    LOG.trace( "Found sequence." );
                    XSObjectList sequence = modelGroup.getParticles();
                    if ( sequence.getLength() != 1 ) {
                        LOG.trace( "Length = '" + sequence.getLength() + "' -> cannot be a geometry property." );
                        return null;
                    }
                    XSParticle particle2 = (XSParticle) sequence.item( 0 );
                    XSTerm geomTerm = particle2.getTerm();
                    switch ( geomTerm.getType() ) {
                    case XSConstants.ELEMENT_DECLARATION: {
                        XSElementDeclaration elementDecl2 = (XSElementDeclaration) geomTerm;
                        // min occurs check should be done, in regards to the xlinking.
                        int minOccurs2 = particle2.getMinOccurs();
                        int maxOccurs2 = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                        if ( maxOccurs2 > 1 ) {
                            LOG.warn( "Only single geometries are currently supported." );
                            return null;
                        }
                        QName elementName = createQName( elementDecl2.getNamespace(), elementDecl2.getName() );
                        if ( geometryNameToGeometryElement.get( elementName ) != null ) {
                            LOG.trace( "Identified a geometry property." );
                            GeometryType geometryType = getGeometryType( elementName );
                            return new GeometryPropertyType( ptName, minOccurs, maxOccurs, geometryType,
                                                             CoordinateDimension.DIM_2_OR_3, elementDecl.getAbstract(),
                                                             ptSubstitutions, BOTH );
                        }
                    }
                    case XSConstants.WILDCARD: {
                        LOG.debug( "Unhandled particle: WILDCARD" );
                        break;
                    }
                    case XSConstants.MODEL_GROUP: {
                        // more then one kind of geometries allowed
                        XSModelGroup geomModelGroup = (XSModelGroup) geomTerm;
                        switch ( geomModelGroup.getType() ) {
                        case XSModelGroup.COMPOSITOR_ALL: {
                            // all geometries?, lets make it a custom property
                            LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                            break;
                        }
                        case XSModelGroup.COMPOSITOR_CHOICE: {
                            XSObjectList geomChoice = geomModelGroup.getParticles();
                            int length = geomChoice.getLength();
                            Set<GeometryType> allowedTypes = new HashSet<GeometryType>();
                            for ( int i = 0; i < length; ++i ) {
                                XSParticle geomChoiceParticle = (XSParticle) sequence.item( i );
                                XSTerm geomChoiceTerm = geomChoiceParticle.getTerm();
                                if ( geomChoiceTerm.getType() == XSConstants.ELEMENT_DECLARATION ) {
                                    // other types are not supported
                                    XSElementDeclaration geomChoiceElement = (XSElementDeclaration) geomChoiceTerm;
                                    // min occurs check should be done, in regards to the xlinking.
                                    int minOccurs3 = geomChoiceParticle.getMinOccurs();
                                    int maxOccurs3 = geomChoiceParticle.getMaxOccursUnbounded() ? -1
                                                                                               : particle2.getMaxOccurs();
                                    if ( maxOccurs3 > 1 ) {
                                        LOG.warn( "Only single geometries are currently supported, ignoring in choice." );
                                        // return null;
                                    }
                                    QName elementName = createQName( geomChoiceElement.getNamespace(),
                                                                     geomChoiceElement.getName() );
                                    if ( geometryNameToGeometryElement.get( elementName ) != null ) {
                                        LOG.trace( "Identified a geometry property." );
                                        GeometryType geometryType = getGeometryType( elementName );
                                        allowedTypes.add( geometryType );
                                    } else {
                                        LOG.warn( "Unknown geometry type." );
                                    }
                                } else {
                                    LOG.warn( "Unsupported type particle type." );
                                }
                            }
                            if ( !allowedTypes.isEmpty() ) {
                                return new GeometryPropertyType( ptName, minOccurs, maxOccurs, allowedTypes,
                                                                 CoordinateDimension.DIM_2_OR_3,
                                                                 elementDecl.getAbstract(), ptSubstitutions, BOTH );
                            }
                        }
                        case XSModelGroup.COMPOSITOR_SEQUENCE: {
                            // sequence of geometries?, lets make it a custom property
                            LOG.debug( "Unhandled model group: COMPOSITOR_SEQUENCE" );
                            break;
                        }
                        }

                        LOG.debug( "Unhandled particle: MODEL_GROUP" );
                        break;
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
            // contents = new Sequence( elements );
            break;
        }
        }
        return null;
    }

    private GeometryType getGeometryType( QName gmlGeometryName ) {
        String localPart = gmlGeometryName.getLocalPart();
        GeometryType result = GeometryType.GEOMETRY;
        try {
            result = GeometryType.fromGMLTypeName( localPart );
        } catch ( Exception e ) {
            LOG.warn( "Unmappable geometry type: " + gmlGeometryName.toString()
                      + " (currently not supported by geometry model)" );
        }
        LOG.trace( "Mapping '" + gmlGeometryName + "' -> " + result );
        return result;
    }

    private PrimitiveType getPrimitiveType( XSSimpleType typeDef ) {

        PrimitiveType pt = null;
        if ( typeDef.getName() != null ) {
            encounteredTypes.add( createQName( typeDef.getNamespace(), typeDef.getName() ) );
        }
        pt = XMLValueMangler.getPrimitiveType( typeDef );
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
