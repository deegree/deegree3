package org.deegree.model.feature;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.model.feature.schema.FeaturePropertyDeclaration;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyDeclaration;
import org.deegree.model.feature.schema.PropertyDeclaration;
import org.deegree.model.feature.schema.SimplePropertyDeclaration;
import org.deegree.model.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class FeatureGMLAdapter extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureGMLAdapter.class );

    private static String FID = "fid";

    private static String GMLID = "id";

    private static String GMLNS = CommonNamespaces.GMLNS;

    // key: feature type name, value: feature type
    private Map<QName, FeatureType> ftNameToFt = new HashMap<QName, FeatureType>();

    // key: feature id, value: Feature
    protected Map<String, Feature> featureMap = new HashMap<String, Feature>();

    // value: XLinkedFeatureProperty
    // protected Collection<XLinkedFeatureProperty> xlinkProperties = new ArrayList<XLinkedFeatureProperty>();

    /**
     * Creates a new <code>FeatureGMLAdapter</code> instance instance that is configured for building features with the
     * specified feature types.
     * 
     * @param fts
     *            feature types
     */
    public FeatureGMLAdapter( List<FeatureType> fts ) {
        for ( FeatureType ft : fts ) {
            ftNameToFt.put( ft.getName(), ft );
        }
    }

    /**
     * Returns the object representation for the feature element event that the cursor of the given
     * <code>XMLStreamReader</code> points at.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event of the feature element, afterwards points at
     *            the next event after the <code>END_ELEMENT</code> event of the feature element
     * @param srsName
     *            default SRS for all descendant geometry properties
     * @return object representation for the given feature element
     * @throws XMLStreamException
     */
    protected Feature parseFeature( XMLStreamReader xmlStream, String srsName )
                            throws XMLStreamException {

        Feature feature = null;
        String fid = parseFeatureId( xmlStream );

        QName featureName = xmlStream.getName();
        FeatureType ft = lookupFeatureType( xmlStream, featureName );

        LOG.debug( " - parsing feature, gml:id=" + fid + " (begin): " + getCurrentEventInfo( xmlStream ) );

        // override defaultSRS with SRS information from boundedBy element (if present)
        // srsName = XMLTools.getNodeAsString( element, "gml:boundedBy/*[1]/@srsName", nsContext, srsName );

        // parse properties
        List<Property<?>> propertyList = new ArrayList<Property<?>>();
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            QName propName = xmlStream.getName();
            LOG.debug( "- property '" + propName + "'" );
            try {
                Property<?> property = parseProperty( xmlStream, ft, srsName );
                if ( property != null ) {
                    propertyList.add( property );
                }
            } catch ( XMLParsingException e ) {
                LOG.debug( "Error parsing property '" + propName + "' of feature '" + featureName + "' with fid: "
                           + fid + ". " + e.getMessage() );
                throw e;
            }
        }

        LOG.debug( " - parsing feature (end): " + getCurrentEventInfo( xmlStream ) );

        feature = ft.newFeature( fid, propertyList );

        if ( fid != null && !"".equals( fid ) ) {
            if ( featureMap.containsKey( fid ) ) {
                String msg = Messages.getMessage( "ERROR_FEATURE_ID_NOT_UNIQUE", fid );
                throw new XMLParsingException( this, xmlStream, msg );
            }
            featureMap.put( fid, feature );
        }

        return feature;
    }

    /**
     * Returns the object representation for the given property element.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event of the property, afterwards points at the
     *            next event after the <code>END_ELEMENT</code> of the property
     * @param ft
     *            feature type of the feature that the property belongs to
     * @param srsName
     *            default SRS for all a descendant geometry properties
     * @return object representation for the given property element.
     * @throws XMLParsingException
     * @throws XMLStreamException
     */
    public Property parseProperty( XMLStreamReader xmlStream, FeatureType ft, String srsName )
                            throws XMLParsingException, XMLStreamException {

        Property property = null;
        QName propertyName = xmlStream.getName();
        LOG.debug( " - parsing property (begin): " + getCurrentEventInfo( xmlStream ) );

//        PropertyDeclaration pt = ft.getPropertyDeclaration( propertyName );
        PropertyDeclaration pt = null;
        if ( pt == null ) {
            String msg = Messages.getMessage( "ERROR_NO_PROPERTY_TYPE", ft.getName(), propertyName );
            throw new XMLParsingException( this, xmlStream, msg );
        }
        // LOG.debug( " - property type: " + pt.getClass().getName() + ", type code: " + pt.getType() );

        if ( pt instanceof SimplePropertyDeclaration ) {
            property = new GenericProperty<String>( pt, xmlStream.getElementText().trim() );
        } else if ( pt instanceof GeometryPropertyDeclaration ) {
            xmlStream.nextTag();
            // TODO geometry parsing
            // Geometry geometry = StAXGeometryParser.parseGeometry( xmlStream, srsName );
            // property = new GenericProperty (pt, geometry);
            skipElement( xmlStream );
            xmlStream.nextTag();
        } else if ( pt instanceof FeaturePropertyDeclaration ) {
            String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
            if ( href != null ) {
                // remote feature (xlinked content)
//                if ( !href.startsWith( "#" ) ) {
//                    String msg = Messages.format( "ERROR_EXTERNAL_XLINK_NOT_SUPPORTED", href );
//                    throw new XMLParsingException( msg );
//                } else {
//                    String fid = href.substring( 1 );
//                    property = new XLinkedFeatureProperty( propertyName, fid );
//                    xlinkProperties.add( (XLinkedFeatureProperty) property );
//                }
                xmlStream.nextTag();
            } else {
                // inline feature
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = Messages.getMessage( "ERROR_INVALID_FEATURE_PROPERTY", propertyName );
                    throw new XMLParsingException( this, xmlStream, msg );
                }
                Feature subFeature = parseFeature( xmlStream, srsName );
                property = new GenericProperty<Feature>( pt, subFeature );
                skipElement( xmlStream );
            }
        }
        LOG.debug( " - parsing property (end): " + getCurrentEventInfo( xmlStream ) );
        return property;
    }

    /**
     * Returns the feature type with the given name.
     * <p>
     * If no feature type with the given name is defined, an XMLParsingException is thrown.
     * 
     * @param ftName
     *            feature type name to look up
     * @return the feature type with the given name
     * @throws XMLParsingException
     *             if no feature type with the given name is defined
     */
    protected FeatureType lookupFeatureType( XMLStreamReader xmlStreamReader, QName ftName )
                            throws XMLParsingException {
        FeatureType ft = null;
        ft = ftNameToFt.get( ftName );
        if ( ft == null ) {
            String msg = Messages.getMessage( "ERROR_SCHEMA_FEATURE_TYPE_UNKNOWN", ftName );
            throw new XMLParsingException( this, xmlStreamReader, msg );
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
    protected String parseFeatureId( XMLStreamReader xmlReader ) {

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

    public void export( XMLStreamWriter writer, Feature feature )
                            throws XMLStreamException {
        QName featureName = feature.getName();
        writer.writeStartElement( featureName.getNamespaceURI(), featureName.getLocalPart() );
        for ( Property<?> prop : feature.getProperties() ) {
            export( writer, prop );
        }
        writer.writeEndElement();
    }

    public void export( XMLStreamWriter writer, Property<?> property )
                            throws XMLStreamException {
        QName propName = property.getName();
        writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );

        // TODO really respect property type here
        Object value = property.getValue();
        if ( value instanceof Feature ) {
            export( writer, (Feature) value );
        } else {
            writer.writeCharacters( value.toString() );
        }

        writer.writeEndElement();
    }
}
