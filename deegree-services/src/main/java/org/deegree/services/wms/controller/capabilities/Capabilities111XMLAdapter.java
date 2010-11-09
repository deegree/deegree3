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

package org.deegree.services.wms.controller.capabilities;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.cs.CRSRegistry.lookup;
import static org.deegree.services.wms.model.Dimension.formatDimensionValueList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.jaxb.main.AddressType;
import org.deegree.services.jaxb.main.KeywordsType;
import org.deegree.services.jaxb.main.LanguageStringType;
import org.deegree.services.jaxb.main.ServiceContactType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.services.wms.model.Dimension;
import org.deegree.services.wms.model.layers.Layer;
import org.slf4j.Logger;

/**
 * <code>Capabilities111XMLAdapter</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "logs problems with CRS when outputting 1.1.1 capabilities", trace = "logs stack traces")
public class Capabilities111XMLAdapter extends XMLAdapter {

    private static final Logger LOG = getLogger( Capabilities111XMLAdapter.class );

    private final String getUrl;

    private final String postUrl;

    private final ServiceIdentificationType identification;

    private final ServiceProviderType provider;

    private MapService service;

    private WMSController controller;

    /**
     * @param identification
     * @param provider
     * @param getUrl
     * @param postUrl
     * @param service
     * @param controller
     */
    public Capabilities111XMLAdapter( ServiceIdentificationType identification, ServiceProviderType provider,
                                      String getUrl, String postUrl, MapService service, WMSController controller ) {
        this.identification = identification;
        this.provider = provider;
        this.getUrl = getUrl;
        this.postUrl = postUrl;
        this.service = service;
        this.controller = controller;
    }

    /**
     * Writes out a 1.1.1 style capabilities document.
     * 
     * @param writer
     * @throws XMLStreamException
     */
    public void export( XMLStreamWriter writer )
                            throws XMLStreamException {

        String dtdrequest = getUrl + "?request=DTD";

        writer.writeDTD( "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"" + dtdrequest
                         + "\" [<!ELEMENT VendorSpecificCapabilities EMPTY>]>\n" );
        writer.writeStartElement( "WMT_MS_Capabilities" );
        writer.writeAttribute( "version", "1.1.1" );
        writer.writeAttribute( "updateSequence", "" + service.updateSequence );

        writeService( writer );

        writeCapability( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void writeCapability( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( "Capability" );

        writeRequest( writer );
        writer.writeStartElement( "Exception" );
        writeElement( writer, "Format", "application/vnd.ogc.se_xml" );
        writeElement( writer, "Format", "application/vnd.ogc.se_inimage" );
        writeElement( writer, "Format", "application/vnd.ogc.se_blank" );
        writer.writeEndElement();
        writeLayers( writer, service.getRootLayer() );

        writer.writeEndElement();
    }

    private void writeLayers( XMLStreamWriter writer, Layer layer )
                            throws XMLStreamException {
        if ( layer.getTitle() == null || !layer.isAvailable() ) {
            return;
        }

        writer.writeStartElement( "Layer" );
        if ( layer.isQueryable() ) {
            writer.writeAttribute( "queryable", "1" );
        }

        maybeWriteElement( writer, "Name", layer.getName() );
        writeElement( writer, "Title", layer.getTitle() );
        maybeWriteElement( writer, "Abstract", layer.getAbstract() );

        if ( !layer.getKeywords().isEmpty() ) {
            writer.writeStartElement( "KeywordList" );
            for ( LanguageStringType lanString : layer.getKeywords() ) {
                writeElement( writer, "Keyword", lanString.getValue() );
            }
            writer.writeEndElement();
        }

        for ( CRS crs : layer.getSrs() ) {
            writeElement( writer, "SRS", crs.getName() );
        }

        CoordinateSystem latlon;
        try {
            latlon = lookup( "CRS:84" );
            Envelope layerEnv = layer.getBbox();
            if ( layerEnv != null && layerEnv.getCoordinateDimension() >= 2 ) {
                Envelope bbox = new GeometryTransformer( latlon ).transform( layerEnv );
                writer.writeStartElement( "LatLonBoundingBox" );
                writer.writeAttribute( "minx", Double.toString( bbox.getMin().get0() ) );
                writer.writeAttribute( "miny", Double.toString( bbox.getMin().get1() ) );
                writer.writeAttribute( "maxx", Double.toString( bbox.getMax().get0() ) );
                writer.writeAttribute( "maxy", Double.toString( bbox.getMax().get1() ) );
                writer.writeEndElement();

                for ( CRS crs : layer.getSrs() ) {
                    if ( crs.getName().startsWith( "AUTO" ) ) {
                        continue;
                    }
                    Envelope envelope;
                    try {
                        if ( layerEnv.getCoordinateSystem() == null ) {
                            envelope = (Envelope) new GeometryTransformer( crs.getWrappedCRS() ).transform( layerEnv,
                                                                                                            latlon );
                        } else {
                            envelope = new GeometryTransformer( crs.getWrappedCRS() ).transform( layerEnv );
                        }
                    } catch ( IllegalArgumentException e ) {
                        LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                        continue;
                    } catch ( TransformationException e ) {
                        LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                        continue;
                    }

                    writer.writeStartElement( "BoundingBox" );
                    writer.writeAttribute( "SRS", crs.getName() );
                    writer.writeAttribute( "minx", Double.toString( envelope.getMin().get0() ) );
                    writer.writeAttribute( "miny", Double.toString( envelope.getMin().get1() ) );
                    writer.writeAttribute( "maxx", Double.toString( envelope.getMax().get0() ) );
                    writer.writeAttribute( "maxy", Double.toString( envelope.getMax().get1() ) );
                    writer.writeEndElement();
                }
            }
        } catch ( UnknownCRSException e ) {
            LOG.warn( "Cannot find: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( IllegalArgumentException e ) {
            LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( TransformationException e ) {
            LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }

        final Map<String, Dimension<?>> dims = layer.getDimensions();
        for ( Entry<String, Dimension<?>> entry : dims.entrySet() ) {
            Dimension<?> dim = entry.getValue();
            writer.writeStartElement( "Dimension" );
            writer.writeAttribute( "name", entry.getKey() );
            writer.writeAttribute( "units", dim.getUnits() == null ? "EPSG:4979" : dim.getUnits() );
            writer.writeAttribute( "unitSymbol", dim.getUnitSymbol() == null ? "" : dim.getUnitSymbol() );
            writer.writeEndElement();
        }

        for ( String name : dims.keySet() ) {
            Dimension<?> dim = dims.get( name );
            writer.writeStartElement( "Extent" );
            writer.writeAttribute( "name", name );
            if ( dim.getDefaultValue() != null ) {
                writer.writeAttribute( "default",
                                       formatDimensionValueList( dim.getDefaultValue(), "time".equals( name ) ) );
            }
            if ( dim.getNearestValue() ) {
                writer.writeAttribute( "nearestValue", "1" );
            }
            writer.writeCharacters( dim.getExtentAsString() );
            writer.writeEndElement();
        }

        Style def = service.getStyles().getDefault( layer.getName() );
        if ( def != null ) {
            if ( def.getName() != null && !def.getName().isEmpty() ) {
                writeStyle( writer, "default", def.getName(), service.getLegendSize( def ), layer.getName(),
                            def.getName() ); // TODO
                // title/description/whatever
            } else {
                writeStyle( writer, "default", "default", service.getLegendSize( def ), layer.getName(), def.getName() ); // TODO
                // title/description/whatever
            }
        }
        HashSet<Style> visited = new HashSet<Style>();
        for ( Style s : service.getStyles().getAll( layer.getName() ) ) {
            if ( visited.contains( s ) ) {
                continue;
            }
            visited.add( s );
            String name = s.getName();
            if ( name != null && !name.isEmpty() ) {
                writeStyle( writer, name, name, service.getLegendSize( s ), layer.getName(), name ); // TODO
                // title/description/whatever
            }
        }

        DoublePair hint = layer.getScaleHint();
        if ( hint.first != NEGATIVE_INFINITY || hint.second != POSITIVE_INFINITY ) {
            double fac = 0.00028;
            writer.writeStartElement( "ScaleHint" );
            writer.writeAttribute( "min", Double.toString( hint.first == NEGATIVE_INFINITY ? MIN_VALUE : hint.first
                                                                                                         * fac ) );
            writer.writeAttribute( "max", Double.toString( hint.second == POSITIVE_INFINITY ? MAX_VALUE : hint.second
                                                                                                          * fac ) );
            writer.writeEndElement();
        }

        for ( Layer l : new LinkedList<Layer>( layer.getChildren() ) ) {
            writeLayers( writer, l );
        }

        writer.writeEndElement();
    }

    private void writeStyle( XMLStreamWriter writer, String name, String title, Pair<Integer, Integer> legendSize,
                             String layerName, String styleName )
                            throws XMLStreamException {
        writer.writeStartElement( "Style" );
        writeElement( writer, "Name", name );
        writeElement( writer, "Title", title );
        writer.writeStartElement( "LegendURL" );
        writer.writeAttribute( "width", "" + legendSize.first );
        writer.writeAttribute( "height", "" + legendSize.second );
        writeElement( writer, "Format", "image/png" );
        writer.setPrefix( "xlink", XLNNS );
        writer.writeStartElement( "OnlineResource" );
        writer.writeAttribute( XLNNS, "type", "simple" );
        String style = styleName == null ? "" : ( "&style=" + styleName );
        writer.writeAttribute( XLNNS, "href", getUrl + "?request=GetLegendGraphic&version=1.1.1&service=WMS&layer="
                                              + layerName + style + "&format=image/png" );
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeDCP( XMLStreamWriter writer, boolean get, boolean post )
                            throws XMLStreamException {
        writer.writeStartElement( "DCPType" );
        writer.writeStartElement( "HTTP" );
        if ( get ) {
            writer.writeStartElement( "Get" );
            writer.writeStartElement( "OnlineResource" );
            writer.setPrefix( "xlink", XLNNS );
            writer.writeAttribute( XLNNS, "type", "simple" );
            writer.writeAttribute( XLNNS, "href", getUrl + "?" );
            writer.writeEndElement();
            writer.writeEndElement();
        }
        if ( post ) {
            writer.writeStartElement( "Post" );
            writer.writeStartElement( "OnlineResource" );
            writer.setPrefix( "xlink", XLNNS );
            writer.writeAttribute( XLNNS, "type", "simple" );
            writer.writeAttribute( XLNNS, "href", postUrl );
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeRequest( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( "Request" );

        writer.writeStartElement( "GetCapabilities" );
        writeElement( writer, "Format", "application/vnd.ogc.wms_xml" );
        writeDCP( writer, true, false );
        writer.writeEndElement();

        writer.writeStartElement( "GetMap" );
        writeImageFormats( writer );
        writeDCP( writer, true, true );
        writer.writeEndElement();

        writer.writeStartElement( "GetFeatureInfo" );
        writeInfoFormats( writer );
        writeDCP( writer, true, false );
        writer.writeEndElement();

        writer.writeStartElement( "GetLegendGraphic" );
        writeInfoFormats( writer );
        writeDCP( writer, true, false );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeImageFormats( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( String f : controller.supportedImageFormats ) {
            writeElement( writer, "Format", f );
        }
    }

    private void writeInfoFormats( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( String f : controller.supportedFeatureInfoFormats.keySet() ) {
            writeElement( writer, "Format", f );
        }
    }

    private void writeService( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( "Service" );

        writeElement( writer, "Name", "OGC:WMS" );

        List<String> titles = identification == null ? null : identification.getTitle();
        String title = ( titles != null && !titles.isEmpty() ) ? titles.get( 0 ) : "deegree 3 WMS";
        writeElement( writer, "Title", title );

        List<String> abstracts = identification == null ? null : identification.getAbstract();
        if ( abstracts != null && !abstracts.isEmpty() ) {
            writeElement( writer, "Abstract", abstracts.get( 0 ) );
        }

        List<KeywordsType> keywords = identification == null ? null : identification.getKeywords();
        if ( keywords != null && !keywords.isEmpty() ) {
            writer.writeStartElement( "KeywordList" );

            for ( KeywordsType key : keywords ) {
                for ( LanguageStringType lanString : key.getKeyword() ) {
                    writeElement( writer, "Keyword", lanString.getValue() );
                }
            }

            writer.writeEndElement();
        }

        writer.setPrefix( "xlink", XLNNS );
        writer.writeStartElement( "OnlineResource" );
        writer.writeAttribute( XLNNS, "type", "simple" );
        writer.writeAttribute( XLNNS, "href", getUrl );
        writer.writeEndElement();

        if ( provider != null ) {
            ServiceContactType contact = provider.getServiceContact();
            if ( contact != null ) {
                writer.writeStartElement( "ContactInformation" );

                if ( contact.getIndividualName() != null ) {
                    writer.writeStartElement( "ContactPersonPrimary" );
                    writeElement( writer, "ContactPerson", contact.getIndividualName() );
                    writeElement( writer, "ContactOrganization", provider.getProviderName() );
                    writer.writeEndElement();
                }

                maybeWriteElement( writer, "ContactPosition", contact.getPositionName() );
                AddressType addr = contact.getAddress();
                if ( addr != null ) {
                    writer.writeStartElement( "ContactAddress" );
                    writeElement( writer, "AddressType", "postal" );
                    for ( String s : addr.getDeliveryPoint() ) {
                        maybeWriteElement( writer, "Address", s );
                    }
                    writeElement( writer, "City", addr.getCity() );
                    writeElement( writer, "StateOrProvince", addr.getAdministrativeArea() );
                    writeElement( writer, "PostCode", addr.getPostalCode() );
                    writeElement( writer, "Country", addr.getCountry() );
                    writer.writeEndElement();
                }

                maybeWriteElement( writer, "ContactVoiceTelephone", contact.getPhone() );
                maybeWriteElement( writer, "ContactFacsimileTelephone", contact.getFacsimile() );
                for ( String email : contact.getElectronicMailAddress() ) {
                    maybeWriteElement( writer, "ContactElectronicMailAddress", email );
                }

                writer.writeEndElement();
            }

            if ( identification != null ) {
                maybeWriteElement( writer, "Fees", identification.getFees() );
                List<String> constr = identification.getAccessConstraints();
                if ( constr != null ) {
                    for ( String cons : constr ) {
                        maybeWriteElement( writer, "AccessConstraints", cons );
                    }
                }
            }

        }

        writeElement( writer, "Fees", "none" );
        writeElement( writer, "AccessConstraints", "none" );

        writer.writeEndElement();
    }

}
