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

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.composite.CompositeCurve;
import org.deegree.model.geometry.composite.CompositeSolid;
import org.deegree.model.geometry.composite.CompositeSurface;
import org.deegree.model.geometry.composite.GeometricComplex;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSolid;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.SurfacePatch;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface GeometryFactory {
    
    public String getName();
    
    public void setName(String name);
    
    public String getDescription();
    
    public void setDescription(String description);
    
    public List<Class> getSupportedGeometries();
    
    public void setSupportedGeometries(List<Class> supportedGeometries);
    
    public List<CurveSegment.INTERPOLATION> getSupportedCurveInterpolations();
    
    public void setSupportedCurveInterpolations(List<CurveSegment.INTERPOLATION> interpolations);
    
    public List<SurfacePatch.INTERPOLATION> getSupportedSurfaceInterpolations();
    
    public void setSupportedSurfaceInterpolations(List<SurfacePatch.INTERPOLATION> interpolations);
    
    public Point createPoint(double[] coordinates, double precision, CoordinateSystem crs);
    
    public Point createPoint(double[] coordinates, CoordinateSystem crs);
    
    public Curve createCurve(Point[][] coordinates, Curve.ORIENTATION orientation, CoordinateSystem crs);
    
    public Curve createCurve(CurveSegment[] segments, Curve.ORIENTATION orientation, CoordinateSystem crs);
    
    public CurveSegment createCurveSegment(List<Point> points, Class type, CurveSegment.INTERPOLATION interpolation);
    
    public Surface createSurface(Curve[] boundary, CoordinateSystem crs, SurfacePatch.INTERPOLATION interpolation);
    
    public Surface createSurface(SurfacePatch[] patches, CoordinateSystem crs);
    
    public SurfacePatch createSurfacePatch(List<Curve> boundary, Class type, SurfacePatch.INTERPOLATION interpolation);
    
    public Solid createSolid(Surface[] outerboundary, Surface[][] innerboundarie, CoordinateSystem crs );
    
    public MultiGeometry<Geometry> createMultiGeometry(List<Geometry> geometries);
    
    public MultiPoint<Point> createMultiPoint(List<Point> points);
    
    public MultiCurve<Curve> createMultiCurve(List<Curve> curves);
    
    public MultiSurface<Surface> createMultiSurface(List<Surface> surfaces);
    
    public MultiSolid<Solid> createMultiSolid(List<Solid> solids);
    
    public CompositeCurve createCompositeCurve(List<Curve> curves);
    
    public CompositeSurface createCompositeSurface(List<Surface> surfaces);
    
    public CompositeSolid createCompositeSolid(List<Solid> solids);
    
    public GeometricComplex createGeometricComplex(List<Geometry> geometries);
    
    public Envelope createEnvelope(double[] min, double[] max, double precision, CoordinateSystem crs);
    
    /**
     * creates an envelope from a SurfacePatch representing a envelope by being constructed by
     * five points: minx,miny minx,maxy maxx,maxy maxx,miny minx,miny 
     * @param patch
     * @return envelope created from a SurfacePatch
     */
    public Envelope createEnvelope(SurfacePatch patch);

}
