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

import static org.deegree.commons.tom.gml.GMLObjectCategory.TIME_SLICE;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.stax.StAXExportingHelper.writeAttribute;
import static org.deegree.feature.types.property.ValueRepresentation.REMOTE;
import static org.deegree.gml.GMLVersion.GML_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.array.TypedObjectNodeArray;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Length;
import org.deegree.commons.uom.Measure;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.timeslice.TimeSlice;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.property.ArrayPropertyType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.LengthPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.StringOrRefPropertyType;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.projection.TimeSliceProjection;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.commons.AbstractGMLObjectWriter;
import org.deegree.gml.reference.FeatureReference;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.time.gml.writer.GmlTimeGeometricPrimitiveWriter;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based GML writer for {@link Feature} (and {@link FeatureCollection}) instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureWriter extends AbstractGMLObjectWriter {

    private static final Logger LOG = LoggerFactory.getLogger( GMLFeatureWriter.class );

    private final QName fidAttr;

    private final String gmlNull;

    private final Map<MultiKey<QName>, PropertyName> allProjections = new HashMap();

    private final List<Filter> timeSliceFilters = new ArrayList<Filter>();

    private final boolean exportSf;

    private final boolean outputGeometries;

    private final boolean exportExtraProps;

    private final boolean exportBoundedBy;

    private final PropertyType boundedByPt;

    private AppSchema schema;

    private GMLSchemaInfoSet schemaInfoset;

    private static final QName XSI_NIL = new QName( XSINS, "nil", "xsi" );

    private static final QName NIL_REASON = new QName( "nilReason" );
    
    /**
     * Creates a new {@link GMLFeatureWriter} instance.
     *
     * @param gmlStreamWriter
     *            GML stream writer, must not be <code>null</code>
     */
    public GMLFeatureWriter( GMLStreamWriter gmlStreamWriter ) {
        super( gmlStreamWriter );

        parseProjections(gmlStreamWriter);

        if ( !version.equals( GML_2 ) ) {
            fidAttr = new QName( gmlNs, "id" );
            gmlNull = "Null";
        } else {
            fidAttr = new QName( "", "fid" );
            gmlNull = "null";
        }

        this.outputGeometries = gmlStreamWriter.getOutputGeometries();
        this.exportSf = false;
        this.exportExtraProps = gmlStreamWriter.getExportExtraProps();
        this.boundedByPt = new EnvelopePropertyType( new QName( gmlNs, "boundedBy" ), 0, 1, null, null );
        this.exportBoundedBy = gmlStreamWriter.getGenerateBoundedByForFeatures();
    }

    /**
     * Exports the given {@link Feature} (or {@link FeatureCollection}).
     *
     * @param feature
     *            feature to be exported, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void export( Feature feature )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        export( feature, referenceExportStrategy.getResolveOptions() );
    }

    /**
     * Exports the given {@link Property}.
     *
     * @param prop
     *            property to be exported, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void export( Property prop )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        export( prop, referenceExportStrategy.getResolveOptions() );
    }

    public void export( TypedObjectNode node, GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        export( null, node, resolveState );
    }

    private void export( QName ftName, TypedObjectNode node, GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( node instanceof GMLObject ) {
            if ( node instanceof Feature ) {
                export( (Feature) node, resolveState );
            } else if ( node instanceof Geometry ) {
                gmlStreamWriter.getGeometryWriter().export( (Geometry) node );
            } else {
                throw new UnsupportedOperationException();
            }
        } else if ( node instanceof PrimitiveValue ) {
            writer.writeCharacters( ( (PrimitiveValue) node ).getAsText() );
        } else if ( node instanceof Property ) {
            export( ftName, (Property) node, resolveState );
        } else if ( node instanceof ElementNode ) {
            ElementNode xmlContent = (ElementNode) node;
            exportGenericXmlElement( xmlContent, resolveState );
        } else if ( node instanceof TypedObjectNodeArray<?> ) {
            for ( TypedObjectNode elem : ( (TypedObjectNodeArray<?>) node ).getElements() ) {
                export( elem, resolveState );
            }
        } else if ( node == null ) {
            LOG.warn( "Null node encountered!?" );
        } else {
            throw new RuntimeException( "Unhandled node type '" + node.getClass() + "'" );
        }
    }

    public void export( final TimeSlice timeSlice, final GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( timeSlice.getId() != null ) {
            referenceExportStrategy.addExportedId( timeSlice.getId() );
        }
        final QName featureName = timeSlice.getType().getName();
        LOG.debug( "Exporting TimeSlice {} with ID {}", featureName, timeSlice.getId() );
        final String namespaceURI = featureName.getNamespaceURI();
        final String localName = featureName.getLocalPart();
        writeStartElementWithNS( namespaceURI, localName );
        if ( timeSlice.getId() != null ) {
            if ( fidAttr.getNamespaceURI() == "" ) {
                writer.writeAttribute( fidAttr.getLocalPart(), timeSlice.getId() );
            } else {
                writeAttributeWithNS( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), timeSlice.getId() );
            }
        }
        for ( final Property prop : timeSlice.getProperties() ) {
            export( prop, resolveState );
        }
        writer.writeEndElement();
    }

    private void export( QName ftName, Property property, GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        QName propName = property.getName();
        PropertyType pt = property.getType();
        if ( pt.getMinOccurs() == 0 ) {
            LOG.debug( "Optional property '" + propName + "', checking if it is requested." );
            if ( !isPropertyRequested( ftName, propName ) ) {
                LOG.debug( "Skipping it." );
                return;
            }
            // required for WMS:
            if ( !outputGeometries && pt instanceof GeometryPropertyType ) {
                LOG.debug( "Skipping it since geometries should not be output." );
                return;
            }
        }

        if ( resolveState.getCurrentLevel() == 0 ) {
            resolveState = getResolveParams( ftName, property, resolveState );
        }

        TypedObjectNode value = property.getValue();

        // if ( value instanceof GenericXMLElement ) {
        // writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
        // export( value, currentLevel, maxInlineLevels );
        // writer.writeEndElement();
        // return;
        // }

        // TODO check for GML 2 properties (gml:pointProperty, ...) and export
        // as "app:gml2PointProperty" for GML 3
        boolean nilled = false;
        Map<QName, PrimitiveValue> attributes = property.getAttributes();
        TypedObjectNode nil = attributes.get( XSI_NIL );
        if ( nil instanceof PrimitiveValue ) {
            nilled = Boolean.TRUE.equals( ( (PrimitiveValue) nil ).getValue() );
        }
        if ( pt instanceof FeaturePropertyType ) {
            if ( nilled ) {
                writeNilledElement( propName, attributes );
            } else {
                exportFeatureProperty( (FeaturePropertyType) pt, (Feature) value, attributes, resolveState );
            }
        } else if ( pt instanceof SimplePropertyType ) {
            if ( nilled ) {
                writeNilledElement( propName, attributes );
            } else {
                // must be a primitive value
                PrimitiveValue pValue = (PrimitiveValue) value;
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( pValue != null ) {
                    writer.writeCharacters( pValue.getAsText() );
                }
                writer.writeEndElement();
            }
        } else if ( pt instanceof GeometryPropertyType ) {
            if ( nilled ) {
                writeNilledElement( propName, attributes );
            } else if ( value == null ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                endEmptyElement();
            } else {
                Geometry gValue = (Geometry) value;
                if ( !exportSf && gValue.getId() != null && referenceExportStrategy.isObjectExported( gValue.getId() ) ) {
                    writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                    writeAttributeWithNS( XLNNS, "href", "#" + gValue.getId() );
                    endEmptyElement();
                } else {
                    writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                    if ( gValue.getId() != null ) {
                        // WFS CITE 1.1.0 test requirement (wfs:GetFeature.XLink-POST-XML-10)
                        writer.writeComment( "Inlined geometry '" + gValue.getId() + "'" );
                    }
                    gmlStreamWriter.getGeometryWriter().export( (Geometry) value );
                    writer.writeEndElement();
                }
            }
        } else if ( pt instanceof CodePropertyType ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( nilled ) {
                writeNilAttributes( attributes );
            }
            CodeType codeType = (CodeType) value;

            if ( codeType != null ) {
                if ( codeType.getCodeSpace() != null && codeType.getCodeSpace().length() > 0 ) {
                    if ( GML_2 != version ) {
                        writer.writeAttribute( "codeSpace", codeType.getCodeSpace() );
                    }
                }
                writer.writeCharacters( codeType.getCode() );
            }
            writer.writeEndElement();
        } else if ( pt instanceof EnvelopePropertyType ) {
            if ( nilled ) {
                writeNilledElement( propName, attributes );
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( value != null ) {
                    gmlStreamWriter.getGeometryWriter().exportEnvelope( (Envelope) value );
                } else {
                    writeStartElementWithNS( gmlNs, gmlNull );
                    writer.writeCharacters( "missing" );
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
        } else if ( pt instanceof LengthPropertyType ) {
            Length length = (Length) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( GML_2 != version ) {
                if ( nilled ) {
                    writeNilAttributes( attributes );
                }
                writer.writeAttribute( "uom", length.getUomUri() );
            }
            if ( !nilled ) {
                writer.writeCharacters( String.valueOf( length.getValue() ) );
            }
            writer.writeEndElement();
        } else if ( pt instanceof MeasurePropertyType ) {
            Measure measure = (Measure) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( GML_2 != version ) {
                writer.writeAttribute( "uom", measure.getUomUri() );
            }
            writer.writeCharacters( String.valueOf( measure.getValue() ) );
            writer.writeEndElement();
        } else if ( pt instanceof StringOrRefPropertyType ) {
            StringOrRef stringOrRef = (StringOrRef) value;
            if ( stringOrRef.getString() == null || stringOrRef.getString().length() == 0 ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( stringOrRef.getRef() != null ) {
                    writeAttributeWithNS( XLNNS, "href", stringOrRef.getRef() );
                }
                if ( nilled ) {
                    writeNilAttributes( attributes );
                }
                endEmptyElement();
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( nilled ) {
                    writeNilAttributes( attributes );
                }

                if ( stringOrRef.getRef() != null ) {
                    writeAttributeWithNS( XLNNS, "href", stringOrRef.getRef() );
                }
                if ( !nilled && stringOrRef.getString() != null ) {
                    writer.writeCharacters( stringOrRef.getString() );
                }
                writer.writeEndElement();
            }
        } else if ( pt instanceof CustomPropertyType ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( attributes != null ) {
                for ( Entry<QName, PrimitiveValue> attr : attributes.entrySet() ) {
                    QName attrKey = attr.getKey();
                    PrimitiveValue attrValue = attr.getValue();
                    if ( XSI_NIL.equals( attrKey ) )
                        writeAttributeWithNS( attrKey.getNamespaceURI(), attrKey.getLocalPart(), attrValue.getAsText() );
                    else
                        writeAttribute( writer, attrKey, attrValue.getAsText() );
                }
            }
            if ( property.getChildren() != null ) {
                for ( TypedObjectNode childNode : property.getChildren() ) {
                    export( childNode, resolveState );
                }
            }
            writer.writeEndElement();
        } else if ( pt instanceof ArrayPropertyType ) {
            if ( nilled ) {
                writeNilledElement( propName, attributes );
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                export( property.getValue(), resolveState );
                writer.writeEndElement();
            }
        } else if ( pt instanceof ObjectPropertyType ) {
            final ObjectPropertyType objectPt = (ObjectPropertyType) pt;
            if ( objectPt.getCategory() == TIME_SLICE && !timeSliceFilters.isEmpty() ) {
                if ( excludeByTimeSliceFilter( property ) ) {
                    return;
                }
            }
            exportGenericObjectProperty( property, (GMLObject) value, resolveState );
        } else {
            throw new RuntimeException( "Internal error. Unhandled property type '" + pt.getClass() + "'" );
        }
    }

    private void writeNilledElement( QName propName, Map<QName, PrimitiveValue> attributes )
                            throws XMLStreamException {
        writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
        writeNilAttributes( attributes );
        endEmptyElement();
    }

    private void writeNilAttributes( Map<QName, PrimitiveValue> attributes )
                            throws XMLStreamException {
        writeAttribute( writer, XSI_NIL, "true" );
        PrimitiveValue value = attributes.get( NIL_REASON );
        if ( value != null )
            writeAttribute( writer, NIL_REASON, value.getAsText() );
    }

    
    private boolean excludeByTimeSliceFilter( Property property ) {
        final TimeSlice timeSlice = (TimeSlice) property.getValue();
        for ( final Filter timeSliceFilter : timeSliceFilters ) {
            TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
            try {
                if ( !timeSliceFilter.evaluate( timeSlice, evaluator ) ) {
                    return true;
                }
            } catch ( FilterEvaluationException e ) {
                LOG.warn( "Unable to evaluate time slice projection filter: " + e.getMessage() );
            }
        }
        return false;
    }

    private void exportBoundedBy( Envelope env, boolean indicateMissing )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( env != null || indicateMissing ) {
            writer.writeStartElement( gmlNs, "boundedBy" );
            if ( env != null ) {
                gmlStreamWriter.getGeometryWriter().exportEnvelope( env );
            } else {
                writer.writeStartElement( gmlNs, gmlNull );
                writer.writeCharacters( "missing" );
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    private void export( Feature feature, GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        setSchema( feature );

        if ( feature.getId() != null ) {
            referenceExportStrategy.addExportedId( feature.getId() );
        }
        if ( feature instanceof GenericFeatureCollection ) {
            LOG.debug( "Exporting generic feature collection." );
            writeStartElementWithNS( gmlNs, "FeatureCollection" );
            if ( feature.getId() != null ) {
                if ( fidAttr.getNamespaceURI() == "" ) {
                    writer.writeAttribute( fidAttr.getLocalPart(), feature.getId() );
                } else {
                    writeAttributeWithNS( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), feature.getId() );
                }
            }
            exportBoundedBy( feature.getEnvelope(), false );
            for ( Feature member : ( (FeatureCollection) feature ) ) {
                String memberFid = member.getId();
                writeStartElementWithNS( gmlNs, "featureMember" );
                if ( memberFid != null && referenceExportStrategy.isObjectExported( memberFid ) ) {
                    writeAttributeWithNS( XLNNS, "href", "#" + memberFid );
                } else {
                    export( member, getResolveStateForNextLevel( resolveState ) );
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        } else {
            QName featureName = feature.getName();
            LOG.debug( "Exporting Feature {} with ID {}", featureName, feature.getId() );
            String namespaceURI = featureName.getNamespaceURI();
            String localName = featureName.getLocalPart();
            writeStartElementWithNS( namespaceURI, localName );

            if ( feature.getId() != null ) {
                if ( fidAttr.getNamespaceURI() == "" ) {
                    writer.writeAttribute( fidAttr.getLocalPart(), feature.getId() );
                } else {
                    writeAttributeWithNS( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), feature.getId() );
                }
            }

            List<Property> props = feature.getProperties();
            if ( exportBoundedBy ) {
                props = augmentBoundedBy( feature );
            }

            for ( Property prop : props ) {
                export( feature.getName(), prop, resolveState );
            }

            if ( exportExtraProps ) {
                ExtraProps extraProps = feature.getExtraProperties();
                if ( extraProps != null ) {
                    for ( Property prop : extraProps.getProperties() ) {
                        export( feature.getName(), prop, resolveState );
                    }
                }
            }
            writer.writeEndElement();
        }
    }

    private void setSchema( Feature feature ) {
        if ( schema == null ) {
            schema = feature.getType().getSchema();
            if ( schema != null ) {
                schemaInfoset = schema.getGMLSchema();
            }
        }
    }

    private List<Property> augmentBoundedBy( Feature f ) {
        LinkedList<Property> props = new LinkedList<Property>( f.getProperties() );
        for ( int i = 0; i < props.size(); i++ ) {
            QName name = props.get( i ).getName();
            if ( !gmlNs.equals( name.getNamespaceURI() ) || name.getLocalPart().equals( "location" ) ) {
                // not a GML property or gml:location -> gml:boundedBy must be included right before it
                Property boundedBy = getBoundedBy( f );
                if ( boundedBy != null ) {
                    props.add( i, boundedBy );
                }
                break;
            } else if ( name.getLocalPart().equals( "boundedBy" ) ) {
                // already present -> don't include it
                break;
            }
        }
        return props;
    }

    private Property getBoundedBy( Feature f ) {
        Envelope env = f.getEnvelope();
        if ( env == null ) {
            env = f.calcEnvelope();
        }
        if ( env == null ) {
            return null;
        }
        return new GenericProperty( boundedByPt, env );
    }

    private void exportFeatureProperty( FeaturePropertyType pt, Feature subFeature,
                                        Map<QName, PrimitiveValue> attributes,
                                        GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        QName propName = pt.getName();
        LOG.debug( "Exporting feature property '" + propName + "'" );
        if ( subFeature == null ) {
            exportEmptyProperty( propName, attributes );
        } else if ( subFeature instanceof FeatureReference ) {
            exportFeatureProperty( pt, (FeatureReference) subFeature, resolveState, propName );
        } else {
            // normal feature
            String subFid = subFeature.getId();
            if ( subFid == null ) {
                // no feature id -> no other chance, but inlining it
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeComment( "Inlined feature '" + subFid + "'" );
                export( subFeature, getResolveStateForNextLevel( resolveState ) );
                writer.writeEndElement();
            } else {
                // has feature id
                if ( referenceExportStrategy.isObjectExported( subFid ) ) {
                    exportAlreadyExportedFeaturePropertyByReference( subFeature, propName );
                } else {
                    exportFeaturePropertyByValue( propName, subFeature, resolveState );
                }
            }
        }
    }

    private void exportGenericObjectProperty( final Property prop, final GMLObject object,
                                              final GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        final ObjectPropertyType pt = (ObjectPropertyType) prop.getType();
        final QName propName = pt.getName();
        LOG.debug( "Exporting object property '" + propName + "'" );
        if ( object == null ) {
            exportEmptyProperty( propName, prop.getAttributes() );
        } else {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            switch ( pt.getCategory() ) {
            case TIME_OBJECT:
                new GmlTimeGeometricPrimitiveWriter().write( writer, (TimeGeometricPrimitive) object );
                break;
            case TIME_SLICE:
                export( (TimeSlice) object, resolveState );
                break;
            default:
                throw new RuntimeException();
            }
            writer.writeEndElement();
        }
    }

    private void exportEmptyProperty( final QName propName, final Map<QName,PrimitiveValue> attrs)
                            throws XMLStreamException {
        writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
        writeAttributes( attrs );
        endEmptyElement();
    }

    private void exportFeatureProperty( FeaturePropertyType pt, FeatureReference ref, GmlXlinkOptions resolveState,
                                        QName propName )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        boolean includeNextLevelInOutput = includeNextLevelInOutput( resolveState );
        if ( includeNextLevelInOutput ) {
            if ( pt.getAllowedRepresentation() == REMOTE ) {
                exportFeaturePropertyByReference( propName, ref, true, resolveState );
            } else {
                if ( referenceExportStrategy.isObjectExported( ref.getId() ) ) {
                    exportAlreadyExportedFeaturePropertyByReference( ref, propName );
                } else {
                    exportFeaturePropertyByValue( propName, ref, resolveState );
                }
            }
        } else {
            exportFeaturePropertyByReference( propName, ref, false, resolveState );
        }
    }

    private void exportAlreadyExportedFeaturePropertyByReference( Feature ref, QName propName )
                            throws XMLStreamException {
        writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
        writeAttributeWithNS( XLNNS, "href", "#" + ref.getId() );
        endEmptyElement();
    }

    private boolean includeNextLevelInOutput( GmlXlinkOptions resolveState ) {
        int maxInlineLevels = resolveState.getDepth();
        int currentLevel = resolveState.getCurrentLevel();
        return maxInlineLevels == -1 || ( maxInlineLevels > 0 && currentLevel < maxInlineLevels );
    }

    private void exportFeaturePropertyByReference( QName propName, FeatureReference ref,
                                                   boolean forceInclusionInDocument, GmlXlinkOptions resolveState )
                            throws XMLStreamException {

        writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
        String uri = null;
        if ( forceInclusionInDocument ) {
            resolveState = getResolveStateForNextLevel( resolveState );
            uri = referenceExportStrategy.requireObject( ref, resolveState );
        } else {
            uri = referenceExportStrategy.handleReference( ref );
        }
        writeAttributeWithNS( XLNNS, "href", uri );
        endEmptyElement();
    }

    private void exportFeaturePropertyByValue( QName propName, Feature subFeature, GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        referenceExportStrategy.addExportedId( subFeature.getId() );
        writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
        writer.writeComment( "Inlined feature '" + subFeature.getId() + "'" );
        export( subFeature, getResolveStateForNextLevel( resolveState ) );
        writer.writeEndElement();
    }

    private void exportGenericXmlElement( ElementNode xmlContent, GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        QName elName = xmlContent.getName();
        LOG.debug( "Exporting " + elName );
        XSElementDeclaration elDecl = xmlContent.getXSType();
        if ( elDecl != null && schemaInfoset != null ) {
            ObjectPropertyType gmlPropertyDecl = schemaInfoset.getGMLPropertyDecl( elDecl, elName, 0, 1, null );
            if ( gmlPropertyDecl instanceof FeaturePropertyType ) {
                List<TypedObjectNode> children = xmlContent.getChildren();
                if ( children != null && children.size() == 1 && children.get( 0 ) instanceof Feature ) {
                    LOG.debug( "Exporting as nested feature property." );
                    exportFeatureProperty( (FeaturePropertyType) gmlPropertyDecl, (Feature) children.get( 0 ), null,
                                           resolveState );
                    return;
                }
            }
        }

        writeStartElementWithNS( elName.getNamespaceURI(), elName.getLocalPart() );
        writeAttributes (xmlContent.getAttributes());
        if ( xmlContent.getChildren() != null ) {
            for ( TypedObjectNode childNode : xmlContent.getChildren() ) {
                export( childNode, resolveState );
            }
        }
        writer.writeEndElement();
    }

    private void writeAttributes( final Map<QName, PrimitiveValue> attrs )
                            throws XMLStreamException {
        if ( attrs != null ) {
            for ( final Entry<QName, PrimitiveValue> attr : attrs.entrySet() ) {
                writeAttributeWithNS( attr.getKey().getNamespaceURI(), attr.getKey().getLocalPart(),
                                      attr.getValue().getAsText() );
            }
        }
    }

    private void parseProjections( GMLStreamWriter gmlStreamWriter ) {
        Map<QName, List<ProjectionClause>> projections = gmlStreamWriter.getProjections();
        if ( projections != null ) {
            for ( Map.Entry<QName, List<ProjectionClause>> projection : projections.entrySet() ) {
                QName ftName = projection.getKey();
                for ( ProjectionClause projectionOfFeatureType : projection.getValue() ) {
                    if ( projectionOfFeatureType instanceof PropertyName ) {
                        PropertyName propName = (PropertyName) projectionOfFeatureType;
                        QName qName = propName.getPropertyName().getAsQName();
                        if ( qName != null ) {
                            allProjections.put( key( ftName, qName ), propName );
                        } else {
                            LOG.debug( "Only simple qualified element names are allowed for PropertyName projections. Ignoring '"
                                       + propName.getPropertyName() + "'" );
                        }
                    } else if ( projectionOfFeatureType instanceof TimeSliceProjection ) {
                        timeSliceFilters.add( ( (TimeSliceProjection) projectionOfFeatureType ).getTimeSliceFilter() );
                    }
                }
            }
        }
    }

    private boolean isPropertyRequested( QName ftName, QName propName ) {
        // ftName is null if the property not on level 0
        if ( ftName == null )
            return true;
        return allProjections.isEmpty() || allProjections.containsKey( key( ftName, propName ) );
    }

    private MultiKey<QName> key( QName ftName, QName propName ) {
        return new MultiKey<>( ftName, propName );
    }

    private GmlXlinkOptions getResolveParams( QName ftName, Property prop, GmlXlinkOptions resolveState ) {
        PropertyName projection = allProjections.get( key( ftName, prop.getName() ) );
        if ( projection != null && projection.getResolveParams() != null ) {
            return new GmlXlinkOptions( projection.getResolveParams() );
        }
        return resolveState;
    }

}