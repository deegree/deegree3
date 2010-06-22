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

package org.deegree.rendering.r3d.opengl.rendering;

import javax.media.opengl.GL;

import org.deegree.rendering.r3d.ViewParams;

/**
 * The <code>RenderContext</code> wraps the current GL context the view params and all other parameters necessary to
 * render a scene with opengl.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RenderContext {

    private GL context;

    private final ViewParams viewParams;

    private float terrainScale;

    private int maxTextureSize;

    // the compositing texture shader programs.
    private ShaderProgram[] ctSPrograms;

    private boolean updateLOD;

    /**
     * Construct the RenderContext with the given view parameters.
     * 
     * @param viewParams
     */
    public RenderContext( ViewParams viewParams ) {
        this.viewParams = viewParams;
    }

    /**
     * Construct the RenderContext with the given values.
     * 
     * @param viewParams
     * @param terrainScale
     *            scaled z value of the terrain.
     * 
     */
    public RenderContext( ViewParams viewParams, float terrainScale ) {
        this.viewParams = viewParams;
        this.terrainScale = Math.max( terrainScale, 0.001f );
    }

    /**
     * Construct the RenderContext with the given values.
     * 
     * @param viewParams
     * @param terrainScale
     *            scaled z value of the terrain.
     * @param maxTextureSize
     * @param compositingTextureShaderPrograms
     *            used for assigning and rendering multiple textures to a macro triangle.
     */
    public RenderContext( ViewParams viewParams, float terrainScale, int maxTextureSize,
                          ShaderProgram[] compositingTextureShaderPrograms ) {
        this.viewParams = viewParams;
        this.terrainScale = Math.max( terrainScale, 0.001f );
        this.maxTextureSize = maxTextureSize;
        this.ctSPrograms = compositingTextureShaderPrograms;
    }

    /**
     * @param context
     *            the current gl context
     */
    public final void setContext( GL context ) {
        this.context = context;
    }

    /**
     * @return the gl context
     */
    public final GL getContext() {
        return context;
    }

    /**
     * @return the viewParams
     */
    public final ViewParams getViewParams() {
        return viewParams;
    }

    /**
     * @return the terrainScale 0.001 or larger
     */
    public final float getTerrainScale() {
        return terrainScale;
    }

    /**
     * @param terrainScale
     *            the terrainScale to set.
     */
    public final void setTerrainScale( float terrainScale ) {
        this.terrainScale = Math.max( terrainScale, 0.001f );
    }

    /**
     * @return the maxTextureSize
     */
    public final int getMaxTextureSize() {
        return maxTextureSize;
    }

    /**
     * @param maxTextureSize
     *            the maxTextureSize to set
     */
    public final void setMaxTextureSize( int maxTextureSize ) {
        this.maxTextureSize = maxTextureSize;
    }

    /**
     * @param numberOfTextures
     *            the number of textures to be blended.
     * @return a compositing texture shader program for the given number of textures.
     */
    public ShaderProgram getCompositingTextureShaderProgram( int numberOfTextures ) {
        return this.ctSPrograms[numberOfTextures - 1];
    }

    /**
     * @param updateLOD
     *            true if updating the lod should be enabled.
     */
    public void setUpdateLOD( boolean updateLOD ) {
        this.updateLOD = updateLOD;
    }

    /**
     * @return true if the lod should be updated
     */
    public boolean updateLOD() {
        return this.updateLOD;
    }

}
