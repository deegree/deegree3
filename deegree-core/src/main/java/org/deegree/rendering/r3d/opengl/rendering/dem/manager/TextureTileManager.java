//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem.manager;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedHashSet;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.RasterAPITextureTileProvider;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureRequest;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTile;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTileProvider;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTileRequest;
import org.slf4j.Logger;

/**
 * Manages the fetching (and caching) of {@link TextureTile} instances from {@link TextureTileProvider}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
@LoggingNotes(debug = "logs information about how texture tiles are managed")
public class TextureTileManager {
    private static final Logger LOG = getLogger( TextureTileManager.class );

    private static final GeometryFactory geomFac = new GeometryFactory();

    private final LinkedHashSet<TextureTile> cachedTiles = new LinkedHashSet<TextureTile>();

    private final TextureTileProvider[] providers;

    private final int maxCached;

    /**
     * Construct a tile manager for the given providers.
     * 
     * @param providers
     * @param maxCached
     */
    public TextureTileManager( TextureTileProvider[] providers, int maxCached ) {
        this.providers = providers;
        this.maxCached = maxCached;
    }

    /**
     * Return the bestfitting tile matching the given request
     * 
     * @param request
     * @return the Texturetile which fits the given request best.
     */
    public TextureTile getMachingTile( TextureTileRequest request ) {
        if ( LOG.isDebugEnabled() ) {
            return getMatchingTileWithLogging( request );
        }
        return getMatchingTileWithout( request );

    }

    /**
     * @param request
     * @return
     */
    private TextureTile getMatchingTileWithout( TextureTileRequest request ) {
        TextureTile tile = null;
        for ( TextureTile candidate : cachedTiles ) {
            if ( request.isFullfilled( candidate ) ) {
                tile = candidate;
                cachedTiles.remove( candidate );
                cachedTiles.add( candidate );
                break;
            }
        }

        if ( tile == null ) {
            tile = getMatchingProvider( request.getUnitsPerPixel() ).getTextureTile( request );
            if ( tile != null && tile.enableCaching() ) {
                addToCache( tile );
            }
        }
        return tile;
    }

    /**
     * @param request
     * @return
     */
    private TextureTile getMatchingTileWithLogging( TextureTileRequest request ) {
        TextureTile tile = null;
        // System.out.println( "Cached tiles: " + cachedTiles.size() );
        LOG.debug( "testing: " + request.toString() );
        for ( TextureTile candidate : cachedTiles ) {
            LOG.debug( "against: " + candidate );
            if ( request.isFullfilled( candidate ) ) {
                LOG.debug( "-- a match " );
                tile = candidate;
                // System.out.println( "using from cache tile: " + tile.hashCode() );
                cachedTiles.remove( candidate );
                cachedTiles.add( candidate );
                break;
            }
        }

        if ( tile == null ) {
            LOG.debug( "-- no match " );
            tile = getMatchingProvider( request.getUnitsPerPixel() ).getTextureTile( request );
            if ( tile != null && tile.enableCaching() ) {
                addToCache( tile );
            }
        }
        return tile;
    }

    private void addToCache( TextureTile tile ) {
        // tile will not be null
        if ( cachedTiles.size() == maxCached ) {
            TextureTile cacheDrop = cachedTiles.iterator().next();
            if ( cacheDrop != null ) {
                cachedTiles.remove( cacheDrop );
                cacheDrop.dispose();
            }
        }
        cachedTiles.add( tile );
    }

    private TextureTileProvider getMatchingProvider( double unitsPerPixel ) {
        TextureTileProvider provider = providers[0];
        for ( int i = 0; i < providers.length; i++ ) {
            // double provRes = providers[i].getNativeResolution();
            if ( !providers[i].hasTextureForResolution( unitsPerPixel ) ) {
                break;
            }
            provider = providers[i];
        }
        LOG.debug( "Using povider with native resolution: " + provider.getNativeResolution() );
        return provider;
    }

    /**
     * 
     * @param unitsPerPixel
     * @return the native resolution of the configured TextureProvider, based on the meters per pixel.
     */
    public double getMatchingResolution( double unitsPerPixel ) {
        TextureTileProvider match = getMatchingProvider( unitsPerPixel );
        if ( match != null ) {
            // System.out.println( "The match: " + match + " requested upp: " + unitsPerPixel );
            if ( Double.isNaN( match.getNativeResolution() ) && match.hasTextureForResolution( unitsPerPixel ) ) {
                // we have a texture but it is not limited to a minimal units per pixel.
                return unitsPerPixel;
            }
            return match.getNativeResolution();
        }

        // match was null or the provider does not supply a texture for the given resolution.
        return Double.NaN;
    }

    /**
     * Create a texture request from the given texture tile providers.
     * 
     * @param glRenderContext
     * @param fragmentBBoxWorldCoordinates
     * @param requiredUnitsPerPixel
     * @param fragment
     * @return a Texture request
     */
    public TextureRequest createTextureRequest( RenderContext glRenderContext, double[][] fragmentBBoxWorldCoordinates,
                                                double requiredUnitsPerPixel, RenderMeshFragment fragment ) {
        double unitsPerPixel = requiredUnitsPerPixel;
        TextureTileProvider provider = getMatchingProvider( unitsPerPixel );
        if ( provider == null ) {
            return null;
        }

        double providerRes = getMatchingResolution( unitsPerPixel );
        // System.out.println( "ProviderRes: " + providerRes );
        if ( !( Double.isNaN( providerRes ) || Double.isInfinite( providerRes ) ) ) {
            // System.out.println( "Setting ProviderRes: " + providerRes );
            unitsPerPixel = providerRes;
        }
        // rb: no 0 values, TODO configuration?
        unitsPerPixel = Math.max( unitsPerPixel, 0.00001 );

        // check if the texture gets too large with respect to the maximum texture size
        unitsPerPixel = clipResolution( unitsPerPixel, fragmentBBoxWorldCoordinates,
                                        glRenderContext.getMaxTextureSize() );

        if ( provider instanceof RasterAPITextureTileProvider ) {
            // RasterGeoReference geoRef = ( (RasterAPITextureTileProvider) provider ).getRasterReference();
            Envelope env = geomFac.createEnvelope( fragmentBBoxWorldCoordinates[0], fragmentBBoxWorldCoordinates[1],
                                                   null );
            // RasterRect rect = geoRef.convertEnvelopeToRasterCRS( env );
            // boolean createNewEnv = false;
            // make the result an even number of pixels.
            // if ( rect.width % 2 != 0 ) {
            // if ( rect.width + 1 >= glRenderContext.getMaxTextureSize() ) {
            // rect.width--;
            // } else {
            // rect.width++;
            // }
            // createNewEnv = true;
            // }
            // if ( rect.height % 2 != 0 ) {
            // if ( rect.height + 1 >= glRenderContext.getMaxTextureSize() ) {
            // rect.height--;
            // } else {
            // rect.height++;
            // }
            // createNewEnv = true;
            // }
            //
            // if ( createNewEnv ) {
            // env = geoRef.getEnvelope( rect, null );
            // }
            int xAxis = 0;
            if ( provider.getCRS() != null ) {
                CoordinateSystem crs = null;
                try {
                    crs = provider.getCRS().getWrappedCRS();
                    xAxis = crs.getEasting();

                } catch ( UnknownCRSException e ) {
                    // nothing
                }
            }
            return new TextureRequest( fragment, env.getMin().get( xAxis ), env.getMin().get( 1 - xAxis ),
                                       env.getMax().get( xAxis ), env.getMax().get( 1 - xAxis ), (float) unitsPerPixel );

        }

        double minX = fragmentBBoxWorldCoordinates[0][0];
        double minY = fragmentBBoxWorldCoordinates[0][1];
        double maxX = fragmentBBoxWorldCoordinates[1][0];
        double maxY = fragmentBBoxWorldCoordinates[1][1];

        double worldWidth = maxX - minX;
        double worldHeight = maxY - minY;

        double iWidth = worldWidth / unitsPerPixel;
        double iHeight = worldHeight / unitsPerPixel;
        int imageWidth = (int) Math.ceil( iWidth );
        int imageHeight = (int) Math.ceil( iHeight );

        // following values are note the half distance to the next pixel in world coordinates.
        // double dW = ( resolution - rW ) * 0.5;
        // double dH = ( resolution - rH ) * 0.5;

        // rb: create an image which is even (needed for opengl).
        if ( imageWidth % 2 != 0 ) {
            double dW = ( unitsPerPixel + ( unitsPerPixel * ( imageWidth - iWidth ) ) ) * 0.5;
            imageWidth++;
            minX -= dW;
            maxX += dW;
        }
        if ( imageHeight % 2 != 0 ) {
            double dH = ( unitsPerPixel + ( unitsPerPixel * ( imageHeight - iHeight ) ) ) * 0.5;
            imageHeight++;
            minY -= dH;
            maxY += dH;
        }

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "frag (world) bbox: " + fragmentBBoxWorldCoordinates[0][0] + ","
                       + fragmentBBoxWorldCoordinates[0][1] + " | " + fragmentBBoxWorldCoordinates[1][0] + ","
                       + fragmentBBoxWorldCoordinates[1][1] );
            LOG.trace( "requ bbox: " + minX + "," + minY + " | " + maxX + "," + maxY );
        }

        return new TextureRequest( fragment, minX, minY, maxX, maxY, (float) unitsPerPixel );

    }

    private double clipResolution( double unitsPerPixel, double[][] tilebbox, int maxTextureSize ) {
        // LOG.warn( "The maxTextureSize in the TextureManager is hardcoded to 1024." );
        double width = tilebbox[1][0] - tilebbox[0][0];
        double height = tilebbox[1][1] - tilebbox[0][1];
        double maxLen = Math.max( width, height );
        int textureSize = (int) Math.ceil( maxLen / unitsPerPixel );
        if ( textureSize > maxTextureSize ) {
            LOG.debug( "Texture size (={}) exceeds maximum texture size (={}). Setting units/Pixel: {}, to: {}",
                       new Object[] { textureSize, maxTextureSize, unitsPerPixel, ( maxLen / maxTextureSize ) } );
            unitsPerPixel = maxLen / maxTextureSize;
        }
        return unitsPerPixel;
    }

    @Override
    public String toString() {
        return "Number of providers: " + providers.length
               + ( ( providers[0] == null ) ? " " : " of type: " + providers[0].getClass() ) + ", cached tiles: ";
        // + cachedTiles.size();
    }
}
