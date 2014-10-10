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
package org.deegree.gml.schema;

import static org.apache.xerces.xs.XSConstants.ELEMENT_DECLARATION;
import static org.apache.xerces.xs.XSConstants.MODEL_GROUP;
import static org.apache.xerces.xs.XSConstants.WILDCARD;

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
import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.GenericGMLObjectType;
import org.deegree.commons.tom.gml.property.PropertyType;
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
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

/**
 * Provides access to the {@link AppSchema} defined in one or more GML schema documents.
 *
 * @see AppSchema
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GMLAppSchemaReader {

    private Logger LOG = LoggerFactory.getLogger( GMLAppSchemaReader.class );

    private GMLSchemaInfoSet analyzer;

    // key: ft name, value: element declaration
    private Map<QName, XSElementDeclaration> ftNameToFtElement = new HashMap<QName, XSElementDeclaration>();

    // key: geometry name, value: element declaration
    private Map<QName, XSElementDeclaration> geometryNameToGeometryElement = new HashMap<QName, XSElementDeclaration>();

    // key: name of feature type, value: feature type
    private Map<QName, FeatureType> ftNameToFt = new HashMap<QName, FeatureType>();

    private Map<QName, GMLObjectType> typeNameToType = new HashMap<QName, GMLObjectType>();

    // key: name of ft A, value: name of ft B (A is in substitionGroup B)
    private Map<QName, QName> ftNameToSubstitutionGroupName = new HashMap<QName, QName>();

    private Map<QName, QName> geometryNameToSubstitutionGroupName = new HashMap<QName, QName>();

    private Map<QName, PropertyType> propNameToGlobalDecl = new HashMap<QName, PropertyType>();

    // stores all feature property types, so the reference to the contained
    // FeatureType can be resolved, after all FeatureTypes have been created
    private List<FeaturePropertyType> featurePropertyTypes = new ArrayList<FeaturePropertyType>();

    private final Map<String, String> prefixToNs = new HashMap<String, String>();

    private final Map<String, String> nsToPrefix = new HashMap<String, String>();

    private int prefixIndex = 0;

    private final GMLVersion gmlVersion;

    /**
     * Creates a new {@link GMLAppSchemaReader} from the given schema URL(s).
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
    public GMLAppSchemaReader( GMLVersion gmlVersion, Map<String, String> namespaceHints, String... schemaUrls )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        analyzer = new GMLSchemaInfoSet( gmlVersion, schemaUrls );
        this.gmlVersion = analyzer.getVersion();

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

            QName abstractFeatureElementName = createQName( analyzer.getAbstractFeatureElementDeclaration().getNamespace(),
                                                            analyzer.getAbstractFeatureElementDeclaration().getName() );
            if ( !ftName.equals( abstractFeatureElementName ) ) {
                XSElementDeclaration substitutionElement = elementDecl.getSubstitutionGroupAffiliation();
                if ( substitutionElement != null ) {
                    QName substitutionElementName = createQName( substitutionElement.getNamespace(),
                                                                 substitutionElement.getName() );
                    ftNameToSubstitutionGroupName.put( ftName, substitutionElementName );
                }
            }
        }

        // geometry element declarations
        List<XSElementDeclaration> geometryElementDecls = analyzer.getGeometryElementDeclarations( null, false );
        for ( XSElementDeclaration elementDecl : geometryElementDecls ) {
            QName elName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
            geometryNameToGeometryElement.put( elName, elementDecl );
            XSElementDeclaration substitutionElement = elementDecl.getSubstitutionGroupAffiliation();
            if ( substitutionElement != null ) {
                QName substitutionElementName = createQName( substitutionElement.getNamespace(),
                                                             substitutionElement.getName() );
                geometryNameToSubstitutionGroupName.put( elName, substitutionElementName );
            }
        }
    }

    /**
     * Creates a new {@link GMLAppSchemaReader} from the given <code>LSInput</code>s.
     *
     * @param gmlVersion
     *            gml version of the schema files, can be null (auto-detect GML version)
     * @param namespaceHints
     *            optional hints (key: prefix, value: namespaces) for generating 'nice' qualified feature type and
     *            property type names, may be null
     * @param inputs
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public GMLAppSchemaReader( GMLVersion gmlVersion, Map<String, String> namespaceHints, LSInput... inputs )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        analyzer = new GMLSchemaInfoSet( gmlVersion, inputs );
        this.gmlVersion = analyzer.getVersion();

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
            XSElementDeclaration substitutionElement = elementDecl.getSubstitutionGroupAffiliation();
            if ( substitutionElement != null ) {
                QName substitutionElementName = createQName( substitutionElement.getNamespace(),
                                                             substitutionElement.getName() );
                geometryNameToSubstitutionGroupName.put( elName, substitutionElementName );
            }
        }
    }

    /**
     * Creates a new {@link GMLAppSchemaReader} from the given schema file (which may be a directory).
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
    public GMLAppSchemaReader( GMLVersion gmlVersion, Map<String, String> namespaceHints, File schemaFile )
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

    public AppSchema extractAppSchema() {

        for ( QName ftName : ftNameToFtElement.keySet() ) {
            FeatureType ft = buildFeatureType( ftNameToFtElement.get( ftName ) );
            ftNameToFt.put( ftName, ft );
        }

        // resolveFtReferences();

        FeatureType[] fts = ftNameToFt.values().toArray( new FeatureType[ftNameToFt.size()] );

        Map<FeatureType, FeatureType> ftSubstitution = new HashMap<FeatureType, FeatureType>();
        for ( QName ftName : ftNameToSubstitutionGroupName.keySet() ) {
            QName substitutionFtName = ftNameToSubstitutionGroupName.get( ftName );
            if ( substitutionFtName != null ) {
                ftSubstitution.put( ftNameToFt.get( ftName ), ftNameToFt.get( substitutionFtName ) );
            }
        }

        List<GMLObjectType> geometryTypes = new ArrayList<GMLObjectType>();
        for ( final XSElementDeclaration elDecl : analyzer.getGeometryElementDeclarations( null, false ) ) {
            final GMLObjectType type = buildGenericObjectType( elDecl );
            geometryTypes.add( type );
            typeNameToType.put( type.getName(), type );
        }

        Map<GMLObjectType, GMLObjectType> typeToSuperType = new HashMap<GMLObjectType, GMLObjectType>();
        for ( QName ftName : geometryNameToSubstitutionGroupName.keySet() ) {
            QName substitutionFtName = geometryNameToSubstitutionGroupName.get( ftName );
            if ( substitutionFtName != null ) {
                typeToSuperType.put( typeNameToType.get( ftName ), typeNameToType.get( substitutionFtName ) );
            }
        }

        final List<GMLObjectType> genericGmlObjectTypes = new ArrayList<GMLObjectType>();
        for ( final XSElementDeclaration elDecl : analyzer.getTimeObjectElementDeclarations( null, false ) ) {
            final GMLObjectType type = buildGenericObjectType( elDecl );
            genericGmlObjectTypes.add( type );
            typeNameToType.put( type.getName(), type );
        }

        return new GenericAppSchema( fts, ftSubstitution, prefixToNs, analyzer, geometryTypes, typeToSuperType );
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
            int minOccurs = particle.getMinOccurs();
            int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
            XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case XSConstants.MODEL_GROUP: {
                addPropertyTypes( pts, (XSModelGroup) term, minOccurs, maxOccurs );
                break;
            }
            case XSConstants.ELEMENT_DECLARATION: {
                pts.add( buildPropertyType( (XSElementDeclaration) term, minOccurs, maxOccurs ) );
                break;
            }
            case XSConstants.WILDCARD: {
                String msg = "Broken GML application schema: Feature element '" + ftName
                             + "' uses wildcard in type model.";
                throw new IllegalArgumentException( msg );
            }
            default: {
                String msg = "Internal error. Unhandled term type: " + term.getName();
                throw new RuntimeException( msg );
            }
            }
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
            LOG.debug( "Empty feature type declaration." );
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_MIXED: {
            String msg = "Broken GML application schema: Feature element '" + ftName
                         + "' uses mixed content in type model.";
            throw new IllegalArgumentException( msg );
        }
        case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE: {
            String msg = "Broken GML application schema: Feature element '" + ftName
                         + "' uses simple content in type model.";
            throw new IllegalArgumentException( msg );
        }
        default: {
            String msg = "Internal error. Unhandled ContentType: " + typeDef.getContentType();
            throw new RuntimeException( msg );
        }
        }

        List<XSElementDeclaration> fcDecls = analyzer.getFeatureCollectionElementDeclarations( null, false );
        if ( fcDecls.contains( featureElementDecl ) ) {
            return new GenericFeatureCollectionType( ftName, pts, featureElementDecl.getAbstract() );
        }

        return new GenericFeatureType( ftName, pts, featureElementDecl.getAbstract() );
    }

    private GMLObjectType buildGenericObjectType( final XSElementDeclaration elDecl ) {
        final QName elName = createQName( elDecl.getNamespace(), elDecl.getName() );
        final GMLObjectCategory category = analyzer.getObjectCategory( elName );
        LOG.debug( "Building object type declaration: '" + elName + "'" );
        if ( elDecl.getTypeDefinition().getType() == XSTypeDefinition.SIMPLE_TYPE ) {
            final String msg = "The schema type of element '" + elName
                               + "' is simple, but object elements must have a complex type.";
            throw new IllegalArgumentException( msg );
        }
        final List<PropertyType> pts = new ArrayList<PropertyType>();
        final XSComplexTypeDefinition typeDef = (XSComplexTypeDefinition) elDecl.getTypeDefinition();
        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            final XSParticle particle = typeDef.getParticle();
            final int minOccurs = particle.getMinOccurs();
            final int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
            final XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case MODEL_GROUP: {
                addPropertyTypes( pts, (XSModelGroup) term, minOccurs, maxOccurs );
                break;
            }
            case ELEMENT_DECLARATION: {
                pts.add( buildPropertyType( (XSElementDeclaration) term, minOccurs, maxOccurs ) );
                break;
            }
            case WILDCARD: {
                final String msg = "Broken GML application schema: Object element '" + elName
                                   + "' uses wildcard in type model.";
                throw new IllegalArgumentException( msg );
            }
            default: {
                final String msg = "Internal error. Unhandled term type: " + term.getName();
                throw new RuntimeException( msg );
            }
            }
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
            LOG.debug( "Empty GML object type declaration." );
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_MIXED: {
            final String msg = "Broken GML application schema: GML object element '" + elName
                               + "' uses mixed content in type model.";
            throw new IllegalArgumentException( msg );
        }
        case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE: {
            final String msg = "Broken GML application schema: GML object element '" + elName
                               + "' uses simple content in type model.";
            throw new IllegalArgumentException( msg );
        }
        default: {
            final String msg = "Internal error. Unhandled ContentType: " + typeDef.getContentType();
            throw new RuntimeException( msg );
        }
        }
        return new GenericGMLObjectType( category, elName, pts, elDecl.getAbstract() );
    }

    private void addPropertyTypes( List<PropertyType> pts, XSModelGroup modelGroup, int parentMinOccurs,
                                   int parentMaxOccurs ) {

        switch ( modelGroup.getCompositor() ) {
        case XSModelGroup.COMPOSITOR_ALL: {
            LOG.debug( "Encountered 'All' compositor in object type model -- treating as sequence." );
            break;
        }
        case XSModelGroup.COMPOSITOR_CHOICE: {
            LOG.debug( "Encountered 'Choice' compositor in object type model -- treating as sequence." );
            break;
        }
        }

        XSObjectList sequence = modelGroup.getParticles();
        for ( int i = 0; i < sequence.getLength(); i++ ) {
            XSParticle particle = (XSParticle) sequence.item( i );
            int minOccurs = getCombinedOccurs( parentMinOccurs, particle.getMinOccurs() );
            int maxOccurs = getCombinedOccurs( parentMaxOccurs,
                                               particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs() );

            switch ( particle.getTerm().getType() ) {
            case XSConstants.ELEMENT_DECLARATION: {
                XSElementDeclaration elementDecl = (XSElementDeclaration) particle.getTerm();
                PropertyType pt = buildPropertyType( elementDecl, minOccurs, maxOccurs );
                if ( pt != null ) {
                    pts.add( pt );
                }
                break;
            }
            case XSConstants.MODEL_GROUP: {
                addPropertyTypes( pts, (XSModelGroup) particle.getTerm(), minOccurs, maxOccurs );
                break;
            }
            case XSConstants.WILDCARD: {
                String msg = "Broken GML application schema: Encountered wildcard in feature type definition.";
                throw new IllegalArgumentException( msg );
            }
            default: {
                String msg = "Internal error. Unhandled term type: " + particle.getTerm().getName();
                throw new RuntimeException( msg );
            }
            }
        }
    }

    private PropertyType buildPropertyType( XSElementDeclaration elementDecl, int minOccurs, int maxOccurs ) {

        PropertyType pt = null;
        QName ptName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.trace( "*** Found property declaration: '" + elementDecl.getName() + "'." );

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
        return pt;
    }

    private PropertyType buildPropertyType( XSElementDeclaration elementDecl, XSComplexTypeDefinition typeDef,
                                            int minOccurs, int maxOccurs, List<PropertyType> ptSubstitutions ) {

        PropertyType pt = null;
        QName ptName = createQName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.trace( "- Property definition '" + ptName + "' uses a complex type for content definition." );

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

    /**
     * Combines the minOccurs/maxOccurs information from a parent model group with the current one.
     * <p>
     * This is basically done to cope with GML application schemas that don't follow good practices (minOccurs/maxOccurs
     * should only be set on the property element declaration).
     * </p>
     *
     * @param parentOccurs
     *            occurence information of the parent model group, -1 means unbounded
     * @param occurs
     *            occurence information of the current particle, -1 means unbounded
     * @return combined occurence information, -1 means unbounded
     */
    private int getCombinedOccurs( int parentOccurs, int occurs ) {
        if ( parentOccurs == -1 || occurs == -1 ) {
            return -1;
        }
        return parentOccurs * occurs;
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
