//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering.r3d.opengl.rendering;

import javax.media.opengl.GL;

import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.WorldObject;

/**
 * The <code>WorldRenderableObject</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WorldRenderableObject extends WorldObject<RenderableQualityModelPart, RenderableQualityModel> implements
                                                                                                          JOGLRenderable {
    private static final long serialVersionUID = 2998719476993351372L;

    private static int counter = 10;

    private static int level = 2;

    /**
     * Creates a new WorldRenderableObject with given number of data quality levels (LOD)
     * 
     * @param id
     * @param time
     * @param bbox
     * 
     * @param levels
     */
    public WorldRenderableObject( String id, String time, Envelope bbox, int levels ) {
        this( id, time, bbox, new RenderableQualityModel[levels] );
    }

    /**
     * @param id
     *            of this object
     * @param time
     *            this object was created in the dbase
     * @param bbox
     *            of this object (may not be null)
     * @param qualityLevels
     *            this data object may render.
     */
    public WorldRenderableObject( String id, String time, Envelope bbox, RenderableQualityModel[] qualityLevels ) {
        super( id, time, bbox, qualityLevels );
    }

    /**
     * Renders the model at the given quality level or the lesser quality level if the requested one is not available.
     * 
     * @param context
     * @param params
     * @param level
     */
    private void render( GL context, ViewParams params, int level ) {
        if ( qualityLevels != null ) {
            if ( level >= 0 && qualityLevels.length > level ) {
                RenderableQualityModel model = qualityLevels[level];
                if ( model == null ) {
                    // first find the next less quality
                    for ( int i = level; i > 0; --i ) {
                        model = qualityLevels[i];
                    }
                    if ( model == null ) {
                        for ( int i = level; i < qualityLevels.length; ++i ) {
                            model = qualityLevels[i];
                        }
                    }
                }
                if ( model != null ) {
                    model.render( context, params );
                }
            }
        }
    }

    @Override
    public void render( GL context, ViewParams params ) {
        render( context, params, calcQualityLevel( params ) );
    }

    /**
     * @param eye
     * @return the level to render.
     */
    protected int calcQualityLevel( ViewParams eye ) {
        // if ( counter-- < 0 ) {
        // counter = 10;
        // level--;
        // }
        // if ( level < 0 ) {
        // level = 2;
        // }
        return level;
    }

    /**
     * @return the number of ordinates in all qualitylevels, needed for the initalization of the direct buffer.
     */
    public int getOrdinateCount() {
        int result = 0;
        if ( qualityLevels != null ) {
            for ( RenderableQualityModel model : qualityLevels ) {
                if ( model != null ) {
                    result += model.getOrdinateCount();
                }
            }
        }
        return result;
    }

    /**
     * @return the number of ordinates in all qualitylevels, needed for the initalization of the direct buffer.
     */
    public int getTextureOrdinateCount() {
        int result = 0;
        if ( qualityLevels != null ) {
            for ( RenderableQualityModel model : qualityLevels ) {
                if ( model != null ) {
                    result += model.getTextureOrdinateCount();
                }
            }
        }
        return result;
    }

}
