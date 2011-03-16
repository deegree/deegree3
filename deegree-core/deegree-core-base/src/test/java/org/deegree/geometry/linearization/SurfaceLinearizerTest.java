//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.geometry.linearization;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.standard.curvesegments.DefaultArc;
import org.deegree.geometry.standard.primitive.DefaultCurve;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.deegree.geometry.standard.primitive.DefaultRing;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * The <code>SurfaceLinearizerTest</code> class verifies the corecteness of the surface linearization
 * 
 * TODO add test to verify
 * <ul>
 * <li>polygon with inner rings</li>
 * <li>surface patches</li>
 * </ul>
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SurfaceLinearizerTest {

    private static final Logger LOG = getLogger( SurfaceLinearizerTest.class );

    final double MAX_ERROR = 0.001;

    final int MAX_POINTS = 10000;

    private org.deegree.geometry.GeometryFactory geomFac;

    private SurfaceLinearizer linearizer;

    @Before
    public void setUp() {
        geomFac = new org.deegree.geometry.GeometryFactory();
        linearizer = new SurfaceLinearizer( geomFac );
    }

    @Test
    public void linearizePolygon() {
        Polygon polygon = new DefaultPolygon( null, null, null, createExteriorRing(), createInteriorRings() );
        Polygon res = (Polygon) linearizer.linearize( polygon, new MaxErrorCriterion( MAX_ERROR, MAX_POINTS ) );
        LOG.debug( "exterior ring:" );
        for ( Curve curve : res.getExteriorRing().getMembers() ) {
            for ( Point p : curve.getControlPoints() ) {
                LOG.debug( p.get0() + ", " + p.get1() );
            }
        }
        for ( Points pts : res.getInteriorRingsCoordinates() ) {
            LOG.debug( "interior ring:" );
            for ( int i = 0; i < pts.size(); i++ )
                LOG.debug( pts.get( i ).get0() + ", " + pts.get( i ).get1() );
        }
    }

    private List<Ring> createInteriorRings() {
        return new ArrayList<Ring>();
    }

    private Ring createExteriorRing() {
        Point p1 = new DefaultPoint( null, null, null, new double[] { 1.0, 2.0 } );
        Point p2 = new DefaultPoint( null, null, null, new double[] { 3.0, 4.0 } );
        Point p3 = new DefaultPoint( null, null, null, new double[] { 5.0, 5.0 } );
        List<CurveSegment> curveSegments = new ArrayList<CurveSegment>();
        curveSegments.add( new DefaultArc( p1, p2, p3 ) );
        Curve firstCurve = new DefaultCurve( null, null, null, curveSegments );
        List<Curve> curves = new ArrayList<Curve>();
        curves.add( firstCurve );
        return new DefaultRing( null, null, null, curves );
    }
}
