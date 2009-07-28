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
package org.deegree.feature.gml;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSObjectList;
import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.gml.GMLVersion;
import org.deegree.commons.types.Measure;
import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericProperty;
import org.deegree.feature.Property;
import org.deegree.feature.generic.GenericCustomPropertyParser;
import org.deegree.feature.gml.schema.ApplicationSchemaXSDDecoder;
import org.deegree.feature.gml.schema.DefaultGMLTypes;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomComplexPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.gml.GML311GeometryDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes GML-encoded features and feature collections.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureDecoder extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( GMLFeatureDecoder.class );

    private static String FID = "fid";

    private static String GMLID = "id";

    private static String GMLNS = CommonNamespaces.GMLNS;

    private ApplicationSchema schema;

    private XSModel xsModel;

    private final GeometryFactory geomFac;

    private final Map<PropertyType, CustomPropertyDecoder<?>> ptToParser = new HashMap<PropertyType, CustomPropertyDecoder<?>>();

    private final GMLIdContext idContext;

    private final GML311GeometryDecoder geomParser;

    public GMLFeatureDecoder( ApplicationSchema schema, GMLIdContext idContext ) {
        this.schema = schema;
        if (schema != null) {
            this.xsModel = schema.getXSModel();
        }
        this.geomFac = new GeometryFactory();
        this.idContext = idContext;
        this.geomParser = new GML311GeometryDecoder( geomFac, idContext );
    }

    /**
     * Creates a new <code>FeatureGMLAdapter</code> instance instance that is configured for building features with the
     * specified feature types.
     * 
     * @param schema
     *            schema
     */
    public GMLFeatureDecoder( ApplicationSchema schema ) {
        this( schema, new GMLIdContext() );
    }

    /**
     * Registers a {@link CustomPropertyDecoder} that is invoked to parse properties of a certain type.
     * 
     * @param pt
     * @param parser
     */
    public void registerCustomPropertyParser( PropertyType pt, CustomPropertyDecoder<?> parser ) {
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
     * Returns the object representation for the feature element event that the cursor of the given
     * <code>XMLStreamReader</code> points at.
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

        // override defaultSRS with SRS information from boundedBy element (if present)
        // srsName = XMLTools.getNodeAsString( element, "gml:boundedBy/*[1]/@srsName", nsContext,
        // srsName );

        // parse properties
        Iterator<PropertyType> declIter = ft.getPropertyDeclarations().iterator();
        PropertyType activeDecl = declIter.next();
        int propOccurences = 0;

        CRS activeCRS = crs;
        List<Property<?>> propertyList = new ArrayList<Property<?>>();
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            QName propName = xmlStream.getName();
            LOG.debug( "- property '" + propName + "'" );
            if ( isElementSubstitutableForProperty( propName, activeDecl ) ) {
                // current property element is equal to active declaration
                if ( activeDecl.getMaxOccurs() != -1 && propOccurences > activeDecl.getMaxOccurs() ) {
                    String msg = Messages.getMessage( "ERROR_PROPERTY_TOO_MANY_OCCURENCES", propName,
                                                      activeDecl.getMaxOccurs(), ft.getName() );
                    throw new XMLParsingException( xmlStream, msg );
                }
            } else {
                // current property element is not equal to active declaration
                while ( declIter.hasNext() && !isElementSubstitutableForProperty( propName, activeDecl ) ) {
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
                if ( !isElementSubstitutableForProperty( propName, activeDecl ) ) {
                    String msg = Messages.getMessage( "ERROR_PROPERTY_UNEXPECTED", propName, ft.getName() );
                    throw new XMLParsingException( xmlStream, msg );
                }
            }

            Property<?> property = parseProperty( xmlStream, activeDecl, activeCRS, fid, propOccurences );
            if ( property != null ) {
                propertyList.add( property );
                // if the property is 'gml:boundedBy', its srsName value sets the default CRS for the following
                // properties of the feature
                if ( QName.valueOf( "{http://www.opengis.net/gml}boundedBy" ).equals( propName ) ) {
                    activeCRS = ( (Envelope) property.getValue() ).getCoordinateSystem();
                }
            }
            propOccurences++;
        }

        LOG.debug( " - parsing feature (end): " + xmlStream.getCurrentEventInfo() );

        feature = ft.newFeature( fid, propertyList );

        if ( fid != null && !"".equals( fid ) ) {
            if ( idContext.getFeature( fid ) != null ) {
                String msg = Messages.getMessage( "ERROR_FEATURE_ID_NOT_UNIQUE", fid );
                throw new XMLParsingException( xmlStream, msg );
            }
            idContext.addFeature( feature );
        }

        return feature;
    }

    private ApplicationSchema buildApplicationSchema( XMLStreamReaderWrapper xmlStream ) throws XMLParsingException {
        String schemaLocation = xmlStream.getAttributeValue( XSINS, "schemaLocation" );
        if ( schemaLocation == null ) {
            throw new XMLParsingException( xmlStream, Messages.getMessage( "ERROR_NO_SCHEMA_LOCATION", xmlStream.getSystemId() ) );
        }
        String [] tokens = schemaLocation.split( "\\s" );
        if (tokens.length % 2 != 0){
            throw new XMLParsingException( xmlStream, Messages.getMessage( "ERROR_SCHEMA_LOCATION_TOKENS_COUNT", xmlStream.getSystemId() ) );
        }
        // TODO handle multi-namespace schemas
        ApplicationSchema schema = null;
        try {
            URL source = new URL (new URL (xmlStream.getSystemId()),tokens[1] );
            ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( source.toString(), GMLVersion.GML_31);
            schema = decoder.extractFeatureTypeSchema();
        } catch ( Exception e ) {
            throw new XMLParsingException (xmlStream, "Error parsing application schema: " + e.getMessage());
        }
        return schema;
    }

    private boolean isElementSubstitutableForProperty( QName elemName, PropertyType pt ) {
        LOG.debug( "Checking if '" + elemName + "' is a valid substitution for '" + pt.getName() + "'" );

        QName ptName = pt.getName();
        if ( elemName.equals( ptName ) ) {
            LOG.debug( "Yep. Names match." );
            return true;
        }

        // TODO we don't want the xsModel here
        if ( xsModel != null ) {

            XSElementDeclaration elementDecl = xsModel.getElementDeclaration( elemName.getLocalPart(),
                                                                              elemName.getNamespaceURI() );

            XSElementDeclaration propElementDecl = xsModel.getElementDeclaration( ptName.getLocalPart(),
                                                                                  ptName.getNamespaceURI() );

            if ( elementDecl == null || propElementDecl == null ) {
                LOG.debug( "Not defined as a top level element." );
                return false;
            }

            XSObjectList list = xsModel.getSubstitutionGroup( propElementDecl );
            for ( int i = 0; i < list.getLength(); i++ ) {
                if ( list.item( i ).equals( elementDecl ) ) {
                    LOG.debug( "Yep. In substitution group." );
                    return true;
                }
            }
        }
        return false;
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
    public Property<?> parseProperty( XMLStreamReaderWrapper xmlStream, PropertyType propDecl, CRS crs, String fid,
                                      int occurence )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Property<?> property = null;
        QName propName = xmlStream.getName();
        LOG.debug( "- parsing property (begin): " + xmlStream.getCurrentEventInfo() );
        LOG.debug( "- property declaration: " + propDecl );

        CustomPropertyDecoder<?> parser = ptToParser.get( propDecl );

        if ( parser == null ) {
            if ( propDecl instanceof SimplePropertyType ) {
                property = new GenericProperty<String>( propDecl, propName, xmlStream.getElementText().trim() );
            } else if ( propDecl instanceof CustomComplexPropertyType ) {
                Object value = null;
                if ( propDecl instanceof EnvelopePropertyType ) {
                    xmlStream.nextTag();
                    value = geomParser.parseEnvelope( xmlStream, crs );
                    xmlStream.nextTag();
                } else if ( propDecl instanceof CodePropertyType ) {
                    String codeSpace = xmlStream.getAttributeValue( null, "codeSpace" );
                    String code = xmlStream.getElementText().trim();
                    value = new CodeType( code, codeSpace );
                } else if ( propDecl instanceof MeasurePropertyType ) {
                    String uom = xmlStream.getAttributeValue( null, "uom" );
                    double number = xmlStream.getElementTextAsDouble();
                    value = new Measure( number, uom );
                } else {
                    value = new GenericCustomPropertyParser().parse( xmlStream );
                }
                property = new GenericProperty<Object>( propDecl, propName, value );
            } else if ( propDecl instanceof GeometryPropertyType ) {
                xmlStream.nextTag();
                Geometry geometry = geomParser.parseAbstractGeometry( xmlStream, crs );
                property = new GenericProperty<Geometry>( propDecl, propName, geometry );
                xmlStream.nextTag();
            } else if ( propDecl instanceof FeaturePropertyType ) {
                String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
                if ( href != null ) {
                    FeatureReference refFeature = new FeatureReference( href,
                                                                        ( (FeaturePropertyType) propDecl ).getValueFt() );

                    // local feature reference?
                    if ( href.startsWith( "#" ) ) {
                        idContext.addFeatureReference( refFeature );
                    }

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
                        if ( !schema.isValidSubstitution( expectedFt, presentFt ) ) {
                            String msg = Messages.getMessage( "ERROR_PROPERTY_WRONG_FEATURE_TYPE",
                                                              expectedFt.getName(), propName, presentFt.getName() );
                            throw new XMLParsingException( xmlStream, msg );
                        }
                    }
                    Feature subFeature = parseFeature( xmlStream, crs );
                    property = new GenericProperty<Feature>( propDecl, propName, subFeature );
                    xmlStream.skipElement();
                }
            }
        } else {
            LOG.trace( "************ Parsing property using custom parser." );
            Object value = parser.parse( xmlStream );
            property = new GenericProperty<Object>( propDecl, propName, value );
        }

        LOG.debug( " - parsing property (end): " + xmlStream.getCurrentEventInfo() );
        return property;
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

        String fid = xmlReader.getAttributeValue( GMLNS, GMLID );
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
