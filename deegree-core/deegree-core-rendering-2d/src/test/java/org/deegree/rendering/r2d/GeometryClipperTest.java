/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d;

import static org.junit.Assert.assertTrue;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import org.locationtech.jts.io.ParseException;

/**
 * Test cases for {@link GeometryClipper}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.3
 */
public class GeometryClipperTest {

    private GeometryClipper clipper;

    @Before
    public void setup() {
        final Envelope viewPort = new GeometryFactory().createEnvelope( 0, 0, 1, 1, null );
        final int width = 1024;
        clipper = new GeometryClipper( viewPort, width );
    }

    @Test
    public void clipGeometryDistinctToViewport()
                            throws ParseException {
        final String wkt = "POLYGON ((2 0,3 0,3 3,0 3,0 2,2 2,2 0))";
        final Geometry geometryDistinct = new WKTReader( null ).read( wkt );
        final Geometry clippedGeometry = clipper.clipGeometry( geometryDistinct );
        assertTrue( clippedGeometry == null );
    }

    @Test
    public void clipGeometryPartiallyIntersectingViewport()
                            throws ParseException {
        final String wkt = "POLYGON ((0 0,3 0,3 3,0 3,0 2,2 2,0 0))";
        final Geometry geometryDistinct = new WKTReader( null ).read( wkt );
        final Geometry clippedGeometry = clipper.clipGeometry( geometryDistinct );
        assertTrue( clippedGeometry != null );
        assertTrue( !geometryDistinct.equals( clippedGeometry ) );
    }

    @Test
    public void clipGeometryContainedInViewport()
                            throws ParseException {
        final String wkt = "POLYGON ((0.0 0.0,0.3 0.0,0.3 0.3,0.0 0.3,0.0 0.2,0.2 0.2,0.0 0.0))";
        final Geometry geometryDistinct = new WKTReader( null ).read( wkt );
        final Geometry clippedGeometry = clipper.clipGeometry( geometryDistinct );
        assertTrue( clippedGeometry != null );
        assertTrue( geometryDistinct.equals( clippedGeometry ) );
    }

}
