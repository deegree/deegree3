/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
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
package org.deegree.geometry.io;

import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class WKTReaderTest {

	@Test
	public void testPoint() throws Exception {
		Geometry geom = new WKTReader(CRSManager.lookup("EPSG:4326")).read("POINT(5.0 18.0)");
		assertThat(geom, instanceOf(Point.class));
		assertThat(((Point) geom).get0(), is(5.0));
		assertThat(((Point) geom).get1(), is(18.0));
	}

	@Test
	public void testLineString() throws Exception {
		Geometry geom = new WKTReader(CRSManager.lookup("EPSG:4326"))
			.read("LINESTRING(5.0 18.0,5.1 18.1,5.2 18.2,5.3 18.3)");
		assertThat(geom, instanceOf(LineString.class));
		assertThat(((LineString) geom).getEndPoint().get0(), is(5.3));
		assertThat(((LineString) geom).getEndPoint().get1(), is(18.3));
	}

	@Test
	public void testPolygon() throws Exception {
		Geometry geom = new WKTReader(CRSManager.lookup("EPSG:4326"))
			.read("POLYGON((5.0 18.0,5.1 18.1,5.2 18.2,5.3 18.3, 5.0 18.0))");
		assertThat(geom, instanceOf(Polygon.class));
	}

}
