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

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.commons.uom.Length;
import org.deegree.commons.uom.Measure;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.types.GenericCustomPropertyValue;
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
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.generic.GenericCustomPropertyWriter;
import org.deegree.gml.geometry.GML2GeometryWriter;
import org.deegree.gml.geometry.GML3GeometryWriter;
import org.deegree.gml.geometry.GMLGeometryWriter;
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

    private final Set<PropertyName> propNames = new HashSet<PropertyName>();

    // export all levels by default
    private int traverseXlinkDepth = -1;

    private int traverseXlinkExpiry;

    private boolean exportSf;

    /**
     * @param writer
     * @param outputCRS
     *            crs used for exported geometries, may be <code>null</code> (in that case, the crs of the geometries is
     *            used)
     */
    public GMLFeatureWriter( XMLStreamWriter writer, CRS outputCRS ) {
        this( GMLVersion.GML_31, writer, outputCRS, null, null, null, 0, -1, false );
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
     *            <code>http://localhost:8080/d3_wfs_lab/services?SERVICE=WFS&REQUEST=GetGmlObject&VERSION=1.1.0&TRAVERSEXLINKDEPTH=1&GMLOBJECTID={}</code>
     *            , the substring <code>{}</code> is replaced by the object id
     * @param requestedProps
     *            properties to be exported, may be <code>null</code> (export all properties)
     * @param traverseXlinkDepth
     * @param traverseXlinkExpiry
     * @param exportSfGeometries
     */
    public GMLFeatureWriter( GMLVersion version, XMLStreamWriter writer, CRS outputCRS, CoordinateFormatter formatter,
                             String referenceTemplate, PropertyName[] requestedProps, int traverseXlinkDepth,
                             int traverseXlinkExpiry, boolean exportSfGeometries ) {
        this.version = version;
        this.writer = writer;
        this.referenceTemplate = referenceTemplate;
        if ( requestedProps != null ) {
            for ( PropertyName propertyName : requestedProps ) {
                this.propNames.add( propertyName );
            }
        }
        this.traverseXlinkDepth = traverseXlinkDepth;
        this.traverseXlinkExpiry = traverseXlinkExpiry;

        gmlNs = version.getNamespace();
        if ( !version.equals( GMLVersion.GML_2 ) ) {
            geometryWriter = new GML3GeometryWriter( version, writer, outputCRS, formatter, exportSfGeometries,
                                                     exportedIds );
            fidAttr = new QName( gmlNs, "id" );
            gmlNull = "Null";
        } else {
            geometryWriter = new GML2GeometryWriter( writer, formatter, exportedIds );
            fidAttr = new QName( NULL_NS_URI, "fid" );
            gmlNull = "null";
        }

        // TODO
        this.exportSf = false;
    }

    public void export( Feature feature )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        export( feature, 0 );
    }

    /**
     * TODO merge with other schema location possibilities
     * 
     * @param col
     * @param noNamespaceSchemaLocation
     *            may be null
     * @throws XMLStreamException
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    public void export( FeatureCollection col, String noNamespaceSchemaLocation )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        LOG.debug( "Exporting generic feature collection." );
        writer.setPrefix( "gml", gmlNs );
        writer.writeStartElement( "FeatureCollection" );
        if ( noNamespaceSchemaLocation != null ) {
            writer.setPrefix( "xsi", XSINS );
            writer.writeAttribute( XSINS, "noNamespaceSchemaLocation", noNamespaceSchemaLocation );
        }

        writer.writeStartElement( gmlNs, "boundedBy" );
        Envelope fcEnv = col.getEnvelope();
        if ( fcEnv != null ) {
            geometryWriter.exportEnvelope( col.getEnvelope() );
        } else {
            writer.writeStartElement( gmlNs, gmlNull );
            writer.writeCharacters( "missing" );
            writer.writeEndElement();
        }
        writer.writeEndElement();
        for ( Feature f : col ) {
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
        writer.setPrefix( "gml", gmlNs );
        writer.writeStartElement( name.getNamespaceURI(), name.getLocalPart() );

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
                export( member, 0 );
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void export( Feature feature, int inlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( feature.getId() != null ) {
            exportedIds.add( feature.getId() );
        }
        if ( feature instanceof GenericFeatureCollection ) {
            LOG.debug( "Exporting generic feature collection." );
            writer.setPrefix( "gml", gmlNs );
            writer.writeStartElement( "FeatureCollection" );
            for ( Feature member : ( (FeatureCollection) feature ) ) {
                String memberFid = member.getId();
                writer.writeStartElement( gmlNs, "featureMember" );
                if ( memberFid != null && exportedIds.contains( memberFid ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + memberFid );
                } else {
                    export( member, inlineLevels + 1 );
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

            // writeStartElementWithNS( featureName.getNamespaceURI(), featureName.getLocalPart() );
            if ( feature.getId() != null ) {
                if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                    writer.writeAttribute( fidAttr.getLocalPart(), feature.getId() );
                } else {
                    writer.writeAttribute( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), feature.getId() );
                }
            }
            for ( Property<?> prop : feature.getProperties( version ) ) {
                // // hack to work around CITE 1.1.0 problems (BTW, export Envelopes?)
                // if (prop.getType() instanceof EnvelopePropertyType) {
                // continue;
                // }
                export( prop, inlineLevels );
            }
            writer.writeEndElement();
        }
    }

    /**
     * @param property
     * @param inlineLevels
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    protected void export( Property<?> property, int inlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        QName propName = property.getName();
        PropertyType<?> propertyType = property.getType();
        if ( propertyType.getMinOccurs() == 0 ) {
            LOG.debug( "Optional property '" + propName + "', checking if it is requested." );
            if ( !isPropertyRequested( propName ) ) {
                LOG.debug( "Skipping it." );
                return;
            }
        }

        // TODO check for GML 2 properties (gml:pointProperty, ...) and export as "app:gml2PointProperty" for GML 3

        Object value = property.getValue();
        if ( propertyType instanceof FeaturePropertyType ) {
            exportFeatureProperty( (FeaturePropertyType) propertyType, (Feature) value, inlineLevels );
        } else if ( propertyType instanceof SimplePropertyType<?> ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( value != null ) {
                writer.writeCharacters( value.toString() );
            }
            writer.writeEndElement();
        } else if ( propertyType instanceof GeometryPropertyType ) {
            Geometry gValue = (Geometry) value;
            if ( !exportSf && gValue.getId() != null && exportedIds.contains( gValue.getId() ) ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XLNNS, "href", "#" + gValue.getId() );
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                geometryWriter.export( (Geometry) value );
                writer.writeEndElement();
            }
        } else if ( propertyType instanceof CodePropertyType ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            CodeType codeType = (CodeType) value;
            if ( codeType.getCodeSpace() != null && codeType.getCodeSpace().length() > 0 )
                writer.writeAttribute( "codeSpace", codeType.getCodeSpace() );
            writer.writeCharacters( codeType.getCode() );
            writer.writeEndElement();
        } else if ( propertyType instanceof EnvelopePropertyType ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            if ( value != null ) {
                geometryWriter.exportEnvelope( (Envelope) value );
            } else {
                writer.writeStartElement( gmlNs, gmlNull );
                writer.writeCharacters( "missing" );
                writer.writeEndElement();
            }
            writer.writeEndElement();
        } else if ( propertyType instanceof LengthPropertyType ) {
            Length length = (Length) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            writer.writeAttribute( "uom", length.getUomUri() );
            writer.writeCharacters( String.valueOf( length.getValue() ) );
            writer.writeEndElement();
        } else if ( propertyType instanceof MeasurePropertyType ) {
            Measure measure = (Measure) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            writer.writeAttribute( "uom", measure.getUomUri() );
            writer.writeCharacters( String.valueOf( measure.getValue() ) );
            writer.writeEndElement();
        } else if ( propertyType instanceof StringOrRefPropertyType ) {
            StringOrRef stringOrRef = (StringOrRef) value;
            if ( stringOrRef.getString() == null || stringOrRef.getString().length() == 0 ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                if ( stringOrRef.getRef() != null ) {
                    writer.writeAttribute( XLNNS, "href", stringOrRef.getRef() );
                }
            } else {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );

                if ( stringOrRef.getRef() != null ) {
                    writer.writeAttribute( XLNNS, "href", stringOrRef.getRef() );
                }
                if ( stringOrRef.getString() != null ) {
                    writer.writeCharacters( stringOrRef.getString() );
                }
                writer.writeEndElement();
            }
        } else if ( propertyType instanceof CustomPropertyType ) {
            GenericCustomPropertyWriter.export( (GenericCustomPropertyValue) value, writer );
        }
    }

    private void exportFeatureProperty( FeaturePropertyType pt, Feature subFeature, int inlineLevels )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        QName propName = pt.getName();
        LOG.debug( "Exporting feature property '" + propName + "'" );

        if ( !( subFeature instanceof FeatureReference ) || ( (FeatureReference) subFeature ).isLocal() ) {
            // normal feature or local feature reference
            String subFid = subFeature.getId();
            if ( subFid == null ) {
                // no feature id -> must put it inline then
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeComment( "Inlined feature '" + subFid + "'" );
                export( subFeature, inlineLevels + 1 );
                writer.writeEndElement();
            } else {
                // has feature id
                if ( exportedIds.contains( subFid ) ) {
                    // already exported -> put a local xlink to an already exported feature instance
                    writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                    writer.writeAttribute( XLNNS, "href", "#" + subFid );
                    writer.writeComment( "Reference to feature '" + subFid + "'" );
                    writer.writeEndElement();
                } else {
                    // not exported yet
                    if ( ( traverseXlinkDepth > 0 && inlineLevels < traverseXlinkDepth ) || referenceTemplate == null
                         || traverseXlinkDepth == -1 ) {
                        // must be exported inline
                        exportedIds.add( subFeature.getId() );
                        writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                        writer.writeComment( "Inlined feature '" + subFid + "'" );
                        export( subFeature, inlineLevels + 1 );
                        writer.writeEndElement();
                    } else {
                        // must be exported by reference
                        writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                        String uri = referenceTemplate.replace( "{}", subFid );
                        writer.writeAttribute( XLNNS, "href", uri );
                        writer.writeComment( "Reference to feature '" + subFid + "'" );
                        writer.writeEndElement();
                    }
                }
            }
        } else {
            FeatureReference ref = (FeatureReference) subFeature;
            // remote feature reference
            if ( ( traverseXlinkDepth > 0 && inlineLevels < traverseXlinkDepth ) || referenceTemplate == null
                 || traverseXlinkDepth == -1 ) {
                // must be exported inline
                LOG.warn( "Inlining of remote feature references is not implemented yet." );
                writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XLNNS, "href", ref.getURI() );
                writer.writeComment( "Reference to remote feature '"
                                     + ref.getURI()
                                     + "' (should have been inlined, but inlining of remote features is not implemented yet." );
                writer.writeEndElement();
            } else {
                // must be exported by reference
                writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XLNNS, "href", ref.getURI() );
                writer.writeComment( "Reference to remote feature '" + ref.getURI() + "'" );
                writer.writeEndElement();
            }
        }
        //
        // if ( subFid != null && exportedIds.contains( subFid ) ) {
        // // put an xlink to an already exported feature instance
        // writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
        // writer.writeAttribute( XLNNS, "href", "#" + subFid );
        // writer.writeComment( "Reference to feature '" + subFid + "'" );
        // writer.writeEndElement();
        // } else {
        // if ( ( subFeature instanceof FeatureReference ) && !( (FeatureReference) subFeature ).isLocal() ) {
        // writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
        // writer.writeAttribute( XLNNS, "href", ( (FeatureReference) subFeature ).getHref() );
        // writer.writeComment( "Reference to feature '" + subFid + "'" );
        // writer.writeEndElement();
        // } else if ( referenceTemplate == null || subFid == null || traverseXlinkDepth == -1
        // || ( traverseXlinkDepth > 0 && ( inlineLevels < traverseXlinkDepth ) ) ) {
        // exportedIds.add( subFeature.getId() );
        // writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
        // writer.writeComment( "Inlined feature '" + subFid + "'" );
        // export( subFeature, inlineLevels + 1 );
        // writer.writeEndElement();
        // } else {
        // writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
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
        return propNames.size() == 0 || propNames.contains( propName );
    }
}
