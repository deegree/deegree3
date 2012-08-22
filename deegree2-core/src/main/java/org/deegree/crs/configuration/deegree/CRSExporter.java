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

package org.deegree.crs.configuration.deegree;

import static org.deegree.crs.projections.ProjectionUtils.EPS11;
import static org.deegree.ogcbase.CommonNamespaces.CRSNS;

import java.io.Writer;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.deegree.crs.Identifiable;
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
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    private static ILogger LOG = LoggerFactory.getLogger( CRSExporter.class );

    /**
     * The namespaces used in deegree.
     */
    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * The prefix to use.
     */
    private final static String PRE = CommonNamespaces.CRS_PREFIX + ":";

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
     * @param writer
     * @param crsToExport
     */
    public void export( Writer writer, List<CoordinateSystem> crsToExport ) {
        if ( crsToExport != null ) {
            if ( crsToExport.size() != 0 ) {
                LOG.logDebug( "Trying to export: " + crsToExport.size() + " coordinate systems." );
                XMLFragment frag = new XMLFragment( new QualifiedName( "crs", "definitions", CommonNamespaces.CRSNS ) );
                Element root = frag.getRootElement();
                LinkedList<String> exportedIDs = new LinkedList<String>();
                for ( CoordinateSystem crs : crsToExport ) {
                    if ( crs.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                        export( (GeocentricCRS) crs, root, exportedIDs );
                    } else if ( crs.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                        export( (GeographicCRS) crs, root, exportedIDs );
                    } else if ( crs.getType() == CoordinateSystem.PROJECTED_CRS ) {
                        export( (ProjectedCRS) crs, root, exportedIDs );
                    } else if ( crs.getType() == CoordinateSystem.COMPOUND_CRS ) {
                        export( (CompoundCRS) crs, root, exportedIDs );
                    }
                }
                root.normalize();
                Document validDoc = createValidDocument( root );
                try {
                    XMLFragment frag2 = new XMLFragment( validDoc, "http://www.deegree.org/crs" );
                    frag2.prettyPrint( writer );
                } catch ( MalformedURLException e ) {
                    LOG.logError( "Could not export crs definitions because: " + e.getMessage(), e );
                } catch ( TransformerException e ) {
                    LOG.logError( "Could not export crs definitions because: " + e.getMessage(), e );
                }
            } else {
                LOG.logWarning( "No coordinate system were given (list.size() == 0)." );
            }
        } else {
            LOG.logError( "No coordinate system were given (list == null)." );
        }
    }

    /**
     * Export the projected CRS to it's appropriate deegree-crs-definitions form.
     *
     * @param projectedCRS
     *            to be exported
     * @param rootNode
     *            to export the projected CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( ProjectedCRS projectedCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( projectedCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "projectedCRS" );
            exportAbstractCRS( projectedCRS, crsElement );
            GeographicCRS underLyingCRS = projectedCRS.getGeographicCRS();
            export( underLyingCRS, rootNode, exportedIds );

            // Add a reference from the geographicCRS element to the projectedCRS element.
            XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedGeographicCRS",
                                    underLyingCRS.getIdentifier() );

            export( projectedCRS.getProjection(), crsElement );

            // Add the ids to the exportedID list.
            for ( String eID : projectedCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Export the geocentric/geographic CRS to it's appropriate deegree-crs-definitions form.
     *
     * @param geographicCRS
     *            to be exported
     * @param rootNode
     *            to export the geographic CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( GeographicCRS geographicCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( geographicCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geographicCRS" );
            exportAbstractCRS( geographicCRS, crsElement );

            // export the datum.
            GeodeticDatum datum = geographicCRS.getGeodeticDatum();
            if ( datum != null ) {
                export( datum, rootNode, exportedIds );
                // Add a reference from the datum element to the geographic element.
                XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedDatum", datum.getIdentifier() );
            } else {
                LOG.logError( "The given datum is not a geodetic one, this mey not be!" );
            }
            // Add the ids to the exportedID list.
            for ( String eID : geographicCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Export the compoundCRS to it's appropriate deegree-crs-definitions form.
     *
     * @param compoundCRS
     *            to be exported
     * @param rootNode
     *            to export the geographic CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( CompoundCRS compoundCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( compoundCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "compoundCRS" );
            exportIdentifiable( compoundCRS, crsElement );
            CoordinateSystem underLyingCRS = compoundCRS.getUnderlyingCRS();
            if ( underLyingCRS.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                export( (GeographicCRS) underLyingCRS, rootNode, exportedIds );
            } else if ( underLyingCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                export( (ProjectedCRS) underLyingCRS, rootNode, exportedIds );
            }

            // Add a reference from the geographicCRS element to the projectedCRS element.
            XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedCRS", underLyingCRS.getIdentifier() );
            export( compoundCRS.getHeightAxis(), crsElement );

            XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "defaultHeight",
                                    Double.toString( compoundCRS.getDefaultHeight() ) );

            // Add the ids to the exportedID list.
            for ( String eID : compoundCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Export the projection to it's appropriate deegree-crs-definitions form.
     *
     * @param projection
     *            to be exported
     * @param rootNode
     *            to export the projection to.
     */
    private void export( Projection projection, Element rootNode ) {
        Element rootElem = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "projection" );
        String elementName = projection.getImplementationName();
        Element projectionElement = XMLTools.appendElement( rootElem, CommonNamespaces.CRSNS, PRE + elementName );
        // exportIdentifiable( projection, projectionElement );
        Element tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE
                                                                                         + "latitudeOfNaturalOrigin",
                                              Double.toString( Math.toDegrees( projection.getProjectionLatitude() ) ) );
        tmp.setAttribute( "inDegrees", "true" );
        tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "longitudeOfNaturalOrigin",
                                      Double.toString( Math.toDegrees( projection.getProjectionLongitude() ) ) );
        tmp.setAttribute( "inDegrees", "true" );

        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "scaleFactor",
                                Double.toString( projection.getScale() ) );
        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "falseEasting",
                                Double.toString( projection.getFalseEasting() ) );
        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "falseNorthing",
                                Double.toString( projection.getFalseNorthing() ) );
        if ( "transverseMercator".equalsIgnoreCase( elementName ) ) {
            XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "northernHemisphere",
                                    Boolean.toString( ( (TransverseMercator) projection ).getHemisphere() ) );
        } else if ( "lambertConformalConic".equalsIgnoreCase( elementName ) ) {
            double paralellLatitude = ( (LambertConformalConic) projection ).getFirstParallelLatitude();
            if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                paralellLatitude = Math.toDegrees( paralellLatitude );
                tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "firstParallelLatitude",
                                              Double.toString( paralellLatitude ) );
                tmp.setAttribute( "inDegrees", "true" );
            }
            paralellLatitude = ( (LambertConformalConic) projection ).getSecondParallelLatitude();
            if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                paralellLatitude = Math.toDegrees( paralellLatitude );
                tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS,
                                              PRE + "secondParallelLatitude", Double.toString( paralellLatitude ) );
                tmp.setAttribute( "inDegrees", "true" );
            }
        } else if ( "stereographicAzimuthal".equalsIgnoreCase( elementName ) ) {
            tmp = XMLTools.appendElement(
                                          projectionElement,
                                          CommonNamespaces.CRSNS,
                                          PRE + "trueScaleLatitude",
                                          Double.toString( ( (StereographicAzimuthal) projection ).getTrueScaleLatitude() ) );
            tmp.setAttribute( "inDegrees", "true" );
        }
    }

    /**
     * Export the confInvo to it's appropriate deegree-crs-definitions form.
     *
     * @param confInvo
     *            to be exported
     * @param rootNode
     *            to export the confInvo to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( Helmert confInvo, Element rootNode, final List<String> exportedIds ) {
        if ( !exportedIds.contains( confInvo.getIdentifier() ) ) {
            Element convElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "wgs84Transformation" );
            exportIdentifiable( confInvo, convElement );

            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "xAxisTranslation",
                                    Double.toString( confInvo.dx ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "yAxisTranslation",
                                    Double.toString( confInvo.dy ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "zAxisTranslation",
                                    Double.toString( confInvo.dz ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "xAxisRotation",
                                    Double.toString( confInvo.ex ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "yAxisRotation",
                                    Double.toString( confInvo.ey ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "zAxisRotation",
                                    Double.toString( confInvo.ez ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "scaleDifference",
                                    Double.toString( confInvo.ppm ) );

            // Add the ids to the exportedID list.
            for ( String eID : confInvo.getIdentifiers() ) {
                exportedIds.add( eID );
            }

            // finally add the WGS84-Transformation node to the rootnode.
            rootNode.appendChild( convElement );
        }

    }

    /**
     * Export the PrimeMeridian to it's appropriate deegree-crs-definitions form.
     *
     * @param pMeridian
     *            to be exported
     * @param rootNode
     *            to export the pMeridian to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( PrimeMeridian pMeridian, Element rootNode, final List<String> exportedIds ) {
        if ( !exportedIds.contains( pMeridian.getIdentifier() ) ) {
            Element meridianElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "primeMeridian" );
            exportIdentifiable( pMeridian, meridianElement );
            export( pMeridian.getAngularUnit(), meridianElement );
            XMLTools.appendElement( meridianElement, CommonNamespaces.CRSNS, PRE + "longitude",
                                    Double.toString( pMeridian.getLongitude() ) );

            // Add the ids to the exportedID list.
            for ( String eID : pMeridian.getIdentifiers() ) {
                exportedIds.add( eID );
            }

            // finally add the prime meridian node to the rootnode.
            rootNode.appendChild( meridianElement );
        }
    }

    /**
     * Export the ellipsoid to it's appropriate deegree-crs-definitions form.
     *
     * @param ellipsoid
     *            to be exported
     * @param rootNode
     *            to export the ellipsoid to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( Ellipsoid ellipsoid, Element rootNode, final List<String> exportedIds ) {
        if ( !exportedIds.contains( ellipsoid.getIdentifier() ) ) {
            Element ellipsoidElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "ellipsoid" );
            exportIdentifiable( ellipsoid, ellipsoidElement );
            XMLTools.appendElement( ellipsoidElement, CommonNamespaces.CRSNS, PRE + "semiMajorAxis",
                                    Double.toString( ellipsoid.getSemiMajorAxis() ) );
            XMLTools.appendElement( ellipsoidElement, CommonNamespaces.CRSNS, PRE + "inverseFlatting",
                                    Double.toString( ellipsoid.getInverseFlattening() ) );
            export( ellipsoid.getUnits(), ellipsoidElement );

            // Add the ids to the exportedID list.
            for ( String eID : ellipsoid.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the ellipsoid node to the rootnode.
            rootNode.appendChild( ellipsoidElement );
        }
    }

    /**
     * Export the datum to it's appropriate deegree-crs-definitions form.
     *
     * @param datum
     *            to be exported
     * @param rootNode
     *            to export the datum to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( GeodeticDatum datum, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( datum.getIdentifier() ) ) {
            Element datumElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geodeticDatum" );
            exportIdentifiable( datum, datumElement );
            /**
             * EXPORT the ELLIPSOID
             */
            Ellipsoid ellipsoid = datum.getEllipsoid();
            if ( ellipsoid != null ) {
                export( ellipsoid, rootNode, exportedIds );
                // Add a reference from the ellipsoid element to the datum element.
                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedEllipsoid",
                                        ellipsoid.getIdentifier() );
            }

            /**
             * EXPORT the PRIME_MERIDIAN
             */
            PrimeMeridian pMeridian = datum.getPrimeMeridian();
            if ( pMeridian != null ) {
                export( pMeridian, rootNode, exportedIds );
                // Add a reference from the prime meridian element to the datum element.
                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedPrimeMeridian",
                                        pMeridian.getIdentifier() );
            }

            /**
             * EXPORT the WGS-84-Conversion INFO
             */
            Helmert confInvo = datum.getWGS84Conversion();
            if ( confInvo != null ) {
                export( confInvo, rootNode, exportedIds );
                // Add a reference from the prime meridian element to the datum element.
                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedWGS84ConversionInfo",
                                        confInvo.getIdentifier() );
            }

            // Add the ids to the exportedID list.
            for ( String eID : datum.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the datum node to the rootnode.
            rootNode.appendChild( datumElement );
        }
    }

    /**
     * Export toplevel crs features.
     *
     * @param crs
     *            to be exported
     * @param crsElement
     *            to export to
     */
    private void exportAbstractCRS( CoordinateSystem crs, Element crsElement ) {
        exportIdentifiable( crs, crsElement );
        Axis[] axis = crs.getAxis();
        StringBuilder axisOrder = new StringBuilder( 200 );
        for ( int i = 0; i < axis.length; ++i ) {
            Axis a = axis[i];
            export( a, crsElement );
            axisOrder.append( a.getName() );
            if ( ( i + 1 ) < axis.length ) {
                axisOrder.append( ", " );
            }
        }
        XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "axisOrder", axisOrder.toString() );

        export( crs.getTransformations(), crsElement );

    }

    /**
     * Export the geocentric CRS to it's appropriate deegree-crs-definitions form.
     *
     * @param geocentricCRS
     *            to be exported
     * @param rootNode
     *            to export the geocentric CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( GeocentricCRS geocentricCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( geocentricCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geocentricCRS" );
            exportAbstractCRS( geocentricCRS, crsElement );
            // export the datum.
            GeodeticDatum datum = geocentricCRS.getGeodeticDatum();
            if ( datum != null ) {
                export( datum, rootNode, exportedIds );
                // Add a reference from the datum element to the geocentric element.
                XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedDatum", datum.getIdentifier() );
            } // Add the ids to the exportedID list.
            for ( String eID : geocentricCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Creates the basic nodes of the identifiable object.
     *
     * @param id
     *            object to be exported.
     * @param currentNode
     *            to expand
     */
    private void exportIdentifiable( Identifiable id, Element currentNode ) {
        for ( String i : id.getIdentifiers() ) {
            if ( i != null ) {
                XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "id", i );
            }

        }
        if ( id.getNames() != null && id.getNames().length > 0 ) {
            for ( String i : id.getNames() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "name", i );
                }
            }
        }
        if ( id.getVersions() != null && id.getVersions().length > 0 ) {
            for ( String i : id.getVersions() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "version", i );
                }
            }
        }
        if ( id.getDescriptions() != null && id.getDescriptions().length > 0 ) {
            for ( String i : id.getDescriptions() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "description", i );
                }
            }
        }
        if ( id.getAreasOfUse() != null && id.getAreasOfUse().length > 0 ) {
            for ( String i : id.getAreasOfUse() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "areaOfUse", i );
                }
            }
        }
    }

    /**
     * Export an axis to xml in the crs-definitions schema layout.
     *
     * @param axis
     *            to be exported.
     * @param currentNode
     *            to export to.
     */
    private void export( Axis axis, Element currentNode ) {
        Document doc = currentNode.getOwnerDocument();
        Element axisElement = doc.createElementNS( CRSNS.toASCIIString(), PRE + "Axis" );
        // The name.
        XMLTools.appendElement( axisElement, CommonNamespaces.CRSNS, PRE + "name", axis.getName() );

        // the units.
        Unit units = axis.getUnits();
        export( units, axisElement );

        XMLTools.appendElement( axisElement, CommonNamespaces.CRSNS, PRE + "axisOrientation",
                                axis.getOrientationAsString() );
        currentNode.appendChild( axisElement );
    }

    /**
     * Export a list of transformations to the crs element to xml with respect to the crs-definitions schema layout.
     *
     * @param transformations
     *            to be exported.
     * @param currentNode
     *            to export to.
     */
    private void export( List<PolynomialTransformation> transformations, Element currentNode ) {
        for ( PolynomialTransformation transformation : transformations ) {
            Element transformationElement = XMLTools.appendElement( currentNode, CRSNS, PRE
                                                                                        + "polynomialTransformation" );
            if ( !"leastsquare".equals( transformation.getImplementationName().toLowerCase() ) ) {
                transformationElement.setAttribute( "class", transformation.getClass().getCanonicalName() );
            }
            Element transformElement = XMLTools.appendElement( transformationElement, CRSNS,
                                                               PRE + transformation.getImplementationName() );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "polynomialOrder",
                                    Integer.toString( transformation.getOrder() ) );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "xParameters",
                                    transformation.getFirstParams().toString() );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "yParameters",
                                    transformation.getSecondParams().toString() );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "targetCRS",
                                    transformation.getTargetCRS().getIdentifier() );
        }
    }

    /**
     * Export a unit to xml in the crs-definitions schema layout.
     *
     * @param units
     *            to be exported.
     * @param currentNode
     *            to export to.
     */
    private void export( Unit units, Element currentNode ) {
        if ( units != null && currentNode != null ) {
            XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "units", units.getName() );
        }
    }

    private Document createValidDocument( Element root ) {
        // List<Element> lastInput = new LinkedList<Element>( 100 );
        try {
            List<Element> valid = XMLTools.getElements( root, PRE + "ellipsoid", nsContext );
            valid.addAll( XMLTools.getElements( root, PRE + "geodeticDatum", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "projectedCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "geographicCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "compoundCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "geocentricCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "primeMeridian", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "wgs84Transformation", nsContext ) );
            Document doc = XMLTools.create();
            Element newRoot = doc.createElementNS( CommonNamespaces.CRSNS.toASCIIString(), PRE + "definitions" );
            newRoot = (Element) doc.importNode( newRoot, false );
            newRoot = (Element) doc.appendChild( newRoot );
            for ( int i = 0; i < valid.size(); ++i ) {
                Element el = valid.get( i );
                el = (Element) doc.importNode( el, true );
                newRoot.appendChild( el );
            }
            XMLTools.appendNSBinding( newRoot, CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
            newRoot.setAttributeNS(
                                    CommonNamespaces.XSINS.toASCIIString(),
                                    "xsi:schemaLocation",
                                    "http://www.deegree.org/crs c:/windows/profiles/rutger/EIGE~VO5/eclipse-projekte/coordinate_systems/resources/schema/crsdefinition.xsd" );
            return doc;
        } catch ( XMLParsingException xmle ) {
            xmle.printStackTrace();
        }
        return root.getOwnerDocument();
    }

}
