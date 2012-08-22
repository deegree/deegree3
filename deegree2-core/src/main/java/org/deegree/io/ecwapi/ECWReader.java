//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2003 by:
 IDgis bv, Holten, The Netherlands
 http://www.idgis.nl

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.ecwapi;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.spatialschema.Envelope;

import com.ermapper.ecw.JNCSException;
import com.ermapper.ecw.JNCSFile;

/**
 * ECWReader.java
 * 
 * @author Herman Assink
 * @author last edited by: $Author$
 * @version 1.0 2003-11-06
 */

public class ECWReader {

    private static final ILogger LOG = LoggerFactory.getLogger( ECWReader.class );

    private static boolean ECW_USE_CACHE = true;

    private boolean usedCacheOnOpen = true;

    private JNCSFile ecwFile;

    /**
     * read part from ECW-file which falls within env and return this part dimenions width and height
     * 
     * @param fileName
     *            full pathname of the ECW-file
     * @throws JNCSException
     */
    public ECWReader( String fileName ) throws JNCSException {

        if ( fileName.toLowerCase().startsWith( "file:/" ) ) {
            try {
                File f = new File( new URL( fileName ).getFile() );
                fileName = f.getAbsolutePath();
            } catch ( Exception e ) {
                LOG.logDebug( "Stack trace of eaten exception: ", e );
                throw new JNCSException( fileName + " is not a valid URL" );
            }
        }
        LOG.logDebug( "ECWReader: " + fileName );

        if ( ECW_USE_CACHE ) {
            this.ecwFile = ECWFileCache.claimAccess( fileName );
            usedCacheOnOpen = true;
        } else {
            this.ecwFile = new JNCSFile( fileName, false );
            usedCacheOnOpen = false;
        }
    }

    /**
     * Decide, if to use the cache.
     * <p>
     * Default is TRUE.
     */
    public static void useECWCache( boolean Use ) {
        ECW_USE_CACHE = Use;
    }

    /**
     * Free the memory of the image cache
     */
    public void close() {
        if ( usedCacheOnOpen ) {
            ECWFileCache.releaseFile( ecwFile );
        } else {
            ecwFile.close( true );
        }
    }

    /**
     * retuns the width of the entire image encapsulated in the ECW file
     * 
     * @return width of the image
     */
    public int getWidth() {
        return ecwFile.width;
    }

    /**
     * retuns the height of the entire image encapsulated in the ECW file
     * 
     * @return height of the image
     */
    public int getHeight() {
        return ecwFile.height;
    }

    /**
     * read part from ECW-file which falls within env and return this part as BufferedImage with dimenions width and
     * height
     * 
     * @param env
     *            bounding box in world coordinates of requested part
     * @param width
     *            width of the returned image
     * @param height
     *            height of the returned image
     */
    public BufferedImage getBufferedImage( Envelope env, int width, int height )
                            throws JNCSException {

        int bandlist[];
        int line, pRGBArray[] = null;

        // Setup the view parameters for the ecw file.
        bandlist = new int[ecwFile.numBands];
        for ( int i = 0; i < ecwFile.numBands; i++ ) {
            bandlist[i] = i;
        }

        // Check if the envelope is within the area of the ecw-image
        double dWorldTLXRequest = env.getMin().getX();
        double dWorldTLYRequest = env.getMax().getY();

        double dWorldTLX = dWorldTLXRequest;
        double dWorldTLY = dWorldTLYRequest;

        LOG.logDebug( "tlx: " + dWorldTLX + " tly: " + dWorldTLY );

        if ( dWorldTLX < ecwFile.originX )
            dWorldTLX = ecwFile.originX;
        if ( dWorldTLY > ecwFile.originY )
            dWorldTLY = ecwFile.originY;

        double dWorldBRXRequest = env.getMax().getX();
        double dWorldBRYRequest = env.getMin().getY();

        double dWorldBRX = dWorldBRXRequest;
        double dWorldBRY = dWorldBRYRequest;

        LOG.logDebug( "brx: " + dWorldBRX + " bry: " + dWorldBRY );

        if ( dWorldBRX > ( ecwFile.originX + ( ( ecwFile.width - 1 ) * ecwFile.cellIncrementX ) ) ) // Huh?
            // ECW
            // does
            // not
            // except
            // the
            // full
            // width
            dWorldBRX = ecwFile.originX + ( ( ecwFile.width - 1 ) * ecwFile.cellIncrementX );
        if ( dWorldBRY < ( ecwFile.originY + ( ecwFile.height * ecwFile.cellIncrementY ) - ( ecwFile.cellIncrementY / 2 ) ) )
            dWorldBRY = ecwFile.originY + ( ecwFile.height * ecwFile.cellIncrementY ) - ( ecwFile.cellIncrementY / 2 );

        // Work out the correct aspect for the setView call.
        // double dEnvAspect = (dWorldBRX - dWorldTLX) / (dWorldTLY - dWorldBRY);
        // double dImgAspect = (double) width / (double) height;

        LOG.logDebug( "tlx: " + dWorldTLX + " tly: " + dWorldTLY );
        LOG.logDebug( "brx: " + dWorldBRX + " bry: " + dWorldBRY );
        LOG.logDebug( "width: " + width + " height: " + height );

        int nDatasetTLX = (int) Math.round( ( dWorldTLX - ecwFile.originX ) / ecwFile.cellIncrementX );
        int nDatasetTLY = (int) Math.round( ( dWorldTLY - ecwFile.originY ) / ecwFile.cellIncrementY );

        LOG.logDebug( "ptlx: " + nDatasetTLX + " ptly: " + nDatasetTLY );

        int nDatasetBRX = (int) Math.round( ( dWorldBRX - ecwFile.originX ) / ecwFile.cellIncrementX );
        int nDatasetBRY = (int) Math.round( ( dWorldBRY - ecwFile.originY ) / ecwFile.cellIncrementY );

        LOG.logDebug( "pbrx: " + nDatasetBRX + " pbry: " + nDatasetBRY );

        if ( nDatasetBRX > ( ecwFile.width - 1 ) )
            nDatasetBRX = ecwFile.width - 1;
        if ( nDatasetBRY > ( ecwFile.height - 1 ) )
            nDatasetBRY = ecwFile.height - 1;

        LOG.logDebug( "pbrx: " + nDatasetBRX + " pbry: " + nDatasetBRY );

        // Check for supersampling
        int viewWidth = width;
        int viewHeight = height;
        if ( ( nDatasetBRX - nDatasetTLX ) < viewWidth || ( nDatasetBRY - nDatasetTLY ) < viewHeight ) {
            viewWidth = nDatasetBRX - nDatasetTLX;
            viewHeight = nDatasetBRY - nDatasetTLY;
        }

        double requestWidth = dWorldBRXRequest - dWorldTLXRequest;
        double worldWidth = dWorldBRX - dWorldTLX;
        if ( requestWidth > worldWidth ) {
            viewWidth *= worldWidth / requestWidth;
        }

        double requestHeight = dWorldTLYRequest - dWorldBRYRequest;
        double worldHeight = dWorldTLY - dWorldBRY;
        if ( requestHeight > worldHeight ) {
            viewHeight *= worldHeight / requestHeight;
        }

        if ( viewWidth == 0 )
            viewWidth = 1;
        if ( viewHeight == 0 )
            viewHeight = 1;

        LOG.logDebug( "Width: " + width + " Height: " + height );
        LOG.logDebug( "viewWidth: " + viewWidth + " viewHeight: " + viewHeight );

        // Create an image of the ecw file.
        BufferedImage ecwImage = new BufferedImage( viewWidth, viewHeight, BufferedImage.TYPE_INT_RGB );
        pRGBArray = new int[width];

        // Set the view
        ecwFile.setView( ecwFile.numBands, bandlist, nDatasetTLX, nDatasetTLY, nDatasetBRX, nDatasetBRY, viewWidth,
                         viewHeight );

        // Read the scan lines
        for ( line = 0; line < viewHeight; line++ ) {
            ecwFile.readLineRGBA( pRGBArray );
            ecwImage.setRGB( 0, line, viewWidth, 1, pRGBArray, 0, viewWidth );
        }

        if ( width != viewWidth || height != viewHeight ) {
            LOG.logDebug( "create larger image" );

            int destX = 0, destY = 0, destWidth = width, destHeight = height;

            if ( requestWidth > worldWidth ) {
                double pixWidth = requestWidth / width;

                destX = (int) ( ( dWorldTLX - dWorldTLXRequest ) / pixWidth + 0.5 );
                destWidth = (int) ( ( dWorldBRX - dWorldTLXRequest ) / pixWidth + 0.5 );
            }

            if ( requestHeight > worldHeight ) {
                double pixHeight = requestHeight / height;

                destY = (int) ( ( dWorldTLYRequest - dWorldTLY ) / pixHeight + 0.5 );
                destHeight = (int) ( ( dWorldTLYRequest - dWorldBRY ) / pixHeight + 0.5 );
            }

            BufferedImage enlargedImg = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
            Graphics g = enlargedImg.getGraphics();
            g.drawImage( ecwImage, destX, destY, destWidth, destHeight, 0, 0, viewWidth, viewHeight, null );
            ecwImage = enlargedImg;
            g.dispose();
        }

        return ecwImage;

    }
}