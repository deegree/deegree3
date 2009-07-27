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

import java.util.LinkedHashSet;
import java.util.Set;

import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTile;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTileProvider;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTileRequest;

/**
 * Manages the fetching (and caching) of {@link TextureTile} instances from {@link TextureTileProvider}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class TextureTileManager {

    private final Set<TextureTile> cachedTiles = new LinkedHashSet<TextureTile>();

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
            tile = getMatchingProvider( request.getMetersPerPixel() ).getTextureTile( request.getMinX(),
                                                                                      request.getMinY(),
                                                                                      request.getMaxX(),
                                                                                      request.getMaxY() );
            addToCache( tile );

        }
        return tile;
    }

    private void addToCache( TextureTile tile ) {
        if ( cachedTiles.size() == maxCached ) {
            TextureTile cacheDrop = cachedTiles.iterator().next();
            cachedTiles.remove( cacheDrop );
        }
        cachedTiles.add( tile );
    }

    private TextureTileProvider getMatchingProvider( double unitsPerPixel ) {
        TextureTileProvider provider = providers[0];
        for ( int i = 0; i < providers.length; i++ ) {
            if ( providers[i].getNativeResolution() > unitsPerPixel ) {
                break;
            }
            provider = providers[i];
        }
        return provider;
    }

    /**
     *
     * @param unitsPerPixel
     * @return the native resolution of the configured TextureProvider, based on the meters per pixel.
     */
    public double getMatchingResolution( double unitsPerPixel ) {
        return getMatchingProvider( unitsPerPixel ).getNativeResolution();
    }

    @Override
    public String toString() {
        return "cached: " + cachedTiles.size();
    }
}
