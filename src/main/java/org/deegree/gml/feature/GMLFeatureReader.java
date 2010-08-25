//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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
package org.deegree.gml.feature;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_MIXED;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_SIMPLE;
import static org.apache.xerces.xs.XSTypeDefinition.SIMPLE_TYPE;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.StringOrRefPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.deegree.gml.feature.schema.DefaultGMLTypes;
import org.deegree.gml.geometry.GML2GeometryReader;
import org.deegree.gml.geometry.GML3GeometryReader;
import org.deegree.gml.geometry.GMLGeometryReader;
import org.deegree.gml.geometry.refs.GeometryReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based reader for GML-encoded features and feature collections.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureReader extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( GMLFeatureReader.class );

    private static String FID = "fid";

    private static String GMLID = "id";

    private final String gmlNs;

    private ApplicationSchema schema;

    private final GeometryFactory geomFac;

    private final GMLDocumentIdContext idContext;

    private GMLGeometryReader geomReader;

    private GMLReferenceResolver specialResolver;

    private final GMLVersion version;

    /**
     * Creates a new {@link GMLFeatureReader} instance that is configured for building features with the specified
     * feature types.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @param schema
     *            application schema that defines the feature types, must not be <code>null</code>
     * @param idContext
     *            id context to be used for registering gml:ids (features and geometries) and resolving local xlinks,
     *            can be <code>null</code>
     * @param defaultCoordDim
     *            defaultValue for coordinate dimension, only used when a posList is parsed and no dimension information
     *            from CRS is available (unknown CRS)
     */
    public GMLFeatureReader( GMLVersion version, ApplicationSchema schema, GMLDocumentIdContext idContext,
                             int defaultCoordDim ) {
        this.schema = schema;
        this.geomFac = new GeometryFactory();
        this.idContext = idContext != null ? idContext : new GMLDocumentIdContext( version );
        if ( version.equals( GMLVersion.GML_2 ) ) {
            this.geomReader = new GML2GeometryReader( geomFac, idContext );
        } else {
            this.geomReader = new GML3GeometryReader( version, geomFac, idContext, defaultCoordDim );
        }
        this.version = version;
        if ( version.equals( GMLVersion.GML_32 ) ) {
            gmlNs = CommonNamespaces.GML3_2_NS;
        } else {
            gmlNs = CommonNamespaces.GMLNS;
        }
    }

    /**
     * Creates a new {@link GMLFeatureReader} instance that is configured for building features with the specified
     * feature types.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @param schema
     *            application schema that defines the feature types, must not be <code>null</code>
     * @param idContext
     *            id context to be used for registering gml:ids (features and geometries and resolving local xlinks and
     * @param defaultCoordDim
     *            defaultValue for coordinate dimension, only used when a posList is parsed and no dimension information
     *            from CRS is available (unknown CRS) *
     * @param resolver
     */
    public GMLFeatureReader( GMLVersion version, ApplicationSchema schema, GMLDocumentIdContext idContext,
                             int defaultCoordDim, GMLReferenceResolver resolver ) {
        this.schema = schema;
        this.geomFac = new GeometryFactory();
        this.idContext = idContext;
        this.specialResolver = resolver;
        if ( version.equals( GMLVersion.GML_2 ) ) {
            this.geomReader = new GML2GeometryReader( geomFac, idContext );
        } else {
            this.geomReader = new GML3GeometryReader( version, geomFac, idContext, defaultCoordDim );
        }
        this.version = version;
        if ( version.equals( GMLVersion.GML_32 ) ) {
            gmlNs = CommonNamespaces.GML3_2_NS;
        } else {
            gmlNs = CommonNamespaces.GMLNS;
        }
    }

    /**
     * @param geomReader
     */
    public void setGeometryReader( GMLGeometryReader geomReader ) {
        this.geomReader = geomReader;
    }

    /**
     * Returns the associated {@link ApplicationSchema} that describes the structure of the feature types.
     * 
     * @return the associated {@link ApplicationSchema}
     */
    public ApplicationSchema getApplicationSchema() {
        return schema;
    }

    /**
     * Returns the {@link GMLDocumentIdContext} that keeps track of objects, identifieres and references.
     * 
     * @return the {@link GMLDocumentIdContext}, never <code>null</code>
     */
    public GMLDocumentIdContext getDocumentIdContext() {
        return idContext;
    }

    /**
     * Returns the object representation for the feature (or feature collection) element event that the cursor of the
     * given <code>XMLStreamReader</code> points at.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event of the feature element, afterwards points at
     *            the next event after the <code>END_ELEMENT</code> event of the feature element
     * @param crs
     *            default CRS for all descendant geometry properties
     * @return object representation for the given feature element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Feature parseFeature( XMLStreamReaderWrapper xmlStream, CRS crs )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        if ( schema == null ) {
            schema = buildApplicationSchema( xmlStream );
        }

        Feature feature = null;
        String fid = parseFeatureId( xmlStream );

        QName featureName = xmlStream.getName();
        FeatureType ft = lookupFeatureType( xmlStream, featureName );

        LOG.debug( "- parsing feature, gml:id=" + fid + " (begin): " + xmlStream.getCurrentEventInfo() );

        // parse properties
        Iterator<PropertyType> declIter = ft.getPropertyDeclarations( version ).iterator();

        PropertyType activeDecl = declIter.next();
        int propOccurences = 0;

        CRS activeCRS = crs;
        List<Property> propertyList = new ArrayList<Property>();

        xmlStream.nextTag();

        while ( xmlStream.getEventType() == START_ELEMENT ) {

            QName propName = xmlStream.getName();

            LOG.debug( "- property '" + propName + "'" );

            if ( findConcretePropertyType( propName, activeDecl ) != null ) {
                // current property element is equal to active declaration
                if ( activeDecl.getMaxOccurs() != -1 && propOccurences > activeDecl.getMaxOccurs() ) {
                    String msg = Messages.getMessage( "ERROR_PROPERTY_TOO_MANY_OCCURENCES", propName,
                                                      activeDecl.getMaxOccurs(), ft.getName() );
                    throw new XMLParsingException( xmlStream, msg );
                }
            } else {
                // current property element is not equal to active declaration
                while ( declIter.hasNext() && findConcretePropertyType( propName, activeDecl ) == null ) {
                    if ( propOccurences < activeDecl.getMinOccurs() ) {
                        String msg = null;
                        if ( activeDecl.getMinOccurs() == 1 ) {
                            msg = Messages.getMessage( "ERROR_PROPERTY_MANDATORY", activeDecl.getName(), ft.getName() );
                        } else {
                            msg = Messages.getMessage( "ERROR_PROPERTY_TOO_FEW_OCCURENCES", activeDecl.getName(),
                                                       activeDecl.getMinOccurs(), ft.getName() );
                        }
                        throw new XMLParsingException( xmlStream, msg );
                    }
                    activeDecl = declIter.next();
                    propOccurences = 0;
                }
                if ( findConcretePropertyType( propName, activeDecl ) == null ) {
                    String msg = Messages.getMessage( "ERROR_PROPERTY_UNEXPECTED", propName, ft.getName() );
                    throw new XMLParsingException( xmlStream, msg );
                }
            }

            Property property = parseProperty( xmlStream, findConcretePropertyType( propName, activeDecl ), activeCRS,
                                               propOccurences );
            if ( property != null ) {
                // if this is the "gml:boundedBy" property, override active CRS
                // (see GML spec. (where???))
                if ( StandardGMLFeatureProps.PT_BOUNDED_BY_GML31.getName().equals( activeDecl.getName() )
                     || StandardGMLFeatureProps.PT_BOUNDED_BY_GML32.getName().equals( activeDecl.getName() ) ) {
                    Envelope bbox = (Envelope) property.getValue();
                    if ( bbox.getCoordinateSystem() != null ) {
                        activeCRS = bbox.getCoordinateSystem();
                        LOG.debug( "- crs (from boundedBy): '" + activeCRS + "'" );
                    }
                }

                propertyList.add( property );
            }
            propOccurences++;
            xmlStream.nextTag();
        }
        LOG.debug( " - parsing feature (end): " + xmlStream.getCurrentEventInfo() );

        feature = ft.newFeature( fid, propertyList, version );

        if ( fid != null && !"".equals( fid ) ) {
            if ( idContext.getObject( fid ) != null ) {
                String msg = Messages.getMessage( "ERROR_FEATURE_ID_NOT_UNIQUE", fid );
                throw new XMLParsingException( xmlStream, msg );
            }
            idContext.addObject( feature );
        }
        return feature;
    }

    private ApplicationSchema buildApplicationSchema( XMLStreamReaderWrapper xmlStream )
                            throws XMLParsingException {
        String schemaLocation = xmlStream.getAttributeValue( XSINS, "schemaLocation" );
        if ( schemaLocation == null ) {
            throw new XMLParsingException( xmlStream, Messages.getMessage( "ERROR_NO_SCHEMA_LOCATION",
                                                                           xmlStream.getSystemId() ) );
        }

        String[] tokens = schemaLocation.trim().split( "\\s+" );
        if ( tokens.length % 2 != 0 ) {
            throw new XMLParsingException( xmlStream, Messages.getMessage( "ERROR_SCHEMA_LOCATION_TOKENS_COUNT",
                                                                           xmlStream.getSystemId() ) );
        }
        String[] schemaUrls = new String[tokens.length / 2];
        for ( int i = 0; i < schemaUrls.length; i++ ) {
            String schemaUrl = tokens[i * 2 + 1];
            try {
                if ( xmlStream.getSystemId() == null ) {
                    // must be absolute, as SystemId is required for resolving relative URLs
                    schemaUrls[i] = new URL( schemaUrl ).toString();
                } else {
                    schemaUrls[i] = new URL( new URL( xmlStream.getSystemId() ), schemaUrl ).toString();
                }
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( xmlStream, "Error parsing referenced application schema. Schema URL '"
                                                          + schemaUrl + "' from xsi:schemaLocation attribute is "
                                                          + "invalid/cannot be resolved." );

            }
        }

        // TODO handle multi-namespace schemas
        ApplicationSchema schema = null;
        try {
            ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( version, null, schemaUrls );
            schema = decoder.extractFeatureTypeSchema();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new XMLParsingException( xmlStream, "Error parsing application schema: " + e.getMessage() );
        }
        return schema;
    }

    private PropertyType findConcretePropertyType( QName elemName, PropertyType pt ) {
        LOG.debug( "Checking if '" + elemName + "' is a valid substitution for '" + pt.getName() + "'" );

        for ( PropertyType substitutionPt : pt.getSubstitutions() ) {
            // TODO !substitutionPt.isAbstract()
            if ( elemName.equals( substitutionPt.getName() ) ) {
                LOG.debug( "Yep. Substitutable for '" + substitutionPt.getName() + "'" );
                return substitutionPt;
            }
        }
        LOG.debug( "Nope." );
        return null;
    }

    /**
     * Returns the object representation for the given property element.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event of the property, afterwards points at the
     *            next event after the <code>END_ELEMENT</code> of the property
     * @param propDecl
     *            property declaration
     * @param crs
     *            default SRS for all a descendant geometry properties
     * @param occurence
     * @return object representation for the given property element.
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Property parseProperty( XMLStreamReaderWrapper xmlStream, PropertyType propDecl, CRS crs, int occurence )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        LOG.debug( "- parsing property (begin): " + xmlStream.getCurrentEventInfo() );
        LOG.debug( "- property declaration: " + propDecl );

        boolean isNilled = false;
        if ( propDecl.isNillable() ) {
            isNilled = StAXParsingHelper.getAttributeValueAsBoolean( xmlStream, XSINS, "nil", false );
        }

        Property property = null;
        if ( propDecl instanceof SimplePropertyType ) {
            property = parseSimpleProperty( xmlStream, (SimplePropertyType) propDecl, isNilled );
        } else if ( propDecl instanceof GeometryPropertyType ) {
            property = parseGeometryProperty( xmlStream, (GeometryPropertyType) propDecl, crs, isNilled );
        } else if ( propDecl instanceof FeaturePropertyType ) {
            property = parseFeatureProperty( xmlStream, (FeaturePropertyType) propDecl, crs, isNilled );
        } else if ( propDecl instanceof CustomPropertyType ) {
            property = parseCustomProperty( xmlStream, (CustomPropertyType) propDecl, crs, isNilled );
        } else if ( propDecl instanceof EnvelopePropertyType ) {
            property = parseEnvelopeProperty( xmlStream, (EnvelopePropertyType) propDecl, crs, isNilled );
        } else if ( propDecl instanceof CodePropertyType ) {
            property = parseCodeProperty( xmlStream, (CodePropertyType) propDecl, isNilled );
        } else if ( propDecl instanceof MeasurePropertyType ) {
            property = parseMeasureProperty( xmlStream, (MeasurePropertyType) propDecl, isNilled );
        } else if ( propDecl instanceof StringOrRefPropertyType ) {
            property = parseStringOrRefProperty( xmlStream, (StringOrRefPropertyType) propDecl, isNilled );
        } else {
            throw new RuntimeException( "Internal error in GMLFeatureReader: property type " + propDecl.getClass()
                                        + " not handled." );
        }

        LOG.debug( " - parsing property (end): " + xmlStream.getCurrentEventInfo() );
        return property;
    }

    private Property parseSimpleProperty( XMLStreamReaderWrapper xmlStream, SimplePropertyType propDecl,
                                          boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException {

        QName propName = xmlStream.getName();
        Property property = null;
        if ( isNilled ) {
            property = new GenericProperty( propDecl, propName, null, isNilled );
            // TODO need to check that element is indeed empty?
            StAXParsingHelper.nextElement( xmlStream );
        } else {
            property = createSimpleProperty( xmlStream, propDecl, xmlStream.getElementText().trim() );
        }
        return property;
    }

    private Property parseFeatureProperty( XMLStreamReaderWrapper xmlStream, FeaturePropertyType propDecl, CRS crs,
                                           boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException, UnknownCRSException {

        QName propName = xmlStream.getName();
        Property property = null;

        String href = xmlStream.getAttributeValue( XLNNS, "href" );
        if ( href != null ) {
            FeatureReference refFeature = null;
            if ( specialResolver != null ) {
                refFeature = new FeatureReference( specialResolver, href, xmlStream.getSystemId() );
            } else {
                refFeature = new FeatureReference( idContext, href, xmlStream.getSystemId() );
            }
            idContext.addReference( refFeature );
            property = new GenericProperty( propDecl, propName, refFeature, isNilled );
            xmlStream.nextTag();
        } else {
            // inline feature
            if ( xmlStream.nextTag() == START_ELEMENT ) {
                // TODO make this check (no constraints on contained feature
                // type) better
                if ( propDecl.getFTName() != null ) {
                    FeatureType expectedFt = propDecl.getValueFt();
                    FeatureType presentFt = lookupFeatureType( xmlStream, xmlStream.getName() );
                    if ( !schema.isSubType( expectedFt, presentFt ) ) {
                        String msg = Messages.getMessage( "ERROR_PROPERTY_WRONG_FEATURE_TYPE", expectedFt.getName(),
                                                          propName, presentFt.getName() );
                        throw new XMLParsingException( xmlStream, msg );
                    }
                }
                Feature subFeature = parseFeature( xmlStream, crs );
                property = new GenericProperty( propDecl, propName, subFeature, isNilled );
                xmlStream.skipElement();
            } else {
                // yes, empty feature property elements are actually valid
                property = new GenericProperty( propDecl, propName, null, isNilled );
            }
        }
        return property;
    }

    private Property parseGeometryProperty( XMLStreamReaderWrapper xmlStream, GeometryPropertyType propDecl, CRS crs,
                                            boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException, UnknownCRSException {

        QName propName = xmlStream.getName();
        Property property = null;

        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null ) {
            GeometryReference<Geometry> refGeometry = null;
            // TODO respect allowed geometry types (Point, Surface, etc.)
            if ( specialResolver != null ) {
                refGeometry = new GeometryReference<Geometry>( specialResolver, href, xmlStream.getSystemId() );
            } else {
                refGeometry = new GeometryReference<Geometry>( idContext, href, xmlStream.getSystemId() );
            }
            idContext.addReference( refGeometry );
            property = new GenericProperty( propDecl, propName, refGeometry, isNilled );
            xmlStream.nextTag();
        } else {
            if ( xmlStream.nextTag() == START_ELEMENT ) {
                Geometry geometry = null;
                geometry = geomReader.parse( xmlStream, crs );
                boolean compatible = false;
                for ( GeometryType allowedType : propDecl.getAllowedGeometryTypes() ) {
                    if ( allowedType.isCompatible( geometry ) ) {
                        compatible = true;
                        break;
                    }
                }
                if ( !compatible ) {
                    String msg = "Value for geometry property is invalid. Specified geometry value "
                                 + geometry.getClass() + " is not allowed here. Allowed geometries are: "
                                 + propDecl.getAllowedGeometryTypes();
                    throw new XMLParsingException( xmlStream, msg );
                }
                property = new GenericProperty( propDecl, propName, geometry, isNilled );
                xmlStream.nextTag();
            } else {
                // yes, empty geometry property elements are actually valid
                property = new GenericProperty( propDecl, propName, null, isNilled );
            }
        }
        return property;
    }

    private Property parseEnvelopeProperty( XMLStreamReaderWrapper xmlStream, EnvelopePropertyType propDecl, CRS crs,
                                            boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException {

        QName propName = xmlStream.getName();
        Property property = null;
        Envelope env = null;
        xmlStream.nextTag();
        if ( xmlStream.getName().equals( new QName( gmlNs, "Null" ) ) ) {
            // TODO
            StAXParsingHelper.skipElement( xmlStream );
        } else {
            env = geomReader.parseEnvelope( xmlStream, crs );
            property = new GenericProperty( propDecl, propName, env, isNilled );
        }
        xmlStream.nextTag();
        return property;
    }

    private Property parseCodeProperty( XMLStreamReaderWrapper xmlStream, CodePropertyType propDecl, boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException {

        QName propName = xmlStream.getName();
        String codeSpace = xmlStream.getAttributeValue( null, "codeSpace" );
        String code = xmlStream.getElementText().trim();
        CodeType value = new CodeType( code, codeSpace );
        return new GenericProperty( propDecl, propName, value, isNilled );
    }

    private Property parseMeasureProperty( XMLStreamReaderWrapper xmlStream, MeasurePropertyType propDecl,
                                           boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException {

        QName propName = xmlStream.getName();
        String uom = xmlStream.getAttributeValue( null, "uom" );
        Measure value = new Measure( xmlStream.getElementText(), uom );
        return new GenericProperty( propDecl, propName, value, isNilled );
    }

    private Property parseStringOrRefProperty( XMLStreamReaderWrapper xmlStream, StringOrRefPropertyType propDecl,
                                               boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException {

        QName propName = xmlStream.getName();
        String ref = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        String string = xmlStream.getElementText().trim();
        return new GenericProperty( propDecl, propName, new StringOrRef( string, ref ), isNilled );
    }

    private Property parseCustomProperty( XMLStreamReaderWrapper xmlStream, CustomPropertyType propDecl, CRS crs,
                                          boolean isNilled )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException, UnknownCRSException {

        QName propName = xmlStream.getName();
        TypedObjectNode value = parseGenericXMLElement( xmlStream, propDecl.getXSDValueType(), crs );

        if ( value instanceof GenericXMLElement ) {
            // unwrap the element -> we just want a node that represents the
            // element's value
            GenericXMLElement xmlEl = (GenericXMLElement) value;
            value = new GenericXMLElementContent( xmlEl.getXSType(), xmlEl.getAttributes(), xmlEl.getChildren() );
        }
        return new GenericProperty( propDecl, propName, value, isNilled );
    }

    private TypedObjectNode parseGenericXMLElement( XMLStreamReaderWrapper xmlStream, XSTypeDefinition xsdValueType,
                                                    CRS crs )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException, UnknownCRSException {
        TypedObjectNode node = null;
        LOG.debug( xmlStream.getName().toString() );
        if ( xsdValueType.getTypeCategory() == SIMPLE_TYPE ) {
            node = parseGenericXMLElement( xmlStream, (XSSimpleTypeDefinition) xsdValueType );
        } else {
            node = parseGenericXMLElement( xmlStream, (XSComplexTypeDefinition) xsdValueType, crs );
        }
        return node;
    }

    private GenericXMLElement parseGenericXMLElement( XMLStreamReaderWrapper xmlStream,
                                                      XSSimpleTypeDefinition xsdValueType )
                            throws XMLStreamException {
        // TODO element has simple type information and primitive type as well?
        TypedObjectNode child = new PrimitiveValue( xmlStream.getElementText(), xsdValueType );
        return new GenericXMLElement( xmlStream.getName(), xsdValueType, null, Collections.singletonList( child ) );
    }

    private GenericXMLElement parseGenericXMLElement( XMLStreamReaderWrapper xmlStream,
                                                      XSComplexTypeDefinition xsdValueType, CRS crs )
                            throws NoSuchElementException, XMLStreamException, XMLParsingException, UnknownCRSException {

        Map<QName, PrimitiveValue> attrs = parseAttributes( xmlStream, xsdValueType );
        List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();

        Map<QName, XSElementDeclaration> childElementDecls = getAllowedChildElementDecls( xsdValueType );

        switch ( xsdValueType.getContentType() ) {
        case CONTENTTYPE_ELEMENT: {
            // TODO respect order + multiplicity of child elements
            int eventType = 0;
            while ( ( eventType = xmlStream.next() ) != END_ELEMENT ) {
                if ( eventType == START_ELEMENT ) {
                    QName childElName = xmlStream.getName();
                    if ( geomReader.isGeometryElement( xmlStream ) ) {
                        children.add( geomReader.parse( xmlStream, crs ) );
                    } else {
                        if ( !childElementDecls.containsKey( childElName ) ) {
                            String msg = "Element '" + childElName + "' is not allowed at this position.";
                            throw new XMLParsingException( xmlStream, msg );
                        }
                        TypedObjectNode child = parseGenericXMLElement(
                                                                        xmlStream,
                                                                        childElementDecls.get( childElName ).getTypeDefinition(),
                                                                        crs );
                        LOG.debug( "adding: " + childElName + ", " + child.getClass().getName() );
                        children.add( child );
                    }
                }
            }
            break;
        }
        case CONTENTTYPE_SIMPLE: {
            int eventType = 0;
            while ( ( eventType = xmlStream.next() ) != END_ELEMENT ) {
                if ( eventType == CDATA || eventType == CHARACTERS ) {
                    PrimitiveValue pb = new PrimitiveValue( xmlStream.getText(), xsdValueType.getSimpleType() );
                    children.add( pb );
                } else if ( eventType == START_ELEMENT ) {
                    QName childElName = xmlStream.getName();
                    if ( !childElementDecls.containsKey( childElName ) ) {
                        String msg = "Element '" + childElName + "' is not allowed at this position.";
                        throw new XMLParsingException( xmlStream, msg );
                    }
                }
            }
            break;
        }
        case CONTENTTYPE_MIXED: {
            int eventType = 0;
            while ( ( eventType = xmlStream.next() ) != END_ELEMENT ) {
                if ( eventType == START_ELEMENT ) {
                    QName childElName = xmlStream.getName();
                    LOG.debug( "Child: " + childElName );
                    if ( geomReader.isGeometryElement( xmlStream ) ) {
                        children.add( geomReader.parse( xmlStream, crs ) );
                    } else {
                        if ( !childElementDecls.containsKey( childElName ) ) {
                            String msg = "Element '" + childElName + "' is not allowed at this position.";
                            throw new XMLParsingException( xmlStream, msg );
                        }
                        TypedObjectNode child = parseGenericXMLElement(
                                                                        xmlStream,
                                                                        childElementDecls.get( childElName ).getTypeDefinition(),
                                                                        crs );
                        LOG.debug( "adding: " + childElName + ", " + child.getClass().getName() );
                        children.add( child );
                    }
                } else if ( eventType == CDATA || eventType == CHARACTERS ) {
                    // mixed content -> use string as primitive type
                    String s = xmlStream.getText();
                    if ( !s.trim().isEmpty() ) {
                        children.add( new PrimitiveValue( s ) );
                    }
                }
            }
            break;
        }
        case CONTENTTYPE_EMPTY: {
            break;
        }
        }

        LOG.debug( "creating element: " + xmlStream.getName() );
        return new GenericXMLElement( xmlStream.getName(), xsdValueType, attrs, children );
    }

    private Map<QName, XSElementDeclaration> getAllowedChildElementDecls( XSComplexTypeDefinition type ) {
        List<XSElementDeclaration> childDecls = new ArrayList<XSElementDeclaration>();
        getChildElementDecls( type.getParticle(), childDecls );

        Map<QName, XSElementDeclaration> childDeclMap = new HashMap<QName, XSElementDeclaration>();
        for ( XSElementDeclaration decl : childDecls ) {
            QName name = new QName( decl.getNamespace(), decl.getName() );
            childDeclMap.put( name, decl );
            for ( XSElementDeclaration substitution : schema.getXSModel().getSubstitutions( decl, null, true, true ) ) {
                name = new QName( substitution.getNamespace(), substitution.getName() );
                LOG.debug( "Adding: " + name );
                childDeclMap.put( name, substitution );
            }
        }
        return childDeclMap;
    }

    private void getChildElementDecls( XSParticle particle, List<XSElementDeclaration> propDecls ) {
        if ( particle != null ) {
            XSTerm term = particle.getTerm();
            if ( term instanceof XSElementDeclaration ) {
                // XSElementDeclaration childElDecl = (XSElementDeclaration) term;
                // QName name = new QName( childElDecl.getNamespace(), childElDecl.getName() );
                propDecls.add( (XSElementDeclaration) term );
            } else if ( term instanceof XSModelGroup ) {
                XSObjectList particles = ( (XSModelGroup) term ).getParticles();
                for ( int i = 0; i < particles.getLength(); i++ ) {
                    getChildElementDecls( (XSParticle) particles.item( i ), propDecls );
                }
            } else {
                LOG.warn( "Unhandled term type: " + term.getClass() );
            }
        }
    }

    private Map<QName, PrimitiveValue> parseAttributes( XMLStreamReader xmlStream, XSComplexTypeDefinition xsdValueType ) {

        Map<QName, XSAttributeDeclaration> attrDecls = new HashMap<QName, XSAttributeDeclaration>();
        for ( int i = 0; i < xsdValueType.getAttributeUses().getLength(); i++ ) {
            XSAttributeDeclaration attrDecl = ( (XSAttributeUse) xsdValueType.getAttributeUses().item( i ) ).getAttrDeclaration();
            QName name = new QName( attrDecl.getNamespace(), attrDecl.getName() );
            attrDecls.put( name, attrDecl );
        }

        Map<QName, PrimitiveValue> attrs = new LinkedHashMap<QName, PrimitiveValue>();
        for ( int i = 0; i < xmlStream.getAttributeCount(); i++ ) {
            QName name = xmlStream.getAttributeName( i );
            XSAttributeDeclaration attrDecl = attrDecls.get( name );
            if ( attrDecl == null && !XSINS.equals( name.getNamespaceURI() ) ) {
                String msg = "Attribute '" + name + "' is not allowed at this position.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( attrDecl != null && !XSINS.equals( name.getNamespaceURI() ) ) {
                String value = xmlStream.getAttributeValue( i );
                // TODO evaluate and check primitive type information
                PrimitiveValue xmlValue = new PrimitiveValue( value, attrDecl.getTypeDefinition() );
                attrs.put( name, xmlValue );
            }
        }

        for ( int i = 0; i < xsdValueType.getAttributeUses().getLength(); i++ ) {
            XSAttributeUse attrUse = (XSAttributeUse) xsdValueType.getAttributeUses().item( i );
            if ( attrUse.getRequired() ) {
                XSAttributeDeclaration attrDecl = attrUse.getAttrDeclaration();
                QName name = new QName( attrDecl.getNamespace(), attrDecl.getName() );
                if ( !attrs.containsKey( name ) ) {
                    String msg = "Required attribute '" + name + "' is missing.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            }
        }
        return attrs;
    }

    private SimpleProperty createSimpleProperty( XMLStreamReader xmlStream, SimplePropertyType pt, String s )
                            throws XMLParsingException {

        SimpleProperty prop = null;
        try {
            prop = new SimpleProperty( pt, s, pt.getPrimitiveType() );
        } catch ( IllegalArgumentException e ) {
            String msg = "Property '" + pt.getName() + "' is not valid: " + e.getMessage();
            throw new XMLParsingException( xmlStream, msg );
        }
        return prop;
    }

    /**
     * Returns the feature type with the given name.
     * <p>
     * If no feature type with the given name is defined, an XMLParsingException is thrown.
     * 
     * @param xmlStreamReader
     * 
     * @param ftName
     *            feature type name to look up
     * @return the feature type with the given name
     * @throws XMLParsingException
     *             if no feature type with the given name is defined
     */
    protected FeatureType lookupFeatureType( XMLStreamReaderWrapper xmlStreamReader, QName ftName )
                            throws XMLParsingException {

        // TODO implement this less hacky
        if ( ftName.equals( DefaultGMLTypes.GML311_FEATURECOLLECTION.getName() ) ) {
            return DefaultGMLTypes.GML311_FEATURECOLLECTION;
        }

        FeatureType ft = null;
        ft = schema.getFeatureType( ftName );
        if ( ft == null ) {
            String msg = Messages.getMessage( "ERROR_SCHEMA_FEATURE_TYPE_UNKNOWN", ftName );
            throw new XMLParsingException( xmlStreamReader, msg );
        }
        return ft;
    }

    /**
     * Parses the feature id attribute from the feature <code>START_ELEMENT</code> event that the given
     * <code>XMLStreamReader</code> points to.
     * <p>
     * Looks after 'gml:id' (GML 3) first, if no such attribute is present, the 'fid' (GML 2) attribute is used.
     * 
     * @param xmlReader
     *            must point to the <code>START_ELEMENT</code> event of the feature
     * @return the feature id, or "" (empty string) if neither a 'gml:id' nor a 'fid' attribute is present
     */
    protected String parseFeatureId( XMLStreamReaderWrapper xmlReader ) {

        String fid = xmlReader.getAttributeValue( gmlNs, GMLID );
        if ( fid == null ) {
            fid = xmlReader.getAttributeValue( null, FID );
        }

        // Check that the feature id has the correct form. "fid" and "gml:id"
        // are both based
        // on the XML type "ID": http://www.w3.org/TR/xmlschema11-2/#NCName
        // Thus, they must match the NCName production rule. This means that
        // they may not contain
        // a separating colon (only at the first position a colon is allowed)
        // and must not
        // start with a digit.
        if ( fid != null && fid.length() > 0 && !fid.matches( "[^\\d][^:]+" ) ) {
            String msg = Messages.getMessage( "ERROR_INVALID_FEATUREID", fid );
            throw new IllegalArgumentException( msg );
        }
        return fid;
    }
}
