//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.commons.utils;

import static org.deegree.model.crs.coordinatesystems.GeographicCRS.WGS84;
import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.TransformationException;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.GeometryTransformer;
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

    private static final double SQRT2 = 1.4142135623730950488016887242096980785;

    /**
     * calculates the map scale (denominator) as defined in the OGC SLD 1.0.0 specification
     * 
     * @param mapWidth
     *            map width in pixel
     * @param mapHeight
     *            map height in pixel
     * @param bbox
     *            bounding box of the map
     * @param crs
     *            may be null (bbox.get... will be used instead)
     * 
     * @return the scale
     * @throws RuntimeException
     */
    public static double calcScale( int mapWidth, int mapHeight, Envelope bbox, CoordinateSystem crs )
                            throws RuntimeException {

        if ( crs == null ) {
            crs = bbox.getCoordinateSystem();
        }

        if ( mapWidth == 0 || mapHeight == 0 ) {
            return 0;
        }

        double scale = 0;

        if ( crs == null ) {
            throw new IllegalArgumentException( "No crs given when calculating scale" );
        }

        try {
            boolean meters = true;
            for ( Unit u : crs.getUnits() ) {
                if ( !u.toString().equalsIgnoreCase( "m" ) ) {
                    meters = false;
                    break;
                }
            }

            if ( meters ) {
                /*
                 * this method to calculate a maps scale as defined in OGC WMS and SLD specification is not required for
                 * maps having a projected reference system. Direct calculation of scale avoids uncertainties
                 */
                double bboxWidth = bbox.getWidth();
                double bboxHeight = bbox.getHeight();
                double d1 = Math.sqrt( ( mapWidth * mapWidth ) + ( mapHeight * mapHeight ) );
                double d2 = Math.sqrt( ( bboxWidth * bboxWidth ) + ( bboxHeight * bboxHeight ) );
                scale = d2 / d1;
            } else {
                if ( !crs.equals( WGS84 ) ) {
                    // transform the bounding box of the request to EPSG:4326
                    GeometryTransformer trans = new GeometryTransformer( WGS84 );
                    bbox = (Envelope) trans.transform( bbox, crs );
                }
                double dx = bbox.getWidth() / mapWidth;
                double dy = bbox.getHeight() / mapHeight;

                double distance = calcDistance( bbox.getMin().getX() + dx * ( mapWidth / 2d - 1 ),
                                                bbox.getMin().getY() + dy * ( mapHeight / 2d - 1 ),
                                                bbox.getMin().getX() + dx * ( mapWidth / 2d ), bbox.getMin().getY()
                                                                                               + dy * ( mapHeight / 2d ) );

                scale = distance * SQRT2;

            }
        } catch ( TransformationException e ) {
            LOG.error( "Cannot transform to WGS84, so scale cannot be calculated." );
        }

        return scale;

    }

    /**
     * calculates the distance in meters between two points in EPSG:4326 coordinates. this is a convenience method
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

        // * 0.75 is just an heuristic correction factor
        return dist * 1000 * 0.75;
    }

}
