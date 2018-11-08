package org.deegree.services.wfs.format.geojson;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.reference.FeatureReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

/**
 * Stream-based writer for GeoJSON documents.
 * <p>
 * Instances of this class are not thread-safe.
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonWriter extends JsonWriter implements GeoJsonFeatureWriter {

    private static final Logger LOG = LoggerFactory.getLogger( GeoJsonWriter.class );

    private final GeoJsonGeometryWriter geoJsonGeometryWriter;

    private boolean isStarted = false;

    /**
     * Instantiates a new {@link GeoJsonWriter}.
     *
     * @param writer
     *            the writer to write the GeoJSON into, never <code>null</code>
     * @throws UnknownCRSException
     *             if "crs:84" is not known as CRS (should never happen)
     */
    public GeoJsonWriter( Writer writer ) throws UnknownCRSException {
        super( writer );
        setIndent( "  " );
        setHtmlSafe( true );
        this.geoJsonGeometryWriter = new GeoJsonGeometryWriter( this );
    }

    @Override
    public void close()
                            throws IOException {
        this.isStarted = false;
        super.close();
    }

    @Override
    public void startFeatureCollection()
                            throws IOException {
        beginObject();
        name( "type" ).value( "FeatureCollection" );
    }

    @Override
    public void endFeatureCollection()
                            throws IOException {
        if ( isStarted )
            endArray();
        endObject();
    }

    @Override
    public void write( Feature feature )
                            throws IOException, TransformationException, UnknownCRSException {
        QName featureName = feature.getName();
        LOG.debug( "Exporting Feature {} with ID {}", featureName, feature.getId() );

        if ( !isStarted ) {
            name( "features" ).beginArray();
            isStarted = true;
        }
        beginObject();
        name( "type" ).value( "Feature" );
        writeGeometry( feature );
        WriteProperties( feature );
        endObject();
    }

    private void writeGeometry( Feature feature )
                            throws IOException, UnknownCRSException, TransformationException {
        List<Property> geometryProperties = feature.getGeometryProperties();
        if ( geometryProperties.isEmpty() ) {
            name( "geometry" ).nullValue();
        } else if ( geometryProperties.size() == 1 ) {
            name( "geometry" );
            Property property = geometryProperties.get( 0 );

            Geometry value = (Geometry) property.getValue();
            geoJsonGeometryWriter.writeGeometry( value );
        } else {
            throw new IOException( "Could not write Feature as GeoJSON. The feature contains more than one geometry." );
        }
    }

    private void WriteProperties( Feature feature )
                            throws IOException, TransformationException, UnknownCRSException {
        List<Property> properties = feature.getProperties();
        name( "properties" );
        if ( properties.isEmpty() ) {
            nullValue();
        } else {
            exportProperties( properties );
        }
    }

    private void exportProperties( List<Property> properties )
                            throws IOException, UnknownCRSException, TransformationException {
        beginObject();
        for ( Property property : properties ) {
            export( property );
        }
        endObject();
    }

    private void export( Property property )
                            throws IOException, TransformationException, UnknownCRSException {
        PropertyType propertyType = property.getType();
        QName propertyName = property.getName();
        if ( property instanceof SimpleProperty ) {
            export( propertyName, ( (SimpleProperty) property ).getValue() );
        } else if ( propertyType instanceof CustomPropertyType ) {
            exportAttributesAndChildren( property );
        } else if ( propertyType instanceof FeaturePropertyType ) {
            exportFeaturePropertyType( property );
        } else if ( propertyType instanceof GeometryPropertyType ) {
            // Do nothing as geometry was exported before.
        } else if ( property instanceof GenericProperty ) {
            exportGenericProperty( (GenericProperty) property );
        } else {
            throw new IOException( "Unhandled property type '" + property.getClass() + "' (property name "
                                   + propertyName + " )" );
        }
    }

    private void exportGenericProperty( GenericProperty property )
                            throws IOException, TransformationException, UnknownCRSException {
        TypedObjectNode value = property.getValue();
        if ( value instanceof PrimitiveValue ) {
            export( property.getName(), (PrimitiveValue) value );
        } else {
            export( value );
        }
    }

    private void exportFeaturePropertyType( Property property )
                            throws IOException, UnknownCRSException, TransformationException {
        QName propertyName = property.getName();
        LOG.debug( "Exporting feature property '" + propertyName + "'" );
        if ( property instanceof Feature ) {
            name( propertyName.getLocalPart() );
            if ( property == null ) {
                nullValue();
            } else {
                write( (Feature) property );
            }
        } else if ( property instanceof FeatureReference ) {
            beginObject();
            name( "href" );
            value( ( (FeatureReference) property ).getURI() );
            endObject();
        } else {
            Map<QName, PrimitiveValue> attributes = property.getAttributes();
            if ( attributes.size() > 0 ) {
                name( property.getName().getLocalPart() ).beginObject();
                for ( Map.Entry<QName, PrimitiveValue> attribute : attributes.entrySet() ) {
                    export( attribute.getKey(), attribute.getValue() );
                }
                endObject();
            }
        }
    }

    private void exportAttributesAndChildren( ElementNode elementNode )
                            throws IOException, TransformationException, UnknownCRSException {
        String propertyName = elementNode.getName().getLocalPart();
        name( propertyName );

        Map<QName, PrimitiveValue> attributes = retrieveAttributes( elementNode );
        List<TypedObjectNode> children = retrieveChildren( elementNode );
        if ( attributes.isEmpty() && children.isEmpty() ) {
            nullValue();
        } else if ( attributes.isEmpty() && children.size() == 1 && children.get( 0 ) instanceof PrimitiveValue ) {
            for ( TypedObjectNode childNode : children ) {
                export( childNode );
            }
        } else {
            beginObject();
            if ( children.size() == 1 && children.get( 0 ) instanceof PrimitiveValue ) {
                name( "value" );
                export( (PrimitiveValue) children.get( 0 ) );
            } else {
                for ( TypedObjectNode childNode : children ) {
                    export( childNode );
                }
            }
            for ( Map.Entry<QName, PrimitiveValue> attribute : attributes.entrySet() ) {
                export( attribute.getKey(), attribute.getValue() );
            }
            endObject();
        }
    }

    private void export( TypedObjectNode node )
                            throws IOException, UnknownCRSException, TransformationException {
        if ( node == null ) {
            LOG.warn( "Null node found." );
            return;
        }
        if ( node instanceof PrimitiveValue ) {
            PrimitiveValue primitiveValue = (PrimitiveValue) node;
            export( primitiveValue );
        } else if ( node instanceof Property ) {
            export( (Property) node );
        } else if ( node instanceof GenericXMLElement ) {
            exportAttributesAndChildren( (GenericXMLElement) node );
        } else if ( node instanceof FeatureReference ) {
            name( "href" );
            value( ( (FeatureReference) node ).getURI() );
        } else {
            throw new IOException( "Unhandled node type '" + node.getClass() );
        }
    }

    private void export( QName name, PrimitiveValue value )
                            throws IOException {
        name( name.getLocalPart() );
        export( value );
    }

    private void export( PrimitiveValue value )
                            throws IOException {
        if ( value == null ) {
            nullValue();
        } else {
            switch ( value.getType().getBaseType() ) {
            case BOOLEAN:
                value( (Boolean) value.getValue() );
                break;
            case INTEGER:
                value( (BigInteger) value.getValue() );
                break;
            case DOUBLE:
                value( (Double) value.getValue() );
                break;
            case DECIMAL:
                value( (BigDecimal) value.getValue() );
                break;
            default:
                value( value.getAsText() );
                break;
            }
        }
    }

    private Map<QName, PrimitiveValue> retrieveAttributes( ElementNode elementNode ) {
        Map<QName, PrimitiveValue> attributes = elementNode.getAttributes();
        if ( attributes != null )
            return attributes;
        return Collections.emptyMap();
    }

    private List<TypedObjectNode> retrieveChildren( ElementNode elementNode ) {
        List<TypedObjectNode> children = elementNode.getChildren();
        if ( children != null )
            return children;
        return Collections.emptyList();
    }
}
