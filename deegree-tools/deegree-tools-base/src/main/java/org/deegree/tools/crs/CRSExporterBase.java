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

package org.deegree.tools.crs;

import static org.deegree.commons.xml.CommonNamespaces.CRSNS;
import static org.deegree.cs.utilities.ProjectionUtils.EPS11;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSResource;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IEllipsoid;
import org.deegree.cs.components.IGeodeticDatum;
import org.deegree.cs.components.IPrimeMeridian;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.ICompoundCRS;
import org.deegree.cs.coordinatesystems.IGeocentricCRS;
import org.deegree.cs.coordinatesystems.IGeographicCRS;
import org.deegree.cs.coordinatesystems.IProjectedCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.coordinatesystems.CRS.CRSType;
import org.deegree.cs.projections.IProjection;
import org.deegree.cs.projections.azimuthal.IStereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.ITransverseMercator;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.polynomial.PolynomialTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSExporter</code> exports to the old version format (no version attribute).
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about the currently exported coordinate system.")
public class CRSExporterBase {

    private static Logger LOG = LoggerFactory.getLogger( CRSExporterBase.class );

    public void export( StringBuilder sb, List<ICRS> crsToExport ) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter( out );

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );

        try {
            XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );
            export( crsToExport, xmlWriter );

            sb.append( out.toString( Charset.defaultCharset().displayName() ) );
        } catch ( UnsupportedEncodingException e ) {
            LOG.error( e.getLocalizedMessage(), e );
        } catch ( XMLStreamException e ) {
            LOG.error( "Error while exporting the coordinates: " + e.getLocalizedMessage(), e );
        }

    }

    /**
     * Export the given list of ICoordinateSystems into the crs-definition format.
     * 
     * 
     * @param crsToExport
     * @param xmlWriter
     *            to write the definitions to.
     * @throws XMLStreamException
     *             if an error occurred while exporting
     */
    public void export( List<ICRS> crsToExport, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( crsToExport != null ) {
            if ( crsToExport.size() != 0 ) {
                LOG.debug( "Trying to export: " + crsToExport.size() + " coordinate systems." );

                // LinkedList<String> exportedIDs = new LinkedList<String>();
                Set<IEllipsoid> ellipsoids = new HashSet<IEllipsoid>();
                Set<IGeodeticDatum> datums = new HashSet<IGeodeticDatum>();
                Set<IGeocentricCRS> geocentrics = new HashSet<IGeocentricCRS>();
                Set<IGeographicCRS> geographics = new HashSet<IGeographicCRS>();
                Set<IProjectedCRS> projecteds = new HashSet<IProjectedCRS>();
                Set<ICompoundCRS> compounds = new HashSet<ICompoundCRS>();
                Set<IPrimeMeridian> primeMeridians = new HashSet<IPrimeMeridian>();
                Set<Helmert> wgs84s = new HashSet<Helmert>();

                for ( ICRS crs : crsToExport ) {
                    if ( crs != null ) {
                        IGeodeticDatum d = (GeodeticDatum) crs.getDatum();
                        datums.add( d );
                        ellipsoids.add( d.getEllipsoid() );

                        final CRSType type = crs.getType();
                        switch ( type ) {
                        case COMPOUND:
                            compounds.add( (CompoundCRS) crs );
                            break;
                        case GEOCENTRIC:
                            geocentrics.add( (GeocentricCRS) crs );
                            break;
                        case GEOGRAPHIC:
                            geographics.add( (GeographicCRS) crs );
                            break;
                        case PROJECTED:
                            projecteds.add( (ProjectedCRS) crs );
                            break;
                        case VERTICAL:
                            // not supported yet
                            break;
                        }
                        primeMeridians.add( d.getPrimeMeridian() );
                        wgs84s.add( d.getWGS84Conversion() );

                    }
                }

                initDocument( xmlWriter );

                for ( IEllipsoid e : ellipsoids ) {
                    export( e, xmlWriter );
                }
                for ( IGeodeticDatum d : datums ) {
                    export( d, xmlWriter );
                }
                for ( IProjectedCRS projected : projecteds ) {
                    export( projected, xmlWriter );
                }
                for ( IGeographicCRS geographic : geographics ) {
                    export( geographic, xmlWriter );
                }
                for ( ICompoundCRS compound : compounds ) {
                    export( compound, xmlWriter );
                }
                for ( IGeocentricCRS geocentric : geocentrics ) {
                    export( geocentric, xmlWriter );
                }
                for ( IPrimeMeridian pm : primeMeridians ) {
                    export( pm, xmlWriter );
                }
                for ( Helmert wgs84 : wgs84s ) {
                    export( wgs84, xmlWriter );
                }

                endDocument( xmlWriter );
            } else {
                LOG.warn( "No coordinate system were given (list.size() == 0)." );
            }
        } else {
            LOG.error( "No coordinate system were given (list == null)." );
        }
    }

    /**
     * Open an XML document from stream for exporting
     * 
     * @param xmlWriter
     */
    protected void initDocument( XMLStreamWriter xmlWriter ) {
        try {
            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement( CRSNS, "definitions" );
            xmlWriter.writeNamespace( CommonNamespaces.CRS_PREFIX, CommonNamespaces.CRSNS );
            xmlWriter.writeNamespace( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
            xmlWriter.writeAttribute( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS, "schemaLocation",
                                      "http://www.deegree.org/crs /home/ionita/workspace/d3_core/resources/schema/crsdefinition.xsd" );
            xmlWriter.writeAttribute( "version", "0.2.0" );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Write the /crs:defintions and the end document and flush the writer.
     * 
     * @param xmlWriter
     * @throws XMLStreamException
     */
    protected void endDocument( XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        xmlWriter.writeEndElement(); // </crs:definitions>
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
    }

    /**
     * Export the confInvo to it's appropriate deegree-crs-definitions form.
     * 
     * @param wgs84
     *            to be exported
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    protected void export( Helmert wgs84, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( wgs84 != null ) {
            xmlWriter.writeStartElement( CRSNS, "wgs84Transformation" );
            exportIdentifiable( wgs84, xmlWriter );
            // xAxisTranslation element
            xmlWriter.writeStartElement( CRSNS, "xAxisTranslation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.dx ) );
            xmlWriter.writeEndElement();
            // yAxisTranslation element
            xmlWriter.writeStartElement( CRSNS, "yAxisTranslation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.dy ) );
            xmlWriter.writeEndElement();
            // zAxisTranslation element
            xmlWriter.writeStartElement( CRSNS, "zAxisTranslation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.dz ) );
            xmlWriter.writeEndElement();
            // xAxisRotation element
            xmlWriter.writeStartElement( CRSNS, "xAxisRotation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ex ) );
            xmlWriter.writeEndElement();
            // yAxisRotation element
            xmlWriter.writeStartElement( CRSNS, "yAxisRotation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ey ) );
            xmlWriter.writeEndElement();
            // zAxisRotation element
            xmlWriter.writeStartElement( CRSNS, "zAxisRotation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ez ) );
            xmlWriter.writeEndElement();
            // scaleDifference element
            xmlWriter.writeStartElement( CRSNS, "scaleDifference" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ppm ) );
            xmlWriter.writeEndElement();

            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the PrimeMeridian to it's appropriate deegree-crs-definitions form.
     * 
     * @param pm
     *            PrimeMeridian to be exported
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    protected void export( IPrimeMeridian pm, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( pm != null ) {
            xmlWriter.writeStartElement( CRSNS, "primeMeridian" );

            exportIdentifiable( pm, xmlWriter );
            // units element
            export( pm.getAngularUnit(), xmlWriter );
            // longitude element
            xmlWriter.writeStartElement( CRSNS, "longitude" );
            xmlWriter.writeCharacters( Double.toString( pm.getLongitude() ) );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the compoundCRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param compoundCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geographic CRS to.
     * @throws XMLStreamException
     */
    protected void export( ICompoundCRS compoundCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( compoundCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "compoundCRS" );

            exportIdentifiable( compoundCRS, xmlWriter );
            ICRS underCRS = compoundCRS.getUnderlyingCRS();
            // usedCRS element
            xmlWriter.writeStartElement( CRSNS, "usedCRS" );
            xmlWriter.writeCharacters( underCRS.getCode().toString() );
            xmlWriter.writeEndElement();
            // heightAxis element
            IAxis heightAxis = compoundCRS.getHeightAxis();
            export( heightAxis, "heightAxis", xmlWriter );
            // defaultHeight element
            double axisHeight = compoundCRS.getDefaultHeight();
            xmlWriter.writeStartElement( CRSNS, "defaultHeight" );
            xmlWriter.writeCharacters( Double.toString( axisHeight ) );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }

    }

    /**
     * Export the projected CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param projectedCRS
     *            to be exported
     * @param xmlWriter
     *            to export the projected CRS to.
     * @throws XMLStreamException
     */
    protected void export( IProjectedCRS projectedCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( projectedCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "projectedCRS" );
            exportAbstractCRS( projectedCRS, xmlWriter );

            xmlWriter.writeStartElement( CRSNS, "usedGeographicCRS" );
            xmlWriter.writeCharacters( projectedCRS.getGeographicCRS().getCode().toString() );
            xmlWriter.writeEndElement();

            // projection
            export( projectedCRS.getProjection(), xmlWriter );
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the projection to it's appropriate deegree-crs-definitions form.
     * 
     * @param projection
     *            to be exported
     * @param xmlWriter
     *            to export the projection to.
     * @throws XMLStreamException
     */
    protected void export( IProjection projection, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( projection != null ) {
            xmlWriter.writeStartElement( CRSNS, "projection" );

            String implName = projection.getImplementationName();
            xmlWriter.writeStartElement( CRSNS, "" + implName );
            // latitudeOfNaturalOrigin
            xmlWriter.writeStartElement( CRSNS, "latitudeOfNaturalOrigin" );
            xmlWriter.writeAttribute( "inDegrees", "true" );
            xmlWriter.writeCharacters( Double.toString( Math.toDegrees( projection.getProjectionLatitude() ) ) );
            xmlWriter.writeEndElement();
            // longitudeOfNaturalOrigin
            xmlWriter.writeStartElement( CRSNS, "longitudeOfNaturalOrigin" );
            xmlWriter.writeAttribute( "inDegrees", "true" );
            xmlWriter.writeCharacters( Double.toString( Math.toDegrees( projection.getProjectionLongitude() ) ) );
            xmlWriter.writeEndElement();
            // scaleFactor element
            xmlWriter.writeStartElement( CRSNS, "scaleFactor" );
            xmlWriter.writeCharacters( Double.toString( projection.getScale() ) );
            xmlWriter.writeEndElement();
            // falseEasting element
            xmlWriter.writeStartElement( CRSNS, "falseEasting" );
            xmlWriter.writeCharacters( Double.toString( projection.getFalseEasting() ) );
            xmlWriter.writeEndElement();
            // falseNorthing element
            xmlWriter.writeStartElement( CRSNS, "falseNorthing" );
            xmlWriter.writeCharacters( Double.toString( projection.getFalseNorthing() ) );
            xmlWriter.writeEndElement();
            if ( "transverseMercator".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "northernHemisphere" );
                xmlWriter.writeCharacters( Boolean.toString( ( (ITransverseMercator) projection ).getHemisphere() ) );
                xmlWriter.writeEndElement();
            } else if ( "lambertConformalConic".equalsIgnoreCase( implName ) ) {
                double paralellLatitude = ( (LambertConformalConic) projection ).getFirstParallelLatitude();
                if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                    paralellLatitude = Math.toDegrees( paralellLatitude );
                    xmlWriter.writeStartElement( CRSNS, "firstParallelLatitude" );
                    xmlWriter.writeAttribute( "inDegrees", "true" );
                    xmlWriter.writeCharacters( Double.toString( paralellLatitude ) );
                    xmlWriter.writeEndElement();
                }
                paralellLatitude = ( (LambertConformalConic) projection ).getSecondParallelLatitude();
                if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                    paralellLatitude = Math.toDegrees( paralellLatitude );
                    xmlWriter.writeStartElement( CRSNS, "secondParallelLatitude" );
                    xmlWriter.writeAttribute( "inDegrees", "true" );
                    xmlWriter.writeCharacters( Double.toString( paralellLatitude ) );
                    xmlWriter.writeEndElement();
                }
            } else if ( "stereographicAzimuthal".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "trueScaleLatitude" );
                xmlWriter.writeAttribute( "inDegrees", "true" );
                xmlWriter.writeCharacters( Double.toString( ( (IStereographicAzimuthal) projection ).getTrueScaleLatitude() ) );
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the geocentric/geographic CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geoGraphicCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geographic CRS to.
     * @throws XMLStreamException
     */
    protected void export( IGeographicCRS geoGraphicCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( geoGraphicCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "geographicCRS" );

            exportAbstractCRS( geoGraphicCRS, xmlWriter );
            xmlWriter.writeStartElement( CRSNS, "usedDatum" );
            xmlWriter.writeCharacters( geoGraphicCRS.getDatum().getCode().toString() );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the geocentric CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geocentricCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geocentric CRS to.
     * @throws XMLStreamException
     */
    protected void export( IGeocentricCRS geocentricCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( geocentricCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "geocentricCRS" );
            exportAbstractCRS( geocentricCRS, xmlWriter );
            xmlWriter.writeStartElement( CRSNS, "usedDatum" );
            xmlWriter.writeCharacters( geocentricCRS.getDatum().getCode().toString() );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export toplevel crs features.
     * 
     * @param crs
     *            to be exported
     * @param xmlWriter
     *            to export to
     * @throws XMLStreamException
     */
    protected void exportAbstractCRS( ICRS crs, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( crs != null ) {
            exportIdentifiable( crs, xmlWriter );

            IAxis[] axes = crs.getAxis();
            StringBuilder axisOrder = new StringBuilder( 4 ); // maxOccurs of Axis = 3 in the schema

            for ( int i = 0; i < axes.length; ++i ) {
                IAxis a = axes[i];
                export( a, "Axis", xmlWriter );
                axisOrder.append( a.getName() );
                if ( ( i + 1 ) < axes.length ) {
                    axisOrder.append( ", " );
                }
            }

            // write the axisOrder
            xmlWriter.writeStartElement( CRSNS, "axisOrder" );
            xmlWriter.writeCharacters( axisOrder.toString() );
            xmlWriter.writeEndElement();

            // export transformations and recurse on their type
            exportTransformations( crs.getTransformations(), xmlWriter );
        }
    }

    /**
     * Export a list of transformations from the crs element to xml with respect to the crs-definitions schema layout.
     * 
     * @param transformations
     *            to be exported.
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    protected void exportTransformations( List<Transformation> transformations, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        for ( Transformation transformation : transformations ) {
            if ( transformation != null ) {
                if ( transformation instanceof PolynomialTransformation ) {
                    PolynomialTransformation trans = (PolynomialTransformation) transformation;
                    xmlWriter.writeStartElement( CRSNS, "polynomialTransformation" );

                    if ( !"leastsquare".equals( transformation.getImplementationName().toLowerCase() ) ) {
                        xmlWriter.writeAttribute( "class", trans.getClass().getCanonicalName() );
                    }
                    xmlWriter.writeStartElement( CRSNS, "" + trans.getImplementationName() );
                    // polynomialOrder
                    xmlWriter.writeStartElement( CRSNS, "polynomialOrder" );
                    xmlWriter.writeCharacters( Integer.toString( trans.getOrder() ) );
                    xmlWriter.writeEndElement();
                    // xParameters
                    xmlWriter.writeStartElement( CRSNS, "xParameters" );
                    xmlWriter.writeCharacters( trans.getFirstParams().toString() );
                    xmlWriter.writeEndElement();
                    // yParameters
                    xmlWriter.writeStartElement( CRSNS, "yParameters" );
                    xmlWriter.writeCharacters( trans.getSecondParams().toString() );
                    xmlWriter.writeEndElement();
                    // targetCRS
                    xmlWriter.writeStartElement( CRSNS, "targetCRS" );
                    xmlWriter.writeCharacters( trans.getTargetCRS().getCode().toString() );
                    xmlWriter.writeEndElement();

                    xmlWriter.writeEndElement();
                    xmlWriter.writeEndElement();
                }
            }
        }
    }

    /**
     * Export an axis to xml in the crs-definitions schema layout.
     * 
     * @param axis
     *            to be exported.
     * @param elName
     *            the name of the element, either 'Axis' or 'heightAxis'
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    protected void export( IAxis axis, String elName, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( axis != null ) {
            xmlWriter.writeStartElement( CRSNS, elName );
            // axis name
            xmlWriter.writeStartElement( CRSNS, "name" );
            xmlWriter.writeCharacters( axis.getName() );
            xmlWriter.writeEndElement();
            // axis units
            export( axis.getUnits(), xmlWriter );
            // axis orientation
            xmlWriter.writeStartElement( CRSNS, "axisOrientation" );
            xmlWriter.writeCharacters( axis.getOrientationAsString() );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export a unit to xml in the crs-definitions schema layout.
     * 
     * @param units
     *            to be exported.
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    protected void export( IUnit units, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( units != null ) {
            xmlWriter.writeStartElement( CRSNS, "units" );
            xmlWriter.writeCharacters( units.getName().toLowerCase() );
            xmlWriter.writeEndElement();
        }

    }

    /**
     * Export the datum to it's appropriate deegree-crs-definitions form.
     * 
     * @param datum
     *            to be exported
     * @param xmlWriter
     *            to export the datum to.
     * @throws XMLStreamException
     */
    protected void export( IGeodeticDatum datum, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( datum != null ) {
            xmlWriter.writeStartElement( CRSNS, "geodeticDatum" );
            exportIdentifiable( datum, xmlWriter );
            // usedEllipsoid element
            xmlWriter.writeStartElement( CRSNS, "usedEllipsoid" );
            xmlWriter.writeCharacters( datum.getEllipsoid().getCode().toString() );
            xmlWriter.writeEndElement();
            // usedPrimeMeridian element
            IPrimeMeridian pm = datum.getPrimeMeridian();
            if ( pm != null ) {
                xmlWriter.writeStartElement( CRSNS, "usedPrimeMeridian" );
                xmlWriter.writeCharacters( pm.getCode().toString() );
                xmlWriter.writeEndElement();
            }
            // usedWGS84ConversionInfo element
            Helmert convInfo = datum.getWGS84Conversion();
            if ( convInfo != null ) {
                xmlWriter.writeStartElement( CRSNS, "usedWGS84ConversionInfo" );
                xmlWriter.writeCharacters( convInfo.getCode().toString() );
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the ellipsoid to it's appropriate deegree-crs-definitions form.
     * 
     * @param ellipsoid
     *            to be exported
     * @param xmlWriter
     *            to export the ellipsoid to.
     * @throws XMLStreamException
     */
    protected void export( IEllipsoid ellipsoid, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( ellipsoid != null ) {
            xmlWriter.writeStartElement( CRSNS, "ellipsoid" );

            // write the elements that are specific to Identifiable
            exportIdentifiable( ellipsoid, xmlWriter );

            double sMajorAxis = ellipsoid.getSemiMajorAxis();
            xmlWriter.writeStartElement( CRSNS, "semiMajorAxis" );
            xmlWriter.writeCharacters( Double.toString( sMajorAxis ) );
            xmlWriter.writeEndElement();

            double inverseF = ellipsoid.getInverseFlattening();
            xmlWriter.writeStartElement( CRSNS, "inverseFlattening" );
            xmlWriter.writeCharacters( Double.toString( inverseF ) );
            xmlWriter.writeEndElement();

            export( ellipsoid.getUnits(), xmlWriter );

            xmlWriter.writeEndElement();
        }
    }

    /**
     * Creates the basic nodes of the identifiable object.
     * 
     * @param identifiable
     *            object to be exported.
     * @param xmlWriter
     *            to export to
     * @throws XMLStreamException
     */
    protected void exportIdentifiable( CRSResource identifiable, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        // ids
        CRSCodeType[] identifiers = identifiable.getCodes();
        for ( CRSCodeType id : identifiers ) {
            if ( id != null ) {
                xmlWriter.writeStartElement( CRSNS, "id" );
                xmlWriter.writeCharacters( id.getOriginal() );
                xmlWriter.writeEndElement();
            }
        }
        // names
        String[] names = identifiable.getNames();
        if ( names != null && names.length > 0 ) {
            for ( String name : names ) {
                if ( name != null ) {
                    xmlWriter.writeStartElement( CRSNS, "name" );
                    xmlWriter.writeCharacters( name );
                    xmlWriter.writeEndElement();
                }
            }
        }
        // versions
        String[] versions = identifiable.getVersions();
        if ( versions != null && versions.length > 0 ) {
            for ( String version : versions ) {
                if ( version != null ) {
                    xmlWriter.writeStartElement( CRSNS, "version" );
                    xmlWriter.writeCharacters( version );
                    xmlWriter.writeEndElement();
                }
            }
        }
        // descriptions
        String[] descriptions = identifiable.getDescriptions();
        if ( descriptions != null && descriptions.length > 0 ) {
            for ( String description : descriptions ) {
                if ( description != null ) {
                    xmlWriter.writeStartElement( CRSNS, "description" );
                    xmlWriter.writeCharacters( description );
                    xmlWriter.writeEndElement();
                }
            }
        }
        // areasOfUse
        String[] areas = identifiable.getAreasOfUse();
        if ( areas != null && areas.length > 0 ) {
            for ( String area : areas ) {
                if ( area != null ) {
                    xmlWriter.writeStartElement( CRSNS, "areaOfUse" );
                    xmlWriter.writeCharacters( area );
                    xmlWriter.writeEndElement();
                }
            }
        }
    }

    // /**
    // * Simple main to test exportation.
    // *
    // * @param args
    // * @throws XMLStreamException
    // * @throws IOException
    // */
    // public static void main( String[] args )
    // throws XMLStreamException, IOException {
    // // ICoordinateSystem lookup = CRSRegistry.lookup( "EPSG:31466" );
    // CRSConfiguration config = CRSConfiguration.getInstance();
    // CRSStore provider = config.getProvider();
    //
    // List<CoordinateSystem> one = provider.getAvailableCRSs();
    // // List<CoordinateSystem> one = new ArrayList<CoordinateSystem>();
    // // ICoordinateSystem a = provider.getCRSByCode( new CRSCodeType( "EPSG:31466" ) );
    // // one.add( a );
    // // a = provider.getCRSByCode( new CRSCodeType( "EPSG:4314" ) );
    // // one.add( a );
    // //
    // // // lcc
    // // a = provider.getCRSByCode( new CRSCodeType( "BBR:0001" ) );
    // // one.add( a );
    // //
    // // // lambera
    // // a = provider.getCRSByCode( new CRSCodeType( "EPSG:2163" ) );
    // // one.add( a );
    // // // mercator
    // // a = provider.getCRSByCode( new CRSCodeType( "EPSG:3857" ) );
    // // one.add( a );
    // //
    // // // stereo
    // // a = provider.getCRSByCode( new CRSCodeType( "EPSG:32661" ) );
    // // one.add( a );
    // //
    // // // stereo al
    // // a = provider.getCRSByCode( new CRSCodeType( "EPSG:2290" ) );
    // // one.add( a );
    // //
    // // // compound
    // // a = provider.getCRSByCode( new CRSCodeType( "EPSG:4979" ) );
    // // one.add( a );
    //
    // // ICoordinateSystem a = provider.getCRSByCode( new CRSCodeType( "EPSG:4809" ) );
    // // one.add( a );
    // // ICoordinateSystem a = provider.getCRSByCode( new CRSCodeType( "EPSG:4157" ) );
    // // one.add( a );
    //
    // CRSExporterBase exporter = new CRSExporter( new Properties() );
    // XMLOutputFactory factory = XMLOutputFactory.newInstance();
    // factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
    // FileOutputStream out = new FileOutputStream( new File( "new_crs.xml" ) );
    // XMLStreamWriter writer = new IndentingXMLStreamWriter( factory.createXMLStreamWriter( out ) );
    // exporter.export( one, writer );
    // writer.close();
    // }
}
