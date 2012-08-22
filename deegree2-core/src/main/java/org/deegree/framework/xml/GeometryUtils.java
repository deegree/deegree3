//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.framework.xml;

import java.util.List;
import java.util.StringTokenizer;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.InvalidGMLException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility methods for handling geometries within XSLT transformations
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryUtils {

    private static ILogger LOG = LoggerFactory.getLogger( GeometryUtils.class );

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    /**
     * @param node
     *            (an Element) describing an envelope to get the coordinates from.
     * @return the coordinates from the envelope separated by ','. Or the empty String if an error occurred.
     */
    public static String getPolygonCoordinatesFromEnvelope( Node node ) {
        StringBuilder sb = new StringBuilder( 500 );
        if ( node != null ) {
            Envelope env = null;
            try {
                env = GMLGeometryAdapter.wrapBox( (Element) node, null );
            } catch ( InvalidGMLException e ) {
                LOG.logError( "Could not get Polygon coordinates of given envelope because: " + e.getMessage(), e );
            } catch ( XMLParsingException e ) {
                LOG.logError( "Could not get Polygon coordinates of given envelope because: " + e.getMessage(), e );
            } catch ( UnknownCRSException e ) {
                LOG.logError( "Could not get Polygon coordinates of given envelope because: " + e.getMessage(), e );
            }
            if ( env != null ) {
                sb.append( env.getMin().getX() ).append( ',' ).append( env.getMin().getY() ).append( ' ' );
                sb.append( env.getMin().getX() ).append( ',' ).append( env.getMax().getY() ).append( ' ' );
                sb.append( env.getMax().getX() ).append( ',' ).append( env.getMax().getY() ).append( ' ' );
                sb.append( env.getMax().getX() ).append( ',' ).append( env.getMin().getY() ).append( ' ' );
                sb.append( env.getMin().getX() ).append( ',' ).append( env.getMin().getY() );
            }
        } else {
            LOG.logWarning( "Could not get Polygon coordinates of given envelope because the given node was null." );
        }

        return sb.toString();
    }

    /**
     * 
     * @param node
     *            (an Element) describing a geometry from which the bbox (envelope) will be returned.
     * @return the coordinates from the bbox of the geometry separated by ','. Or the empty String if an error occurred.
     */
    public static String getEnvelopeFromGeometry( Node node ) {
        StringBuilder sb = new StringBuilder( 500 );
        if ( node != null ) {
            Envelope env = null;
            try {
                env = GMLGeometryAdapter.wrap( (Element) node, null ).getEnvelope();
            } catch ( GeometryException e ) {
                LOG.logError( "Could not get envelope of geometry because: " + e.getMessage(), e );
            }
            if ( env != null ) {
                sb.append( env.getMin().getX() ).append( ',' ).append( env.getMin().getY() ).append( ' ' );
                sb.append( env.getMin().getX() ).append( ',' ).append( env.getMax().getY() ).append( ' ' );
                sb.append( env.getMax().getX() ).append( ',' ).append( env.getMax().getY() ).append( ' ' );
                sb.append( env.getMax().getX() ).append( ',' ).append( env.getMin().getY() ).append( ' ' );
                sb.append( env.getMin().getX() ).append( ',' ).append( env.getMin().getY() );
            }
        } else {
            LOG.logWarning( "Could not get envelope of geometry because the given node was null." );
        }

        return sb.toString();
    }

    /**
     * returns the coordinates of the out ring of a polygon as comma separated list. The coordinate tuples are separated
     * by a blank. If required the polygon will first transformed to the target CRS
     * 
     * @param node
     * @param sourceCRS
     * @param targetCRS
     * @return the coordinates of the out ring of a polygon as comma separated list, or the empty String if an exception
     *         occurred in the extracting/transforming process.
     */
    public static String getPolygonOuterRing( Node node, String sourceCRS, String targetCRS ) {
        StringBuilder coords = new StringBuilder( 10000 );
        if ( node != null ) {
            Surface surface = null;
            try {
                surface = (Surface) GMLGeometryAdapter.wrap( (Element) node, sourceCRS );
                if ( !targetCRS.equals( sourceCRS ) ) {
                    GeoTransformer gt = new GeoTransformer( targetCRS );
                    surface = (Surface) gt.transform( surface );
                }
            } catch ( GeometryException e ) {
                LOG.logError( "Could not extract outer ring of polygon because: " + e.getMessage(), e );
            } catch ( IllegalArgumentException e ) {
                LOG.logError( "Could not extract outer ring of polygon because: " + e.getMessage(), e );
            } catch ( CRSTransformationException e ) {
                LOG.logError( "Could not transform outer ring of polygon because: " + e.getMessage(), e );
                surface = null;
            } catch ( UnknownCRSException e ) {
                LOG.logError( "Could not transform outer ring of polygon because: " + e.getMessage(), e );
                surface = null;
            }
            if ( surface != null ) {
                Position[] pos = surface.getSurfaceBoundary().getExteriorRing().getPositions();
                int dim = pos[0].getCoordinateDimension();
                for ( int i = 0; i < pos.length; i++ ) {
                    coords.append( pos[i].getX() ).append( ',' ).append( pos[i].getY() );
                    if ( dim == 3 ) {
                        coords.append( ',' ).append( pos[i].getZ() );
                    }
                    coords.append( ' ' );
                }
            }
        } else {
            LOG.logWarning( "Could not extract outer ring of polygon because the given node was null." );
        }

        return coords.toString();
    }

    /**
     * 
     * @param node
     * @param index
     * @param sourceCRS
     * @param targetCRS
     * @return the inner ring of the given polyong / surface found in the node or the empty String otherwise.
     */
    public static String getPolygonInnerRing( Node node, int index, String sourceCRS, String targetCRS ) {
        StringBuilder coords = new StringBuilder( 10000 );

        if ( node != null ) {
            if ( "Polygon".equals( node.getLocalName() ) || "Surface".equals( node.getLocalName() ) ) {
                try {
                    Surface surface = (Surface) GMLGeometryAdapter.wrap( (Element) node, sourceCRS );
                    if ( !targetCRS.equals( sourceCRS ) ) {
                        GeoTransformer gt = new GeoTransformer( targetCRS );
                        surface = (Surface) gt.transform( surface );
                    }
                    Position[] pos = surface.getSurfaceBoundary().getInteriorRings()[index - 1].getPositions();
                    int dim = pos[0].getCoordinateDimension();
                    for ( int i = 0; i < pos.length; i++ ) {
                        coords.append( pos[i].getX() ).append( ',' ).append( pos[i].getY() );
                        if ( dim == 3 ) {
                            coords.append( ',' ).append( pos[i].getZ() );
                        }
                        coords.append( ' ' );
                    }
                } catch ( Exception e ) {
                    LOG.logError( "Could not extract Innerring because: " + e.getMessage(), e );
                }
            } else {
                LOG.logError( "The given node '" + node.getLocalName()
                              + "' does not contain a Polygon or a Surface, their could not extract inner-ring" );
            }
        } else {
            LOG.logWarning( "Could not extract inner-ring because the given node was null." );
        }
        return coords.toString();
    }

    /**
     * 
     * @param node
     *            to extract the geometry from
     * @return the area of the geometry inside the node or -1 if the given geometry node does not contain a surface or
     *         multi surface, or an exception occurred while extracting the geometry.
     */
    public static double calcArea( Node node ) {
        double area = -1;
        if ( node != null ) {
            Geometry geom = null;
            try {
                geom = GMLGeometryAdapter.wrap( (Element) node, null );
            } catch ( GeometryException e ) {
                LOG.logError( "Could not calculate the area of node with localname: '" + node.getLocalName()
                              + "' because: " + e.getLocalizedMessage(), e );
            }
            if ( geom != null ) {
                if ( geom instanceof Surface ) {
                    area = ( (Surface) geom ).getArea();
                } else if ( geom instanceof MultiSurface ) {
                    area = ( (MultiSurface) geom ).getArea();
                }
            }
        } else {
            LOG.logWarning( "Could not calculate the area because the given node was null." );
        }

        return area;
    }

    /**
     * @param node
     *            to extract the geometry from from which the length of the outerboundary will be calculated
     * @return the length outerboundary of the geometry inside the node or 0 if the given geometry node does not contain
     *         a surface or multi surface, or an exception occurred while extracting the geometry.
     */
    public static double calcOuterBoundaryLength( Node node ) {
        double length = 0;
        if ( node != null ) {
            try {
                Geometry geom = GMLGeometryAdapter.wrap( (Element) node, null );
                if ( geom instanceof Surface ) {
                    Ring ring = ( (Surface) geom ).getSurfaceBoundary().getExteriorRing();
                    length = ring.getAsCurveSegment().getLength();
                } else if ( geom instanceof MultiSurface ) {
                    MultiSurface ms = ( (MultiSurface) geom );
                    for ( int i = 0; i < ms.getSize(); i++ ) {
                        Ring ring = ms.getSurfaceAt( i ).getSurfaceBoundary().getExteriorRing();
                        length += ring.getAsCurveSegment().getLength();
                    }
                }
            } catch ( GeometryException e ) {
                LOG.logError( "Could not calculate length of the outer boundary because: " + e.getMessage(), e );
            }

        } else {
            LOG.logWarning( "Could not calculate length of the outer boundary because the given node was null." );
        }
        return length;
    }

    /**
     * Calculates the centroid of the given node.
     * 
     * @param node
     *            to extract the given node from
     * @param targetCRS
     *            to transform the given centroid to.
     * @return the centroid of the given geometry transformed to the targetCRS. or <code>null</code> if it could not be
     *         calculated.
     */
    private static Point calcCentroid( Node node, String targetCRS ) {
        Point point = null;
        if ( node != null ) {
            try {
                if ( "Envelope".equals( node.getLocalName() ) ) {
                    Envelope env = GMLGeometryAdapter.wrapBox( (Element) node, null );
                    point = env.getCentroid();
                } else {
                    Geometry geom = GMLGeometryAdapter.wrap( (Element) node, null );
                    point = geom.getCentroid();
                }
                if ( targetCRS != null && !"".equals( targetCRS ) && point.getCoordinateSystem() != null ) {
                    GeoTransformer gt = new GeoTransformer( targetCRS );
                    point = (Point) gt.transform( point );
                }
            } catch ( InvalidGMLException e ) {
                LOG.logError( "Could not calculate centroid because: " + e.getMessage(), e );
            } catch ( XMLParsingException e ) {
                LOG.logError( "Could not calculate centroid because: " + e.getMessage(), e );
            } catch ( UnknownCRSException e ) {
                LOG.logError( "Could not calculate centroid because: " + e.getMessage(), e );
            } catch ( GeometryException e ) {
                LOG.logError( "Could not calculate centroid because: " + e.getMessage(), e );
            } catch ( IllegalArgumentException e ) {
                LOG.logError( "Could not calculate centroid because: " + e.getMessage(), e );
            } catch ( CRSTransformationException e ) {
                LOG.logError( "Could not calculate centroid because: " + e.getMessage(), e );
            }

        } else {
            LOG.logWarning( "Could not calculate centroid because the given node was null." );
        }
        return point;
    }

    /**
     * returns the X coordinate of the centroid of the geometry represented by the passed Node
     * 
     * @param node
     *            to calculate the centroid from.
     * @param targetCRS
     *            of resulting centroid, may be null
     * @return the x coordinate of the (transformed) centroid or -1 if it could not be calculated.
     */
    public static double getCentroidX( Node node, String targetCRS ) {
        Point p = calcCentroid( node, targetCRS );
        return ( p == null ) ? -1 : p.getX();
    }

    /**
     * returns the y coordinate of the centroid of the geometry represented by the passed Node
     * 
     * @param node
     *            to calculate the centroid from.
     * @param targetCRS
     *            of resulting centroid, may be null
     * @return the y coordinate of the (transformed) centroid or -1 if it could not be calculated.
     */
    public static double getCentroidY( Node node, String targetCRS ) {
        Point p = calcCentroid( node, targetCRS );
        return ( p == null ) ? -1 : p.getY();
    }

    /**
     * Searches for a gml:poslist, gml:pos or gml:coordinates and creates a String from them.
     * 
     * @param node
     *            to get the coordinates from
     * @return A string representation of the coordinates found, or the emtpy string if not.
     */
    public static String getCurveCoordinates( Node node ) {
        StringBuilder sb = new StringBuilder( 10000 );
        if ( node != null ) {
            try {
                List<Node> list = XMLTools.getNodes( node, ".//gml:posList | gml:pos | gml:coordinates", nsc );
                for ( Node node2 : list ) {
                    String s = XMLTools.getStringValue( node2 ).trim();
                    if ( node2.getLocalName().equals( "posList" ) ) {
                        String[] sl = StringTools.toArray( s, " ", false );
                        int dim = XMLTools.getNodeAsInt( node2, "./@srsDimension", nsc, 2 );
                        for ( int i = 0; i < sl.length; i++ ) {
                            sb.append( sl[i] );
                            if ( ( i + 1 ) % dim == 0 ) {
                                sb.append( ' ' );
                            } else {
                                sb.append( ',' );
                            }
                        }
                    } else if ( node2.getLocalName().equals( "pos" ) ) {
                        String[] sl = StringTools.toArray( s, "\t\n\r\f ,", false );
                        for ( int i = 0; i < sl.length; i++ ) {
                            sb.append( sl[i] );
                            if ( i < sl.length - 1 ) {
                                sb.append( ',' );
                            } else {
                                sb.append( ' ' );
                            }
                        }
                    } else if ( node2.getLocalName().equals( "coordinates" ) ) {
                        sb.append( s );
                    }
                }
            } catch ( XMLParsingException e ) {
                LOG.logError( "Could not get the Curve coordinates because: " + e.getMessage(), e );
            }
        } else {
            LOG.logWarning( "Could not get the Curve coordinates because the given node was null." );
        }
        return sb.toString();
    }

    /**
     * Transforms the string contents of a GML 3.1.1-style <code>gml:posList</code> into a GML 2.1-style
     * <code>gml:coordinates</code> element.
     * 
     * @param input
     *            coordinates string that separates all number using withspace characters
     * @param dimension
     * @return translated coordinates string, with a "," between each coordinate and a " " between each ordinate of a
     *         coordinate
     */
    public static String toGML2Coordinates( String input, int dimension ) {

        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer( input );
        if ( st.hasMoreTokens() ) {
            sb.append( st.nextToken() );
        }
        int i = 0;
        while ( st.hasMoreTokens() ) {
            if ( i++ % dimension == 0 ) {
                sb.append( ',' );
            } else {
                sb.append( ' ' );
            }
            String token = st.nextToken();
            sb.append( token );
        }
        return sb.toString();
    }
}
