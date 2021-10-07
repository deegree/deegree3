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

package org.deegree.coverage.raster.io.imageio.geotiff;

import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.GTModelTypeGeoKey;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.GTRasterTypeGeoKey;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.GeographicTypeGeoKey;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.ModelTypeGeocentric;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.ModelTypeGeographic;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.ModelTypeProjected;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.ProjectedCSTypeGeoKey;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.RasterPixelIsArea;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.RasterPixelIsPoint;
import static org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffKey.VerticalCSTypeGeoKey;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriter;
import it.geosolutions.imageio.plugins.tiff.GeoTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFDirectory;
import it.geosolutions.imageio.plugins.tiff.TIFFField;
import it.geosolutions.imageio.plugins.tiff.TIFFTag;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.CRS.CRSType;

/**
 * ImageIO based geo tiff writer. Currently following geotiff tags are exported:
 * <ul>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeoTiffWriter {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( GeoTiffWriter.class );

    private static final GeoTIFFTagSet GEO_TAG_SET = GeoTIFFTagSet.getInstance();

    /**
     * The model pixel scale tag holds following values.<code>
     * <ul>
     * <li>Tag = 33550</li>
     * <li>Type = DOUBLE (IEEE Double precision)</li>
     * <li> N = 3 (3 dimensional)</li>
     * <li> x, y, z resolution</li>
     * </ul>
     * </code>
     * 
     * 
     * @param geoRef
     *            the geo reference to get the values from.
     * @return the tiff field containing the scale
     */
    private static TIFFField createModelPixelScaleTag( RasterGeoReference geoRef ) {
        TIFFTag tag = GEO_TAG_SET.getTag( GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE );
        return new TIFFField( tag, TIFFTag.TIFF_DOUBLE, 3, new double[] { geoRef.getResolutionX(),
                                                                         geoRef.getResolutionY(), 0 } );
    }

    /**
     * The model tie point tag consists of following values.
     * <ul>
     * <li>Tag = 33922 (8482.H)</li>
     * <li>Type = DOUBLE (IEEE Double precision)</li>
     * <li>N = 6*K, K = number of tiepoints</li>
     * <li>the values, 6 dimensionals (3 raster points, 3 world coordinates) per tie point</li>
     * </ul>
     * 
     * @param envelope
     * @param rasterGeoReference
     * @return the tiff field containing the tie point
     */
    private static TIFFField createModelTiePointTag( RasterGeoReference rasterGeoReference ) {

        // ModelTiepointTag:
        // calculate the first points for the upper-left corner {0,0,0} of the
        // tiff
        double worldX = rasterGeoReference.getOriginEasting();
        double worldY = rasterGeoReference.getOriginNorthing();

        // the upper left point in the raster have given world coordinates
        double[] tiepoint = { 0, 0, 0, worldX, worldY, 0 };
        TIFFTag tag = GEO_TAG_SET.getTag( GeoTIFFTagSet.TAG_MODEL_TIE_POINT );
        return new TIFFField( tag, TIFFTag.TIFF_DOUBLE, 6, tiepoint );
    }

    /**
     * 
     * see http://www.remotesensing.org/geotiff/spec/geotiff2.4.html#2.4 especially the KeyEntry part.
     * 
     * The key (Integer) is the GeoTIFF Key ID http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.2 The value is
     * an int array of the size 3. If you want to store simple GeoTIFF key-values set the array to { 0, 1, VALUE }.
     * 
     * @return a Tifffield containing the geo directory, which contains keys for the crs description and the raster
     *         type.
     */
    private static TIFFField createDirectoryTag( RasterGeoReference geoRef ) {
        Map<Integer, char[]> geoKeyDirectoryTag = new HashMap<Integer, char[]>();
        addCRS( geoKeyDirectoryTag, geoRef.getCrs() );
        addGTRasterTypeGeoKey( geoKeyDirectoryTag, geoRef.getOriginLocation() );
        // space for the header and the keys.
        char[] geoKeys = extractGeoKeys( geoKeyDirectoryTag );
        if ( geoKeys.length > 4 ) {
            TIFFTag tag = GEO_TAG_SET.getTag( GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY );
            return new TIFFField( tag, TIFFTag.TIFF_SHORT, geoKeys.length, geoKeys );
        }
        return null;
    }

    /**
     * @param geoKeyDirectoryTag2
     * @param originLocation
     */
    private static void addGTRasterTypeGeoKey( Map<Integer, char[]> geoKeyDirectoryTag, OriginLocation originLocation ) {
        char val = originLocation == OriginLocation.CENTER ? RasterPixelIsArea : RasterPixelIsPoint;
        geoKeyDirectoryTag.put( GTRasterTypeGeoKey, new char[] { 0, 1, val } );
    }

    /**
     * @param geoKeyDirectoryTag2
     * @return
     */
    private static char[] extractGeoKeys( Map<Integer, char[]> geoKeyDirectoryTag ) {
        Set<Integer> keys = geoKeyDirectoryTag.keySet();
        int keySize = 4;
        for ( Integer key : keys ) {
            char[] values = geoKeyDirectoryTag.get( key );
            if ( values != null ) {
                // the key
                keySize++;
                // the values
                keySize += values.length;
            }
        }
        char[] result = new char[keySize];

        int i = 4;
        if ( keySize > 4 ) {
            for ( Integer key : keys ) {
                // get the values of the HashMap (int[]) at the key keyID
                char[] values = geoKeyDirectoryTag.get( key );
                result[i++] = (char) key.intValue();
                for ( int v = 0; v < values.length; ++v ) {
                    result[i++] = values[v];
                }
            }
        }
        result[0] = 1;// key version type
        result[1] = 0; // revision
        result[2] = 2;// minor revision
        result[3] = (char) keys.size();
        return result;
    }

    /**
     * Adds the description of the crs to the directory.
     * 
     * @param geoKeyDirectoryTag
     *            to add the values of the crs to.
     * @param crs
     *            to add.
     */
    private static void addCRS( Map<Integer, char[]> geoKeyDirectoryTag, ICRS crs ) {
        if ( crs == null ) {
            return;
        }
        ICRS srs = crs;
        if ( srs != null ) {
            CRSCodeType[] codes = srs.getCodes();
            // set to user defined
            int epsgCode = 32767;
            for ( int i = 0; i < codes.length && epsgCode == 32767; ++i ) {
                CRSCodeType code = codes[i];
                if ( code instanceof EPSGCode ) {
                    epsgCode = ( (EPSGCode) code ).getCodeNo();
                } else {
                    String codeString = code.getOriginal();
                    if ( codeString != null && codeString.toLowerCase().contains( "espg" ) ) {
                        int index = codeString.lastIndexOf( ":" );
                        if ( index == -1 ) {
                            index = codeString.lastIndexOf( "#" );
                        }
                        if ( index != -1 ) {
                            String subString = codeString.substring( index );
                            try {
                                epsgCode = Integer.parseInt( subString );
                            } catch ( NumberFormatException e ) {
                                // ignore and just try next one
                            }
                        }
                    }
                }
            }
            char[] keyEntry = new char[] { 0, 1, (char) epsgCode };
            CRSType type = srs.getType();
            switch ( type ) {
            case COMPOUND:
                LOG.warn( "Can't save coordinate system. coordinate type " + srs.getType() + " not supported" );
                break;
            case GEOCENTRIC:
                LOG.warn( "Can't save coordinate system. coordinate type " + srs.getType() + " not supported" );
                geoKeyDirectoryTag.put( GTModelTypeGeoKey, new char[] { 0, 1, ModelTypeGeocentric } );
                geoKeyDirectoryTag.put( GeographicTypeGeoKey, keyEntry );
                if ( epsgCode == 32767 ) {
                    // add projection parameters etc, this value means user defined.
                }
                break;
            case GEOGRAPHIC:
                geoKeyDirectoryTag.put( GTModelTypeGeoKey, new char[] { 0, 1, ModelTypeGeographic } );
                geoKeyDirectoryTag.put( GeographicTypeGeoKey, keyEntry );
                if ( epsgCode == 32767 ) {
                    // add projection parameters etc, this value means user defined.
                }
                break;
            case PROJECTED:
                geoKeyDirectoryTag.put( GTModelTypeGeoKey, new char[] { 0, 1, ModelTypeProjected } );
                geoKeyDirectoryTag.put( ProjectedCSTypeGeoKey, keyEntry );
                if ( epsgCode == 32767 ) {
                    // add projection parameters etc, this value means user defined.
                }
                break;
            case VERTICAL:
                geoKeyDirectoryTag.put( GTModelTypeGeoKey, new char[] { 0, 1, 32767 } );
                geoKeyDirectoryTag.put( VerticalCSTypeGeoKey, keyEntry );
                if ( epsgCode == 32767 ) {
                    // add projection parameters etc, this value means user defined.
                }
                break;
            }
        }
    }

    /**
     * @param raster
     * @param writer
     * @throws IOException
     */
    private static void write( AbstractRaster raster, ImageWriter writer )
                            throws IOException {
        write( RasterFactory.imageFromRaster( raster ), raster.getRasterReference(), writer );

    }

    private static void write( BufferedImage img, RasterGeoReference ref, ImageWriter writer )
                            throws IOException {
        ImageWriteParam encodeParam = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata( null, encodeParam );
        TIFFDirectory tiffDir = null;
        try {
            tiffDir = TIFFDirectory.createFromMetadata( metadata );
        } catch ( IIOInvalidTreeException e ) {
            throw new IOException( "Could not write the meta data for the GeoTIFF file because: "
                                   + e.getLocalizedMessage(), e );
        }

        TIFFField tag = createModelTiePointTag( ref );
        if ( tag != null ) {
            tiffDir.addTIFFField( tag );
        }
        tag = createModelPixelScaleTag( ref );
        if ( tag != null ) {
            tiffDir.addTIFFField( tag );
        }
        tag = createDirectoryTag( ref );
        if ( tag != null ) {
            tiffDir.addTIFFField( tag );
        }
        metadata = tiffDir.getAsMetadata();
        IIOImage wImage = new IIOImage( img, null, metadata );
        writer.write( wImage );

    }

    private static ImageWriter getWriter()
                            throws IOException {
        Iterator<ImageWriter> imageWritersByFormatName = ImageIO.getImageWritersByFormatName( "tiff" );
        ImageWriter writer = null;

        while ( imageWritersByFormatName.hasNext() && writer == null ) {
            writer = imageWritersByFormatName.next();
            if ( !( writer instanceof TIFFImageWriter ) ) {
                writer = null;
            }
        }
        if ( writer == null ) {
            throw new IOException( "Could not create an ImageIO writer for format:  geoTiff" );
        }
        return writer;
    }

    /**
     * @param raster
     * @param out
     * @throws IOException
     */
    public static void save( AbstractRaster raster, OutputStream out )
                            throws IOException {
        ImageOutputStream stream = ImageIO.createImageOutputStream( out );
        ImageWriter writer = getWriter();
        writer.setOutput( stream );
        write( raster, writer );
        stream.flush();
    }

    /**
     * @param raster
     * @param file
     * @throws IOException
     */
    public static void save( AbstractRaster raster, File file )
                            throws IOException {
        ImageOutputStream stream = ImageIO.createImageOutputStream( file );
        ImageWriter writer = getWriter();
        writer.setOutput( stream );
        write( raster, writer );
        stream.flush();
        stream.close();
    }

    public static void save( BufferedImage img, RasterGeoReference ref, OutputStream out )
                            throws IOException {
        ImageOutputStream stream = ImageIO.createImageOutputStream( out );
        ImageWriter writer = getWriter();
        writer.setOutput( stream );
        write( img, ref, writer );
        stream.flush();
    }
}
