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

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.types.datetime.Date;
import org.deegree.commons.types.datetime.DateTime;
import org.deegree.commons.types.datetime.Time;
import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericProperty;
import org.deegree.feature.Property;
import org.deegree.feature.i18n.Messages;
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
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.generic.GenericCustomPropertyReader;
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

    // private XSModel xsModel;

    private final GeometryFactory geomFac;

    private final Map<PropertyType, CustomPropertyReader<?>> ptToParser = new HashMap<PropertyType, CustomPropertyReader<?>>();

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
     */
    public GMLFeatureReader( GMLVersion version, ApplicationSchema schema, GMLDocumentIdContext idContext ) {
        this.schema = schema;
        this.geomFac = new GeometryFactory();
        this.idContext = idContext != null ? idContext : new GMLDocumentIdContext( version );
        if ( version.equals( GMLVersion.GML_2 ) ) {
            this.geomReader = new GML2GeometryReader( geomFac, idContext );
        } else {
            this.geomReader = new GML3GeometryReader( version, geomFac, idContext );
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
     * @param resolver
     */
    public GMLFeatureReader( GMLVersion version, ApplicationSchema schema, GMLDocumentIdContext idContext,
                             GMLReferenceResolver resolver ) {
        this.schema = schema;
        this.geomFac = new GeometryFactory();
        this.idContext = idContext;
        this.specialResolver = resolver;
        if ( version.equals( GMLVersion.GML_2 ) ) {
            this.geomReader = new GML2GeometryReader( geomFac, idContext );
        } else {
            this.geomReader = new GML3GeometryReader( version, geomFac, idContext );
        }
        this.version = version;
        if ( version.equals( GMLVersion.GML_32 ) ) {
            gmlNs = CommonNamespaces.GML3_2_NS;
        } else {
            gmlNs = CommonNamespaces.GMLNS;
        }
    }

    public void setGeometryReader( GMLGeometryReader geomReader ) {
        this.geomReader = geomReader;
    }

    /**
     * Registers a {@link CustomPropertyReader} that is invoked to parse properties of a certain type.
     * 
     * @param pt
     * @param parser
     */
    public void registerCustomPropertyParser( PropertyType pt, CustomPropertyReader<?> parser ) {
        this.ptToParser.put( pt, parser );
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
        Iterator<PropertyType<?>> declIter = ft.getPropertyDeclarations( version ).iterator();

        PropertyType activeDecl = declIter.next();
        int propOccurences = 0;

        CRS activeCRS = crs;
        List<Property<?>> propertyList = new ArrayList<Property<?>>();

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

            Property<?> property = parseProperty( xmlStream, findConcretePropertyType( propName, activeDecl ),
                                                  activeCRS, propOccurences );
            if ( property != null ) {
                // if this is the "gml:boundedBy" property, override active CRS (see GML spec. (where???))
                if ( StandardGMLFeatureProps.PT_BOUNDED_BY_GML31.getName().equals( activeDecl.getName() ) ) {
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
                schemaUrls[i] = new URL( new URL( xmlStream.getSystemId() ), schemaUrl ).toString();
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( xmlStream, "Error parsing application schema: " + e.getMessage() );
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

    private PropertyType<?> findConcretePropertyType( QName elemName, PropertyType<?> pt ) {
        LOG.debug( "Checking if '" + elemName + "' is a valid substitution for '" + pt.getName() + "'" );

        for ( PropertyType<?> substitutionPt : pt.getSubstitutions() ) {
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
    public Property<?> parseProperty( XMLStreamReaderWrapper xmlStream, PropertyType propDecl, CRS crs, int occurence )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Property<?> property = null;
        QName propName = xmlStream.getName();
        LOG.debug( "- parsing property (begin): " + xmlStream.getCurrentEventInfo() );
        LOG.debug( "- property declaration: " + propDecl );

        CustomPropertyReader<?> parser = ptToParser.get( propDecl );

        if ( parser == null ) {
            if ( propDecl instanceof SimplePropertyType ) {
                property = createSimpleProperty( xmlStream, (SimplePropertyType) propDecl,
                                                 xmlStream.getElementText().trim() );
            } else if ( propDecl instanceof GeometryPropertyType ) {
                String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
                if ( href != null ) {
                    // TODO respect geometry type information (Point, Surface, etc.)
                    GeometryReference<Geometry> refGeometry = null;
                    if ( specialResolver != null ) {
                        refGeometry = new GeometryReference<Geometry>( specialResolver, href, xmlStream.getSystemId() );
                    } else {
                        refGeometry = new GeometryReference<Geometry>( idContext, href, xmlStream.getSystemId() );
                    }
                    idContext.addReference( refGeometry );
                    property = new GenericProperty<Geometry>( propDecl, propName, refGeometry );
                    xmlStream.nextTag();
                } else {
                    xmlStream.nextTag();
                    Geometry geometry = null;
                    geometry = geomReader.parse( xmlStream, crs );
                    property = new GenericProperty<Geometry>( propDecl, propName, geometry );
                    xmlStream.nextTag();
                }
            } else if ( propDecl instanceof FeaturePropertyType ) {
                String uri = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
                if ( uri != null ) {
                    FeatureReference refFeature = null;
                    if ( specialResolver != null ) {
                        refFeature = new FeatureReference( specialResolver, uri, xmlStream.getSystemId() );
                    } else {
                        refFeature = new FeatureReference( idContext, uri, xmlStream.getSystemId() );
                    }
                    idContext.addReference( refFeature );
                    property = new GenericProperty<Feature>( propDecl, propName, refFeature );
                    xmlStream.nextTag();
                } else {
                    // inline feature
                    if ( xmlStream.nextTag() != START_ELEMENT ) {
                        String msg = Messages.getMessage( "ERROR_INVALID_FEATURE_PROPERTY", propName );
                        throw new XMLParsingException( xmlStream, msg );
                    }
                    // TODO make this check (no constraints on contained feature type) better
                    if ( ( (FeaturePropertyType) propDecl ).getFTName() != null ) {
                        FeatureType expectedFt = ( (FeaturePropertyType) propDecl ).getValueFt();
                        FeatureType presentFt = lookupFeatureType( xmlStream, xmlStream.getName() );
                        if ( !schema.isSubType( expectedFt, presentFt ) ) {
                            String msg = Messages.getMessage( "ERROR_PROPERTY_WRONG_FEATURE_TYPE",
                                                              expectedFt.getName(), propName, presentFt.getName() );
                            throw new XMLParsingException( xmlStream, msg );
                        }
                    }
                    Feature subFeature = parseFeature( xmlStream, crs );
                    property = new GenericProperty<Feature>( propDecl, propName, subFeature );
                    xmlStream.skipElement();
                }
            } else if ( propDecl instanceof CustomPropertyType ) {
                Object value = new GenericCustomPropertyReader().parse( xmlStream );
                property = new GenericProperty<Object>( propDecl, propName, value );
            } else if ( propDecl instanceof EnvelopePropertyType ) {
                Envelope env = null;
                xmlStream.nextTag();
                if ( xmlStream.getName().equals( new QName( gmlNs, "Null" ) ) ) {
                    // TODO
                    StAXParsingHelper.skipElement( xmlStream );
                } else {
                    env = geomReader.parseEnvelope( xmlStream, crs );
                    property = new GenericProperty<Object>( propDecl, propName, env );
                }
                xmlStream.nextTag();
            } else if ( propDecl instanceof CodePropertyType ) {
                String codeSpace = xmlStream.getAttributeValue( null, "codeSpace" );
                String code = xmlStream.getElementText().trim();
                Object value = new CodeType( code, codeSpace );
                property = new GenericProperty<Object>( propDecl, propName, value );
            } else if ( propDecl instanceof MeasurePropertyType ) {
                String uom = xmlStream.getAttributeValue( null, "uom" );
                Object value = new Measure( xmlStream.getElementText(), uom );
                property = new GenericProperty<Object>( propDecl, propName, value );
            } else if ( propDecl instanceof StringOrRefPropertyType ) {
                String ref = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
                String string = xmlStream.getElementText().trim();
                property = new GenericProperty<Object>( propDecl, propName, new StringOrRef( string, ref ) );
            }
        } else {
            LOG.trace( "************ Parsing property using custom parser." );
            Object value = parser.parse( xmlStream );
            property = new GenericProperty<Object>( propDecl, propName, value );
        }

        LOG.debug( " - parsing property (end): " + xmlStream.getCurrentEventInfo() );
        return property;
    }

    private Property<?> createSimpleProperty( XMLStreamReader xmlStream, SimplePropertyType propDecl, String s )
                            throws XMLParsingException {

        Object propValue = s;
        switch ( propDecl.getPrimitiveType() ) {
        case BOOLEAN: {
            if ( s.equals( "true" ) || s.equals( "1" ) ) {
                propValue = Boolean.TRUE;
            } else if ( s.equals( "false" ) || s.equals( "0" ) ) {
                propValue = Boolean.FALSE;
            } else {
                String msg = "Value ('" + s + "') for xs:boolean property '" + propDecl.getName()
                             + "' is invalid. Valid values are 'true', 'false', '1' and '0'.";
                throw new XMLParsingException( xmlStream, msg );
            }
            break;
        }
        case DATE: {
            try {
                propValue = new Date( s );
            } catch ( ParseException e ) {
                String msg = "Value ('" + s + "') for xs:date property '" + propDecl.getName() + "' is invalid.";
                throw new XMLParsingException( xmlStream, msg );
            }
            break;
        }
        case DATE_TIME: {
            try {
                propValue = new DateTime( s );
            } catch ( ParseException e ) {
                String msg = "Value ('" + s + "') for xs:dateTime property '" + propDecl.getName() + "' is invalid.";
                throw new XMLParsingException( xmlStream, msg );
            }
            break;
        }
        case DECIMAL: {
            propValue = new BigDecimal( s );
            break;
        }
        case DOUBLE: {
            propValue = new Double( s );
            break;
        }
        case INTEGER: {
            propValue = new BigInteger( s );
            break;
        }
        case STRING: {
            break;
        }
        case TIME: {
            try {
                propValue = new Time( s );
            } catch ( ParseException e ) {
                String msg = "Value ('" + s + "') for xs:time property '" + propDecl.getName() + "' is invalid.";
                throw new XMLParsingException( xmlStream, msg );
            }
            break;
        }
        default: {
            LOG.warn( "Unhandled primitive type " + propDecl.getPrimitiveType() + " -- treating as string value." );
        }
        }
        return new GenericProperty<Object>( propDecl, null, propValue );
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

        // Check that the feature id has the correct form. "fid" and "gml:id" are both based
        // on the XML type "ID": http://www.w3.org/TR/xmlschema11-2/#NCName
        // Thus, they must match the NCName production rule. This means that they may not contain
        // a separating colon (only at the first position a colon is allowed) and must not
        // start with a digit.
        if ( fid != null && fid.length() > 0 && !fid.matches( "[^\\d][^:]+" ) ) {
            String msg = Messages.getMessage( "ERROR_INVALID_FEATUREID", fid );
            throw new IllegalArgumentException( msg );
        }
        return fid;
    }
}
