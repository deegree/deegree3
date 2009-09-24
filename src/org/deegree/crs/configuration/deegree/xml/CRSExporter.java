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

package org.deegree.crs.configuration.deegree.xml;

import static org.deegree.commons.xml.CommonNamespaces.CRSNS;
import static org.deegree.crs.projections.ProjectionUtils.EPS11;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.PrimeMeridian;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeocentricCRS;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.crs.transformations.polynomial.PolynomialTransformation;
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
public class CRSExporter {

    private static Logger LOG = LoggerFactory.getLogger( CRSExporter.class );

    /**
     * 
     * @param properties
     *            to read configuration from.
     */
    public CRSExporter( Properties properties ) {
        // nothing yet.
    }

    /**
     * Export the given list of CoordinateSystems into the crs-definition format.
     * 
     * 
     * @param crsToExport
     * @param xmlWriter
     *            to write the definitions to.
     * @throws XMLStreamException
     *             if an error occurred while exporting
     */
    public void export( List<CoordinateSystem> crsToExport, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( crsToExport != null ) {
            if ( crsToExport.size() != 0 ) {
                LOG.debug( "Trying to export: " + crsToExport.size() + " coordinate systems." );

                // LinkedList<String> exportedIDs = new LinkedList<String>();
                Set<Ellipsoid> ellipsoids = new HashSet<Ellipsoid>();
                Set<GeodeticDatum> datums = new HashSet<GeodeticDatum>();
                Set<GeocentricCRS> geocentrics = new HashSet<GeocentricCRS>();
                Set<GeographicCRS> geographics = new HashSet<GeographicCRS>();
                Set<ProjectedCRS> projecteds = new HashSet<ProjectedCRS>();
                Set<CompoundCRS> compounds = new HashSet<CompoundCRS>();
                Set<PrimeMeridian> primeMeridians = new HashSet<PrimeMeridian>();
                Set<Helmert> wgs84s = new HashSet<Helmert>();

                for ( CoordinateSystem crs : crsToExport ) {
                    if ( crs != null ) {
                        GeodeticDatum d = (GeodeticDatum) crs.getDatum();
                        datums.add( d );
                        ellipsoids.add( d.getEllipsoid() );

                        if ( crs.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                            geocentrics.add( (GeocentricCRS) crs );
                        } else if ( crs.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                            geographics.add( (GeographicCRS) crs );
                        } else if ( crs.getType() == CoordinateSystem.PROJECTED_CRS ) {
                            projecteds.add( (ProjectedCRS) crs );
                        } else if ( crs.getType() == CoordinateSystem.COMPOUND_CRS ) {
                            compounds.add( (CompoundCRS) crs );
                        }

                        primeMeridians.add( d.getPrimeMeridian() );
                        wgs84s.add( d.getWGS84Conversion() );

                    }
                }

                initDocument( xmlWriter );

                for ( Ellipsoid e : ellipsoids ) {
                    export( e, xmlWriter );
                }
                for ( GeodeticDatum d : datums ) {
                    export( d, xmlWriter );
                }
                for ( ProjectedCRS projected : projecteds ) {
                    export( projected, xmlWriter );
                }
                for ( GeographicCRS geographic : geographics ) {
                    export( geographic, xmlWriter );
                }
                for ( CompoundCRS compound : compounds ) {
                    export( compound, xmlWriter );
                }
                for ( GeocentricCRS geocentric : geocentrics ) {
                    export( geocentric, xmlWriter );
                }
                for ( PrimeMeridian pm : primeMeridians ) {
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
    protected void export( PrimeMeridian pm, XMLStreamWriter xmlWriter )
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
    protected void export( CompoundCRS compoundCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( compoundCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "compoundCRS" );

            exportIdentifiable( compoundCRS, xmlWriter );
            CoordinateSystem underCRS = compoundCRS.getUnderlyingCRS();
            // usedCRS element
            xmlWriter.writeStartElement( CRSNS, "usedCRS" );
            xmlWriter.writeCharacters( underCRS.getCode().toString() );
            xmlWriter.writeEndElement();
            // heightAxis element
            Axis heightAxis = compoundCRS.getHeightAxis();
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
    protected void export( ProjectedCRS projectedCRS, XMLStreamWriter xmlWriter )
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
    protected void export( Projection projection, XMLStreamWriter xmlWriter )
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
                xmlWriter.writeCharacters( Boolean.toString( ( (TransverseMercator) projection ).getHemisphere() ) );
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
                xmlWriter.writeCharacters( Double.toString( ( (StereographicAzimuthal) projection ).getTrueScaleLatitude() ) );
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
    protected void export( GeographicCRS geoGraphicCRS, XMLStreamWriter xmlWriter )
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
    protected void export( GeocentricCRS geocentricCRS, XMLStreamWriter xmlWriter )
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
    protected void exportAbstractCRS( CoordinateSystem crs, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( crs != null ) {
            exportIdentifiable( crs, xmlWriter );

            Axis[] axes = crs.getAxis();
            StringBuilder axisOrder = new StringBuilder( 4 ); // maxOccurs of Axis = 3 in the schema

            for ( int i = 0; i < axes.length; ++i ) {
                Axis a = axes[i];
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
    protected void exportTransformations( List<PolynomialTransformation> transformations, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        for ( PolynomialTransformation transformation : transformations ) {
            if ( transformation != null ) {
                xmlWriter.writeStartElement( CRSNS, "polynomialTransformation" );

                if ( !"leastsquare".equals( transformation.getImplementationName().toLowerCase() ) ) {
                    xmlWriter.writeAttribute( "class", transformation.getClass().getCanonicalName() );
                }
                xmlWriter.writeStartElement( CRSNS, "" + transformation.getImplementationName() );
                // polynomialOrder
                xmlWriter.writeStartElement( CRSNS, "polynomialOrder" );
                xmlWriter.writeCharacters( Integer.toString( transformation.getOrder() ) );
                xmlWriter.writeEndElement();
                // xParameters
                xmlWriter.writeStartElement( CRSNS, "xParameters" );
                xmlWriter.writeCharacters( transformation.getFirstParams().toString() );
                xmlWriter.writeEndElement();
                // yParameters
                xmlWriter.writeStartElement( CRSNS, "yParameters" );
                xmlWriter.writeCharacters( transformation.getSecondParams().toString() );
                xmlWriter.writeEndElement();
                // targetCRS
                xmlWriter.writeStartElement( CRSNS, "targetCRS" );
                xmlWriter.writeCharacters( transformation.getTargetCRS().getCode().toString() );
                xmlWriter.writeEndElement();

                xmlWriter.writeEndElement();
                xmlWriter.writeEndElement();
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
    protected void export( Axis axis, String elName, XMLStreamWriter xmlWriter )
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
    protected void export( Unit units, XMLStreamWriter xmlWriter )
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
    protected void export( GeodeticDatum datum, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( datum != null ) {
            xmlWriter.writeStartElement( CRSNS, "geodeticDatum" );
            exportIdentifiable( datum, xmlWriter );
            // usedEllipsoid element
            xmlWriter.writeStartElement( CRSNS, "usedEllipsoid" );
            xmlWriter.writeCharacters( datum.getEllipsoid().getCode().toString() );
            xmlWriter.writeEndElement();
            // usedPrimeMeridian element
            PrimeMeridian pm = datum.getPrimeMeridian();
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
    protected void export( Ellipsoid ellipsoid, XMLStreamWriter xmlWriter )
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
    protected void exportIdentifiable( CRSIdentifiable identifiable, XMLStreamWriter xmlWriter )
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

}
