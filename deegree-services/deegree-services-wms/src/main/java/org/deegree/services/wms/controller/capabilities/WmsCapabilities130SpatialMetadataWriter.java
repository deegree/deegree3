//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.cs.persistence.CRSManager.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.services.wms.controller.WMSController130;
import org.slf4j.Logger;

/**
 * Responsible for writing out envelopes and crs.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class WmsCapabilities130SpatialMetadataWriter {

    private static final Logger LOG = getLogger( WmsCapabilities130SpatialMetadataWriter.class );

    static void writeSrsAndEnvelope( XMLStreamWriter writer, List<ICRS> crsList, Envelope layerEnv )
                            throws XMLStreamException {
        writeSrs( crsList, writer );

        ICRS latlon;
        try {
            latlon = lookup( "CRS:84" );
            if ( layerEnv != null && layerEnv.getCoordinateDimension() >= 2 ) {
                Envelope bbox = new GeometryTransformer( latlon ).transform( layerEnv );
                writer.writeStartElement( WMSNS, "EX_GeographicBoundingBox" );
                Point min = bbox.getMin();
                Point max = bbox.getMax();
                if ( min.equals( max ) ) {
                    // TODO uncomment this once it's implemented
                    min = new DefaultPoint( min.getId(), min.getCoordinateSystem(), min.getPrecision(),
                                            new double[] { min.get0() - 0.0001, min.get1() - 0.0001 } );
                    // bbox = (Envelope) bbox.getBuffer( 0.0001 ); // should be ok to just use the same value for all
                    // crs
                }
                writeElement( writer, WMSNS, "westBoundLongitude", min.get0() + "" );
                writeElement( writer, WMSNS, "eastBoundLongitude", max.get0() + "" );
                writeElement( writer, WMSNS, "southBoundLatitude", min.get1() + "" );
                writeElement( writer, WMSNS, "northBoundLatitude", max.get1() + "" );
                writer.writeEndElement();

                for ( ICRS crs : crsList ) {
                    if ( crs.getAlias().startsWith( "AUTO" ) ) {
                        continue;
                    }
                    // try {
                    // crs.getWrappedCRS();
                    // } catch ( UnknownCRSException e ) {
                    // LOG.warn( "Cannot find: {}", e.getLocalizedMessage() );
                    // LOG.trace( "Stack trace:", e );
                    // continue;
                    // }
                    Envelope envelope;
                    ICRS srs = crs;
                    try {
                        Envelope src = layerEnv;
                        GeometryTransformer transformer = new GeometryTransformer( srs );
                        if ( src.getCoordinateSystem() == null ) {
                            envelope = transformer.transform( layerEnv, latlon );
                        } else {
                            envelope = transformer.transform( layerEnv );
                        }
                    } catch ( Throwable e ) {
                        LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                        continue;
                    }

                    writer.writeStartElement( WMSNS, "BoundingBox" );
                    writer.writeAttribute( "CRS", crs.getAlias() );

                    min = envelope.getMin();
                    max = envelope.getMax();
                    if ( min.equals( max ) ) {
                        // TODO uncomment this once it's implemented
                        min = new DefaultPoint( min.getId(), min.getCoordinateSystem(), min.getPrecision(),
                                                new double[] { min.get0() - 0.0001, min.get1() - 0.0001 } );
                        // bbox = (Envelope) bbox.getBuffer( 0.0001 ); // should be ok to just use the same value for
                        // all
                        // crs
                    }

                    // check for srs with northing as first axis
                    // try {
                    srs = WMSController130.getCRS( crs.getAlias() );
                    // } catch ( UnknownCRSException e ) {
                    // // may fail if CRS is determined eg. from .prj
                    // LOG.warn( "Cannot find: {}", e.getLocalizedMessage() );
                    // LOG.trace( "Stack trace:", e );
                    // }
                    // switch ( srs.getAxis()[0].getOrientation() ) {
                    // case Axis.AO_NORTH:
                    // writer.writeAttribute( "miny", Double.toString( min.get0() ) );
                    // writer.writeAttribute( "minx", Double.toString( min.get1() ) );
                    // writer.writeAttribute( "maxy", Double.toString( max.get0() ) );
                    // writer.writeAttribute( "maxx", Double.toString( max.get1() ) );
                    // break;
                    // default:
                    writer.writeAttribute( "minx", Double.toString( min.get0() ) );
                    writer.writeAttribute( "miny", Double.toString( min.get1() ) );
                    writer.writeAttribute( "maxx", Double.toString( max.get0() ) );
                    writer.writeAttribute( "maxy", Double.toString( max.get1() ) );
                    // }
                    writer.writeEndElement();
                }
            }
        } catch ( Throwable e ) {
            LOG.warn( "Cannot transform: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }

    static void writeSrs( List<ICRS> crsList, XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( ICRS crs : crsList ) {
            if ( crs.getAlias().startsWith( "AUTO" ) ) {
                writeElement( writer, WMSNS, "CRS", crs.getAlias().replace( "AUTO", "AUTO2" ) );
            } else {
                writeElement( writer, WMSNS, "CRS", crs.getAlias() );
            }
        }
    }

}
