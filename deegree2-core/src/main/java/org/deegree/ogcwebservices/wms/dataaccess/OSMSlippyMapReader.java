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

package org.deegree.ogcwebservices.wms.dataaccess;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfoResult;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;

/**
 * TODO add class documentation here.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OSMSlippyMapReader implements ExternalRasterDataAccess {

    private static final ILogger LOG = LoggerFactory.getLogger( OSMSlippyMapReader.class );

    private static int tileSize = 256;

    private static CoordinateSystem crsWGS84;

    private static CoordinateSystem crsOSM;

    private static GeoTransformer gt2OSM;

    private static GeoTransformer gt2WGS84;

    private int width;

    private int height;

    private Envelope envelope;

    private BufferedImage target;

    private Envelope targetEnv;

    private Properties props;

    private BufferedImage legend;

    private int minLevel = 0;

    private int maxLevel = 18;

    private String extension = ".png";

    // String base = "http://andy.sandbox.cloudmade.com/tiles/cycle/";
    // String base = "http://tile.openstreetmap.org/";
    // String base = "http://andy.sandbox.cloudmade.com/tiles/cycle/";

    static Map<double[], Integer> levels;
    static {
        if ( levels == null ) {
            levels = new LinkedHashMap<double[], Integer>( 20 );
            double min = 0;
            double max = 0.844517829;
            for ( int i = 0; i < 19; i++ ) {
                levels.put( new double[] { min, max }, 18 - i );
                min = max;
                max *= 2d;
            }
            try {
                crsWGS84 = CRSFactory.create( "EPSG:4326" );
                crsOSM = CRSFactory.create( "OSM_SLIPPY_MAP" );
                gt2OSM = new GeoTransformer( crsOSM );
                gt2WGS84 = new GeoTransformer( crsWGS84 );
            } catch ( UnknownCRSException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * default initialization of configuration
     */
    private void init() {
        if ( props == null ) {
            props = new Properties();
            props.put( "TILEROOT", "http://tile.openstreetmap.org/" );
            props.put( "LEGEND", props.getProperty( "TILEROOT" ) + "14/8514/5504.png" );
        }
    }

    /**
     *
     * @param lat
     * @param lon
     * @param zoom
     * @return
     */
    private int[] getTileNumber( final double lon, final double lat, final int zoom ) {
        int xtile = (int) Math.floor( ( lon + 180 ) / 360 * ( 1 << zoom ) );
        int ytile = (int) Math.floor( ( 1 - Math.log( Math.tan( lat * Math.PI / 180 ) + 1
                                                      / Math.cos( lat * Math.PI / 180 ) )
                                            / Math.PI )
                                      / 2 * ( 1 << zoom ) );

        return new int[] { xtile, ytile };
    }

    private void readSlippyMaps()
                            throws Exception {

        double scale = MapUtils.calcScale( width, height, envelope, crsWGS84, 1 );
        int level = calculateLevel( scale );

        List<List<String>> tiles = new ArrayList<List<String>>( 5 );

        double cx = envelope.getMin().getX();
        double cy = envelope.getMin().getY();
        double[] bb = new double[] { 0, 0, 0, 0 };
        int[] idx = getTileNumber( cx, cy, level );
        int xx = idx[0];
        int yy = idx[1];
        double minx = 9E99;
        double miny = 9E99;
        double maxx = -9E99;
        double maxy = -9E99;
        while ( cy < envelope.getMax().getY() + ( bb[3] - bb[1] ) ) {
            List<String> row = new ArrayList<String>( 10 );
            while ( cx < envelope.getMax().getX() + ( bb[2] - bb[0] ) ) {
                bb = getTileBBox( xx, yy, level );
                String s = props.getProperty( "TILEROOT" ) + level + "/" + xx + "/" + yy + extension;
                xx++;
                row.add( s );
                if ( bb[0] < minx ) {
                    minx = bb[0];
                }
                if ( bb[1] < miny ) {
                    miny = bb[1];
                }
                if ( bb[2] > maxx ) {
                    maxx = bb[2];
                }
                if ( bb[3] > maxy ) {
                    maxy = bb[3];
                }
                cx = cx + ( bb[2] - bb[0] );
            }
            cx = envelope.getMin().getX();
            cy = cy + ( bb[3] - bb[1] );
            tiles.add( row );
            xx = idx[0];
            yy--;
        }

        target = new BufferedImage( tiles.get( 0 ).size() * tileSize, tiles.size() * tileSize,
                                    BufferedImage.TYPE_INT_RGB );
        Graphics g = target.getGraphics();

        OSMReader[] threads = new OSMReader[tiles.size()];
        for ( int i = 0; i < tiles.size(); i++ ) {
            List<String> row = tiles.get( i );
            threads[i] = new OSMReader( row, i, g );
            threads[i].start();
        }
        while ( !isFinished( threads ) ) {
            Thread.sleep( 50 );
        }
        g.dispose();

        // calc result resolution in x- and y-direction ...
        Point minP = (Point) gt2OSM.transform( GeometryFactory.createPoint( minx, miny, crsWGS84 ) );
        Point maxP = (Point) gt2OSM.transform( GeometryFactory.createPoint( maxx, maxy, crsWGS84 ) );
        double resx = ( maxP.getX() - minP.getX() ) / target.getWidth();
        double resy = ( maxP.getY() - minP.getY() ) / target.getHeight();
        // ... and rescale image to have same resolutions in both directions because otherwise
        // deegree and other clients will not be able to render it correctly
        if ( resx != resy ) {
            target = scale( target, resx, resy );
        }
        targetEnv = GeometryFactory.createEnvelope( minP.getPosition(), maxP.getPosition(), crsOSM );
    }

    /**
     * @param threads
     * @return
     */
    private boolean isFinished( OSMReader[] threads ) {
        for ( OSMReader reader : threads ) {
            if ( !reader.isFinished() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param target
     * @param resx
     * @param resy
     * @return
     */
    private BufferedImage scale( BufferedImage target, double resx, double resy ) {
        Interpolation interpolation = new InterpolationBilinear();
        double scaleX = 1;
        double scaleY = 1;
        if ( resx < resy ) {
            scaleY = resy / resx;
        } else {
            scaleX = resx / resy;
        }
        LOG.logDebug( "Scale image: by factors: " + scaleX + ' ' + scaleY );
        ParameterBlock pb = new ParameterBlock();
        pb.addSource( target );
        pb.add( (float) scaleX ); // The xScale
        pb.add( (float) scaleY ); // The yScale
        pb.add( 0.0F ); // The x translation
        pb.add( 0.0F ); // The y translation
        pb.add( interpolation ); // The interpolation
        // Create the scale operation
        RenderedOp ro = JAI.create( "scale", pb, null );
        try {
            target = ro.getAsBufferedImage();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return target;
    }

    private double[] getTileBBox( final int x, final int y, final int zoom ) {
        double north = tile2lat( y, zoom );
        double south = tile2lat( y + 1, zoom );
        double west = tile2lon( x, zoom );
        double east = tile2lon( x + 1, zoom );
        return new double[] { west, south, east, north };
    }

    private static double tile2lon( int x, int z ) {
        return ( x / Math.pow( 2.0, z ) * 360.0 ) - 180;
    }

    private static double tile2lat( int y, int z ) {
        double n = Math.PI - ( ( 2.0 * Math.PI * y ) / Math.pow( 2.0, z ) );
        return 180.0 / Math.PI * Math.atan( 0.5 * ( Math.exp( n ) - Math.exp( -n ) ) );
    }

    /**
     * @param scale
     * @return required OSM slippy map level
     */
    private int calculateLevel( double scale ) {
        Iterator<double[]> range = levels.keySet().iterator();
        while ( range.hasNext() ) {
            double[] ds = (double[]) range.next();
            if ( scale > ds[0] && scale <= ds[1] ) {
                int l = levels.get( ds );
                if ( l < minLevel ) {
                    l = minLevel;
                } else if ( l > maxLevel ) {
                    l = maxLevel;
                }
                return l;
            }
        }
        return minLevel;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalRasterDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetMap)
     */
    public synchronized GetMapResult perform( GetMap getMap )
                            throws Exception {
        width = getMap.getWidth();
        height = getMap.getHeight();
        Envelope gmBBox = getMap.getBoundingBox();
        CoordinateSystem gmCRS = CRSFactory.create( getMap.getSrs() );
        if ( !gmCRS.equals( crsWGS84 ) ) {
            envelope = gt2WGS84.transform( gmBBox, gmCRS, true );
        } else {
            envelope = gmBBox;
        }
        // do the work
        readSlippyMaps();

        // create a result map/image that matches the requested one
        GeoTransformer gt = new GeoTransformer( gmCRS );
        target = gt.transform( target, targetEnv, gmBBox, width, height, 6, 3,
                               Interpolation.getInstance( Interpolation.INTERP_BILINEAR ) );
        return new GetMapResult( getMap, target );
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetFeatureInfo)
     */
    public GetFeatureInfoResult perform( GetFeatureInfo gfi ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetLegendGraphic)
     */
    public BufferedImage perform( GetLegendGraphic glg ) {
        return legend;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#setConfigurationFile(java.net.URL)
     */
    public void setConfigurationFile( URL url )
                            throws IOException {
        if ( url == null ) {
            init();
            try {
                BufferedImage tmp = ImageUtils.loadImage( new URL( props.getProperty( "LEGEND" ) ) );
                legend = new BufferedImage( tileSize, tileSize, BufferedImage.TYPE_INT_RGB );
                Graphics g = legend.getGraphics();
                g.drawImage( tmp, 0, 0, null );
                g.dispose();
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
            }
        } else {
            props = new Properties();
            InputStream is = url.openStream();
            props.load( is );
            is.close();
            if ( props.get( "TILEROOT" ) == null ) {
                props.put( "TILEROOT", "http://tile.openstreetmap.org/" );
            }
            if ( !props.getProperty( "TILEROOT" ).endsWith( "/" ) ) {
                props.put( "TILEROOT", props.getProperty( "TILEROOT" ) + "/" );
            }
            if ( props.getProperty( "TILEROOT" ).startsWith( "http://tah.openstreetmap.org" ) ) {
                maxLevel = 17;
            } else if ( props.getProperty( "TILEROOT" ).startsWith( "http://tah.openstreetmap.org" ) ) {
                minLevel = 12;
                maxLevel = 16;
            } else if ( props.getProperty( "TILEROOT" ).startsWith( "http://richard.dev.openstreetmap.org" ) ) {
                minLevel = 13;
                maxLevel = 15;
                extension = ".jpg";
            }

            if ( props.get( "LEGEND" ) == null ) {
                props.put( "LEGEND", "http://tile.openstreetmap.org/14/8514/5504.png" );
            }
            XMLFragment dummy = new XMLFragment();
            dummy.setSystemId( url );
            URL legendURL = dummy.resolve( props.getProperty( "LEGEND" ) );
            try {
                BufferedImage tmp = ImageUtils.loadImage( legendURL );
                legend = new BufferedImage( tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_RGB );
                Graphics g = legend.getGraphics();
                g.drawImage( tmp, 0, 0, null );
                g.dispose();
                return;
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
        LOG.logDebug( "properties: ", props );
       
    }

    // //////////////////////////////////////////////////////////////////////////////
    // inner classes
    // //////////////////////////////////////////////////////////////////////////////

    private class OSMReader extends Thread {

        private List<String> row;

        private int y;

        private Graphics g;

        private boolean finished = false;

        /**
         *
         * @param row
         * @param y
         */
        OSMReader( List<String> row, int y, Graphics g ) {
            this.row = row;
            this.y = y;
            this.g = g;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {
            for ( int j = 0; j < row.size(); j++ ) {
                BufferedImage tmp;
                try {
                    tmp = ImageUtils.loadImage( new URL( row.get( j ) ) );
                } catch ( IOException e ) {
                    throw new RuntimeException( e );
                }
                synchronized ( g ) {
                    g.drawImage( tmp, j * tileSize, target.getHeight() - ( y * tileSize ) - tileSize, null );
                }
            }
            finished = true;
        }

        /**
         * @return the finished
         */
        public boolean isFinished() {
            return finished;
        }

    }
}
