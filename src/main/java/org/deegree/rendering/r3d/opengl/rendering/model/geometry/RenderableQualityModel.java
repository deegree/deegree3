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

package org.deegree.rendering.r3d.opengl.rendering.model.geometry;

import java.util.ArrayList;

import org.deegree.rendering.r3d.model.QualityModel;
import org.deegree.rendering.r3d.opengl.rendering.JOGLRenderable;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypePool;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypeReference;

/**
 * The <code>RenderableQualityModel</code> defines the basis for a QualityModel of a Renderable object. It holds a set
 * of geometries or a reference to a prototype, never both.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RenderableQualityModel extends QualityModel<RenderableQualityModelPart> implements JOGLRenderable {

    /**
     *
     */
    private static final long serialVersionUID = 4351593641629010871L;

    /**
     * Creates a GeometryQualityModel with an empty list of geometry patches
     * 
     */
    public RenderableQualityModel() {
        super();
    }

    /**
     * Creates a GeometryQualityModel with the given geometry patches
     * 
     * @param qualityModelParts
     */
    public RenderableQualityModel( ArrayList<RenderableQualityModelPart> qualityModelParts ) {
        super( qualityModelParts );
    }

    /**
     * Creates a GeometryQualityModel with the given geometry patch
     * 
     * @param geometryPatch
     */
    public RenderableQualityModel( RenderableQualityModelPart geometryPatch ) {
        super( geometryPatch );
    }

    /**
     * @param prototypeReference
     */
    public RenderableQualityModel( PrototypeReference prototypeReference ) {
        super( prototypeReference );
    }

    @Override
    public void render( RenderContext glRenderContext ) {
        this.renderPrepared( glRenderContext, null );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( "RenderableQualitModel:" );
        if ( prototype != null ) {
            sb.append( "\nPrototype: " );
            sb.append( prototype.toString() );
        } else {
            if ( qualityModelParts != null && qualityModelParts.size() > 0 ) {
                for ( RenderableQualityModelPart data : qualityModelParts ) {
                    sb.append( "\nQualityModelPart: " );
                    if ( data != null ) {
                        sb.append( data.toString() );
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * @return the number of ordinates in each quality model part.
     */
    public int getOrdinateCount() {
        int result = 0;
        if ( qualityModelParts != null && !qualityModelParts.isEmpty() ) {
            for ( RenderableQualityModelPart rqmp : qualityModelParts ) {
                if ( rqmp != null ) {
                    result += rqmp.getOrdinateCount();
                }
            }
        }
        return result;
    }

    /**
     * @return the number of texture ordinates in each quality model part.
     */
    public int getTextureOrdinateCount() {
        int result = 0;
        if ( qualityModelParts != null && !qualityModelParts.isEmpty() ) {
            for ( RenderableQualityModelPart rqmp : qualityModelParts ) {
                if ( rqmp != null ) {
                    result += rqmp.getTextureOrdinateCount();
                }
            }
        }
        return result;
    }

    /**
     * @param glRenderContext
     * @param geomBuffer
     */
    public void renderPrepared( RenderContext glRenderContext, DirectGeometryBuffer geomBuffer ) {
        if ( prototype != null ) {
            PrototypePool.render( glRenderContext, prototype, geomBuffer );
        } else {
            // no prototype to render, trying geometries
            if ( qualityModelParts != null && qualityModelParts.size() > 0 ) {
                for ( RenderableQualityModelPart data : qualityModelParts ) {
                    if ( data != null ) {
                        data.renderPrepared( glRenderContext, geomBuffer );
                    }
                }
            }
        }
    }
}
