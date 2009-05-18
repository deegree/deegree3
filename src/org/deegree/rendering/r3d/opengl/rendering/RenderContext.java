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

    private int[] shaderProgramIds;

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
     * @param shaderProgramIds
     * 
     */
    public RenderContext( ViewParams viewParams, float terrainScale, int maxTextureSize, int[] shaderProgramIds ) {
        this.viewParams = viewParams;
        this.terrainScale = Math.max( terrainScale, 0.001f );
        this.maxTextureSize = maxTextureSize;
        this.shaderProgramIds = shaderProgramIds;
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
     * @return the shaderProgramIds
     */
    public final int[] getShaderProgramIds() {
        return shaderProgramIds;
    }

    /**
     * @param shaderProgramIds
     *            the shaderProgramIds to set
     */
    public final void setShaderProgramIds( int[] shaderProgramIds ) {
        this.shaderProgramIds = shaderProgramIds;
    }

}
