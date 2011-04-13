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

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.feature.types.property.ValueRepresentation.REMOTE;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.array.TypedObjectNodeArray;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Length;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.stax.StAXExportingHelper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.property.ArrayPropertyType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.LengthPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.StringOrRefPropertyType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLReference;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML2GeometryWriter;
import org.deegree.gml.geometry.GML3GeometryWriter;
import org.deegree.gml.geometry.GMLGeometryWriter;
import org.deegree.protocol.wfs.getfeature.XLinkPropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based writer for GML-encoded features and feature collections.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureWriter {

    private static final Logger LOG = LoggerFactory.getLogger( GMLFeatureWriter.class );

    private final GMLVersion version;

    private final String gmlNs;

    private final QName fidAttr;

    private final String gmlNull;

    private final Set<String> exportedIds = new HashSet<String>();

    private final XMLStreamWriter writer;

    private final GMLGeometryWriter geometryWriter;

    private final String remoteXlinkTemplate;

    // TODO handle properties that are more complex XPath-expressions
    private final Set<QName> propNames = new HashSet<QName>();

    // TODO handle properties that are more complex XPath-expressions
    private final Map<QName, XLinkPropertyName> xlinkPropNames = new HashMap<QName, XLinkPropertyName>();

    // export all levels by default
    private final int traverseXlinkDepth;

    // private int traverseXlinkExpiry;

    private final boolean exportSf;

    private final boolean outputGeometries;

    private final Map<String, String> nsToPrefix = new HashMap<String, String>();

    private final GMLForwardReferenceHandler additionalObjectHandler;

    private final boolean exportExtraProps;

    /**
     * Creates a new {@link GMLFeatureWriter} instance.
     * 
     * @param version
     *            GML version of the output, must not be <code>null</code>
     * @param writer
     *            xml stream to write to, must not be <code>null</code>
     * @param outputCRS
     *            crs used for exported geometries, may be <code>null</code> (in this case, the original crs of the
     *            geometries is used)
     * @param formatter
     *            formatter to use for exporting coordinates, e.g. to limit the number of decimal places, may be
     *            <code>null</code> (fallback to default {@link DecimalCoordinateFormatter})
     * @param remoteXlinkTemplate
     *            URI template used to create references to subfeatures that will not be included in the document, e.g.
     *            <code>#{}</code>, substring <code>{}</code> is replaced by the object id
     * @param requestedProps
     *            properties to be exported, may be <code>null</code> (export all properties)
     * @param traverseXlinkDepth
     *            number of subfeature levels to export (0...) or -1 (unlimited)
     * @param traverseXlinkExpiry
     *            timeout for resolving remote feature references (currently unsupported)
     * @param xlinkProps
     *            properties with special xlink behaviour, can be <code>null</code>
     * @param exportSfGeometries
     *            if true, geometries are exported as SFS geometries (only applies to complex geometries)
     * @param outputGeometries
     *            if false, geometry properties are omitted from the output
     * @param prefixToNs
     *            namespace bindings to use, may be <code>null</code>
     * @param additionalObjectHandler
     *            handler that is invoked when a feature property has to be expanded, may be <code>null</code>
     * @param exportExtraProps
     *            if true, {@link ExtraProps} associated with features are exported as property elements
     */
    public GMLFeatureWriter( GMLVersion version, XMLStreamWriter writer, ICRS outputCRS, CoordinateFormatter formatter,
                             String remoteXlinkTemplate, PropertyName[] requestedProps, int traverseXlinkDepth,
                             int traverseXlinkExpiry, XLinkPropertyName[] xlinkProps, boolean exportSfGeometries,
                             boolean outputGeometries, Map<String, String> prefixToNs,
                             GMLForwardReferenceHandler additionalObjectHandler, boolean exportExtraProps ) {

        this.version = version;
        this.writer = writer;
        if ( remoteXlinkTemplate != null ) {
            this.remoteXlinkTemplate = remoteXlinkTemplate;
        } else {
            this.remoteXlinkTemplate = "#{}";
        }
        if ( requestedProps != null ) {
            for ( PropertyName propertyName : requestedProps ) {
                QName qName = propertyName.getAsQName();
                if ( qName != null ) {
                    this.propNames.add( qName );
                } else {
                    LOG.warn( "Currently, only simple qualified names are supported for restricting output properties." );
                }
            }
        }
        this.traverseXlinkDepth = traverseXlinkDepth;
        // this.traverseXlinkExpiry = traverseXlinkExpiry;
        if ( xlinkProps != null ) {
            for ( XLinkPropertyName xlinkProp : xlinkProps ) {
                QName qName = xlinkProp.getPropertyName().getAsQName();
                if ( qName != null ) {
                    this.xlinkPropNames.put( qName, xlinkProp );
                } else {
                    LOG.warn( "Currently, only simple qualified names are supported for setting special XLink property output behaviour." );
                }
            }
        }

        gmlNs = version.getNamespace();
        if ( !version.equals( GMLVersion.GML_2 ) ) {
            geometryWriter = new GML3GeometryWriter( version, writer, outputCRS, formatter, exportSfGeometries,
                                                     exportedIds );
            fidAttr = new QName( gmlNs, "id" );
            gmlNull = "Null";
        } else {
            geometryWriter = new GML2GeometryWriter( writer, outputCRS, formatter, exportedIds );
            fidAttr = new QName( NULL_NS_URI, "fid" );
            gmlNull = "null";
        }

        this.outputGeometries = outputGeometries;
        this.exportSf = false;

        if ( prefixToNs != null ) {
            for ( Entry<String, String> prefixAndNs : prefixToNs.entrySet() ) {
                nsToPrefix.put( prefixAndNs.getValue(), prefixAndNs.getKey() );
            }
        }
        this.additionalObjectHandler = additionalObjectHandler;
        this.exportExtraProps = exportExtraProps;
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
        export( feature, 0, traverseXlinkDepth );
    }

    public void export( Feature feature, int level )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        export( feature, level, traverseXlinkDepth );
    }

    /**
     * TODO merge with other schema location possibilities
     * 
     * @param fc
     * @param noNamespaceSchemaLocation
     *            may be null
     * @param bindings
     *            optional additional schema locations
     * @throws XMLStreamException
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    public void export( FeatureCollection fc, String noNamespaceSchemaLocation, Map<String, String> bindings )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        LOG.debug( "Exporting generic feature collection." );
        if ( fc.getId() != null ) {
            exportedIds.add( fc.getId() );
        }

        writer.setDefaultNamespace( WFS_NS );
        writer.writeStartElement( WFS_NS, "FeatureCollection" );
        writer.writeDefaultNamespace( WFS_NS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        writer.writeNamespace( GML_PREFIX, gmlNs );

        if ( fc.getId() != null ) {
            if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                writer.writeAttribute( fidAttr.getLocalPart(), fc.getId() );
            } else {
                writer.writeAttribute( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), fc.getId() );
            }
        }

        if ( noNamespaceSchemaLocation != null ) {
            writer.writeAttribute( XSINS, "noNamespaceSchemaLocation", noNamespaceSchemaLocation );
        }
        if ( bindings != null && !bindings.isEmpty() ) {

            String locs = null;
            for ( Entry<String, String> e : bindings.entrySet() ) {
                if ( locs == null ) {
                    locs = "";
                } else {
                    locs += " ";
                }
                locs += e.getKey() + " " + e.getValue();
            }
            writer.writeAttribute( XSINS, "schemaLocation", locs );
        }

        writer.writeStartElement( gmlNs, "boundedBy" );
        Envelope fcEnv = fc.getEnvelope();
        if ( fcEnv != null ) {
            geometryWriter.exportEnvelope( fc.getEnvelope() );
        } else {
            writer.writeStartElement( gmlNs, gmlNull );
            writer.writeCharacters( "missing" );
            writer.writeEndElement();
        }
        writer.writeEndElement();
        for ( Feature f : fc ) {
            writer.writeStartElement( gmlNs, "featureMember" );
            export( f );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Returns whether the specified feature has already been written.
     * 
     * @param fid
     *            feature id, must not be <code>null</code>
     * @return true, if the feature has been exported, false otherwise
     */
    public boolean isExported( String fid ) {
        return exportedIds.contains( fid );
    }

    private void export( Feature feature, int currentLevel, int maxInlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( feature.getId() != null ) {
            exportedIds.add( feature.getId() );
        }
        if ( feature instanceof GenericFeatureCollection ) {
            LOG.debug( "Exporting generic feature collection." );
            writeStartElementWithNS( gmlNs, "FeatureCollection" );
            if ( feature.getId() != null ) {
                if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                    writer.writeAttribute( fidAttr.getLocalPart(), feature.getId() );
                } else {
                    writeAttributeWithNS( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), feature.getId() );
                }
            }
            for ( Feature member : ( (FeatureCollection) feature ) ) {
                String memberFid = member.getId();
                writeStartElementWithNS( gmlNs, "featureMember" );
                if ( memberFid != null && exportedIds.contains( memberFid ) ) {
                    writeAttributeWithNS( XLNNS, "href", "#" + memberFid );
                } else {
                    export( member, currentLevel + 1, maxInlineLevels );
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
                if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                    writer.writeAttribute( fidAttr.getLocalPart(), feature.getId() );
                } else {
                    writeAttributeWithNS( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), feature.getId() );
                }
            }
            for ( Property prop : feature.getProperties( version ) ) {
                if ( currentLevel == 0 ) {
                    maxInlineLevels = getInlineLevels( prop );
                }
                export( prop, currentLevel, maxInlineLevels );
            }
            if ( exportExtraProps ) {
                ExtraProps extraProps = feature.getExtraProperties();
                if ( extraProps != null ) {
                    for ( Property prop : extraProps.getProperties() ) {
                        export( prop, currentLevel, maxInlineLevels );
                    }
                }
            }
            writer.writeEndElement();
        }
    }

    private int getInlineLevels( Property prop ) {
        XLinkPropertyName xlinkPropName = xlinkPropNames.get( prop.getName() );
        if ( xlinkPropName != null ) {
            if ( xlinkPropName.getTraverseXlinkDepth().equals( "*" ) ) {
                return -1;
            }
            return Integer.parseInt( xlinkPropName.getTraverseXlinkDepth() );
        }
        return traverseXlinkDepth;
    }

    private void export( Property property, int currentLevel, int maxInlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        QName propName = property.getName();
        PropertyType propertyType = property.getType();
        if ( propertyType.getMinOccurs() == 0 ) {
            LOG.debug( "Optional property '" + propName + "', checking if it is requested." );
            if ( !isPropertyRequested( propName ) ) {
                LOG.debug( "Skipping it." );
                return;
            }
            // required for WMS:
            if ( !outputGeometries && propertyType instanceof GeometryPropertyType ) {
                LOG.debug( "Skipping it since geometries should not be output." );
                return;
            }
        }

        TypedObjectNode value = property.getValue();

//        if ( value instanceof GenericXMLElement ) {
//            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
//            export( value, currentLevel, maxInlineLevels );
//            writer.writeEndElement();
//            return;
//        }

        // TODO check for GML 2 properties (gml:pointProperty, ...) and export
        // as "app:gml2PointProperty" for GML 3
        if ( propertyType instanceof FeaturePropertyType ) {
            if ( property.isNilled() ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writeAttributeWithNS( XSINS, "nil", "true" );
            } else {
                exportFeatureProperty( (FeaturePropertyType) propertyType, (Feature) value, currentLevel,
                                       maxInlineLevels );
            }
        } else if ( propertyType instanceof SimplePropertyType ) {
            if ( property.isNilled() ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writeAttributeWithNS( XSINS, "nil", "true" );
            } else {
                // must be a primitive value
                PrimitiveValue pValue = (PrimitiveValue) value;
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( pValue != null ) {
                    // TODO
                    if ( pValue.getType() == PrimitiveType.DECIMAL ) {
                        writer.writeCharacters( pValue.getValue().toString() );
                    } else {
                        writer.writeCharacters( pValue.getAsText() );
                    }
                }
                writer.writeEndElement();
            }
        } else if ( propertyType instanceof GeometryPropertyType ) {
            if ( property.isNilled() ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writeAttributeWithNS( XSINS, "nil", "true" );
            } else {
                Geometry gValue = (Geometry) value;
                if ( !exportSf && gValue.getId() != null && exportedIds.contains( gValue.getId() ) ) {
                    writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                    writeAttributeWithNS( XLNNS, "href", "#" + gValue.getId() );
                } else {
                    writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                    if ( gValue.getId() != null ) {
                        // WFS CITE 1.1.0 test requirement (wfs:GetFeature.XLink-POST-XML-10)
                        writer.writeComment( "Inlined geometry '" + gValue.getId() + "'" );
                    }
                    geometryWriter.export( (Geometry) value );
                    writer.writeEndElement();
                }
            }
        } else if ( propertyType instanceof CodePropertyType ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( property.isNilled() ) {
                writeAttributeWithNS( XSINS, "nil", "true" );
            }
            CodeType codeType = (CodeType) value;
            if ( codeType.getCodeSpace() != null && codeType.getCodeSpace().length() > 0 ) {
                if ( GML_2 != version ) {
                    writer.writeAttribute( "codeSpace", codeType.getCodeSpace() );
                }
            }
            writer.writeCharacters( codeType.getCode() );
            writer.writeEndElement();
        } else if ( propertyType instanceof EnvelopePropertyType ) {
            if ( property.isNilled() ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writeAttributeWithNS( XSINS, "nil", "true" );
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( value != null ) {
                    geometryWriter.exportEnvelope( (Envelope) value );
                } else {
                    writeStartElementWithNS( gmlNs, gmlNull );
                    writer.writeCharacters( "missing" );
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
        } else if ( propertyType instanceof LengthPropertyType ) {
            Length length = (Length) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( GML_2 != version ) {
                if ( property.isNilled() ) {
                    writeAttributeWithNS( XSINS, "nil", "true" );
                }
                writer.writeAttribute( "uom", length.getUomUri() );
            }
            if ( !property.isNilled() ) {
                writer.writeCharacters( String.valueOf( length.getValue() ) );
            }
            writer.writeEndElement();
        } else if ( propertyType instanceof MeasurePropertyType ) {
            Measure measure = (Measure) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( GML_2 != version ) {
                writer.writeAttribute( "uom", measure.getUomUri() );
            }
            writer.writeCharacters( String.valueOf( measure.getValue() ) );
            writer.writeEndElement();
        } else if ( propertyType instanceof StringOrRefPropertyType ) {
            StringOrRef stringOrRef = (StringOrRef) value;
            if ( stringOrRef.getString() == null || stringOrRef.getString().length() == 0 ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( stringOrRef.getRef() != null ) {
                    writeAttributeWithNS( XLNNS, "href", stringOrRef.getRef() );
                }
                if ( property.isNilled() ) {
                    writeAttributeWithNS( XSINS, "nil", "true" );
                }
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( property.isNilled() ) {
                    writeAttributeWithNS( XSINS, "nil", "true" );
                }

                if ( stringOrRef.getRef() != null ) {
                    writeAttributeWithNS( XLNNS, "href", stringOrRef.getRef() );
                }
                if ( !property.isNilled() && stringOrRef.getString() != null ) {
                    writer.writeCharacters( stringOrRef.getString() );
                }
                writer.writeEndElement();
            }
        } else if ( propertyType instanceof CustomPropertyType ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( property.getAttributes() != null ) {
                for ( Entry<QName, PrimitiveValue> attr : property.getAttributes().entrySet() ) {
                    StAXExportingHelper.writeAttribute( writer, attr.getKey(), attr.getValue().getAsText() );
                }
            }
            if ( property.getChildren() != null ) {
                for ( TypedObjectNode childNode : property.getChildren() ) {
                    export( childNode, currentLevel, maxInlineLevels );
                }
            }
            writer.writeEndElement();
        } else if ( propertyType instanceof ArrayPropertyType ) {
            if ( property.isNilled() ) {
                writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                writeAttributeWithNS( XSINS, "nil", "true" );
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                export( (TypedObjectNode) property.getValue(), currentLevel, maxInlineLevels );
                writer.writeEndElement();
            }
        } else {
            throw new RuntimeException( "Internal error. Unhandled property type '" + propertyType.getClass() + "'" );
        }
    }

    private void exportFeatureProperty( FeaturePropertyType pt, Feature subFeature, int currentLevel,
                                        int maxInlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        QName propName = pt.getName();
        LOG.debug( "Exporting feature property '" + propName + "'" );
        if ( subFeature == null ) {
            writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
        } else if ( !( subFeature instanceof FeatureReference ) || ( (FeatureReference) subFeature ).isLocal() ) {
            // normal feature or local feature reference
            String subFid = subFeature.getId();
            if ( subFid == null ) {
                // no feature id -> no other chance, but inlining it
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeComment( "Inlined feature '" + subFid + "'" );
                export( subFeature, currentLevel + 1, maxInlineLevels );
                writer.writeEndElement();
            } else {
                // has feature id
                if ( exportedIds.contains( subFid ) ) {
                    // already exported -> put a local xlink to the feature instance
                    writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                    writeAttributeWithNS( XLNNS, "href", "#" + subFid );
                } else {
                    // not exported yet
                    if ( maxInlineLevels == -1 || ( maxInlineLevels > 0 && currentLevel < maxInlineLevels ) ) {
                        // force export (maximum number of inline levels not reached)
                        if ( pt.getAllowedRepresentation() == REMOTE ) {
                            // only export by reference possible
                            writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                            if ( additionalObjectHandler != null ) {
                                String uri = additionalObjectHandler.requireObject( (GMLReference<?>) subFeature );
                                writeAttributeWithNS( XLNNS, "href", uri );
                            } else {
                                LOG.debug( "No additionalObjectHandler registered. Exporting xlink-only feature property inline." );
                                String uri = remoteXlinkTemplate.replace( "{}", subFid );
                                writeAttributeWithNS( XLNNS, "href", uri );
                            }
                        } else {
                            // export inline
                            exportedIds.add( subFeature.getId() );
                            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                            writer.writeComment( "Inlined feature '" + subFid + "'" );
                            export( subFeature, currentLevel + 1, maxInlineLevels );
                            writer.writeEndElement();
                        }
                    } else {
                        // don't force export (maximum number of inline levels reached)
                        writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                        if ( additionalObjectHandler != null ) {
                            String uri = additionalObjectHandler.handleReference( (GMLReference<?>) subFeature );
                            writeAttributeWithNS( XLNNS, "href", uri );
                        } else {
                            LOG.warn( "No additionalObjectHandler registered. Exporting xlink-only feature inline." );
                            String uri = remoteXlinkTemplate.replace( "{}", subFid );
                            writeAttributeWithNS( XLNNS, "href", uri );
                        }
                    }
                }
            }
        } else {
            // remote feature reference
            FeatureReference ref = (FeatureReference) subFeature;
            if ( ( maxInlineLevels > 0 && currentLevel < maxInlineLevels ) || remoteXlinkTemplate == null
                 || maxInlineLevels == -1 ) {
                String uri = ref.getURI();
                try {
                    new URL( uri );
                    throw new UnsupportedOperationException(
                                                             "Inlining of remote feature references is not implemented yet." );
                } catch ( MalformedURLException e ) {
                    LOG.warn( "Not inlining remote feature reference -- not a valid URI." );
                    writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                    writeAttributeWithNS( XLNNS, "href", ref.getURI() );
                }
            } else {
                // must be exported by reference
                writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                writeAttributeWithNS( XLNNS, "href", ref.getURI() );
            }
        }
    }

    private void export( TypedObjectNode node, int currentLevel, int maxInlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( node instanceof GMLObject ) {
            if ( node instanceof Feature ) {
                export( (Feature) node, currentLevel, maxInlineLevels );
            } else if ( node instanceof Geometry ) {
                geometryWriter.export( (Geometry) node );
            } else {
                throw new UnsupportedOperationException();
            }
        } else if ( node instanceof ElementNode ) {
            ElementNode xmlContent = (ElementNode) node;
            QName elName = xmlContent.getName();
            writeStartElementWithNS( elName.getNamespaceURI(), elName.getLocalPart() );
            if ( xmlContent.getAttributes() != null ) {
                for ( Entry<QName, PrimitiveValue> attr : xmlContent.getAttributes().entrySet() ) {
                    StAXExportingHelper.writeAttribute( writer, attr.getKey(), attr.getValue().getAsText() );
                }
            }
            if ( xmlContent.getChildren() != null ) {
                for ( TypedObjectNode childNode : xmlContent.getChildren() ) {
                    export( childNode, currentLevel, maxInlineLevels );
                }
            }
            writer.writeEndElement();
        } else if ( node instanceof PrimitiveValue ) {
            writer.writeCharacters( ( (PrimitiveValue) node ).getAsText() );
        } else if ( node instanceof TypedObjectNodeArray<?> ) {
            for ( TypedObjectNode elem : ( (TypedObjectNodeArray<?>) node ).getElements() ) {
                export( elem, currentLevel, maxInlineLevels );
            }
        } else if ( node == null ) {
            LOG.warn( "Null node encountered!?" );
        } else {
            throw new RuntimeException( "Unhandled node type '" + node.getClass() + "'" );
        }
    }

    private void writeStartElementWithNS( String namespaceURI, String localname )
                            throws XMLStreamException {
        if ( namespaceURI == null || namespaceURI.length() == 0 ) {
            writer.writeStartElement( localname );
        } else {
            if ( writer.getNamespaceContext().getPrefix( namespaceURI ) == null ) {
                String prefix = nsToPrefix.get( namespaceURI );
                if ( prefix != null ) {
                    writer.setPrefix( prefix, namespaceURI );
                    writer.writeStartElement( prefix, localname, namespaceURI );
                    writer.writeNamespace( prefix, namespaceURI );
                } else {
                    LOG.warn( "No prefix for namespace '{}' configured. Depending on XMLStream auto-repairing.",
                              namespaceURI );
                    writer.writeStartElement( namespaceURI, localname );
                }
            } else {
                writer.writeStartElement( namespaceURI, localname );
            }
        }
    }

    private void writeAttributeWithNS( String namespaceURI, String localname, String value )
                            throws XMLStreamException {
        if ( namespaceURI == null || namespaceURI.length() == 0 ) {
            writer.writeAttribute( localname, value );
        } else {
            String prefix = writer.getNamespaceContext().getPrefix( namespaceURI );
            if ( prefix == null ) {
                prefix = nsToPrefix.get( namespaceURI );
                if ( prefix != null ) {
                    writer.writeNamespace( prefix, namespaceURI );
                } else {
                    LOG.warn( "No prefix for namespace '{}' configured. Depending on XMLStream auto-repairing.",
                              namespaceURI );
                }
            }
            writer.writeAttribute( prefix, namespaceURI, localname, value );
        }
    }

    private void writeEmptyElementWithNS( String namespaceURI, String localname )
                            throws XMLStreamException {
        if ( namespaceURI == null || namespaceURI.length() == 0 ) {
            writer.writeEmptyElement( localname );
        } else {
            if ( writer.getNamespaceContext().getPrefix( namespaceURI ) == null ) {
                String prefix = nsToPrefix.get( namespaceURI );
                if ( prefix != null ) {
                    writer.setPrefix( prefix, namespaceURI );
                } else {
                    LOG.warn( "No prefix for namespace '{}' configured. Depending on XMLStream auto-repairing.",
                              namespaceURI );
                }
            }
            writer.writeEmptyElement( namespaceURI, localname );
        }
    }

    private boolean isPropertyRequested( QName propName ) {
        // TODO compare names properly (different types)
        return ( propNames.isEmpty() || propNames.contains( propName ) ) || xlinkPropNames.containsKey( propName );
    }
}