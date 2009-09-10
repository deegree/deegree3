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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;

import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

/**
 * This class is for writing GeoTIFF files from any java.awt.image. At that time, only writing the Bounding Box is
 * available.
 * 
 * 
 * @author <a href="mailto:schaefer@lat-lon.de">Axel Schaefer </A>
 * @author last edited by: $Author$
 * @version 2.0. $Revision$, $Date$
 * @since 2.0
 */
public class GeoTiffWriter {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( GeoTiffWriter.class );

    // GeoTIFF values for GTModelTypeGeoKey
    // http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.1
    private static final int ValueModelTypeProjected = 1;

    private static final int ValueModelTypeGeographic = 2;

    private List<TIFFField> tiffields = null;

    /**
     * see http://www.remotesensing.org/geotiff/spec/geotiff2.4.html#2.4 especialy the KeyEntry part.
     * 
     * The key (Integer) is the GeoTIFF Key ID http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.2 The value is
     * an int array of the size 3. If you want to store simple GeoTIFF key-values set the array to { 0, 1, VALUE }.
     */
    private HashMap<Integer, int[]> geoKeyDirectoryTag = null;

    private BufferedImage bi = null;

    private double offset = 0;

    private double scaleFactor = 1;

    /**
     * creates an GeoTiffWriter instance from an java.awt.image.
     * 
     * @param image
     *            the image, to be transformed to a GeoTIFF.
     * @param envelope
     *            the BoundingBox, the GeoTIFF should have
     * @param resx
     *            The X-Resolution
     * @param resy
     *            The Y-Resolution
     * @param crs
     */
    public GeoTiffWriter( BufferedImage image, Envelope envelope, double resx, double resy, CRS crs ) {
        this( image, envelope, resx, resy, crs, 0, 1 );
    }

    /**
     * creates an GeoTiffWriter instance from an java.awt.image.
     * 
     * @param image
     *            the image, to be transformed to a GeoTIFF.
     * @param envelope
     *            the BoundingBox, the GeoTIFF should have
     * @param resx
     *            The X-Resolution
     * @param resy
     *            The Y-Resolution
     * @param crs
     * @param offset
     * @param scaleFactor
     */
    public GeoTiffWriter( BufferedImage image, Envelope envelope, double resx, double resy, CRS crs, double offset,
                          double scaleFactor ) {
        this.tiffields = new ArrayList<TIFFField>();
        this.geoKeyDirectoryTag = new HashMap<Integer, int[]>();
        int[] header = { 1, 2, 0 };
        this.bi = image;
        this.offset = offset;
        this.scaleFactor = scaleFactor;
        // sets the header. this key must be overwritten in the write-method.
        addKeyToGeoKeyDirectoryTag( 1, header );
        // sets the boundingbox (with envelope and resolution)
        setBoxInGeoTIFF( envelope, resx, resy );
        // sets the CoordinateSystem
        try {
            setCoordinateSystem( crs );
        } catch ( UnknownCRSException e ) {
            LOG.debug( "Could not get coordinate system of the crs: " + e.getLocalizedMessage(), e );
        }
    }

    /**
     * returns the GeoKeys as an array of Tiff Fields.
     * 
     * @return an array of TIFFFields
     */
    private TIFFField[] getGeoTags() {
        TIFFField[] extraFields = null;

        if ( this.tiffields != null && this.tiffields.size() > 0 ) {
            extraFields = new TIFFField[this.tiffields.size()];
            for ( int i = 0; i < extraFields.length; i++ ) {
                extraFields[i] = this.tiffields.get( i );
            }
        }
        return extraFields;
    }

    /**
     * gets the GeoKeyDirectoryTag as a chararrary.
     * 
     * @return the GeoKeyDirectoryTag as a chararrary
     */
    private char[] getGeoKeyDirectoryTag() {
        char[] ch = null;

        // check, if it contains more fields than the header
        if ( this.geoKeyDirectoryTag.size() > 1 ) {
            ch = new char[this.geoKeyDirectoryTag.size() * 4];
            Set set = this.geoKeyDirectoryTag.keySet();
            Object[] o = set.toArray();

            Integer keyID = null;
            int[] temparray = new int[3];

            // o.length is equals this.geoKeyDirectoryTag.size()
            for ( int i = 0; i < o.length; i++ ) {
                // get the key-ID from the ObjectArray 'o'
                keyID = (Integer) o[i];
                // get the values of the HashMap (int[]) at the key keyID
                temparray = this.geoKeyDirectoryTag.get( keyID );
                ch[i * 4] = (char) keyID.intValue();
                ch[i * 4 + 1] = (char) temparray[0];
                ch[i * 4 + 2] = (char) temparray[1];
                ch[i * 4 + 3] = (char) temparray[2];
            }
        }

        return ch;
    }

    /**
     * 
     * @param key
     * @param values
     */
    private void addKeyToGeoKeyDirectoryTag( int key, int[] values ) {
        this.geoKeyDirectoryTag.put( new Integer( key ), values );
    }

    /**
     * Writes the GeoTIFF as a BufferedImage to an OutputStream. The OutputStream isn't closed after the method.
     * 
     * @param os
     *            the output stream, which has to be written.
     * @throws IOException
     */
    public void write( OutputStream os )
                            throws IOException {
        if ( this.geoKeyDirectoryTag.size() > 1 ) {
            // overwrite header with *real* size of GeoKeyDirectoryTag
            int[] header = { 1, 2, this.geoKeyDirectoryTag.size() - 1 };
            addKeyToGeoKeyDirectoryTag( 1, header );

            char[] ch = getGeoKeyDirectoryTag();

            // int tag, int type, int count, java.lang.Object data
            TIFFField geokeydirectorytag = new TIFFField( GeoTiffTag.GeoKeyDirectoryTag, TIFFField.TIFF_SHORT,
                                                          ch.length, ch );
            this.tiffields.add( geokeydirectorytag );
        }

        // get the geokeys
        TIFFField[] tiffields_array = getGeoTags();

        TIFFEncodeParam encodeParam = new TIFFEncodeParam();
        if ( tiffields_array != null && tiffields_array.length > 0 ) {
            encodeParam.setExtraFields( tiffields_array );
        }
        TIFFImageEncoder encoder = new TIFFImageEncoder( os, encodeParam );

        // void encoder( java.awt.image.RenderedImage im )
        encoder.encode( bi );
    }

    // ************************************************************************
    // BoundingBox
    // ************************************************************************
    /**
     * description: Extracts the GeoKeys of the GeoTIFF. The Following Tags will be
     * extracted(http://www.remotesensing.org/geotiff/spec/geotiffhome.html):
     * <ul>
     * <li>ModelPixelScaleTag = 33550 (SoftDesk)
     * <li>ModelTiepointTag = 33922 (Intergraph)
     * </ul>
     * implementation status: working
     */
    private void setBoxInGeoTIFF( Envelope envelope, double resx, double resy ) {

        double[] resolution = { resx, resy, 1d / scaleFactor };
        // ModelPixelScaleTag:
        // Tag = 33550
        // Type = DOUBLE (IEEE Double precision)
        // N = 3
        // Owner: SoftDesk
        TIFFField modelPixelScaleTag = new TIFFField( GeoTiffTag.ModelPixelScaleTag, TIFFField.TIFF_DOUBLE, 3,
                                                      resolution );

        this.tiffields.add( modelPixelScaleTag );

        // ModelTiepointTag:
        // calculate the first points for the upper-left corner {0,0,0} of the
        // tiff
        double tp_01x = 0.0; // (0, val1)
        double tp_01y = 0.0; // (1, val2)
        double tp_01z = 0.0; // (2) z-value. not needed

        // the real-world coordinates for the upper points (tp_01.)
        // these are the unknown variables which have to be calculated.
        double tp_02x = 0.0; // (3, val4)
        double tp_02y = 0.0; // (4, val5)
        double tp_02z = -offset; // (5) z-value. not needed

        double xmin = envelope.getMin().get0();
        double ymax = envelope.getMax().get1();

        // transform this equation: xmin = ?[val4] - ( tp_01x * resx )
        tp_02x = xmin + ( tp_01x * resx );

        // transform this equation: ymax = ?[val5] + ( tp_01y * resy )
        tp_02y = ymax + ( tp_01y * resy );

        double[] tiepoint = { tp_01x, tp_01y, tp_01z, tp_02x, tp_02y, tp_02z };

        // ModelTiepointTag:
        // Tag = 33922 (8482.H)
        // Type = DOUBLE (IEEE Double precision)
        // N = 6*K, K = number of tiepoints
        // Alias: GeoreferenceTag
        // Owner: Intergraph
        TIFFField modelTiepointTag = new TIFFField( GeoTiffTag.ModelTiepointTag, TIFFField.TIFF_DOUBLE, 6, tiepoint );

        this.tiffields.add( modelTiepointTag );
    }

    // ************************************************************************
    // CoordinateSystem
    // ************************************************************************
    /**
     * @throws UnknownCRSException
     * 
     */
    private void setCoordinateSystem( CRS crs )
                            throws UnknownCRSException {
        org.deegree.crs.coordinatesystems.CoordinateSystem crs2 = crs.getWrappedCRS();
        if ( crs2 != null ) {
            String[] identifiers = crs2.getOrignalCodeStrings();
            int epsg = -1;
            for ( String id : identifiers ) {
                LOG.debug( "trying to find EPSG code: " + id );
                if ( id.startsWith( "EPSG:" ) ) {
                    try {
                        epsg = Integer.parseInt( id.substring( 5 ) );
                        break;
                    } catch ( NumberFormatException e ) {
                        // ignore and just try next one
                    }
                }
            }
            if ( epsg != -1 ) {
                int[] keyEntry = new int[] { 0, 1, epsg };
                if ( crs2.getType() == org.deegree.crs.coordinatesystems.CoordinateSystem.GEOGRAPHIC_CRS ) {
                    addKeyToGeoKeyDirectoryTag( GeoTiffKey.GTModelTypeGeoKey, new int[] { 0, 1,
                                                                                         ValueModelTypeGeographic } );
                    addKeyToGeoKeyDirectoryTag( GeoTiffKey.GeographicTypeGeoKey, keyEntry );
                } else if ( crs2.getType() == org.deegree.crs.coordinatesystems.CoordinateSystem.PROJECTED_CRS ) {
                    addKeyToGeoKeyDirectoryTag( GeoTiffKey.GTModelTypeGeoKey,
                                                new int[] { 0, 1, ValueModelTypeProjected } );
                    addKeyToGeoKeyDirectoryTag( GeoTiffKey.ProjectedCSTypeGeoKey, keyEntry );
                } else {
                    LOG.warn( "Can't save coordinate system. coordinate type " + crs2.getType() + " not supported" );
                }
            }
        }
    }
}
