// $HeadURL$
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
package org.deegree.model.coverage.grid;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.deegree.datatypes.CodeList;
import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.OperationParameterIm;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.io.ecwapi.ECWReader;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.wcs.configuration.File;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 * GridCoverageReader for reading files as defined by the deegree CoverageOffering Extension type
 * 'File'. Known formats are: tiff, GeoTiff, jpeg, bmp, gif, png and img (IDRISI)
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class ImageGridCoverageReader extends AbstractGridCoverageReader {

    private static final ILogger LOGGER = LoggerFactory.getLogger( ImageGridCoverageReader.class );

    /**
     * @param source
     *            source file of the coverage
     * @param description
     *            description of the data contained in the source file
     * @param envelope
     *            desired envelope of the coverage to be read
     * @param format
     *            image format of the source file
     */
    public ImageGridCoverageReader( File source, CoverageOffering description, Envelope envelope, Format format ) {
        super( source, description, envelope, format );
    }

    /**
     * @param source
     * @param description
     *            description of the data contained in the source file
     * @param envelope
     *            desired envelope of the coverage to be read
     * @param format
     *            image format of the source file
     */
    public ImageGridCoverageReader( InputStream source, CoverageOffering description, Envelope envelope, Format format ) {
        super( source, description, envelope, format );
    }

    /**
     * Read the grid coverage from the current stream position, and move to the next grid coverage.
     *
     * @param parameters
     *            An optional set of parameters. Should be any or all of the parameters returned by
     *            {@link "org.opengis.coverage.grid.Format#getReadParameters"}.
     * @return A new {@linkplain GridCoverage grid coverage} from the input source.
     * @throws InvalidParameterNameException
     *             if a parameter in <code>parameters</code> doesn't have a recognized name.
     * @throws InvalidParameterValueException
     *             if a parameter in <code>parameters</code> doesn't have a valid value.
     * @throws ParameterNotFoundException
     *             if a parameter was required for the operation but was not provided in the
     *             <code>parameters</code> list.
     * @throws IOException
     *             if a read operation failed for some other input/output reason, including
     *             {@link java.io.FileNotFoundException} if no file with the given <code>name</code>
     *             can be found, or {@link javax.imageio.IIOException} if an error was thrown by the
     *             underlying image library.
     */
    public GridCoverage read( GeneralParameterValueIm[] parameters )
                            throws InvalidParameterNameException, InvalidParameterValueException,
                            ParameterNotFoundException, IOException {

        String frmt = description.getSupportedFormats().getNativeFormat().getCode();
        GridCoverage gc = null;
        if ( frmt.equalsIgnoreCase( "ecw" ) ) {
            gc = performECW( parameters );
        } else if ( frmt.equalsIgnoreCase( "png" ) || frmt.equalsIgnoreCase( "bmp" ) || frmt.equalsIgnoreCase( "tif" )
                    || frmt.equalsIgnoreCase( "tiff" ) || frmt.equalsIgnoreCase( "gif" )
                    || frmt.equalsIgnoreCase( "jpg" ) || frmt.equalsIgnoreCase( "jpeg" ) ) {
            gc = performImage();
        } else {
            throw new InvalidParameterValueException( "unknown format", "native format", format );
        }

        return gc;
    }

    /**
     * performs the creation of a <tt>ImageGridCoverage</tt> from the source assigned to this
     * reader.
     *
     * @param parameters
     * @throws IOException
     */
    private GridCoverage performECW( GeneralParameterValueIm[] parameters )
                            throws IOException {

        BufferedImage bi = null;
        CoverageOffering co = null;
        Object[] o = null;

        ECWReader ecwFile = null;
        try {
            String s = ( (File) source ).getName();
            ecwFile = new ECWReader( s );

            // get the requested dimension in pixels
            int reqWidth = 0;
            int reqHeight = 0;
            for ( int i = 0; i < parameters.length; i++ ) {
                OperationParameterIm op = (OperationParameterIm) parameters[i].getDescriptor();
                String name = op.getName();
                if ( name.equalsIgnoreCase( "WIDTH" ) ) {
                    Object vo = op.getDefaultValue();
                    reqWidth = ( (Integer) vo ).intValue();
                } else if ( name.equalsIgnoreCase( "HEIGHT" ) ) {
                    Object vo = op.getDefaultValue();
                    reqHeight = ( (Integer) vo ).intValue();
                }
            }

            // calculate image region of interest
            o = getECWImageRegion( reqWidth, reqHeight );
            Envelope envl = (Envelope) o[1];

            Rectangle rect = (Rectangle) o[0];
            bi = ecwFile.getBufferedImage( envl, rect.width, rect.height );

            // create a coverage description that matches the sub image (coverage)
            // for this a new LonLatEnvelope must be set
            co = (CoverageOffering) description.clone();
            co.setLonLatEnvelope( (LonLatEnvelope) o[2] );

        } catch ( Exception e ) {
            e.printStackTrace();
            throw new IOException( StringTools.stackTraceToString( e ) );
        } finally {
            // free the ECW cache memory
            if ( ecwFile != null ) {
                ecwFile.close();
            }
        }

        return new ImageGridCoverage( co, (Envelope) o[1], bi );

    }

    /**
     * performs the creation of a <tt>ImageGridCoverage</tt> from the source assigned to this
     * reader.
     *
     * @throws IOException
     * @throws ParameterNotFoundException
     */
    private GridCoverage performImage()
                            throws ParameterNotFoundException, IOException {

        BufferedImage bi = readImage();

        // get image rectangle of interrest, envelope and lonlatenvelope
        Object[] o = getImageRegion( bi.getWidth(), bi.getHeight() );
        Rectangle rect = (Rectangle) o[0];
        // return null if the result GC would have a width or height of zero
        if ( rect.width == 0 || rect.height == 0 ) {
            return null;
        }
        bi = bi.getSubimage( rect.x, rect.y, rect.width, rect.height );

        // create a coverage description that matches the sub image (coverage)
        // for this a new LonLatEnvelope must be set
        CoverageOffering co = (CoverageOffering) description.clone();
        co.setLonLatEnvelope( (LonLatEnvelope) o[2] );

        return new ImageGridCoverage( co, (Envelope) o[1], bi );
    }

    /**
     * reads an image from its source
     *
     * @throws IOException
     */
    private BufferedImage readImage()
                            throws IOException {
        BufferedImage bi = null;
        if ( source.getClass() == File.class ) {
            String s = ( (File) source ).getName();
            String tmp = s.toLowerCase();
            if ( tmp.startsWith( "file:" ) ) {
                tmp = s.substring( 6, s.length() );
                bi = ImageUtils.loadImage( new java.io.File( tmp ) );
            } else if ( tmp.startsWith( "http:" ) ) {
                bi = ImageUtils.loadImage( new URL( s ) );
            } else {
                bi = ImageUtils.loadImage( new java.io.File( s ) );
            }
        } else {
            bi = ImageUtils.loadImage( (InputStream) source );
        }
        return bi;
    }

    /**
     * Return the SRS code of our native SRS.
     */
    private String getNativeSRSCode() {
        CodeList[] cl = description.getSupportedCRSs().getNativeSRSs();
        return cl[0].getCodes()[0];
    }

    /**
     * return the LonLatEnvelope of the entire image in "EPSG:4326"
     */
    private Envelope getLLEAsEnvelope() {
        String code = getNativeSRSCode();
        LonLatEnvelope lle = description.getLonLatEnvelope();
        Envelope tmp = GeometryFactory.createEnvelope( lle.getMin().getX(), lle.getMin().getY(), lle.getMax().getX(),
                                                       lle.getMax().getY(), null );
        try {
            if ( !code.equals( "EPSG:4326" ) ) {
                GeoTransformer trans = new GeoTransformer( code );
                tmp = trans.transform( tmp, "EPSG:4326" );
            }
        } catch ( Exception e ) {
            LOGGER.logError( StringTools.stackTraceToString( e ) );
        }

        return tmp;
    }

    /**
     * Calculate the rectangle that belongs to the given destination envelope in the given source
     * image.
     */
    private Object[] calcRegionRectangle( Envelope dstenv, Envelope srcenv, double srcwidth, double srcheight ) {
        GeoTransform gt = new WorldToScreenTransform( srcenv.getMin().getX(), srcenv.getMin().getY(),
                                                      srcenv.getMax().getX(), srcenv.getMax().getY(), 0, 0,
                                                      srcwidth - 1, srcheight - 1 );

        int minx = (int) Math.round( gt.getDestX( dstenv.getMin().getX() ) );
        int miny = (int) Math.round( gt.getDestY( dstenv.getMax().getY() ) );
        int maxx = (int) Math.round( gt.getDestX( dstenv.getMax().getX() ) );
        int maxy = (int) Math.round( gt.getDestY( dstenv.getMin().getY() ) );
        Rectangle rect = new Rectangle( minx, miny, maxx - minx + 1, maxy - miny + 1 );
        LonLatEnvelope lonLatEnvelope = calcLonLatEnvelope( dstenv, getNativeSRSCode() );

        return new Object[] { rect, dstenv, lonLatEnvelope };
    }

    /**
     * returns the region of the source image that intersects with the GridCoverage to be created as
     * Rectangle as well as the Envelope of the region in the native CRS and the LonLatEnvelope of
     * this region.
     *
     * @param imgwidth
     *            width of the source image
     * @param imgheight
     *            height of the source image
     * @return the region of the source image that intersects with the GridCoverage to be created as
     *         Rectangle as well as the Envelope of the region in the native CRS and the
     *         LonLatEnvelope of this region.
     */
    private Object[] getImageRegion( int imgwidth, int imgheight ) {

        Envelope imgenvelope = getLLEAsEnvelope();
        Envelope dstenvelope = envelope.createIntersection( imgenvelope );
        return calcRegionRectangle( dstenvelope, imgenvelope, imgwidth, imgheight );
    }

    /**
     * returns the region of the source image that intersects with the GridCoverage to be created as
     * Rectangle as well as the Envelope of the region in the native CRS and the LonLatEnvelope of
     * this region. For ECW files these values reflect exactly the desired result, as the library
     * will cut and sample the source image during read.
     *
     * @param reqwidth
     *            width of the requested region
     * @param reqheight
     *            height of the requested region
     * @return the region of the source image that intersects with the GridCoverage to be created as
     *         Rectangle as well as the Envelope of the region in the native CRS and the
     *         LonLatEnvelope of this region.
     */
    private Object[] getECWImageRegion( int reqwidth, int reqheight ) {

        Envelope imgenvelope = getLLEAsEnvelope();
        Envelope dstenvelope = envelope.createIntersection( imgenvelope );

        // In case of ECW files, we calculate the rectangle according
        // to the desired result, not according to the source image,
        // as clipping and sampling will be done by the ECW library.
        // Hence dstenvelope and srcenvelope are the same and width/height
        // must be clipped with our grid-cell (if necessary).
        double dWidth = ( dstenvelope.getWidth() * reqwidth ) / envelope.getWidth();
        double dHeight = ( dstenvelope.getHeight() * reqheight ) / envelope.getHeight();
        return calcRegionRectangle( dstenvelope, dstenvelope, dWidth, dHeight );
    }

    /**
     * Allows any resources held by this object to be released. The result of calling any other
     * method subsequent to a call to this method is undefined. It is important for applications to
     * call this method when they know they will no longer be using this
     * <code>GridCoverageReader</code>. Otherwise, the reader may continue to hold on to
     * resources indefinitely.
     *
     * @throws IOException
     *             if an error occured while disposing resources (for example while closing a file).
     */
    public void dispose()
                            throws IOException {
        if ( source instanceof InputStream ) {
            ( (InputStream) source ).close();
        }
    }

}
