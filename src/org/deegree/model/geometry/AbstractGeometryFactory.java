//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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

package org.deegree.model.geometry;

import java.util.List;

import org.deegree.model.geometry.primitive.SurfacePatch;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public abstract class AbstractGeometryFactory implements GeometryFactory {

    private String description;

    private String name;

    private List<CurveSegment.Interpolation> curveInterpolations;

    private List<SurfacePatch.Interpolation> surfaceInterpolations;

    private List<Class<?>> geometries;



    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#getSupportedCurveInterpolations()
     */
    public List<CurveSegment.Interpolation> getSupportedCurveInterpolations() {
        return curveInterpolations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#getSupportedGeometries()
     */
    public List<Class<?>> getSupportedGeometries() {
        return geometries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#getSupportedSurfaceInterpolations()
     */
    public List<SurfacePatch.Interpolation> getSupportedSurfaceInterpolations() {
        return surfaceInterpolations;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.GeometryFactory#setDescription(java.lang.String)
     */
    public void setDescription( String description ) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.GeometryFactory#setName(java.lang.String)
     */
    public void setName( String name ) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.GeometryFactory#setSupportedCurveInterpolations(java.util.List)
     */
    public void setSupportedCurveInterpolations( List<CurveSegment.Interpolation> interpolations ) {
        this.curveInterpolations = interpolations;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.GeometryFactory#setSupportedGeometries(java.util.List)
     */
    public void setSupportedGeometries( List<Class<?>> supportedGeometries ) {
        this.geometries = supportedGeometries;   
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.GeometryFactory#setSupportedSurfaceInterpolations(java.util.List)
     */
    public void setSupportedSurfaceInterpolations( List<SurfacePatch.Interpolation> interpolations ) {
        this.surfaceInterpolations = interpolations;
        
    }
    
}
