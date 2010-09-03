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
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Length;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.stax.StAXExportingHelper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.Property;
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
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML2GeometryWriter;
import org.deegree.gml.geometry.GML3GeometryWriter;
import org.deegree.gml.geometry.GMLGeometryWriter;
import org.deegree.protocol.wfs.getfeature.XLinkPropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based writer for GML-encoded features and feature collections.
 * <p>
 * <h4>XLink handling</h4>
 * This implementation is aware of xlinks (local as well as remote) and allows to set the
 * <code>traverseXlinkDepth</code> parameter, which controls the number of feature levels that are exported inline.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
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

    private final String referenceTemplate;

    // TODO handle properties that are more complex XPath-expressions
    private final Set<QName> propNames = new HashSet<QName>();

    // TODO handle properties that are more complex XPath-expressions
    private final Map<QName, XLinkPropertyName> xlinkPropNames = new HashMap<QName, XLinkPropertyName>();

    // export all levels by default
    private int traverseXlinkDepth = -1;

    private int traverseXlinkExpiry;

    private boolean exportSf;

    private boolean outputGeometries = true;

    /**
     * @param writer
     * @param outputCRS
     *            crs used for exported geometries, may be <code>null</code> (in that case, the crs of the geometries is
     *            used)
     */
    public GMLFeatureWriter( XMLStreamWriter writer, CRS outputCRS ) {
        this( GMLVersion.GML_31, writer, outputCRS, null, null, null, 0, -1, null, false );
    }

    /**
     * @param version
     *            GML version of the output, must not be <code>null</code>
     * @param writer
     * @param outputCRS
     *            crs used for exported geometries, may be <code>null</code> (in this case, the original crs of the
     *            geometries is used)
     * @param formatter
     *            formatter to use for exporting coordinates, e.g. to limit the number of decimal places, may be
     *            <code>null</code> (use 5 decimal places)
     * @param referenceTemplate
     *            URI template used to create references to local objects, e.g.
     * 
     *            <code>http://localhost:8080/d3_wfs_lab/services?SERVICE=WFS&REQUEST=GetGmlObject&VERSION=1.1.0&TRAVERSEXLINKDEPTH=1&GMLOBJECTID={}</code>
     *            , the substring <code>{}</code> is replaced by the object id
     * @param requestedProps
     *            properties to be exported, may be <code>null</code> (export all properties)
     * @param traverseXlinkDepth
     * @param traverseXlinkExpiry
     * @param xlinkProps
     * @param exportSfGeometries
     * @param outputGeometries
     *            whether to output optional geometry properties
     */
    public GMLFeatureWriter( GMLVersion version, XMLStreamWriter writer, CRS outputCRS, CoordinateFormatter formatter,
                             String referenceTemplate, PropertyName[] requestedProps, int traverseXlinkDepth,
                             int traverseXlinkExpiry, XLinkPropertyName[] xlinkProps, boolean exportSfGeometries,
                             boolean outputGeometries ) {
        this( version, writer, outputCRS, formatter, referenceTemplate, requestedProps, traverseXlinkDepth,
              traverseXlinkExpiry, xlinkProps, exportSfGeometries );
        this.outputGeometries = outputGeometries;
    }

    /**
     * @param version
     *            GML version of the output, must not be <code>null</code>
     * @param writer
     * @param outputCRS
     *            crs used for exported geometries, may be <code>null</code> (in this case, the original crs of the
     *            geometries is used)
     * @param formatter
     *            formatter to use for exporting coordinates, e.g. to limit the number of decimal places, may be
     *            <code>null</code> (use 5 decimal places)
     * @param referenceTemplate
     *            URI template used to create references to local objects, e.g.
     * 
     *            <code>http://localhost:8080/d3_wfs_lab/services?SERVICE=WFS&REQUEST=GetGmlObject&VERSION=1.1.0&TRAVERSEXLINKDEPTH=1&GMLOBJECTID={}</code>
     *            , the substring <code>{}</code> is replaced by the object id
     * @param requestedProps
     *            properties to be exported, may be <code>null</code> (export all properties)
     * @param traverseXlinkDepth
     * @param traverseXlinkExpiry
     * @param xlinkProps
     * @param exportSfGeometries
     */
    public GMLFeatureWriter( GMLVersion version, XMLStreamWriter writer, CRS outputCRS, CoordinateFormatter formatter,
                             String referenceTemplate, PropertyName[] requestedProps, int traverseXlinkDepth,
                             int traverseXlinkExpiry, XLinkPropertyName[] xlinkProps, boolean exportSfGeometries ) {
        this.version = version;
        this.writer = writer;
        this.referenceTemplate = referenceTemplate;
        if ( requestedProps != null ) {
            for ( PropertyName propertyName : requestedProps ) {
                // TODO what about non-simple property names
                QName qName = propertyName.getAsQName();
                if ( qName != null ) {
                    this.propNames.add( qName );
                }
            }
        }
        this.traverseXlinkDepth = traverseXlinkDepth;
        this.traverseXlinkExpiry = traverseXlinkExpiry;
        if ( xlinkProps != null ) {
            for ( XLinkPropertyName xlinkProp : xlinkProps ) {
                // TODO what about non-simple property names
                QName qName = xlinkProp.getPropertyName().getAsQName();
                if ( qName != null ) {
                    this.xlinkPropNames.put( qName, xlinkProp );
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

        // TODO
        this.exportSf = false;
    }

    public void export( Feature feature )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        export( feature, 0, traverseXlinkDepth );
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

        writer.setPrefix( "gml", gmlNs );
        writer.setPrefix( "wfs", WFS_NS );
        writer.writeStartElement( WFS_NS, "FeatureCollection" );

        if ( fc.getId() != null ) {
            if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                writer.writeAttribute( fidAttr.getLocalPart(), fc.getId() );
            } else {
                writer.writeAttribute( "gml", fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), fc.getId() );
            }
        }

        if ( noNamespaceSchemaLocation != null ) {
            writer.setPrefix( "xsi", XSINS );
            writer.writeAttribute( XSINS, "noNamespaceSchemaLocation", noNamespaceSchemaLocation );
        }
        if ( bindings != null && !bindings.isEmpty() ) {
            writer.setPrefix( "xsi", XSINS );
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
     * @param fc
     * @param name
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void export( FeatureCollection fc, QName name )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        LOG.debug( "Exporting feature collection with explicit name." );

        if ( fc.getId() != null ) {
            exportedIds.add( fc.getId() );
        }

        writer.setPrefix( "gml", gmlNs );
        writer.writeStartElement( name.getNamespaceURI(), name.getLocalPart() );

        if ( fc.getId() != null ) {
            if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                writer.writeAttribute( fidAttr.getLocalPart(), fc.getId() );
            } else {
                writer.writeAttribute( "gml", fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), fc.getId() );
            }
        }

        // gml:boundedBy (mandatory)
        Envelope fcEnv = fc.getEnvelope();
        writeStartElementWithNS( gmlNs, "boundedBy" );
        if ( fcEnv != null ) {
            geometryWriter.exportEnvelope( fc.getEnvelope() );
        } else {
            writer.writeStartElement( gmlNs, gmlNull );
            writer.writeCharacters( "missing" );
            writer.writeEndElement();
        }
        writer.writeEndElement();

        for ( Feature member : fc ) {
            String memberFid = member.getId();
            writer.writeStartElement( gmlNs, "featureMember" );
            if ( memberFid != null && exportedIds.contains( memberFid ) ) {
                writer.writeAttribute( XLNNS, "href", "#" + memberFid );
            } else {
                export( member, 0, traverseXlinkDepth );
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void export( Feature feature, int currentLevel, int maxInlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( feature.getId() != null ) {
            exportedIds.add( feature.getId() );
        }
        if ( feature instanceof GenericFeatureCollection ) {
            LOG.debug( "Exporting generic feature collection." );
            writer.setPrefix( "gml", gmlNs );
            writer.writeStartElement( "FeatureCollection" );
            if ( feature.getId() != null ) {
                if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                    writer.writeAttribute( fidAttr.getLocalPart(), feature.getId() );
                } else {
                    writer.writeAttribute( "gml", fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), feature.getId() );
                }
            }
            for ( Feature member : ( (FeatureCollection) feature ) ) {
                String memberFid = member.getId();
                writer.writeStartElement( gmlNs, "featureMember" );
                if ( memberFid != null && exportedIds.contains( memberFid ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + memberFid );
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
            String prefix = featureName.getPrefix() == null ? "app" : featureName.getPrefix();
            if ( namespaceURI == null || namespaceURI.length() == 0 ) {
                writer.writeStartElement( localName );
            } else {
                writer.writeStartElement( prefix, localName, namespaceURI );
            }

            if ( feature.getId() != null ) {
                if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                    writer.writeAttribute( fidAttr.getLocalPart(), feature.getId() );
                } else {
                    writer.writeAttribute( "gml", fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), feature.getId() );
                }
            }
            for ( Property prop : feature.getProperties( version ) ) {
                if ( currentLevel == 0 ) {
                    maxInlineLevels = getInlineLevels( prop );
                }
                export( prop, currentLevel, maxInlineLevels );
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

    /**
     * @param property
     * @param currentLevel
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    protected void export( Property property, int currentLevel, int maxInlineLevels )
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

        // TODO check for GML 2 properties (gml:pointProperty, ...) and export
        // as "app:gml2PointProperty" for GML 3
        Object value = property.getValue();
        if ( propertyType instanceof FeaturePropertyType ) {
            if ( property.isNilled() ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XSINS, "nil", "true" );
            } else {
                exportFeatureProperty( (FeaturePropertyType) propertyType, (Feature) value, currentLevel,
                                       maxInlineLevels );
            }
        } else if ( propertyType instanceof SimplePropertyType ) {
            if ( property.isNilled() ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XSINS, "nil", "true" );
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
                writer.writeAttribute( XSINS, "nil", "true" );
            } else {
                Geometry gValue = (Geometry) value;
                if ( !exportSf && gValue.getId() != null && exportedIds.contains( gValue.getId() ) ) {
                    writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                    writer.writeAttribute( XLNNS, "href", "#" + gValue.getId() );
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
                writer.writeAttribute( XSINS, "nil", "true" );
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
                writer.writeAttribute( XSINS, "nil", "true" );
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( value != null ) {
                    geometryWriter.exportEnvelope( (Envelope) value );
                } else {
                    writer.writeStartElement( gmlNs, gmlNull );
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
                    writer.writeAttribute( XSINS, "nil", "true" );
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
                    writer.writeAttribute( XLNNS, "href", stringOrRef.getRef() );
                }
                if ( property.isNilled() ) {
                    writer.writeAttribute( XSINS, "nil", "true" );
                }
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( property.isNilled() ) {
                    writer.writeAttribute( XSINS, "nil", "true" );
                }

                if ( stringOrRef.getRef() != null ) {
                    writer.writeAttribute( XLNNS, "href", stringOrRef.getRef() );
                }
                if ( !property.isNilled() && stringOrRef.getString() != null ) {
                    writer.writeCharacters( stringOrRef.getString() );
                }
                writer.writeEndElement();
            }
        } else if ( propertyType instanceof CustomPropertyType ) {
            if ( property.isNilled() ) {
                writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XSINS, "nil", "true" );
                // TODO make sure that only attributes are exported and nothing else
                export( (TypedObjectNode) property.getValue(), currentLevel, maxInlineLevels );
                writer.writeEndElement();
            } else {
                writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                export( (TypedObjectNode) property.getValue(), currentLevel, maxInlineLevels );
                writer.writeEndElement();
            }
        }
    }

    private void export( TypedObjectNode genericXML, int currentLevel, int maxInlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( genericXML instanceof GMLObject ) {
            if ( genericXML instanceof Feature ) {
                export( (Feature) genericXML, currentLevel, maxInlineLevels );
            } else if ( genericXML instanceof Geometry ) {
                geometryWriter.export( (Geometry) genericXML );
            } else {
                throw new UnsupportedOperationException();
            }
        } else if ( genericXML instanceof GenericXMLElement ) {
            GenericXMLElement xmlContent = (GenericXMLElement) genericXML;
            QName elName = xmlContent.getName();
            writer.writeStartElement( elName.getPrefix(), elName.getLocalPart(), elName.getNamespaceURI() );
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
        } else if ( genericXML instanceof GenericXMLElementContent ) {
            GenericXMLElementContent xmlContent = (GenericXMLElementContent) genericXML;
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
        } else if ( genericXML instanceof PrimitiveValue ) {
            writer.writeCharacters( ( (PrimitiveValue) genericXML ).getAsText() );
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
                // no feature id -> no other chance than putting it inline then
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeComment( "Inlined feature '" + subFid + "'" );
                export( subFeature, currentLevel + 1, maxInlineLevels );
                writer.writeEndElement();
            } else {
                // has feature id
                if ( exportedIds.contains( subFid ) ) {
                    // already exported -> put a local xlink to an already
                    // exported feature instance
                    writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                    writer.writeAttribute( XLNNS, "href", "#" + subFid );
                } else {
                    // not exported yet
                    if ( ( maxInlineLevels > 0 && currentLevel < maxInlineLevels ) || referenceTemplate == null
                         || maxInlineLevels == -1 ) {
                        // must be exported inline
                        exportedIds.add( subFeature.getId() );
                        writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                        writer.writeComment( "Inlined feature '" + subFid + "'" );
                        export( subFeature, currentLevel + 1, maxInlineLevels );
                        writer.writeEndElement();
                    } else {
                        // must be exported by reference
                        writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                        String uri = referenceTemplate.replace( "{}", subFid );
                        writer.writeAttribute( XLNNS, "href", uri );
                    }
                }
            }
        } else {
            FeatureReference ref = (FeatureReference) subFeature;
            // remote feature reference
            if ( ( maxInlineLevels > 0 && currentLevel < maxInlineLevels ) || referenceTemplate == null
                 || maxInlineLevels == -1 ) {
                String uri = ref.getURI();
                try {
                    new URL( uri );
                    throw new UnsupportedOperationException(
                                                             "Inlining of remote feature references is not implemented yet." );
                    // LOG.warn( "Inlining of remote feature references is not implemented yet." );
                    // writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                    // writer.writeAttribute( XLNNS, "href", ref.getURI() );
                    // writer.writeComment( "Reference to remote feature '"
                    // + ref.getURI()
                    // + "' (should have been inlined, but inlining of remote features is not implemented yet)." );
                    // writer.writeEndElement();
                } catch ( MalformedURLException e ) {
                    LOG.warn( "Not inlining remote feature reference -- not a valid URI." );
                    writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                    writer.writeAttribute( XLNNS, "href", ref.getURI() );
                }
            } else {
                // must be exported by reference
                writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XLNNS, "href", ref.getURI() );
            }
        }
        //
        // if ( subFid != null && exportedIds.contains( subFid ) ) {
        // // put an xlink to an already exported feature instance
        // writer.writeStartElement( propName.getNamespaceURI(),
        // propName.getLocalPart() );
        // writer.writeAttribute( XLNNS, "href", "#" + subFid );
        // writer.writeComment( "Reference to feature '" + subFid + "'" );
        // writer.writeEndElement();
        // } else {
        // if ( ( subFeature instanceof FeatureReference ) && !(
        // (FeatureReference) subFeature ).isLocal() ) {
        // writer.writeStartElement( propName.getNamespaceURI(),
        // propName.getLocalPart() );
        // writer.writeAttribute( XLNNS, "href", ( (FeatureReference) subFeature
        // ).getHref() );
        // writer.writeComment( "Reference to feature '" + subFid + "'" );
        // writer.writeEndElement();
        // } else if ( referenceTemplate == null || subFid == null ||
        // traverseXlinkDepth == -1
        // || ( traverseXlinkDepth > 0 && ( inlineLevels < traverseXlinkDepth )
        // ) ) {
        // exportedIds.add( subFeature.getId() );
        // writeStartElementWithNS( propName.getNamespaceURI(),
        // propName.getLocalPart() );
        // writer.writeComment( "Inlined feature '" + subFid + "'" );
        // export( subFeature, inlineLevels + 1 );
        // writer.writeEndElement();
        // } else {
        // writer.writeStartElement( propName.getNamespaceURI(),
        // propName.getLocalPart() );
        // String uri = referenceTemplate.replace( "{}", subFid );
        // writer.writeAttribute( XLNNS, "href", uri );
        // writer.writeComment( "Reference to feature '" + subFid + "'" );
        // writer.writeEndElement();
        // }
        // }
    }

    private void writeStartElementWithNS( String namespaceURI, String localname )
                            throws XMLStreamException {
        if ( namespaceURI == null || namespaceURI.length() == 0 ) {
            writer.writeStartElement( localname );
        } else {
            writer.writeStartElement( namespaceURI, localname );
        }
    }

    private void writeEmptyElementWithNS( String namespaceURI, String localname )
                            throws XMLStreamException {
        if ( namespaceURI == null || namespaceURI.length() == 0 ) {
            writer.writeEmptyElement( localname );
        } else {
            writer.writeEmptyElement( namespaceURI, localname );
        }
    }

    public boolean isExported( String memberFid ) {
        return exportedIds.contains( memberFid );
    }

    private boolean isPropertyRequested( QName propName ) {
        // TODO compare names properly (different types)
        return ( propNames.size() == 0 || propNames.contains( propName ) ) || xlinkPropNames.containsKey( propName );
    }
}
