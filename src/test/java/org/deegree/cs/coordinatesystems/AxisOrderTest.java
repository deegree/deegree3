//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.cs.coordinatesystems;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import junit.framework.TestCase;

import org.deegree.cs.CRS;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.components.Axis;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class AxisOrderTest extends TestCase {

    @Test
    public void testAxisOrderDef()
                            throws IllegalArgumentException, UnknownCRSException {
        CRS crsLikeDefined = new CRS( "4326_AO" );
        CoordinateSystem wCRSLikeDefined = crsLikeDefined.getWrappedCRS();
        assertNotNull( wCRSLikeDefined );
        Axis[] axisLikeDefined = wCRSLikeDefined.getAxis();
        assertNotNull( axisLikeDefined );
        assertTrue( axisLikeDefined.length > 0 );
        assertEquals( Axis.AO_NORTH, axisLikeDefined[0].getOrientation() );
        assertEquals( Axis.AO_EAST, axisLikeDefined[1].getOrientation() );
    }

    @Test
    public void testAxisOrderXY()
                            throws IllegalArgumentException, UnknownCRSException {
        CRS crsXY = new CRS( "4326_AO", true );
        CoordinateSystem wCRSXY = crsXY.getWrappedCRS();
        assertNotNull( wCRSXY );
        Axis[] axisXY = wCRSXY.getAxis();
        assertNotNull( axisXY );
        assertTrue( axisXY.length > 0 );
        assertEquals( Axis.AO_EAST, axisXY[0].getOrientation() );
        assertEquals( Axis.AO_NORTH, axisXY[1].getOrientation() );
    }

    @Test
    public void testWGS84DefToDef()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "4326_AO" );
        CRS targetCRS = new CRS( "4326_AO" );
        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 46.074, 9.799, Double.NaN ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertEquals( 46.074, transformedPoints.get( 0 ).x );
        assertEquals( 9.799, transformedPoints.get( 0 ).y );
        assertEquals( Double.NaN, transformedPoints.get( 0 ).z );
    }

    @Test
    public void testWGS84XYToDef()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "4326_AO", true );
        CRS targetCRS = new CRS( "4326_AO" );
        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 9.799, 46.074, Double.NaN ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertEquals( 46.074, transformedPoints.get( 0 ).x );
        assertEquals( 9.799, transformedPoints.get( 0 ).y );
        assertEquals( Double.NaN, transformedPoints.get( 0 ).z );
    }

    @Test
    public void testWGS84XYToXY()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "4326_AO", true );
        CRS targetCRS = new CRS( "4326_AO", true );
        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 9.799, 46.074, Double.NaN ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertEquals( 9.799, transformedPoints.get( 0 ).x );
        assertEquals( 46.074, transformedPoints.get( 0 ).y );
        assertEquals( Double.NaN, transformedPoints.get( 0 ).z );
    }

    @Test
    public void testWGS84DefToXY()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "4326_AO" );
        CRS targetCRS = new CRS( "4326_AO", true );
        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 46.074, 9.799, Double.NaN ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertEquals( 9.799, transformedPoints.get( 0 ).x );
        assertEquals( 46.074, transformedPoints.get( 0 ).y );
        assertEquals( Double.NaN, transformedPoints.get( 0 ).z );
    }

    @Test
    public void testTransformDefToDef()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "4326_AO" );
        CRS targetCRS = new CRS( "EPSG:31467" );

        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 47.851111, 9.432778, Double.NaN ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertTrue( 3532465.55 < transformedPoints.get( 0 ).x && 3532465.6 > transformedPoints.get( 0 ).x );
        assertTrue( 5301523.45 < transformedPoints.get( 0 ).y && 5301523.55 > transformedPoints.get( 0 ).y );
        assertEquals( Double.NaN, transformedPoints.get( 0 ).z );
    }

    @Test
    public void testTransformDefToDefInverse()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "EPSG:31467" );
        CRS targetCRS = new CRS( "4326_AO" );
        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 3532465.57, 5301523.49, 817 ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertTrue( 47.825 < transformedPoints.get( 0 ).x && 47.875 > transformedPoints.get( 0 ).x );
        assertTrue( 9.4 < transformedPoints.get( 0 ).y && 9.45 > transformedPoints.get( 0 ).y );
        assertEquals( 817.0, transformedPoints.get( 0 ).z );
    }

    @Test
    public void testTransformXYToDef()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "4326_AO", true );
        CRS targetCRS = new CRS( "EPSG:31467" );
        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 9.432778, 47.851111, Double.NaN ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertTrue( 3532465.55 < transformedPoints.get( 0 ).x && 3532465.6 > transformedPoints.get( 0 ).x );
        assertTrue( 5301523.45 < transformedPoints.get( 0 ).y && 5301523.55 > transformedPoints.get( 0 ).y );
        assertEquals( Double.NaN, transformedPoints.get( 0 ).z );
    }

    @Test
    public void testTransformDefToXY()
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CRS sourceCRS = new CRS( "EPSG:31467" );
        CRS targetCRS = new CRS( "4326_AO", true );
        CoordinateTransformer tranformer = new CoordinateTransformer( targetCRS.getWrappedCRS() );
        List<Point3d> points = new ArrayList<Point3d>();
        points.add( new Point3d( 3532465.57, 5301523.49, 817 ) );
        List<Point3d> transformedPoints = tranformer.transform( sourceCRS.getWrappedCRS(), points );

        assertNotNull( transformedPoints );
        assertEquals( 1, transformedPoints.size() );
        assertTrue( 9.4 < transformedPoints.get( 0 ).x && 9.45 > transformedPoints.get( 0 ).x );
        assertTrue( 47.825 < transformedPoints.get( 0 ).y && 47.875 > transformedPoints.get( 0 ).y );
        assertEquals( 817.0, transformedPoints.get( 0 ).z );
    }

}