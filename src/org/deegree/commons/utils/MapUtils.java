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

package org.deegree.commons.utils;

import static java.lang.Math.sqrt;
import static org.deegree.crs.coordinatesystems.GeographicCRS.WGS84;
import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.slf4j.Logger;

/**
 * <code>MapUtils</code>
 * 
 * Note: methods currently more or less copied from deegree 2.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MapUtils {

    private static final Logger LOG = getLogger( MapUtils.class );

    /** The value of sqrt(2) */
    public static final double SQRT2 = sqrt( 2 );

    /** The Value of a PixelSize */
    public static final double DEFAULT_PIXEL_SIZE = 0.00028;

    /**
     * @param mapWidth
     * @param mapHeight
     * @param bbox
     * @param crs
     * @return the WMS 1.1.1 scale (size of the diagonal pixel)
     */
    public static double calcScaleWMS111( int mapWidth, int mapHeight, Envelope bbox, CoordinateSystem crs ) {
        if ( mapWidth == 0 || mapHeight == 0 ) {
            return 0;
        }
        double scale = 0;

        if ( crs == null ) {
            throw new RuntimeException( "Invalid crs: " + crs );
        }

        if ( "m".equalsIgnoreCase( crs.getAxis()[0].getUnits().toString() ) ) {
            /*
             * this method to calculate a maps scale as defined in OGC WMS and SLD specification is not required for
             * maps having a projected reference system. Direct calculation of scale avoids uncertainties
             */
            double dx = bbox.getSpan0() / mapWidth;
            double dy = bbox.getSpan1() / mapHeight;
            scale = sqrt( dx * dx + dy * dy );
        } else {

            if ( !crs.equals( WGS84 ) ) {
                // transform the bounding box of the request to EPSG:4326
                GeometryTransformer trans = new GeometryTransformer( WGS84 );
                try {
                    bbox = (Envelope) trans.transform( bbox, crs );
                } catch ( IllegalArgumentException e ) {
                    LOG.error( "Unknown error", e );
                } catch ( TransformationException e ) {
                    LOG.error( "Unknown error", e );
                }
            }
            double dx = bbox.getSpan0() / mapWidth;
            double dy = bbox.getSpan1() / mapHeight;
            double minx = bbox.getMin().get0() + dx * ( mapWidth / 2d - 1 );
            double miny = bbox.getMin().get1() + dy * ( mapHeight / 2d - 1 );
            double maxx = bbox.getMin().get0() + dx * ( mapWidth / 2d );
            double maxy = bbox.getMin().get1() + dy * ( mapHeight / 2d );

            double distance = calcDistance( minx, miny, maxx, maxy );

            scale = distance / SQRT2;

        }

        return scale;
    }

    /**
     * @param mapWidth
     * @param mapHeight
     * @param bbox
     * @param crs
     * @return the WMS 1.3.0 scale (horizontal size of the pixel, pixel size == 0.28mm)
     */
    public static double calcScaleWMS130( int mapWidth, int mapHeight, Envelope bbox, CoordinateSystem crs ) {
        if ( mapWidth == 0 || mapHeight == 0 ) {
            return 0;
        }

        double scale = 0;

        if ( crs == null ) {
            throw new RuntimeException( "Invalid crs: " + crs );
        }

        if ( "m".equalsIgnoreCase( crs.getAxis()[0].getUnits().toString() ) ) {
            /*
             * this method to calculate a maps scale as defined in OGC WMS and SLD specification is not required for
             * maps having a projected reference system. Direct calculation of scale avoids uncertainties
             */
            double dx = bbox.getSpan0() / mapWidth;
            scale = dx / DEFAULT_PIXEL_SIZE;
        } else {

            if ( !crs.equals( WGS84 ) ) {
                // transform the bounding box of the request to EPSG:4326
                GeometryTransformer trans = new GeometryTransformer( WGS84 );
                try {
                    bbox = (Envelope) trans.transform( bbox, crs );
                } catch ( IllegalArgumentException e ) {
                    LOG.error( "Unknown error", e );
                } catch ( TransformationException e ) {
                    LOG.error( "Unknown error", e );
                }
            }
            double dx = bbox.getSpan0() / mapWidth;
            double dy = bbox.getSpan1() / mapHeight;

            double minx = bbox.getMin().get0() + dx * ( mapWidth / 2d - 1 );
            double miny = bbox.getMin().get1() + dy * ( mapHeight / 2d - 1 );
            double maxx = bbox.getMin().get0() + dx * ( mapWidth / 2d );
            double maxy = bbox.getMin().get1() + dy * ( mapHeight / 2d - 1 );

            double distance = calcDistance( minx, miny, maxx, maxy );

            scale = distance / SQRT2 / DEFAULT_PIXEL_SIZE;

        }

        return scale;
    }

    /**
     * calculates the distance in meters between two points in EPSG:4326 coodinates. this is a convenience method
     * assuming the world is a ball
     * 
     * @param lon1
     * @param lat1
     * @param lon2
     * @param lat2
     * @return the distance in meters between two points in EPSG:4326 coords
     */
    public static double calcDistance( double lon1, double lat1, double lon2, double lat2 ) {
        double r = 6378.137;
        double rad = Math.PI / 180d;
        double cose = Math.sin( rad * lon1 ) * Math.sin( rad * lon2 ) + Math.cos( rad * lon1 ) * Math.cos( rad * lon2 )
                      * Math.cos( rad * ( lat1 - lat2 ) );
        double dist = r * Math.acos( cose ) * Math.cos( rad * Math.min( lat1, lat2 ) );

        // * 0.835 is just an heuristic correction factor
        return dist * 1000 * 0.835;
    }

}
