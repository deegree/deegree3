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
package org.deegree.services.wcs.describecoverage;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.protocol.wcs.WCSConstants.VERSION_100;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_NS;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_PRE;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_SCHEMA;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.rangeset.ValueType;
import org.deegree.coverage.rangeset.Interval.Closure;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.cs.CRSRegistry;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wcs.WCSConstants;
import org.deegree.services.wcs.coverages.WCSCoverage;
import org.deegree.services.wcs.model.CoverageOptions;

/**
 * This is an XMLAdapter for the CoverageDescription of the WCS 1.0.0 spec.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CoverageDescription100XMLAdapter extends XMLAdapter {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( CoverageDescription100XMLAdapter.class );

    private static final String GML_PREFIX = "gml";

    private static final String GML_NS = "http://www.opengis.net/gml";

    /**
     * @param writer
     * @param coverages
     * @param updateSequence
     * @throws XMLStreamException
     */
    public static void export( XMLStreamWriter writer, List<WCSCoverage> coverages, int updateSequence )
                            throws XMLStreamException {
        writer.setDefaultNamespace( WCS_100_NS );
        // writer.setPrefix( "wcs", WCS_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xsi", XSINS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartElement( WCS_100_NS, "CoverageDescription" );
        writer.writeAttribute( XSINS, "schemaLocation", WCS_100_NS + " " + WCS_100_SCHEMA );
        writer.writeAttribute( "version", VERSION_100.toString() );
        writer.writeAttribute( "updateSequence", Integer.toString( updateSequence ) );

        for ( WCSCoverage coverage : coverages ) {
            exportCoverageOffering( writer, coverage );
        }

        writer.writeEndElement(); // CoverageDescription
        writer.writeEndDocument();
    }

    private static void exportCoverageOffering( XMLStreamWriter writer, WCSCoverage coverage )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "CoverageOffering" );
        exportBriefCoverageData( writer, coverage );
        exportDomainSet( writer, coverage );
        exportRangeSet( writer, coverage );
        exportSupportedCRSs( writer, coverage );
        exportSupportedFormats( writer, coverage.getCoverageOptions() );
        exportSupportedInterpolations( writer, coverage.getCoverageOptions() );
        writer.writeEndElement(); // CoverageOffering
    }

    private static void exportSupportedCRSs( XMLStreamWriter writer, WCSCoverage coverage )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "supportedCRSs" );

        CoverageOptions options = coverage.getCoverageOptions();
        for ( String crs : options.getCRSs() ) {
            writeElement( writer, WCS_100_NS, "requestResponseCRSs", crs );
        }
        String nativeCRS = coverage.getEnvelope().getCoordinateSystem().getName();
        writeElement( writer, WCS_100_NS, "nativeCRSs", nativeCRS );
        writer.writeEndElement(); // supportedCRSs
    }

    private static void exportSupportedFormats( XMLStreamWriter writer, CoverageOptions options )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "supportedFormats" );
        if ( options.getNativeFormat() != null ) {
            writer.writeAttribute( "nativeFormat", options.getNativeFormat() );
        }
        boolean hadGeoTIFF = false;
        for ( String format : options.getOutputFormats() ) {
            if ( "GeoTIFF".equals( format ) ) {
                hadGeoTIFF = true;
            }
            writeElement( writer, WCS_100_NS, "formats", format );
        }
        if ( !hadGeoTIFF ) {
            // needed according to 03-065r6 section 8.3.5 SupportedFormats
            writeElement( writer, WCS_100_NS, "formats", "GeoTIFF" );
        }

        writer.writeEndElement(); // supportedFormats
    }

    private static void exportSupportedInterpolations( XMLStreamWriter writer, CoverageOptions options )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "supportedInterpolations" );
        for ( InterpolationType interpolation : options.getInterpolations() ) {
            writeElement( writer, WCS_100_NS, "interpolationMethod",
                          WCSConstants.InterpolationMethod.map( interpolation ).getProtocolName( VERSION_100 ) );
        }
        writer.writeEndElement(); // supportedFormats
    }

    private static void exportDomainSet( XMLStreamWriter writer, WCSCoverage coverage )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "domainSet" );
        writer.writeStartElement( WCS_100_NS, "spatialDomain" );
        Envelope origEnv = coverage.getEnvelope();

        // rb: what about temporal domain sets?

        // first do default
        exportGMLEnvelope( writer, origEnv );

        for ( Envelope env : coverage.responseEnvelopes ) {
            if ( env != null ) {
                exportGMLEnvelope( writer, env );
            }
        }

        // export grid?
        // export polygon?

        writer.writeEndElement(); // spatialDomain
        writer.writeEndElement(); // domainSet
    }

    /**
     * Export the range set from the given coverage.
     * 
     * @param writer
     * @param coverage
     * @throws XMLStreamException
     */
    protected static void exportRangeSet( XMLStreamWriter writer, WCSCoverage coverage )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "rangeSet" );
        writer.writeStartElement( WCS_100_NS, "RangeSet" );
        writer.writeAttribute( "refSys", coverage.getEnvelope().getCoordinateSystem().getName() );
        RangeSet rs = coverage.getRangeSet();
        if ( rs == null ) {
            LOG.info( "No range sets defined for requested coverage, creating a default one from the coverage parameters." );
            writeElement( writer, WCS_100_NS, "name", coverage.getName() );
            writeElement( writer, WCS_100_NS, "label", coverage.getLabel() );
        } else {
            writeElement( writer, WCS_100_NS, "name", rs.getName() );
            writeElement( writer, WCS_100_NS, "label", rs.getLabel() );
            // write the axis descriptions
            exportAxisDescriptions( writer, rs.getAxisDescriptions() );
            exportNullValues( writer, rs.getNullValue() );
        }

        writer.writeEndElement(); // RangeSet
        writer.writeEndElement(); // rangeSet
    }

    /**
     * @param writer
     * @param nullValue
     * @throws XMLStreamException
     */
    protected static void exportNullValues( XMLStreamWriter writer, SingleValue<?> nullValue )
                            throws XMLStreamException {
        if ( nullValue != null ) {
            writer.writeStartElement( WCS_100_NS, "nullValues" );
            exportSingleValueType( writer, nullValue, "singleValue" );
            writer.writeEndElement();// WCS_100_NS, "nullValues" );
        }

    }

    /**
     * @param writer
     * @param singleValue
     * @param elementName
     * @throws XMLStreamException
     */
    protected static void exportSingleValueType( XMLStreamWriter writer, SingleValue<?> singleValue, String elementName )
                            throws XMLStreamException {
        if ( singleValue != null ) {
            String type = ( singleValue.type == ValueType.Void ) ? ValueType.String.toString()
                                                                : singleValue.type.toString();
            writeElement( writer, WCS_100_NS, elementName, singleValue.value.toString(), WCS_100_NS, WCS_100_PRE,
                          "type", type );
        }
    }

    /**
     * @param writer
     * @param axisDescriptions
     * @throws XMLStreamException
     */
    protected static void exportAxisDescriptions( XMLStreamWriter writer, List<AxisSubset> axisDescriptions )
                            throws XMLStreamException {
        if ( !axisDescriptions.isEmpty() ) {
            for ( AxisSubset ass : axisDescriptions ) {
                if ( ass != null ) {
                    writer.writeStartElement( WCS_100_NS, "axisDescription" );
                    writer.writeStartElement( WCS_100_NS, "AxisDescription" );
                    writeElement( writer, WCS_100_NS, "name", ass.getName() );
                    writeElement( writer, WCS_100_NS, "label", ass.getLabel() );

                    writer.writeStartElement( WCS_100_NS, "values" );

                    exportIntervals( writer, ass.getIntervals() );
                    if ( ass.getSingleValues() != null ) {
                        for ( SingleValue<?> sv : ass.getSingleValues() ) {
                            exportSingleValueType( writer, sv, "singleValue" );
                        }
                    }
                    writer.writeEndElement();// WCS_100_NS, "values" );

                    writer.writeEndElement();// WCS_100_NS, "AxisDescription" );
                    writer.writeEndElement();// WCS_100_NS, "axisDescription" );
                }
            }
        }
    }

    /**
     * Export a list of intervals.
     * 
     * @param writer
     * @param intervals
     * @throws XMLStreamException
     */
    protected static void exportIntervals( XMLStreamWriter writer, List<Interval<?, ?>> intervals )
                            throws XMLStreamException {
        if ( intervals != null && !intervals.isEmpty() ) {
            for ( Interval<?, ?> interval : intervals ) {
                exportInterval( writer, interval );
            }
        }

    }

    /**
     * @param writer
     * @param interval
     * @throws XMLStreamException
     */
    protected static void exportInterval( XMLStreamWriter writer, Interval<?, ?> interval )
                            throws XMLStreamException {
        if ( interval != null ) {
            writer.writeStartElement( WCS_100_NS, "interval" );
            if ( interval.getSemantic() != null ) {
                writer.writeAttribute( WCS_100_PRE, WCS_100_NS, "semantic", interval.getSemantic() );
            }

            writer.writeAttribute( "atomic", interval.isAtomic() ? "true" : "false" );
            Closure closure = interval.getClosure();
            writer.writeAttribute( WCS_100_PRE, WCS_100_NS, "closure", closure.name().replaceAll( "_", "-" ) );

            exportSingleValueType( writer, interval.getMin(), "min" );
            exportSingleValueType( writer, interval.getMax(), "max" );
            exportSingleValueType( writer, interval.getSpacing(), "res" );

            writer.writeEndElement();// WCS_100_NS, "interval" );
        }
    }

    /**
     * Writes common base data of the coverage like name, lable, lonLatEnvelope.
     * 
     * @param writer
     * @param coverage
     * @throws XMLStreamException
     */
    public static void exportBriefCoverageData( XMLStreamWriter writer, WCSCoverage coverage )
                            throws XMLStreamException {

        // metadataLink [0,n]
        // -> some metadata from
        // -> @gml:AssociationAttributeGroup
        // -> @about optional
        // -> gml:_MetaData element [0,n]
        // description [0,1]
        // name [1]
        writeElement( writer, WCS_100_NS, "name", coverage.getName() );
        // -> @codeSpace optional
        // label [1]
        writeElement( writer, WCS_100_NS, "label", coverage.getLabel() );
        // keywords [0,n]
        // -> keyword [1, n]
        // -> type [0,1]

        exportLonLatEnvelope( writer, coverage.getEnvelope() );

        // keywords [0,n]
        // -> keyword [1, n]
        // -> type [0,1]

    }

    private static void exportLonLatEnvelope( XMLStreamWriter writer, Envelope envelope )
                            throws XMLStreamException {
        try {
            CoordinateSystem wgs84 = CRSRegistry.lookup( "EPSG:4326" );
            GeometryTransformer transformer = new GeometryTransformer( wgs84 );
            Envelope lonLatEnv = (Envelope) transformer.transform( envelope );

            writer.writeStartElement( WCS_100_NS, "lonLatEnvelope" );
            // @srsName urn:ogc:def:crs:OGC:1.3:CRS84
            writer.writeAttribute( "srsName", "urn:ogc:def:crs:OGC:1.3:CRS84" );
            exportGMLPos( writer, lonLatEnv.getMin().get0(), lonLatEnv.getMin().get1() );
            exportGMLPos( writer, lonLatEnv.getMax().get0(), lonLatEnv.getMax().get1() );
            writer.writeEndElement(); // lonLatEnvelope
        } catch ( UnknownCRSException e ) {
            e.printStackTrace();
            return;
        } catch ( TransformationException e ) {
            e.printStackTrace();
            return;
        }
    }

    private static void exportGMLPos( XMLStreamWriter writer, double... values )
                            throws XMLStreamException {
        writer.writeStartElement( GML_NS, "pos" );
        writer.writeAttribute( "dimension", Integer.toString( values.length ) );
        writer.writeCharacters( ArrayUtils.join( " ", values ) );
        writer.writeEndElement(); // pos
    }

    private static void exportGMLEnvelope( XMLStreamWriter writer, Envelope envelope )
                            throws XMLStreamException {
        writer.writeStartElement( GML_NS, "Envelope" );
        writer.writeAttribute( "srsName", envelope.getCoordinateSystem().getName() );

        exportGMLPos( writer, envelope.getMin().get0(), envelope.getMin().get1() );
        exportGMLPos( writer, envelope.getMax().get0(), envelope.getMax().get1() );
        writer.writeEndElement(); // Envelope
    }

}
