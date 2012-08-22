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
package org.deegree.framework.util;

import static java.lang.Math.sqrt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;

/**
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class MapUtils {

    private static ILogger LOG = LoggerFactory.getLogger( MapUtils.class );

    /**
     * The value of sqrt(2)
     */
    public static final double SQRT2 = sqrt( 2 );

    /**
     * The Value of a PixelSize
     */
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

        try {
            if ( "m".equalsIgnoreCase( crs.getAxisUnits()[0].toString() ) ) {
                /*
                 * this method to calculate a maps scale as defined in OGC WMS and SLD specification is not required for
                 * maps having a projected reference system. Direct calculation of scale avoids uncertainties
                 */
                double dx = bbox.getWidth() / mapWidth;
                double dy = bbox.getHeight() / mapHeight;
                scale = sqrt( dx * dx + dy * dy );
            } else {

                if ( !crs.getIdentifier().equalsIgnoreCase( "EPSG:4326" ) ) {
                    // transform the bounding box of the request to EPSG:4326
                    GeoTransformer trans = new GeoTransformer( CRSFactory.create( "EPSG:4326" ) );
                    bbox = trans.transform( bbox, crs );
                }
                double dx = bbox.getWidth() / mapWidth;
                double dy = bbox.getHeight() / mapHeight;
                Position min = GeometryFactory.createPosition( bbox.getMin().getX() + dx * ( mapWidth / 2d - 1 ),
                                                               bbox.getMin().getY() + dy * ( mapHeight / 2d - 1 ) );
                Position max = GeometryFactory.createPosition( bbox.getMin().getX() + dx * ( mapWidth / 2d ),
                                                               bbox.getMin().getY() + dy * ( mapHeight / 2d ) );

                double distance = calcDistance( min.getX(), min.getY(), max.getX(), max.getY() );

                scale = distance / SQRT2;

            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RuntimeException( Messages.getMessage( "FRAMEWORK_ERROR_SCALE_CALC", e.getMessage() ) );
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

        try {
            if ( "m".equalsIgnoreCase( crs.getAxisUnits()[0].toString() ) ) {
                /*
                 * this method to calculate a maps scale as defined in OGC WMS and SLD specification is not required for
                 * maps having a projected reference system. Direct calculation of scale avoids uncertainties
                 */
                double dx = bbox.getWidth() / mapWidth;
                scale = dx / DEFAULT_PIXEL_SIZE;
            } else {

                if ( !crs.getIdentifier().equalsIgnoreCase( "EPSG:4326" ) ) {
                    // transform the bounding box of the request to EPSG:4326
                    GeoTransformer trans = new GeoTransformer( CRSFactory.create( "EPSG:4326" ) );
                    bbox = trans.transform( bbox, crs );
                }
                double dx = bbox.getWidth() / mapWidth;
                double dy = bbox.getHeight() / mapHeight;

                double minx = bbox.getMin().getX() + dx * ( mapWidth / 2d - 1 );
                double miny = bbox.getMin().getY() + dy * ( mapHeight / 2d - 1 );
                double maxx = bbox.getMin().getX() + dx * ( mapWidth / 2d );
                double maxy = bbox.getMin().getY() + dy * ( mapHeight / 2d - 1 );

                double distance = calcDistance( minx, miny, maxx, maxy );

                scale = distance / SQRT2 / DEFAULT_PIXEL_SIZE;

            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RuntimeException( Messages.getMessage( "FRAMEWORK_ERROR_SCALE_CALC", e.getMessage() ) );
        }

        return scale;
    }

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
     *            coordinate reference system of the map
     * @param pixelSize
     *            size of one pixel of the map measured in meter
     * 
     * @return a maps scale based on the diagonal size of a pixel at the center of the map in meter.
     * @throws RuntimeException
     */
    public static double calcScale( int mapWidth, int mapHeight, Envelope bbox, CoordinateSystem crs, double pixelSize )
                            throws RuntimeException {

        double sqpxsize;
        if ( pixelSize == 1d ) {
            LOG.logDebug( "Calculating WMS 1.1.1 scale." );
            return calcScaleWMS111( mapWidth, mapHeight, bbox, crs );
        } else if ( pixelSize == DEFAULT_PIXEL_SIZE ) {
            LOG.logDebug( "Calculating WMS 1.3.0 scale." );
            return calcScaleWMS130( mapWidth, mapHeight, bbox, crs );
        } else {
            sqpxsize = pixelSize * pixelSize;
            sqpxsize += sqpxsize;
            sqpxsize = sqrt( sqpxsize );
        }

        if ( mapWidth == 0 || mapHeight == 0 ) {
            return 0;
        }

        double scale = 0;

        CoordinateSystem cs = crs;

        if ( cs == null ) {
            throw new RuntimeException( "Invalid crs: " + crs );
        }

        try {
            if ( "m".equalsIgnoreCase( cs.getAxisUnits()[0].toString() ) ) {
                /*
                 * this method to calculate a maps scale as defined in OGC WMS and SLD specification is not required for
                 * maps having a projected reference system. Direct calculation of scale avoids uncertainties
                 */
                double dx = bbox.getWidth() / mapWidth;
                double dy = bbox.getHeight() / mapHeight;
                scale = Math.sqrt( dx * dx + dy * dy ) / sqpxsize;
            } else {

                if ( !crs.getIdentifier().equalsIgnoreCase( "EPSG:4326" ) ) {
                    // transform the bounding box of the request to EPSG:4326
                    GeoTransformer trans = new GeoTransformer( CRSFactory.create( "EPSG:4326" ) );
                    bbox = trans.transform( bbox, crs );
                }
                double dx = bbox.getWidth() / mapWidth;
                double dy = bbox.getHeight() / mapHeight;
                Position min = GeometryFactory.createPosition( bbox.getMin().getX() + dx * ( mapWidth / 2d - 1 ),
                                                               bbox.getMin().getY() + dy * ( mapHeight / 2d - 1 ) );
                Position max = GeometryFactory.createPosition( bbox.getMin().getX() + dx * ( mapWidth / 2d ),
                                                               bbox.getMin().getY() + dy * ( mapHeight / 2d ) );

                double distance = calcDistance( min.getX(), min.getY(), max.getX(), max.getY() );

                scale = distance / sqpxsize;

            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RuntimeException( Messages.getMessage( "FRAMEWORK_ERROR_SCALE_CALC", e.getMessage() ) );
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

    /**
     * The method calculates a new Envelope from the <code>requestedBarValue</code> It will either zoom in or zoom out
     * of the <code>actualBBOX<code> depending
     * on the ratio of the <code>requestedBarValue</code> to the <code>actualBarValue</code>
     * 
     * @param currentEnvelope
     *            current Envelope
     * @param currentScale
     *            the scale of the current envelope
     * @param requestedScale
     *            requested scale value
     * @return a new Envelope
     */
    public static Envelope scaleEnvelope( Envelope currentEnvelope, double currentScale, double requestedScale ) {

        double ratio = requestedScale / currentScale;
        double newWidth = currentEnvelope.getWidth() * ratio;
        double newHeight = currentEnvelope.getHeight() * ratio;
        double midX = currentEnvelope.getMin().getX() + ( currentEnvelope.getWidth() / 2d );
        double midY = currentEnvelope.getMin().getY() + ( currentEnvelope.getHeight() / 2d );

        double minx = midX - newWidth / 2d;
        double maxx = midX + newWidth / 2d;
        double miny = midY - newHeight / 2d;
        double maxy = midY + newHeight / 2d;

        return GeometryFactory.createEnvelope( minx, miny, maxx, maxy, currentEnvelope.getCoordinateSystem() );

    }

    /**
     * This method ensures the bbox is resized (shrunk) to match the aspect ratio defined by mapHeight/mapWidth
     * 
     * @param bbox
     * @param mapWith
     * @param mapHeight
     * @return a new bounding box with the aspect ratio given my mapHeight/mapWidth
     */
    public static final Envelope ensureAspectRatio( Envelope bbox, double mapWith, double mapHeight ) {

        double minx = bbox.getMin().getX();
        double miny = bbox.getMin().getY();
        double maxx = bbox.getMax().getX();
        double maxy = bbox.getMax().getY();

        double dx = maxx - minx;
        double dy = maxy - miny;

        double ratio = mapHeight / mapWith;

        if ( dx >= dy ) {
            // height has to be corrected
            double[] normCoords = getNormalizedCoords( dx, ratio, miny, maxy );
            miny = normCoords[0];
            maxy = normCoords[1];
        } else {
            // width has to be corrected
            ratio = mapWith / mapHeight;
            double[] normCoords = getNormalizedCoords( dy, ratio, minx, maxx );
            minx = normCoords[0];
            maxx = normCoords[1];
        }
        CoordinateSystem crs = bbox.getCoordinateSystem();

        return GeometryFactory.createEnvelope( minx, miny, maxx, maxy, crs );
    }

    private static final double[] getNormalizedCoords( double normLen, double ratio, double min, double max ) {
        double mid = ( max - min ) / 2 + min;
        min = mid - ( normLen / 2 ) * ratio;
        max = mid + ( normLen / 2 ) * ratio;
        double[] newCoords = { min, max };
        return newCoords;
    }

    /**
     * 
     * @param img
     * @param bbox
     * @param mapSize
     * @param fontName
     * @param fontSize
     */
    public static void drawScalbar( Graphics2D g, int desiredSize, Envelope bbox, Dimension mapSize, String fontName,
                                    int fontSize ) {

        desiredSize -= 30;
        GeoTransform gt = new WorldToScreenTransform( bbox.getMin().getX(), bbox.getMin().getY(), bbox.getMax().getX(),
                                                      bbox.getMax().getY(), 0, 0, mapSize.getWidth() - 1,
                                                      mapSize.getHeight() - 1 );

        // calculate scale bar max scale and size
        int length = 0;
        double lx = gt.getDestX( bbox.getMin().getX() );
        double scale = 0;
        for ( int i = 0; i < 100; i++ ) {
            double k = 0;
            double dec = 30 * Math.pow( 10, i );
            for ( int j = 0; j < 9; j++ ) {
                k += dec;
                double tx = gt.getDestX( bbox.getMin().getX() + k );
                if ( Math.abs( tx - lx ) < desiredSize ) {
                    length = (int) Math.round( Math.abs( tx - lx ) );
                    scale = k;
                } else {
                    break;
                }
            }
        }

        // draw scale bar base line
        g.setStroke( new BasicStroke( ( desiredSize + 30 ) / 250 ) );
        g.setColor( Color.black );
        g.drawLine( 10, 30, length + 10, 30 );
        double dx = length / 3d;
        double vdx = scale / 3;
        double div = 1;
        String uom = "m";
        if ( scale > 1000 ) {
            div = 1000;
            uom = "km";
        }
        // draw scale bar scales
        if ( fontName == null ) {
            fontName = "SANS SERIF";
        }
        g.setFont( new Font( fontName, Font.PLAIN, fontSize ) );
        DecimalFormat df = new DecimalFormat( "##.# " + uom );
        for ( int i = 0; i < 4; i++ ) {
            double val = ( vdx * i ) / div;
            g.drawString( df.format( val ), (int) Math.round( 10 + i * dx ) - 8, 10 );
            g.drawLine( (int) Math.round( 10 + i * dx ), 30, (int) Math.round( 10 + i * dx ), 20 );
        }
        for ( int i = 0; i < 7; i++ ) {
            g.drawLine( (int) Math.round( 10 + i * dx / 2d ), 30, (int) Math.round( 10 + i * dx / 2d ), 25 );
        }

        g.dispose();

    }

}
