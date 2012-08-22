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
package org.deegree.model.feature;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.FeaturePropertyType;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.MultiGeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.GMLDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Parser and wrapper class for GML feature documents.
 * <p>
 * <b>Validation</b><br>
 * Has validation capabilities: if the schema is provided or the document contains a reference to a schema the structure
 * of the generated features is checked. If no schema information is available, feature + property types are
 * heuristically determined from the feature instance in the document (guessing of simple property types can be turned
 * off, because it may cause unwanted effects).
 * </p>
 * <p>
 * <b>XLinks</b><br>
 * Has some basic understanding of XLink: Supports internal XLinks (i.e. the content for a feature is given by a
 * reference to a feature element in the same document). No support for external XLinks yet.
 * </p>
 * <p>
 * <b>Propagation of srsName attribute</b><br>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GMLFeatureDocument extends GMLDocument {

    private static final long serialVersionUID = -7626943858143104276L;

    private final static ILogger LOG = LoggerFactory.getLogger( GMLFeatureDocument.class );

    private static String FID = "fid";

    private static String GMLID = "id";

    private static URI GMLNS = CommonNamespaces.GMLNS;

    private static String GMLID_NS = CommonNamespaces.GMLNS.toString();

    private static QualifiedName PROP_NAME_BOUNDED_BY = new QualifiedName( "boundedBy", GMLNS );

    private static QualifiedName PROP_NAME_DESCRIPTION = new QualifiedName( "description", GMLNS );

    private static QualifiedName PROP_NAME_NAME = new QualifiedName( "name", GMLNS );

    private static QualifiedName PROP_NAME_WKB_GEOM = new QualifiedName( "wkbGeom", GMLNS );

    private static QualifiedName TYPE_NAME_BOX = new QualifiedName( "Box", GMLNS );

    private static QualifiedName TYPE_NAME_LINESTRING = new QualifiedName( "LineString", GMLNS );

    private static QualifiedName TYPE_NAME_MULTIGEOMETRY = new QualifiedName( "MultiGeometry", GMLNS );

    private static QualifiedName TYPE_NAME_MULTILINESTRING = new QualifiedName( "MultiLineString", GMLNS );

    private static QualifiedName TYPE_NAME_MULTIPOINT = new QualifiedName( "MultiPoint", GMLNS );

    private static QualifiedName TYPE_NAME_MULTIPOLYGON = new QualifiedName( "MultiPolygon", GMLNS );

    private static QualifiedName TYPE_NAME_POINT = new QualifiedName( "Point", GMLNS );

    private static QualifiedName TYPE_NAME_POLYGON = new QualifiedName( "Polygon", GMLNS );

    private static QualifiedName TYPE_NAME_SURFACE = new QualifiedName( "Surface", GMLNS );

    private static QualifiedName TYPE_NAME_CURVE = new QualifiedName( "Curve", GMLNS );

    private static QualifiedName TYPE_NAME_MULTISURFACE = new QualifiedName( "MultiSurface", GMLNS );

    private static QualifiedName TYPE_NAME_MULTICURVE = new QualifiedName( "MultiCurve", GMLNS );

    // key: namespace URI, value: GMLSchema
    protected Map<URI, GMLSchema> gmlSchemaMap;

    // key: feature id, value: Feature
    protected Map<String, Feature> featureMap = new HashMap<String, Feature>();

    // value: XLinkedFeatureProperty
    protected Collection<XLinkedFeatureProperty> xlinkPropertyList = new ArrayList<XLinkedFeatureProperty>();

    private boolean guessSimpleTypes = false;

    /**
     * Creates a new instance of <code>GMLFeatureDocument</code>.
     * <p>
     * Simple types encountered during parsing are "guessed", i.e. the parser tries to convert the values to double,
     * integer, calendar, etc. However, this may lead to unwanted results, e.g. a property value of "054604" is
     * converted to "54604".
     */
    public GMLFeatureDocument() {
        super();
    }

    /**
     * Creates a new instance of <code>GMLFeatureDocument</code>.
     * <p>
     *
     * @param guessSimpleTypes
     *            set to true, if simple types should be "guessed" during parsing
     */
    public GMLFeatureDocument( boolean guessSimpleTypes ) {
        super();
        this.guessSimpleTypes = guessSimpleTypes;
    }

    /**
     * Explicitly sets the GML schema information that the document must comply to.
     * <p>
     * This overrides any schema information that the document refers to.
     *
     * @param gmlSchemaMap
     *            key: namespace URI, value: GMLSchema
     */
    public void setSchemas( Map<URI, GMLSchema> gmlSchemaMap ) {
        this.gmlSchemaMap = gmlSchemaMap;
    }

    /**
     * Returns the object representation for the root feature element.
     *
     * @return object representation for the root feature element.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Feature parseFeature()
                            throws XMLParsingException, UnknownCRSException {
        return this.parseFeature( (String) null );
    }

    /**
     * Returns the object representation for the root feature element.
     *
     * @param defaultSRS
     *            default SRS for all a descendant geometry properties
     * @return object representation for the root feature element.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Feature parseFeature( String defaultSRS )
                            throws XMLParsingException, UnknownCRSException {
        Feature feature = this.parseFeature( this.getRootElement(), defaultSRS );
        resolveXLinkReferences();
        return feature;
    }

    /**
     * Returns the object representation for the given feature element.
     *
     * @param element
     *            feature element
     * @return object representation for the given feature element.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected Feature parseFeature( Element element )
                            throws XMLParsingException, UnknownCRSException {
        return parseFeature( element, null );
    }

    /**
     * Returns the object representation for the given feature element.
     *
     * @param element
     *            feature element
     * @param srsName
     *            default SRS for all descendant geometry properties
     * @return object representation for the given feature element
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected Feature parseFeature( Element element, String srsName )
                            throws XMLParsingException, UnknownCRSException {

        Feature feature = null;
        String fid = parseFeatureId( element );
        FeatureType ft = getFeatureType( element );

        // override defaultSRS with SRS information from boundedBy element (if present)
        srsName = XMLTools.getNodeAsString( element, "gml:boundedBy/*[1]/@srsName", nsContext, srsName );

        ElementList childList = XMLTools.getChildElements( element );
        Collection<FeatureProperty> propertyList = new ArrayList<FeatureProperty>( childList.getLength() );
        for ( int i = 0; i < childList.getLength(); i++ ) {
            Element propertyElement = childList.item( i );
            QualifiedName propertyName = getQualifiedName( propertyElement );

            if ( PROP_NAME_BOUNDED_BY.equals( propertyName ) || PROP_NAME_WKB_GEOM.equals( propertyName ) ) {
                // TODO
            } else if ( PROP_NAME_NAME.equals( propertyName ) || PROP_NAME_DESCRIPTION.equals( propertyName ) ) {
                String s = XMLTools.getStringValue( propertyElement );
                if ( s != null ) {
                    s = s.trim();
                }
                FeatureProperty property = createSimpleProperty( s, propertyName, Types.VARCHAR );
                if ( property != null ) {
                    propertyList.add( property );
                }
            } else {
                try {
                    FeatureProperty property = parseProperty( childList.item( i ), ft, srsName );
                    if ( property != null ) {
                        propertyList.add( property );
                    }
                } catch ( XMLParsingException xmle ) {
                    LOG.logInfo( "An error occurred while trying to parse feature with fid: " + fid );
                    throw xmle;
                }
            }
        }

        FeatureProperty[] featureProperties = propertyList.toArray( new FeatureProperty[propertyList.size()] );
        feature = FeatureFactory.createFeature( fid, ft, featureProperties );

        if ( !"".equals( fid ) ) {
            if ( this.featureMap.containsKey( fid ) ) {
                String msg = Messages.format( "ERROR_FEATURE_ID_NOT_UNIQUE", fid );
                throw new XMLParsingException( msg );
            }
            this.featureMap.put( fid, feature );
        }

        return feature;
    }

    /**
     * Returns the object representation for the given property element.
     *
     * @param propertyElement
     *            property element
     * @param ft
     *            feature type of the feature that the property belongs to
     * @return object representation for the given property element
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public FeatureProperty parseProperty( Element propertyElement, FeatureType ft )
                            throws XMLParsingException, UnknownCRSException {
        return parseProperty( propertyElement, ft, null );
    }

    /**
     * Returns the object representation for the given property element.
     *
     * @param propertyElement
     *            property element
     * @param ft
     *            feature type of the feature that the property belongs to
     * @param srsName
     *            default SRS for all a descendant geometry properties
     * @return object representation for the given property element.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public FeatureProperty parseProperty( Element propertyElement, FeatureType ft, String srsName )
                            throws XMLParsingException, UnknownCRSException {

        FeatureProperty property = null;
        QualifiedName propertyName = getQualifiedName( propertyElement );

        PropertyType propertyType = ft.getProperty( propertyName );
        if ( propertyType == null ) {
            throw new XMLParsingException( Messages.format( "ERROR_NO_PROPERTY_TYPE", ft.getName(), propertyName ) );
        }

        if ( propertyType instanceof SimplePropertyType ) {
            int typeCode = propertyType.getType();
            String s = null;
            if ( typeCode == Types.ANYTYPE ) {
                Element child = XMLTools.getRequiredElement( propertyElement, "*", nsContext );
                s = DOMPrinter.nodeToString( child, CharsetUtils.getSystemCharset() );
            } else {
                s = XMLTools.getStringValue( propertyElement ).trim();
            }

            String nil = propertyElement.getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "nil" );

            if ( nil == null || !nil.equals( "true" ) ) {
                property = createSimpleProperty( s, propertyName, typeCode );
            }

        } else if ( propertyType instanceof GeometryPropertyType ) {
            Element contentElement = XMLTools.getFirstChildElement( propertyElement );
            if ( contentElement == null ) {
                String msg = Messages.format( "ERROR_PROPERTY_NO_CHILD", propertyName, "geometry" );
                throw new XMLParsingException( msg );
            }

            String nil = propertyElement.getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "nil" );

            if ( nil == null || !nil.equals( "true" ) ) {
                property = createGeometryProperty( contentElement, propertyName, srsName );
            }
        } else if ( propertyType instanceof MultiGeometryPropertyType ) {
            throw new XMLParsingException( "Handling of MultiGeometryPropertyType not "
                                           + "implemented in GMLFeatureDocument yet." );
        } else if ( propertyType instanceof FeaturePropertyType ) {
            String nil = propertyElement.getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "nil" );

            if ( nil != null && nil.equals( "true" ) ) {
                return null;
            }

            List<Node> childElements = XMLTools.getNodes( propertyElement, "*", nsContext );
            switch ( childElements.size() ) {
            case 0: {
                // feature content must be xlinked
                Text xlinkHref = (Text) XMLTools.getNode( propertyElement, "@xlink:href/text()", nsContext );
                if ( xlinkHref == null ) {
                    String msg = Messages.format( "ERROR_INVALID_FEATURE_PROPERTY", propertyName );
                    throw new XMLParsingException( msg );
                }
                String href = xlinkHref.getData();
                if ( !href.startsWith( "#" ) ) {
                    try {
                        property = FeatureFactory.createFeatureProperty( propertyName, new URL( href ) );
                        break;
                    } catch ( MalformedURLException e ) {
                        throw new XMLParsingException( Messages.format( "ERROR_XLINK_NOT_VALID", href ) );
                    }
                }
                String fid = href.substring( 1 );
                property = new XLinkedFeatureProperty( propertyName, fid );
                xlinkPropertyList.add( (XLinkedFeatureProperty) property );
                break;
            }
            case 1: {
                // feature content is given inline
                Feature propertyValue = parseFeature( (Element) childElements.get( 0 ), srsName );
                property = FeatureFactory.createFeatureProperty( propertyName, propertyValue );
                break;
            }
            default: {
                // String msg =
                Messages.format( "ERROR_INVALID_FEATURE_PROPERTY2", propertyName, childElements.size() );
                // throw new XMLParsingException( msg );
            }
            }
        }
        return property;
    }

    protected void resolveXLinkReferences()
                            throws XMLParsingException {
        Iterator<XLinkedFeatureProperty> iter = this.xlinkPropertyList.iterator();
        while ( iter.hasNext() ) {
            XLinkedFeatureProperty xlinkProperty = iter.next();
            String fid = xlinkProperty.getTargetFeatureId();
            Feature targetFeature = this.featureMap.get( fid );
            if ( targetFeature == null ) {
                String msg = Messages.format( "ERROR_XLINK_NOT_RESOLVABLE", fid );
                throw new XMLParsingException( msg );
            }
            xlinkProperty.setValue( targetFeature );
        }
    }

    /**
     * Creates a simple property from the given parameters.
     * <p>
     * Converts the string value to the given target type.
     *
     * @param s
     *            string value from a simple property to be converted
     * @param propertyName
     *            name of the simple property
     * @param typeCode
     *            target type code
     * @return property value in the given target type.
     * @throws XMLParsingException
     */
    private FeatureProperty createSimpleProperty( String s, QualifiedName propertyName, int typeCode )
                            throws XMLParsingException {

        Object propertyValue = null;
        switch ( typeCode ) {
        case Types.VARCHAR:
        case Types.ANYTYPE: {
            propertyValue = s;
            break;
        }
        case Types.TINYINT:
        case Types.INTEGER:
        case Types.SMALLINT: {
            try {
                propertyValue = new Long( s );
            } catch ( NumberFormatException e ) {
                String msg = Messages.format( "ERROR_CONVERTING_PROPERTY", s, propertyName, "Integer" );
                throw new XMLParsingException( msg );
            }
            break;
        }
        case Types.NUMERIC:
        case Types.DOUBLE: {
            try {
                propertyValue = new Double( s );
            } catch ( NumberFormatException e ) {
                String msg = Messages.format( "ERROR_CONVERTING_PROPERTY", s, propertyName, "Double" );
                throw new XMLParsingException( msg );
            }
            break;
        }
        case Types.REAL:
        case Types.DECIMAL:
        case Types.FLOAT: {
            try {
                propertyValue = new Float( s );
            } catch ( NumberFormatException e ) {
                String msg = Messages.format( "ERROR_CONVERTING_PROPERTY", s, propertyName, "Float" );
                throw new XMLParsingException( msg );
            }
            break;
        }
        case Types.BOOLEAN: {
            propertyValue = new Boolean( s );
            break;
        }
        case Types.DATE:
        case Types.TIMESTAMP: {
            propertyValue = TimeTools.createCalendar( s ).getTime();
            break;
        }
        default: {
            String typeString = "" + typeCode;
            try {
                typeString = Types.getTypeNameForSQLTypeCode( typeCode );
            } catch ( UnknownTypeException e ) {
                LOG.logError( "No type name for code: " + typeCode );
            }
            String msg = Messages.format( "ERROR_UNHANDLED_TYPE", "" + typeString );
            LOG.logError( msg );
            throw new XMLParsingException( msg );
        }
        }
        FeatureProperty property = FeatureFactory.createFeatureProperty( propertyName, propertyValue );
        return property;
    }

    /**
     * Creates a geometry property from the given parameters.
     *
     * @param contentElement
     *            child element of a geometry property to be converted
     * @param propertyName
     *            name of the geometry property
     * @param srsName
     *            default SRS for the geometry (may be overwritten in geometry elements)
     * @return geometry property
     * @throws XMLParsingException
     */
    private FeatureProperty createGeometryProperty( Element contentElement, QualifiedName propertyName, String srsName )
                            throws XMLParsingException {

        Geometry propertyValue = null;
        try {
            propertyValue = GMLGeometryAdapter.wrap( contentElement, srsName );
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.format( "ERROR_CONVERTING_GEOMETRY_PROPERTY", propertyName, "-", e.getMessage() );
            throw new XMLParsingException( msg );
        }

        FeatureProperty property = FeatureFactory.createFeatureProperty( propertyName, propertyValue );
        return property;
    }

    /**
     * Determines and retrieves the GML schemas that the document refers to.
     *
     * @return the GML schemas that are attached to the document, keys are URIs (namespaces), values are GMLSchemas.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected Map<URI, GMLSchema> getGMLSchemas()
                            throws XMLParsingException, UnknownCRSException {

        if ( this.gmlSchemaMap == null ) {
            gmlSchemaMap = new HashMap<URI, GMLSchema>();
            Map<URI, URL> schemaMap = getAttachedSchemas();
            Iterator<URI> it = schemaMap.keySet().iterator();
            while ( it.hasNext() ) {
                URI nsURI = it.next();
                URL schemaURL = schemaMap.get( nsURI );
                GMLSchemaDocument schemaDocument = new GMLSchemaDocument();
                LOG.logDebug( "Retrieving schema document for namespace '" + nsURI + "' from URL '" + schemaURL + "'." );
                try {
                    schemaDocument.load( schemaURL );
                    GMLSchema gmlSchema = schemaDocument.parseGMLSchema();
                    gmlSchemaMap.put( nsURI, gmlSchema );
                } catch ( IOException e ) {
                    String msg = Messages.format( "ERROR_RETRIEVING_SCHEMA", schemaURL, e.getMessage() );
                    throw new XMLParsingException( msg );
                } catch ( SAXException e ) {
                    String msg = Messages.format( "ERROR_SCHEMA_NOT_XML", schemaURL, e.getMessage() );
                    throw new XMLParsingException( msg );
                } catch ( XMLParsingException e ) {
                    String msg = Messages.format( "ERROR_SCHEMA_PARSING1", schemaURL, e.getMessage() );
                    throw new XMLParsingException( msg );
                }
            }
        }

        return this.gmlSchemaMap;
    }

    /**
     * Returns the GML schema for the given namespace.
     *
     * @param ns
     * @return the GML schema for the given namespace if it is declared, null otherwise.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected GMLSchema getSchemaForNamespace( URI ns )
                            throws XMLParsingException, UnknownCRSException {
        Map<URI, GMLSchema> gmlSchemaMap = getGMLSchemas();
        GMLSchema schema = gmlSchemaMap.get( ns );
        return schema;
    }

    /**
     * Returns the feature type with the given name.
     * <p>
     * If schema information is available and a feature type with the given name is not defined, an XMLParsingException
     * is thrown.
     *
     * @param ftName
     *            feature type to look up
     * @return the feature type with the given name if it is declared, null otherwise.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected FeatureType getFeatureType( QualifiedName ftName )
                            throws XMLParsingException, UnknownCRSException {
        FeatureType featureType = null;
        if ( this.gmlSchemaMap != null ) {
            GMLSchema schema = getSchemaForNamespace( ftName.getNamespace() );
            if ( schema == null ) {
                String msg = Messages.format( "ERROR_SCHEMA_NO_SCHEMA_FOR_NS", ftName.getNamespace() );
                //throw new XMLParsingException( msg );
                LOG.logWarning( msg );
                return null;
            }
            featureType = schema.getFeatureType( ftName );
            if ( featureType == null ) {
                String msg = Messages.format( "ERROR_SCHEMA_FEATURE_TYPE_UNKNOWN", ftName );
                //throw new XMLParsingException( msg );
                LOG.logWarning( msg );
                return null;
            }
        }
        return featureType;
    }

    /**
     * Parses the feature id attribute from the given feature element.
     * <p>
     * Looks after 'gml:id' (GML 3 style) first, if no such attribute is present, the 'fid' (GML 2 style) attribute is
     * used.
     *
     * @param featureElement
     * @return the feature id, this is "" if neither a 'gml:id' nor a 'fid' attribute is present
     */
    protected String parseFeatureId( Element featureElement ) {
        String fid = featureElement.getAttributeNS( GMLID_NS, GMLID );
        if ( fid.length() == 0 ) {
            fid = featureElement.getAttribute( FID );
        }

        // Check that the feature id has the correct form. "fid" and "gml:id" are both based
        // on the XML type "ID": http://www.w3.org/TR/xmlschema11-2/#NCName
        // Thus, they must match the NCName production rule. Basically, they may not contain
        // a separating colon (only at the first position a colon is allowed) and must not
        // start with a digit.
        if ( fid != null && fid.length() > 0 && !fid.matches( "[^\\d][^:]+" ) ) {
            String msg = Messages.format( "ERROR_INVALID_FEATUREID", fid );
            throw new InvalidParameterValueException( msg, "gml:id", fid );
        }

        return fid;
    }

    /**
     * Returns the feature type for the given feature element.
     * <p>
     * If a schema defines a feature type with the element's name, it is returned. Otherwise, a feature type is
     * generated that matches the child elements (properties) of the feature.
     *
     * @param element
     *            feature element
     * @return the feature type.
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private FeatureType getFeatureType( Element element )
                            throws XMLParsingException, UnknownCRSException {
        QualifiedName ftName = getQualifiedName( element );
        FeatureType featureType = getFeatureType( ftName );
        if ( featureType == null ) {
            LOG.logDebug( "Feature type '" + ftName
                          + "' is not defined in schema. Generating feature type dynamically." );
            featureType = generateFeatureType( element );
        }
        return featureType;
    }

    /**
     * Method to create a <code>FeatureType</code> from the child elements (properties) of the given feature element.
     * Used if no schema (=FeatureType definition) is available.
     *
     * @param element
     *            feature element
     * @return the generated feature type.
     * @throws XMLParsingException
     */
    private FeatureType generateFeatureType( Element element )
                            throws XMLParsingException {
        ElementList el = XMLTools.getChildElements( element );
        ArrayList<PropertyType> propertyList = new ArrayList<PropertyType>( el.getLength() );

        for ( int i = 0; i < el.getLength(); i++ ) {
            Element propertyElement = el.item( i );
            QualifiedName propertyName = getQualifiedName( propertyElement );

            if ( !propertyName.equals( PROP_NAME_BOUNDED_BY ) && !propertyName.equals( PROP_NAME_NAME )
                 && !propertyName.equals( PROP_NAME_DESCRIPTION ) ) {
                PropertyType propertyType = determinePropertyType( propertyElement, propertyName );
                if ( !propertyList.contains( propertyType ) ) {
                    propertyList.add( propertyType );
                }
            }
        }

        PropertyType[] properties = new PropertyType[propertyList.size()];
        properties = propertyList.toArray( properties );
        QualifiedName ftName = getQualifiedName( element );
        FeatureType featureType = FeatureFactory.createFeatureType( ftName, false, properties );

        return featureType;
    }

    /**
     * Determines the property type for the given property element heuristically.
     *
     * @param propertyElement
     *            property element
     * @param propertyName
     *            qualified name of the property element
     * @return the property type.
     * @throws XMLParsingException
     */
    private PropertyType determinePropertyType( Element propertyElement, QualifiedName propertyName )
                            throws XMLParsingException {

        PropertyType pt = null;
        ElementList childList = XMLTools.getChildElements( propertyElement );

        // xlink attr present -> feature property
        Attr xlink = (Attr) XMLTools.getNode( propertyElement, "@xlink:href", nsContext );

        // hack for determining properties of type "xsd:anyType"
        String skipParsing = XMLTools.getNodeAsString( propertyElement, "@deegreewfs:skipParsing", nsContext, "false" );
        if ( "true".equals( skipParsing ) ) {
            pt = FeatureFactory.createSimplePropertyType( propertyName, Types.ANYTYPE, 0, -1 );
            return pt;
        }

        if ( childList.getLength() == 0 && xlink == null ) {
            // no child elements -> simple property
            String value = XMLTools.getStringValue( propertyElement );
            if ( value != null ) {
                value = value.trim();
            }
            pt = guessSimplePropertyType( value, propertyName );
        } else {
            // geometry or feature property
            if ( xlink != null ) {
                // TODO could be xlinked geometry as well
                pt = FeatureFactory.createFeaturePropertyType( propertyName, 0, -1 );
            } else {
                QualifiedName elementName = getQualifiedName( childList.item( 0 ) );
                if ( isGeometry( elementName ) ) {
                    pt = FeatureFactory.createGeometryPropertyType( propertyName, elementName, 0, -1 );
                } else {
                    // feature property
                    pt = FeatureFactory.createFeaturePropertyType( propertyName, 0, -1 );
                }
            }
        }
        return pt;
    }

    /**
     * Heuristically determines the simple property type from the given property value.
     * <p>
     * NOTE: This method may produce unwanted results, for example if an "xsd:string" property contains a value that can
     * be parsed as an integer, it is always determined as a numeric property.
     *
     * @param value
     *            string value to be used to determine property type
     * @param propertyName
     *            name of the property
     * @return the simple property type.
     */
    private SimplePropertyType guessSimplePropertyType( String value, QualifiedName propertyName ) {

        int typeCode = Types.VARCHAR;

        if ( this.guessSimpleTypes ) {
            // parseable as integer?
            try {
                Integer.parseInt( value );
                typeCode = Types.INTEGER;
            } catch ( NumberFormatException e ) {
                // so it's not an integer
            }

            // parseable as double?
            if ( typeCode == Types.VARCHAR ) {
                try {
                    Double.parseDouble( value );
                    typeCode = Types.NUMERIC;
                } catch ( NumberFormatException e ) {
                    // so it's not a double
                }
            }

            // parseable as ISO date?
            /*
             * if (typeCode == Types.VARCHAR) { try { TimeTools.createCalendar( value ); typeCode = Types.DATE; } catch
             * (Exception e) {} }
             */
        }

        SimplePropertyType propertyType = FeatureFactory.createSimplePropertyType( propertyName, typeCode, 0, -1 );
        return propertyType;
    }

    /**
     * Returns true if the given element name is a known GML geometry.
     *
     * @param elementName
     * @return true if the given element name is a known GML geometry, false otherwise.
     */
    private boolean isGeometry( QualifiedName elementName ) {
        boolean isGeometry = false;
        if ( TYPE_NAME_BOX.equals( elementName ) || TYPE_NAME_LINESTRING.equals( elementName )
             || TYPE_NAME_MULTIGEOMETRY.equals( elementName ) || TYPE_NAME_MULTILINESTRING.equals( elementName )
             || TYPE_NAME_MULTIPOINT.equals( elementName ) || TYPE_NAME_MULTIPOLYGON.equals( elementName )
             || TYPE_NAME_POINT.equals( elementName ) || TYPE_NAME_POLYGON.equals( elementName )
             || TYPE_NAME_SURFACE.equals( elementName ) || TYPE_NAME_MULTISURFACE.equals( elementName )
             || TYPE_NAME_CURVE.equals( elementName ) || TYPE_NAME_MULTICURVE.equals( elementName ) ) {
            isGeometry = true;
        }
        return isGeometry;
    }
}
