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
package org.deegree.geometry;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.cs.CRSUtils;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.points.PointsArray;
import org.junit.Before;
import org.junit.Test;

public class SFSProfilerTest {

    private SFSProfiler simplifier;

    private GeometryFactory fac = new GeometryFactory();

    private final static int POINTS_PER_ARC = 10;

    @Before
    public void setUp() {
        simplifier = new SFSProfiler( new NumPointsCriterion( POINTS_PER_ARC ) );
    }

    @Test
    public void simplifyPoint() {
        Point original = fac.createPoint( null, 47.11, 23.09, CRSUtils.EPSG_4326 );
        Point simplified = simplifier.simplify( original );
        assertEquals( original.get0(), simplified.get0() );
        assertEquals( original.get1(), simplified.get1() );
        assertEquals( original.getCoordinateSystem(), simplified.getCoordinateSystem() );
    }

    @Test
    public void simplifyLineString() {
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 5.0, 5.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        LineString ls = fac.createLineString( null, CRSUtils.EPSG_4326, new PointsArray( p0, p1, p2, p3 ) );
        LineString simplified = simplifier.simplify( ls );
        assertEquals( 4, simplified.getControlPoints().size() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get0() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get1() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get0() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get1() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get0() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get1() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get0() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get1() );
    }

    @Test
    public void simplifyCurve1() {
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 5.0, 5.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        LineStringSegment segment = fac.createLineStringSegment( new PointsArray( p0, p1, p2, p3 ) );
        Curve curve = fac.createCurve( null, CRSUtils.EPSG_4326, segment );
        LineString simplified = simplifier.simplify( curve );
        assertEquals( 4, simplified.getControlPoints().size() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get0() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get1() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get0() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get1() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get0() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get1() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get0() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get1() );
    }

    @Test
    public void simplifyCurve2() {
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 5.0, 5.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p4 = fac.createPoint( null, 20.0, 20.0, CRSUtils.EPSG_4326 );
        Point p5 = fac.createPoint( null, 30.0, 30.0, CRSUtils.EPSG_4326 );
        LineStringSegment segment1 = fac.createLineStringSegment( new PointsArray( p0, p1, p2, p3 ) );
        LineStringSegment segment2 = fac.createLineStringSegment( new PointsArray( p3, p4, p5 ) );
        LineStringSegment segment3 = fac.createLineStringSegment( new PointsArray( p5, p0 ) );
        Curve curve = fac.createCurve( null, CRSUtils.EPSG_4326, segment1, segment2, segment3 );
        LineString simplified = simplifier.simplify( curve );

        assertEquals( 7, simplified.getControlPoints().size() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get0() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get1() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get0() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get1() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get0() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get1() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get0() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get1() );
        assertEquals( 20.0, simplified.getControlPoints().get( 4 ).get0() );
        assertEquals( 20.0, simplified.getControlPoints().get( 4 ).get1() );
        assertEquals( 30.0, simplified.getControlPoints().get( 5 ).get0() );
        assertEquals( 30.0, simplified.getControlPoints().get( 5 ).get1() );
        assertEquals( 0.0, simplified.getControlPoints().get( 6 ).get0() );
        assertEquals( 0.0, simplified.getControlPoints().get( 6 ).get1() );
    }

    @Test
    public void simplifyCurve3() {
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 5.0, 5.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p4 = fac.createPoint( null, 20.0, 22.0, CRSUtils.EPSG_4326 );
        Point p5 = fac.createPoint( null, 30.0, 35.0, CRSUtils.EPSG_4326 );
        LineStringSegment segment1 = fac.createLineStringSegment( new PointsArray( p0, p1, p2, p3 ) );
        Arc segment2 = fac.createArc( p3, p4, p5 );
        Curve curve = fac.createCurve( null, CRSUtils.EPSG_4326, segment1, segment2 );
        LineString simplified = simplifier.simplify( curve );

        assertEquals( POINTS_PER_ARC + 3, simplified.getControlPoints().size() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get0() );
        assertEquals( 0.0, simplified.getControlPoints().get( 0 ).get1() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get0() );
        assertEquals( 1.0, simplified.getControlPoints().get( 1 ).get1() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get0() );
        assertEquals( 5.0, simplified.getControlPoints().get( 2 ).get1() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get0() );
        assertEquals( 10.0, simplified.getControlPoints().get( 3 ).get1() );
        assertEquals( 30.0, simplified.getEndPoint().get0() );
        assertEquals( 35.0, simplified.getEndPoint().get1() );
    }

    @Test
    public void simplifyPolygon() {
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 10.0, 0.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p4 = fac.createPoint( null, 0.0, 10.0, CRSUtils.EPSG_4326 );
        Point p5 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p6 = fac.createPoint( null, 1.0, 9.0, CRSUtils.EPSG_4326 );
        Point p7 = fac.createPoint( null, 9.0, 9.0, CRSUtils.EPSG_4326 );
        Point p8 = fac.createPoint( null, 9.0, 1.0, CRSUtils.EPSG_4326 );
        Point p9 = fac.createPoint( null, 1.0, 10.0, CRSUtils.EPSG_4326 );
        Ring exterior = fac.createLinearRing( null, CRSUtils.EPSG_4326, new PointsArray( p0, p1, p2, p3, p4 ) );
        Ring interior = fac.createLinearRing( null, CRSUtils.EPSG_4326, new PointsArray( p5, p6, p7, p8, p9 ) );
        Polygon polygon = fac.createPolygon( null, CRSUtils.EPSG_4326, exterior, Collections.singletonList( interior ) );
        Polygon simplified = (Polygon) simplifier.simplify( polygon );
        assertEquals(
                      "POLYGON ((0.000000 0.000000,10.000000 0.000000,10.000000 10.000000,10.000000 10.000000,0.000000 10.000000),(1.000000 1.000000,1.000000 9.000000,9.000000 9.000000,9.000000 1.000000,1.000000 10.000000))",
                      simplified.toString() );
    }

    @Test
    public void simplifySurface1() {
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 10.0, 0.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p4 = fac.createPoint( null, 0.0, 10.0, CRSUtils.EPSG_4326 );
        Point p5 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p6 = fac.createPoint( null, 1.0, 9.0, CRSUtils.EPSG_4326 );
        Point p7 = fac.createPoint( null, 9.0, 9.0, CRSUtils.EPSG_4326 );
        Point p8 = fac.createPoint( null, 9.0, 1.0, CRSUtils.EPSG_4326 );
        Point p9 = fac.createPoint( null, 1.0, 10.0, CRSUtils.EPSG_4326 );
        LineStringSegment seg0 = fac.createLineStringSegment( new PointsArray( p0, p1, p2, p3, p4 ) );
        LineStringSegment seg1 = fac.createLineStringSegment( new PointsArray( p5, p6, p7, p8, p9 ) );
        Curve curve0 = fac.createCurve( null, CRSUtils.EPSG_4326, seg0 );
        Curve curve1 = fac.createCurve( null, CRSUtils.EPSG_4326, seg1 );
        Ring exteriorRing = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve0 ) );
        Ring interiorRing = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve1 ) );
        SurfacePatch patch0 = fac.createPolygonPatch( exteriorRing, Collections.singletonList( interiorRing ) );
        Surface surface = fac.createSurface( null, Collections.singletonList( patch0 ), CRSUtils.EPSG_4326 );
        Polygon simplified = (Polygon) simplifier.simplify( surface );
        assertEquals(
                      "POLYGON ((0.000000 0.000000,10.000000 0.000000,10.000000 10.000000,10.000000 10.000000,0.000000 10.000000),(1.000000 1.000000,1.000000 9.000000,9.000000 9.000000,9.000000 1.000000,1.000000 10.000000))",
                      simplified.toString() );
    }

    @Test
    public void simplifySurface2() {
        // exterior 1
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 10.0, 0.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p4 = fac.createPoint( null, 0.0, 10.0, CRSUtils.EPSG_4326 );
        // interior 1
        Point p5 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p6 = fac.createPoint( null, 1.0, 9.0, CRSUtils.EPSG_4326 );
        Point p7 = fac.createPoint( null, 9.0, 9.0, CRSUtils.EPSG_4326 );
        Point p8 = fac.createPoint( null, 9.0, 1.0, CRSUtils.EPSG_4326 );
        Point p9 = fac.createPoint( null, 1.0, 10.0, CRSUtils.EPSG_4326 );
        // exterior 2 (touches exterior 1)
        Point p10 = fac.createPoint( null, 10.0, 0.0, CRSUtils.EPSG_4326 );
        Point p11 = fac.createPoint( null, 20.0, 0.0, CRSUtils.EPSG_4326 );
        Point p12 = fac.createPoint( null, 20.0, 10.0, CRSUtils.EPSG_4326 );
        Point p13 = fac.createPoint( null, 20.0, 10.0, CRSUtils.EPSG_4326 );
        Point p14 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );

        LineStringSegment seg0 = fac.createLineStringSegment( new PointsArray( p0, p1, p2, p3, p4 ) );
        LineStringSegment seg1 = fac.createLineStringSegment( new PointsArray( p5, p6, p7, p8, p9 ) );
        LineStringSegment seg2 = fac.createLineStringSegment( new PointsArray( p10, p11, p12, p13, p14 ) );

        Curve curve0 = fac.createCurve( null, CRSUtils.EPSG_4326, seg0 );
        Curve curve1 = fac.createCurve( null, CRSUtils.EPSG_4326, seg1 );
        Curve curve2 = fac.createCurve( null, CRSUtils.EPSG_4326, seg2 );

        Ring exteriorRing = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve0 ) );
        Ring interiorRing = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve1 ) );
        Ring exteriorRing2 = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve2 ) );

        SurfacePatch patch0 = fac.createPolygonPatch( exteriorRing, Collections.singletonList( interiorRing ) );
        SurfacePatch patch1 = fac.createPolygonPatch( exteriorRing2, null );
        List<SurfacePatch> patches = new ArrayList<SurfacePatch>();
        patches.add( patch0 );
        patches.add( patch1 );

        Surface surface = fac.createSurface( null, patches, CRSUtils.EPSG_4326 );
        MultiPolygon simplified = (MultiPolygon) simplifier.simplify( surface );
        System.out.println( simplified );
        assertEquals(
                      "MULTIPOLYGON (((0.000000 0.000000,10.000000 0.000000,10.000000 10.000000,10.000000 10.000000,0.000000 10.000000),(1.000000 1.000000,1.000000 9.000000,9.000000 9.000000,9.000000 1.000000,1.000000 10.000000)),((10.000000 0.000000,20.000000 0.000000,20.000000 10.000000,20.000000 10.000000,10.000000 10.000000)))",
                      simplified.toString() );
    }

    @Test
    public void simplifyMultiCurve() {

        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 5.0, 5.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p4 = fac.createPoint( null, 20.0, 22.0, CRSUtils.EPSG_4326 );
        Point p5 = fac.createPoint( null, 30.0, 35.0, CRSUtils.EPSG_4326 );

        LineString ls = fac.createLineString( null, CRSUtils.EPSG_4326, new PointsArray( p0, p1, p2, p3 ) );

        LineStringSegment segment1 = fac.createLineStringSegment( new PointsArray( p0, p1, p2, p3 ) );
        Arc segment2 = fac.createArc( p3, p4, p5 );
        Curve curve = fac.createCurve( null, CRSUtils.EPSG_4326, segment1, segment2 );
        List<Curve> memberCurves = new ArrayList<Curve>();
        memberCurves.add( ls );
        memberCurves.add( curve );
        MultiCurve<?> multiCurve = fac.createMultiCurve( null, CRSUtils.EPSG_4326, memberCurves );
        MultiLineString simplified = (MultiLineString) simplifier.simplify( multiCurve );
        assertEquals(
                      "MULTILINESTRING ((0.000000 0.000000,1.000000 1.000000,5.000000 5.000000,10.000000 10.000000),(0.000000 0.000000,1.000000 1.000000,5.000000 5.000000,10.000000 10.000000,12.317848 12.699677,14.612186 15.419362,16.882842 18.158850,19.129644 20.917935,21.352424 23.696410,23.551014 26.494065,25.725249 29.310690,27.874965 32.146073,30.000000 35.000000))",
                      simplified.toString() );
    }

    @Test
    public void simplifyMultiSurface() {
        // exterior 1
        Point p0 = fac.createPoint( null, 0.0, 0.0, CRSUtils.EPSG_4326 );
        Point p1 = fac.createPoint( null, 10.0, 0.0, CRSUtils.EPSG_4326 );
        Point p2 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p3 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );
        Point p4 = fac.createPoint( null, 0.0, 10.0, CRSUtils.EPSG_4326 );
        // interior 1
        Point p5 = fac.createPoint( null, 1.0, 1.0, CRSUtils.EPSG_4326 );
        Point p6 = fac.createPoint( null, 1.0, 9.0, CRSUtils.EPSG_4326 );
        Point p7 = fac.createPoint( null, 9.0, 9.0, CRSUtils.EPSG_4326 );
        Point p8 = fac.createPoint( null, 9.0, 1.0, CRSUtils.EPSG_4326 );
        Point p9 = fac.createPoint( null, 1.0, 10.0, CRSUtils.EPSG_4326 );
        // exterior 2 (touches exterior 1)
        Point p10 = fac.createPoint( null, 10.0, 0.0, CRSUtils.EPSG_4326 );
        Point p11 = fac.createPoint( null, 20.0, 0.0, CRSUtils.EPSG_4326 );
        Point p12 = fac.createPoint( null, 20.0, 10.0, CRSUtils.EPSG_4326 );
        Point p13 = fac.createPoint( null, 20.0, 10.0, CRSUtils.EPSG_4326 );
        Point p14 = fac.createPoint( null, 10.0, 10.0, CRSUtils.EPSG_4326 );

        LineStringSegment seg0 = fac.createLineStringSegment( new PointsArray( p0, p1, p2, p3, p4 ) );
        LineStringSegment seg1 = fac.createLineStringSegment( new PointsArray( p5, p6, p7, p8, p9 ) );
        LineStringSegment seg2 = fac.createLineStringSegment( new PointsArray( p10, p11, p12, p13, p14 ) );

        Curve curve0 = fac.createCurve( null, CRSUtils.EPSG_4326, seg0 );
        Curve curve1 = fac.createCurve( null, CRSUtils.EPSG_4326, seg1 );
        Curve curve2 = fac.createCurve( null, CRSUtils.EPSG_4326, seg2 );

        Ring exteriorRing = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve0 ) );
        Ring interiorRing = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve1 ) );
        Ring exteriorRing2 = fac.createRing( null, CRSUtils.EPSG_4326, Collections.singletonList( curve2 ) );

        SurfacePatch patch0 = fac.createPolygonPatch( exteriorRing, Collections.singletonList( interiorRing ) );
        SurfacePatch patch1 = fac.createPolygonPatch( exteriorRing2, null );
        List<SurfacePatch> patches = new ArrayList<SurfacePatch>();
        patches.add( patch0 );
        patches.add( patch1 );
        Surface surface = fac.createSurface( null, patches, CRSUtils.EPSG_4326 );
        List<Surface> surfaces = new ArrayList<Surface>();
        surfaces.add( surface );
        surfaces.add( surface );
        MultiSurface<?> multiSurface = fac.createMultiSurface( null, CRSUtils.EPSG_4326, surfaces );
        MultiPolygon simplified = (MultiPolygon) simplifier.simplify( multiSurface );
        System.out.println( simplified );
        assertEquals(
                      "MULTIPOLYGON (((0.000000 0.000000,10.000000 0.000000,10.000000 10.000000,10.000000 10.000000,0.000000 10.000000),(1.000000 1.000000,1.000000 9.000000,9.000000 9.000000,9.000000 1.000000,1.000000 10.000000)),((10.000000 0.000000,20.000000 0.000000,20.000000 10.000000,20.000000 10.000000,10.000000 10.000000)),((0.000000 0.000000,10.000000 0.000000,10.000000 10.000000,10.000000 10.000000,0.000000 10.000000),(1.000000 1.000000,1.000000 9.000000,9.000000 9.000000,9.000000 1.000000,1.000000 10.000000)),((10.000000 0.000000,20.000000 0.000000,20.000000 10.000000,20.000000 10.000000,10.000000 10.000000)))",
                      simplified.toString() );
    }
}
