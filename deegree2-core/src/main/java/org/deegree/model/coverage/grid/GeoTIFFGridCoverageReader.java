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
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.deegree.datatypes.CodeList;
import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.wcs.configuration.File;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/**
 * GridCoverageReader for reading files as defined by the deegree CoverageOffering Extension type
 * 'File'. Known formats are: tiff, GeoTiff, jpeg, bmp, gif, png and img (IDRISI)
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GeoTIFFGridCoverageReader extends AbstractGridCoverageReader {

    private static final ILogger LOG = LoggerFactory.getLogger( GeoTIFFGridCoverageReader.class );

    private SeekableStream sst = null;

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
    public GeoTIFFGridCoverageReader( File source, CoverageOffering description, Envelope envelope, Format format ) {
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
    public GeoTIFFGridCoverageReader( InputStream source, CoverageOffering description, Envelope envelope, Format format ) {
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

        RenderedOp rop = readGeoTIFF();
        int w = rop.getWidth();
        int h = rop.getHeight();

        // get image rectangle of interrest, envelope and lonlatenvelope
        Object[] o = getRasterRegion( w, h );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "image rectangle of interrest, envelope and lonlatenvelope:" );
            for ( int i = 0; i < o.length; i++ ) {
                LOG.logDebug( o[i].toString() );
            }
        }
        Rectangle rect = (Rectangle) o[0];
        // return null if the result GC would have a width or height of zero
        if ( rect.width == 0 || rect.height == 0 ) {
            return null;
        }
        // create a coverage description that matches the sub image (coverage)
        // for this a new LonLatEnvelope must be set
        CoverageOffering co = (CoverageOffering) description.clone();
        co.setLonLatEnvelope( (LonLatEnvelope) o[2] );

        // extract required area from the tiff data
        Raster raster = rop.getData( rect );
        // use 8 BIT as default assuming a raster contains simple
        // data like a Landsat TM Band
        int pxSize = 8;
        if ( rop.getColorModel() != null ) {
            pxSize = rop.getColorModel().getPixelSize();
        }

        return createGridCoverage( raster, co, (Envelope) o[1], pxSize );

    }

    /**
     * creates an instance of <tt>GridCoverage</tt> from the passed Raster, CoverageOffering and
     * Envelope. Depending on the pixel size of the the passed raster different types of
     * GirdCoverages will be created. possilbe pixel sized are:
     * <ul>
     * <li>8
     * <li>16
     * <li>32
     * <li>64
     * </ul>
     *
     * @param raster
     * @param co
     * @param env
     * @param pxSize
     * @return the coverage
     * @throws InvalidParameterValueException
     */
    private GridCoverage createGridCoverage( Raster raster, CoverageOffering co, Envelope env, int pxSize )
                            throws InvalidParameterValueException {

        GridCoverage gc = null;
        switch ( pxSize ) {
        case 8: {
            gc = createByteGridCoverage( raster, co, env );
            break;
        }
        case 16: {
            gc = createShortGridCoverage( raster, co, env );
            break;
        }
        case 32:
        case 64: {
            String s = Messages.getMessage( "GC_NOT_SUPPORTED_PS", pxSize );
            throw new InvalidParameterValueException( s, "type", pxSize );
        }
        default:
            String s = Messages.getMessage( "GC_UNKNOWN_PS", pxSize );
            throw new InvalidParameterValueException( s, "type", pxSize );
        }

        return gc;
    }

    /**
     * creates a GridCoverage from the passed Raster. The contains data in
     * <tt>DataBuffer.TYPE_BYTE</tt> format so the result GridCoverage is of type
     * <tt>ByteGridCoverage </tt>
     *
     * @param raster
     * @param co
     * @param env
     * @return the coverage
     */
    private ByteGridCoverage createByteGridCoverage( Raster raster, CoverageOffering co, Envelope env ) {

        Rectangle rect = raster.getBounds();
        int bands = raster.getNumBands();
        byte[] data = (byte[]) raster.getDataElements( rect.x, rect.y, rect.width, rect.height, null );
        byte[][][] mat = new byte[bands][rect.height][rect.width];
        int k = 0;
        for ( int i = 0; i < mat[0].length; i++ ) {
            for ( int j = 0; j < mat[0][i].length; j++ ) {
                for ( int b = 0; b < bands; b++ ) {
                    mat[b][i][j] = data[k++];
                }
            }
        }

        return new ByteGridCoverage( co, env, mat );
    }

    /**
     * creates a GridCoverage from the passed Raster. The contains data in
     * <tt>DataBuffer.TYPE_SHORT</tt> format so the result GridCoverage is of type
     * <tt>ShortGridCoverage </tt>
     *
     * @param raster
     * @param co
     * @param env
     * @return the coverage
     */
    private ShortGridCoverage createShortGridCoverage( Raster raster, CoverageOffering co, Envelope env ) {

        Rectangle rect = raster.getBounds();
        int bands = raster.getNumBands();
        short[] data = (short[]) raster.getDataElements( rect.x, rect.y, rect.width, rect.height, null );
        short[][][] mat = new short[bands][rect.height][rect.width];
        int k = 0;
        for ( int i = 0; i < mat[0].length; i++ ) {
            for ( int j = 0; j < mat[0][i].length; j++ ) {
                for ( int b = 0; b < bands; b++ ) {
                    mat[b][i][j] = data[k++];
                }
            }
        }

        return new ShortGridCoverage( co, env, mat );
    }

    /**
     * reads an image from its source
     *
     * @return the image
     * @throws IOException
     */
    private RenderedOp readGeoTIFF()
                            throws IOException {

        RenderedOp ro = null;
        if ( source.getClass() == File.class ) {
            String s = ( (File) source ).getName();
            String tmp = s.toLowerCase();
            LOG.logDebug( "load: ", tmp );
            URL url = null;
            if ( tmp.startsWith( "file:" ) ) {
                tmp = s.substring( 6, s.length() );
                url = new java.io.File( tmp ).toURL();
            } else if ( tmp.startsWith( "http:" ) ) {
                url = new URL( s );
            } else {
                url = new java.io.File( s ).toURL();
            }
            sst = new MemoryCacheSeekableStream( url.openStream() );
            ro = JAI.create( "stream", sst );
        } else {
            sst = new MemoryCacheSeekableStream( (InputStream) source );
            ro = JAI.create( "stream", sst );
        }

        return ro;
    }

    /**
     * returns the region of the source image that intersects with the GridCoverage to be created as
     * Rectange as well as the Envelope of the region in the native CRS and the LonLatEnvelope of
     * this region.
     *
     * @param width
     *            width of the source image
     * @param height
     *            height of the source image
     * @return the region
     */
    private Object[] getRasterRegion( int width, int height ) {

        CodeList[] cl = description.getSupportedCRSs().getNativeSRSs();
        String code = cl[0].getCodes()[0];

        LonLatEnvelope lle = description.getLonLatEnvelope();
        Envelope tmp = GeometryFactory.createEnvelope( lle.getMin().getX(), lle.getMin().getY(), lle.getMax().getX(),
                                                       lle.getMax().getY(), null );
        try {
            // transform if native CRS is <> EPSG:4326
            if ( !( code.equals( "EPSG:4326" ) ) ) {
                GeoTransformer trans = new GeoTransformer( code );
                tmp = trans.transform( tmp, "EPSG:4326" );
            }
        } catch ( Exception e ) {
            LOG.logError( StringTools.stackTraceToString( e ) );
        }
        // creat tranform object to calculate raster coordinates from
        // geo coordinates
        GeoTransform gt = new WorldToScreenTransform( tmp.getMin().getX(), tmp.getMin().getY(), tmp.getMax().getX(),
                                                      tmp.getMax().getY(), 0, 0, width - 1, height - 1 );

        // calculate envelope of the part of the grid coverage that is contained
        // within the image
        Envelope env = envelope.createIntersection( tmp );

        LonLatEnvelope lonLatEnvelope = calcLonLatEnvelope( env, code );
        // calc image coordinates matching the area that is requested
        int minx = (int) Math.round( gt.getDestX( env.getMin().getX() ) );
        int miny = (int) Math.round( gt.getDestY( env.getMax().getY() ) );
        int maxx = (int) Math.round( gt.getDestX( env.getMax().getX() ) );
        int maxy = (int) Math.round( gt.getDestY( env.getMin().getY() ) );
        Rectangle rect = new Rectangle( minx, miny, maxx - minx + 1, maxy - miny + 1 );

        return new Object[] { rect, env, lonLatEnvelope };
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
        if ( sst != null ) {
            sst.close();
        }
    }

}
