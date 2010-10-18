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
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.deegree.gml.GMLVersion.GML_31;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.LogUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.grid.GridFileReader;
import org.deegree.coverage.raster.io.grid.GridReader;
import org.deegree.coverage.raster.io.grid.GridWriter;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
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
 * Provides a texture created from geometries supplied by a Featurstore and filled with a color or fillpattern, which
 * columns are the geometries and which columns define the color/texture is defined by SE-Style file. Texture rendering
 * is done by using the {@link Java2DRenderer}.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
public class StyledGeometryTTProvider implements TextureTileProvider {

    static Logger LOG = LoggerFactory.getLogger( StyledGeometryTTProvider.class );

    private final GeometryFactory fac;

    private final double res = Double.NaN;

    private final double maxResolution;

    private FeatureStore featureStore;

    private Style style;

    private final CRS wpvsCRS;

    private final QName rootFT;

    private Envelope envelope;

    private RasterCache rasterCache;

    private SGTextureCache textureCache;

    /**
     * Use the given feature store to create a 'dataset' from a style.
     * 
     * @param offsetVector
     *            translation vector of the WPVS
     * @param wpvsCRS
     *            crs of the WPVS
     * @param featureStore
     *            to get the features / geometries from.
     * @param style
     *            used to evaluate the given features.
     * @param maxUnitsPerPixel
     *            for which this provider will become active.
     * @param cacheDir
     *            to cache textures
     * @param cacheSize
     *            size of the cache in bytes.
     * @throws IOException
     * @throws FeatureStoreException
     */
    public StyledGeometryTTProvider( double[] offsetVector, CRS wpvsCRS, FeatureStore featureStore, Style style,
                                     double maxUnitsPerPixel, File cacheDir, long cacheSize ) throws IOException,
                            FeatureStoreException {

        this.maxResolution = Math.max( 0, maxUnitsPerPixel );
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

        // TODO: offset vector is never used. bug?
        // if ( offsetVector == null || offsetVector.length < 2 ) {
        // offsetVector = new double[] { 0, 0, 0 };
        // }
        this.wpvsCRS = wpvsCRS;
        this.envelope = this.featureStore.getEnvelope( this.rootFT );
        this.rasterCache = RasterCache.getInstance( cacheDir, true );
        File directory = this.rasterCache.getCacheDirectory();
        if ( cacheSize >= 0 ) {
            long freeSpace = directory.getFreeSpace();
            if ( cacheSize > 0 ) {
                freeSpace = Math.min( freeSpace, cacheSize );
            }
            this.textureCache = new SGTextureCache( freeSpace );
        } else {
            LOG.info( "Not using a styled geometry cache (because the requested memory cache size was negative)." );
            this.textureCache = null;
        }

    }

    @Override
    public TextureTile getTextureTile( TextureTileRequest request ) {
        if ( request.getUnitsPerPixel() >= maxResolution || request.getUnitsPerPixel() < 0.009 ) {
            // System.out.println( "NULL: " + request.getMetersPerPixel() );
            return null;
        }

        ByteBuffer imageBuffer = null;
        double minX = request.getMinX();
        double minY = request.getMinY();
        double maxX = request.getMaxX();
        double maxY = request.getMaxY();
        double worldWidth = maxX - minX;
        double worldHeight = maxY - minY;
        double resolution = request.getUnitsPerPixel();

        double iWidth = worldWidth / resolution;
        double iHeight = worldHeight / resolution;
        int imageWidth = (int) Math.round( iWidth );
        int imageHeight = (int) Math.round( iHeight );

        // boolean debug = Math.abs( minX - 2579520.0 ) < 0.001 && Math.abs( minY - 5620912.0 ) < 0.001;
        // if ( debug ) {
        // System.out.println( "Hier" );
        // System.out.println( "minX: " + minX );
        // System.out.println( "maxX: " + maxX );
        // System.out.println( "minY: " + minY );
        // System.out.println( "maxY: " + maxY );
        // System.out.println( "resolution: " + resolution );
        // System.out.println( "w/h" + imageWidth + "/" + imageHeight );
        // }

        if ( textureCache != null ) {
            Pair<TextureTileRequest, File> cachedReq = textureCache.get( request );
            if ( cachedReq != null ) {
                try {
                    TextureTileRequest ttr = cachedReq.first;
                    File cF = cachedReq.second;
                    if ( cF.exists() && cF.length() > 0 ) {

                        // get the image from cache
                        GridReader gr = new GridFileReader( cF, null );
                        imageBuffer = gr.getTileData( 0, 0, null );
                        if ( imageBuffer != null ) {
                            imageBuffer.rewind();
                            minX = ttr.getMinX();
                            minY = ttr.getMinY();
                            maxX = ttr.getMaxX();
                            maxY = ttr.getMaxY();

                            worldWidth = maxX - minX;
                            worldHeight = maxY - minY;
                            resolution = ttr.getUnitsPerPixel();

                            iWidth = worldWidth / resolution;
                            iHeight = worldHeight / resolution;
                            imageWidth = (int) Math.round( iWidth );
                            imageHeight = (int) Math.round( iHeight );
                            LOG.debug( "Found a cachefile, using texture from cache" );

                        }
                    }

                } catch ( Exception e ) {
                    LOG.debug( "Found a cached file but could not read it because: " + e.getLocalizedMessage(), e );
                }
            }
        }
        Envelope tileEnv = fac.createEnvelope( minX, minY, maxX, maxY, wpvsCRS );
        if ( imageBuffer == null ) {
            LOG.debug( "No cache file found, creating new texture." );

            // TODO: as: the values calculated here were never used...
            // rb: create an image which is even (needed for opengl).
            // if ( imageWidth % 2 != 0 ) {
            // double dW = ( resolution + ( resolution * ( imageWidth - iWidth ) ) ) * 0.5;

            // System.out.println( "GEOM: Texturewidth " + imageWidth + " is not even with resolution: " +
            // resolution
            // + ", updating world width : " + worldWidth + " to " + ( worldWidth + ( 2 * dW ) )
            // + " new width: " + Math.round( ( worldWidth + ( 2 * dW ) ) / resolution ) );

            // imageWidth++;
            // minX -= dW;
            // maxX += dW;
            // }
            // if ( imageHeight % 2 != 0 ) {
            // double dH = ( resolution + ( resolution * ( imageHeight - iHeight ) ) ) * 0.5;
            // System.out.println( "GEOM: TextureHeight " + imageHeight + " is not even with resolution: " +
            // resolution
            // + ", updating world height: " + worldHeight + " to " + ( worldHeight + ( 2 * dH ) )
            // + " new height: " + Math.round( ( worldHeight + ( 2 * dH ) ) / resolution ) );
            // imageHeight++;
            // minY -= dH;
            // maxY += dH;
            // }

            Query q = new Query( this.rootFT, tileEnv, null, -1, -1, -1 );
            FeatureResultSet frs = null;
            long sT = currentTimeMillis();
            try {
                frs = this.featureStore.query( q );
            } catch ( FeatureStoreException e ) {
                LOG.error( "Could not create a geometry layer texture because: " + e.getLocalizedMessage(), e );
            } catch ( FilterEvaluationException e ) {
                LOG.error( "Could not create a geometry layer texture because: " + e.getLocalizedMessage(), e );
            }
            if ( frs == null || !frs.iterator().hasNext() ) {
                // no objects found
                return null;
            }
            LOG.debug( LogUtils.createDurationTimeString( "Getting features", sT, false ) );
            try {
                BufferedImage bImg = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR );

                sT = currentTimeMillis();
                Graphics2D graphics = bImg.createGraphics();
                Java2DRenderer renderer = new Java2DRenderer( graphics, imageWidth, imageHeight, tileEnv );
                LOG.debug( LogUtils.createDurationTimeString( "Creating graphics object", sT, false ) );
                Iterator<Feature> it = frs.iterator();

                // TODO
                FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_31 );

                sT = currentTimeMillis();
                int index = 0;
                while ( it.hasNext() ) {
                    index++;
                    Feature feature = it.next();
                    LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evald = style.evaluate( feature,
                                                                                                      evaluator );
                    for ( Triple<Styling, LinkedList<Geometry>, String> tr : evald ) {
                        renderer.render( tr.first, tr.second );
                    }

                }
                graphics.dispose();
                LOG.debug( LogUtils.createDurationTimeString( "Drawing textures", sT, false ) );

                Raster imageRaster = bImg.getRaster();
                // wrap
                imageBuffer = ByteBuffer.wrap( (byte[]) imageRaster.getDataElements( 0, 0, imageWidth, imageHeight,
                                                                                     null ) );
                imageBuffer.rewind();
                if ( textureCache != null ) {
                    addToCache( imageBuffer, request, resolution, tileEnv, imageWidth, imageHeight );
                }
            } catch ( Exception e ) {
                LOG.debug( "Could not create a styled geometry texture because: " + e.getLocalizedMessage(), e );
                LOG.warn( "Error while creating styled geometry texture: " + e );
                return null;
            }
        }
        return new TextureTile( minX, minY, maxX, maxY, imageWidth, imageHeight, imageBuffer, true, false );
    }

    private void addToCache( ByteBuffer imageBuffer, TextureTileRequest request, double resolution,
                             Envelope tileEnvelope, int imageWidth, int imageHeight ) {
        // if ( debug ) {
        File newCacheFile = this.rasterCache.createCacheFile( "_" + request.hashCode() );
        if ( newCacheFile.exists() ) {
            newCacheFile.delete();
        }
        // delete cache files on exit.
        newCacheFile.deleteOnExit();
        long sT = currentTimeMillis();
        RasterGeoReference gRef = new RasterGeoReference( CENTER, resolution, -resolution,
                                                          tileEnvelope.getMin().get0(), tileEnvelope.getMax().get1(),
                                                          tileEnvelope.getCoordinateSystem() );
        RasterDataInfo rdi = new RasterDataInfo( new BandType[] { BandType.ALPHA, BandType.RED, BandType.GREEN,
                                                                 BandType.BLUE }, DataType.BYTE, InterleaveType.PIXEL );
        try {
            GridWriter gw = new GridWriter( 1, 1, tileEnvelope, gRef, newCacheFile, rdi );
            if ( gw.getTileRasterWidth() != imageWidth ) {
                gw.setTileRasterWidth( imageWidth );
            }
            if ( gw.getTileRasterHeight() != imageHeight ) {
                gw.setTileRasterHeight( imageHeight );
            }
            gw.writeEntireFile( imageBuffer );
            File gmf = gw.writeMetadataFile( null );
            if ( gmf != null ) {
                gmf.deleteOnExit();
            }
            textureCache.put( request, newCacheFile, imageBuffer.capacity() );
        } catch ( IOException e ) {
            // could not create a grid writer, don't add to cache
            LOG.debug( "Not adding styled geometry to cache because no grid writer could be created: "
                       + e.getLocalizedMessage(), e );
            LOG.warn( "Not writing cachefile becaue: " + e.getLocalizedMessage() );
            newCacheFile.delete();
            return;
        }
        LOG.debug( LogUtils.createDurationTimeString( "Writing cachefile", sT, false ) );
    }

    @Override
    public double getNativeResolution() {
        return res;
    }

    @Override
    public boolean hasTextureForResolution( double unitsPerPixel ) {
        return unitsPerPixel <= maxResolution;
    }

    @Override
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * 
     * Keeps references of cache files, with a given texture tile request.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private class SGTextureCache extends LinkedHashMap<TextureTileRequest, File> {

        /**
         * 
         */
        private static final long serialVersionUID = -7410767607350583210L;

        private long usedCacheSpace = 0;

        private long freeSpace = 0;

        SGTextureCache( long freeSpace ) {
            // 268435456 == maximum size of texture with textureside 8192 and 4 bytes.
            super( (int) Math.ceil( freeSpace / 268435456 ), 0.75f, false );
            this.freeSpace = freeSpace;
            LOG.info( "Styled geometry dataset cache will use: " + freeSpace / ( 1024 * 1024d ) + " Mb." );
        }

        public Pair<TextureTileRequest, File> get( TextureTileRequest req ) {
            Pair<TextureTileRequest, File> result = null;
            File f = super.get( req );
            if ( f == null ) {
                for ( TextureTileRequest key : keySet() ) {
                    if ( key != null && req.isFullfilled( key, 0.003 ) ) {
                        f = super.get( key );
                        result = new Pair<TextureTileRequest, File>( key, f );
                        break;
                    }
                }
            } else {
                result = new Pair<TextureTileRequest, File>( req, f );
            }
            // if ( result != null ) {
            // System.out.println( "******************Found a cachefile******************" );
            // }
            return result;
        }

        public File put( TextureTileRequest request, File cacheFile, long size ) {
            File put = super.put( request, cacheFile );
            if ( put == null ) {
                usedCacheSpace += size;
                freeSpace -= size;
            }
            return put;
        }

        /**
         * Overrides to the needs of a cache.
         * 
         * @param eldest
         * @return true as defined by the contract in {@link LinkedHashMap}.
         */
        @Override
        protected boolean removeEldestEntry( Map.Entry<TextureTileRequest, File> eldest ) {
            if ( this.usedCacheSpace >= freeSpace ) {
                LOG.debug( "Removing styled geometry texture from cache, because configured cache size is reached." );
                long fSize = eldest.getValue().length();

                if ( eldest.getValue().delete() ) {
                    this.usedCacheSpace -= fSize;
                    this.freeSpace += fSize;
                } else {
                    LOG.warn( "Could not delete file: " + eldest.getValue()
                              + " from the styled geometry cache, please clear some files from the cache directory: "
                              + eldest.getValue().getParent() + " manually." );
                }

                return true;
            }
            return false;
        }
    }

    @Override
    public CRS getCRS() {
        return featureStore.getStorageSRS();
    }

}
