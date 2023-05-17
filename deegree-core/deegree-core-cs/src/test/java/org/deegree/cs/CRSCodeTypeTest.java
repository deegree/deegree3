/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.cs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Test class for CRSCodeType
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 */
public class CRSCodeTypeTest extends TestCase {

	@Test
	public void test_urn_x_ogc() {
		CRSCodeType code = new CRSCodeType("URN:X-OGC:DEF:CRS:EPSG:6.11:4326");
		assertThat(code.toString(), is("epsg:6.11:4326"));
		assertThat(code.getCode(), is("4326"));
		assertThat(code.getCodeVersion(), is("6.11"));
		assertThat(code.getCodeSpace(), is("epsg"));
	}

	@Test
	public void test_urn_x_ogc_3PartVersion() {
		CRSCodeType code = new CRSCodeType("URN:X-OGC:DEF:CRS:EPSG:6.11.2:4326");
		assertThat(code.toString(), is("epsg:6.11.2:4326"));
		assertThat(code.getCode(), is("4326"));
		assertThat(code.getCodeVersion(), is("6.11.2"));
		assertThat(code.getCodeSpace(), is("epsg"));
	}

	@Test
	public void test_http_opengis_gml() {
		CRSCodeType code = new CRSCodeType("HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#4326");
		assertThat(code.toString(), is("epsg:4326"));
		assertThat(code.getCode(), is("4326"));
		assertThat(code.getCodeVersion(), is(""));
		assertThat(code.getCodeSpace(), is("epsg"));
	}

	@Test
	public void test_urn_opengis() {
		CRSCodeType code = new CRSCodeType("URN:OPENGIS:DEF:CRS:EPSG::4326");
		assertThat(code.toString(), is("epsg:4326"));
		assertThat(code.getCode(), is("4326"));
		assertThat(code.getCodeVersion(), is(""));
		assertThat(code.getCodeSpace(), is("epsg"));
	}

	@Test
	public void test_crs() {
		CRSCodeType code = new CRSCodeType("CRS:84");
		assertThat(code.toString(), is("CRS:84"));
		assertThat(code.getCode(), is(""));
		assertThat(code.getCodeVersion(), is(""));
		assertThat(code.getCodeSpace(), is(""));
	}

	@Test
	public void test_urn_ogc() {
		CRSCodeType code = new CRSCodeType("URN:OGC:DEF:CRS:OGC:1.3:CRS84");
		assertThat(code.toString(), is("URN:OGC:DEF:CRS:OGC:1.3:CRS84"));
		assertThat(code.getCode(), is(""));
		assertThat(code.getCodeVersion(), is(""));
		assertThat(code.getCodeSpace(), is(""));
	}

	@Test
	public void test_wgs84() {
		CRSCodeType code = new CRSCodeType("WGS84(DD)");
		assertThat(code.toString(), is("WGS84(DD)"));
		assertThat(code.getCode(), is(""));
		assertThat(code.getCodeVersion(), is(""));
		assertThat(code.getCodeSpace(), is(""));
	}

	@Test
	public void test_http_opengis_def() {
		CRSCodeType code = new CRSCodeType("HTTP://WWW.OPENGIS.NET/DEF/CRS/EPSG/0/4326");
		assertThat(code.toString(), is("epsg:0:4326"));
		assertThat(code.getCode(), is("4326"));
		assertThat(code.getCodeVersion(), is("0"));
		assertThat(code.getCodeSpace(), is("epsg"));
	}

}
