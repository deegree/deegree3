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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import static java.lang.System.currentTimeMillis;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.Triple;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.crs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.styling.Styling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>GeometryLayerTextureTileProvider</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GeometryLayerTextureTileProvider implements TextureTileProvider {

    private static Logger LOG = LoggerFactory.getLogger( GeometryLayerTextureTileProvider.class );

    private final GeometryFactory fac;

    private final double res = Double.NaN;

    private final double minResolution = 0.09;

    private FeatureStore featureStore;

    private final static RasterIOOptions options = new RasterIOOptions( OriginLocation.CENTER );

    private Style style;

    private double[] offsetVector;

    private final CRS wpvsCRS;

    private final QName rootFT;

    private Envelope envelope;

    /**
     * Use the given feature store to create a 'dataset' from a style.
     * 
     * @param offsetVector
     * @param wpvsCRS
     * @param featureStore
     * @param dbp
     * @param style
     * @throws IOException
     */
    public GeometryLayerTextureTileProvider( double[] offsetVector, CRS wpvsCRS, FeatureStore featureStore, Style style )
                            throws IOException {

        fac = new GeometryFactory();
        if ( featureStore == null ) {
            throw new NullPointerException( "The feature store may not be null." );
        }
        this.featureStore = featureStore;
        ApplicationSchema schema = this.featureStore.getSchema();
        if ( schema == null ) {
            throw new NullPointerException( "The feature store must have an application schema to work with." );
        }
        FeatureType[] rootFeatureTypes = schema.getRootFeatureTypes();
        if ( rootFeatureTypes == null || rootFeatureTypes.length == 0 ) {
            throw new NullPointerException( "The application schema must have root feature types to work with." );
        }

        if ( style == null ) {
            throw new NullPointerException(
                                            "No style defined, the unevaluated style (read from an Symbology Encoding (SE) definition file) may not be null." );
        }
        this.style = style;

        QName styleFT = style.getFeatureType();
        QName posFT = styleFT;
        if ( styleFT != null ) {
            FeatureType sFT = schema.getFeatureType( styleFT );
            if ( sFT == null ) {
                posFT = null;
                LOG.warn( "The defined syle featuretype did not exist in the feature store, trying to use the first feature type with geometries." );
            }
        }

        for ( int i = 0; i < rootFeatureTypes.length && posFT == null; ++i ) {
            FeatureType possibleFT = rootFeatureTypes[i];
            if ( possibleFT != null ) {
                GeometryPropertyType geoPT = possibleFT.getDefaultGeometryPropertyDeclaration();
                if ( geoPT != null ) {
                    posFT = possibleFT.getName();
                }
            }
        }
        if ( posFT == null ) {
            throw new NullPointerException( "No (root) feature type with a geometry property found, this may not be." );
        }
        this.rootFT = posFT;

        // this.res = 1;
        this.offsetVector = offsetVector;
        if ( offsetVector == null || offsetVector.length < 2 ) {
            offsetVector = new double[] { 0, 0, 0 };
        }
        this.wpvsCRS = wpvsCRS;
        this.envelope = this.featureStore.getEnvelope( this.rootFT );
    }

    @Override
    public TextureTile getTextureTile( TextureTileRequest request ) {
        if ( request.getMetersPerPixel() >= minResolution || request.getMetersPerPixel() < 0.009 ) {
            // System.out.println( "NULL: " + request.getMetersPerPixel() );
            return null;
        }
        System.out.println( "Enable: " + request.getMetersPerPixel() );

        double minX = request.getMinX();
        double minY = request.getMinY();
        double maxX = request.getMaxX();
        double maxY = request.getMaxY();

        double worldWidth = maxX - minX;
        double worldHeight = maxY - minY;
        double resolution = request.getMetersPerPixel();

        double iWidth = worldWidth / resolution;
        double iHeight = worldHeight / resolution;
        // System.out.println( "w|h: " + iWidth + " | " + iHeight );
        int imageWidth = (int) Math.round( iWidth );
        int imageHeight = (int) Math.round( iHeight );

        // double dW = ( resolution * ( imageWidth - iWidth ) ) * 0.5;
        // double dH = ( resolution * ( imageHeight - iHeight ) ) * 0.5;
        // following values are note the half distance to the next pixel in world coordinates.
        // double dW = ( resolution - rW ) * 0.5;
        // double dH = ( resolution - rH ) * 0.5;

        // rb: create an image which is even (needed for opengl).
        if ( imageWidth % 2 != 0 ) {
            double dW = ( resolution + ( resolution * ( imageWidth - iWidth ) ) ) * 0.5;

            // System.out.println( "GEOM: Texturewidth " + imageWidth + " is not even with resolution: " + resolution
            // + ", updating world width : " + worldWidth + " to " + ( worldWidth + ( 2 * dW ) )
            // + " new width: " + Math.round( ( worldWidth + ( 2 * dW ) ) / resolution ) );

            imageWidth++;
            minX -= dW;
            maxX += dW;
        }
        if ( imageHeight % 2 != 0 ) {
            double dH = ( resolution + ( resolution * ( imageHeight - iHeight ) ) ) * 0.5;
            // System.out.println( "GEOM: TextureHeight " + imageHeight + " is not even with resolution: " + resolution
            // + ", updating world height: " + worldHeight + " to " + ( worldHeight + ( 2 * dH ) )
            // + " new height: " + Math.round( ( worldHeight + ( 2 * dH ) ) / resolution ) );
            imageHeight++;
            minY -= dH;
            maxY += dH;
        }

        // imageWidth = (int) worldWidth;
        // imageHeight = (int) worldHeight;
        // System.out.println( "imageWidth: " + imageWidth );
        // System.out.println( "imageHeight: " + imageHeight );
        // System.out.println( "resolution: " + resolution );

        Envelope tileEnv = fac.createEnvelope( minX, minY, maxX, maxY, wpvsCRS );

        Query q = new Query( this.rootFT, tileEnv, null, true, -1 );
        FeatureResultSet frs = null;
        long sT = currentTimeMillis();
        try {
            frs = this.featureStore.query( q );
        } catch ( FeatureStoreException e ) {
            LOG.error( "Could not create a geometry layer texture because: " + e.getLocalizedMessage(), e );
        } catch ( FilterEvaluationException e ) {
            LOG.error( "Could not create a geometry layer texture because: " + e.getLocalizedMessage(), e );
        }
        long eT = currentTimeMillis() - sT;
        // System.out.println( "Getting features took: " + eT + " ms." );
        // PooledByteBuffer directBuffer = null;
        ByteBuffer imageBuffer = null;
        try {
            // rgba
            // directBuffer = this.dbp.allocate_( imageWidth * imageHeight * 4 );

            // RawDataBufferByte rbb = new RawDataBufferByte( directBuffer.getBuffer(), imageWidth, imageHeight, 4 );
            // SampleModel fm = new BandedSampleModel( DataBuffer.TYPE_BYTE, imageWidth, imageHeight, 4 );
            // WritableRaster wr = Raster.createWritableRaster( fm, rbb, null );
            // ColorModel cm = new BytePixelColorModel( 255 );
            //
            // BufferedImage bImg = new BufferedImage( cm, wr, false, null );
            // RawDataBufferByte rbb = new RawDataBufferByte( directBuffer.getBuffer(), imageWidth, imageHeight, 4 );
            // SampleModel fm = new BandedSampleModel( DataBuffer.TYPE_BYTE, imageWidth, imageHeight, 4 );
            // WritableRaster wr = Raster.createWritableRaster( fm, rbb, null );
            // ColorModel cm = new BytePixelColorModel( 255 );

            BufferedImage bImg = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR );

            sT = currentTimeMillis();
            Graphics2D graphics = bImg.createGraphics();
            Java2DRenderer renderer = new Java2DRenderer( graphics, imageWidth, imageHeight, tileEnv );
            eT = currentTimeMillis() - sT;
            // System.out.println( "Getting graphics took: " + eT + " ms." );

            if ( frs == null ) {
                return null;
            }
            Iterator<Feature> it = frs.iterator();

            sT = currentTimeMillis();
            int index = 0;

            while ( it.hasNext() ) {
                index++;
                Feature feature = it.next();
                LinkedList<Triple<Styling, Geometry, String>> evald = style.evaluate( feature );
                for ( Triple<Styling, Geometry, String> tr : evald ) {
                    renderer.render( tr.first, tr.second );
                }

            }
            graphics.dispose();
            eT = currentTimeMillis() - sT;
            // System.out.println( "Drawing features (" + index + " ) took: " + eT + " ms." );

            sT = currentTimeMillis();
            Raster imageRaster = bImg.getRaster();
            imageBuffer = ByteBuffer.allocate( imageHeight * imageWidth * 4 );
            // directBuffer.rewind();
            imageBuffer.put( (byte[]) imageRaster.getDataElements( 0, 0, imageWidth, imageHeight, null ) );
            eT = currentTimeMillis() - sT;
            // System.out.println( "Copying bytes took: " + eT + " ms." );
        } catch ( Throwable t ) {
            t.printStackTrace();
            return null;
        }

        // String f = "/tmp/" + tileEnv + ".png";
        // File file = new File( f );
        // int counter = 0;
        // while ( file.exists() ) {
        // f = "/tmp/" + tileEnv + "_" + ( counter++ ) + ".png";
        // }
        // try {
        // ImageIO.write( bImg, "png", file );
        // } catch ( IOException e ) {
        // e.printStackTrace();
        // }

        // TextureTile tile = new TextureTile( minX, minY, maxX, maxY, request.getMetersPerPixel(), imageWidth,
        // imageHeight, imageBuffer, true );
        TextureTile tile = new TextureTile( minX, minY, maxX, maxY, imageWidth, imageHeight, imageBuffer, true );
        return tile;
    }

    @Override
    public double getNativeResolution() {
        return res;
    }

    @Override
    public boolean hasTextureForResolution( double unitsPerPixel ) {
        return unitsPerPixel <= minResolution;
    }

    @Override
    public Envelope getEnvelope() {
        return envelope;
    }

}
