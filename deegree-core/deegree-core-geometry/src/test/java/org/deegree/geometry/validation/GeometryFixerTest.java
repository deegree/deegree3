//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.geometry.validation;

import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.ARC;
import static org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType.LINE_STRING_SEGMENT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.standard.curvesegments.DefaultLineStringSegment;
import org.deegree.geometry.standard.points.PointsArray;
import org.deegree.geometry.standard.primitive.DefaultCurve;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeometryFixerTest {

    private static final GeometryFactory GEOM_FACTORY = new GeometryFactory();

    private static final ICRS CRS = CRSManager.getCRSRef( "EPSG:25832" );

    @Test
    public void testInvertOrientationOfACurveWithOneSegment() {
        Curve curve = createSimpleCurve();
        Curve invertedCurve = GeometryFixer.invertOrientation( curve );

        List<CurveSegment> curveSegments = invertedCurve.getCurveSegments();
        assertThat( curveSegments.size(), is( 1 ) );

        CurveSegment firstCurveSegment = curveSegments.get( 0 );

        assertThat( firstCurveSegment.getSegmentType(), is( LINE_STRING_SEGMENT ) );

        assertThat( firstCurveSegment.getStartPoint().get0(), is( 553976.438 ) );
        assertThat( firstCurveSegment.getStartPoint().get1(), is( 5937165.552 ) );
        assertThat( firstCurveSegment.getEndPoint().get0(), is( 553978.334 ) );
        assertThat( firstCurveSegment.getEndPoint().get1(), is( 5937293.390 ) );
    }

    @Test
    public void testInvertOrientationOfACurveWithMultipleSegments() {
        Curve curve = createCurve();
        Curve invertedCurve = GeometryFixer.invertOrientation( curve );

        List<CurveSegment> curveSegments = invertedCurve.getCurveSegments();
        assertThat( curveSegments.size(), is( 3 ) );

        CurveSegment firstCurveSegment = curveSegments.get( 0 );
        CurveSegment secondCurveSegment = curveSegments.get( 1 );
        CurveSegment thirdCurveSegment = curveSegments.get( 2 );

        assertThat( firstCurveSegment.getSegmentType(), is( LINE_STRING_SEGMENT ) );
        assertThat( secondCurveSegment.getSegmentType(), is( ARC ) );
        assertThat( thirdCurveSegment.getSegmentType(), is( LINE_STRING_SEGMENT ) );

        assertTrue( invertedCurve.isClosed() );
    }

    private Curve createCurve() {
        return createCurve( createFirstSegment(), createSecondSegment(), createThirdSegment() );
    }

    private Curve createSimpleCurve() {
        return createCurve( createFirstSegment() );
    }

    private Curve createCurve( CurveSegment... curveSegments ) {
        return new DefaultCurve( "id", CRS, null, Arrays.asList( curveSegments ) );
    }

    private DefaultLineStringSegment createFirstSegment() {
        Point point1 = GEOM_FACTORY.createPoint( "idp11", 553978.334, 5937293.390, CRS );
        Point point2 = GEOM_FACTORY.createPoint( "idp12", 553979.368, 5937260.444, CRS );
        Point point3 = GEOM_FACTORY.createPoint( "idp13", 553978.261, 5937235.356, CRS );
        Point point4 = GEOM_FACTORY.createPoint( "idp14", 553976.771, 5937199.562, CRS );
        Point point5 = GEOM_FACTORY.createPoint( "idp15", 553976.438, 5937165.552, CRS );
        Points points = new PointsArray( point1, point2, point3, point4, point5 );
        return new DefaultLineStringSegment( points );
    }

    private CurveSegment createSecondSegment() {
        Point point1 = GEOM_FACTORY.createPoint( "idpa1", 553976.438, 5937165.552, CRS );
        Point point2 = GEOM_FACTORY.createPoint( "idpa2", 553974.601, 5937161.092, CRS );
        Point point3 = GEOM_FACTORY.createPoint( "idpa3", 553969.988, 5937159.683, CRS );
        return GEOM_FACTORY.createArc( point1, point2, point3 );
    }

    private CurveSegment createThirdSegment() {
        Point point1 = GEOM_FACTORY.createPoint( "idp21", 553969.988, 5937159.683, CRS );
        Point point2 = GEOM_FACTORY.createPoint( "idp22", 553938.699, 5937160.863, CRS );
        Point point3 = GEOM_FACTORY.createPoint( "idp23", 553903.017, 5937162.213, CRS );
        Point point4 = GEOM_FACTORY.createPoint( "idp24", 553897.200, 5937200.469, CRS );
        Point point5 = GEOM_FACTORY.createPoint( "idp25", 553897.613, 5937236.333, CRS );
        Point point6 = GEOM_FACTORY.createPoint( "idp26", 553897.901, 5937261.378, CRS );
        Point point7 = GEOM_FACTORY.createPoint( "idp27", 553898.295, 5937295.670, CRS );
        Point point8 = GEOM_FACTORY.createPoint( "idp28", 553938.710, 5937300.829, CRS );
        Point point9 = GEOM_FACTORY.createPoint( "idp29", 553978.334, 5937293.390, CRS );

        Points points = new PointsArray( point1, point2, point3, point4, point5, point6, point7, point8, point9 );
        return new DefaultLineStringSegment( points );
    }

}
