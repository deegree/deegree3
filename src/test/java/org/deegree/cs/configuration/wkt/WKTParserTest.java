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
package org.deegree.cs.configuration.wkt;

import java.io.IOException;

import junit.framework.Assert;

import org.deegree.cs.components.Datum;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.utilities.ProjectionUtils;
import org.junit.Test;

/**
 * The <code>WKTParserTest</code> class provides a detailed test case for the {@link WKTParser} class.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WKTParserTest {

    @Test
    public void testProjectedCS()
                            throws IOException {
        String s = createProjectedCS();

        CoordinateSystem cs = WKTParser.parse( s );
        Assert.assertTrue( cs instanceof ProjectedCRS );
        ProjectedCRS projCRS = (ProjectedCRS) cs;
        Assert.assertEquals( "Custom projected BBR crs", projCRS.getName() );
        Assert.assertEquals( "Custom projected BBR crs:bbr:0001", projCRS.getCode().getOriginal() );
        Assert.assertEquals( "x", projCRS.getAxis()[0].getName() );
        Assert.assertEquals( "east", projCRS.getAxis()[0].getOrientationAsString() );
        Assert.assertEquals( "y", projCRS.getAxis()[1].getName() );
        Assert.assertEquals( "north", projCRS.getAxis()[1].getOrientationAsString() );

        GeographicCRS geographicCRS = projCRS.getGeographicCRS();
        Assert.assertEquals( "WGS 84", geographicCRS.getName() );
        Assert.assertEquals( "WGS 84:epsg:4326", geographicCRS.getCode().getOriginal() );
        Assert.assertEquals( "Longitude", geographicCRS.getAxis()[0].getName() );
        Assert.assertEquals( "east", geographicCRS.getAxis()[0].getOrientationAsString() );
        Assert.assertEquals( "Latitude", geographicCRS.getAxis()[1].getName() );
        Assert.assertEquals( "north", geographicCRS.getAxis()[1].getOrientationAsString() );

        Datum datum = geographicCRS.getDatum();
        Assert.assertEquals( "WGS_1984", datum.getName() );
        Ellipsoid ellipsoid = ( (GeodeticDatum) datum ).getEllipsoid();
        Assert.assertEquals( "WGS84_Ellipsoid", ellipsoid.getName() );
        Assert.assertEquals( "WGS84_Ellipsoid:epsg:7030", ellipsoid.getCode().getOriginal() );
        Assert.assertEquals( 6378137.0, ellipsoid.getSemiMajorAxis() );
        Assert.assertEquals( 298.257223563, ellipsoid.getInverseFlattening() );

        PrimeMeridian pm = ( (GeodeticDatum) datum ).getPrimeMeridian();
        Assert.assertEquals( "Greenwich", pm.getName() );
        Assert.assertEquals( "Greenwich:urn:opengis:def:crs:epsg::8901", pm.getCode().getOriginal() );
        Assert.assertEquals( Unit.DEGREE, pm.getAngularUnit() );
        Assert.assertEquals( 0.0, pm.getLongitude() );
        Projection proj = projCRS.getProjection();
        Assert.assertTrue( proj instanceof LambertConformalConic );

        LambertConformalConic lcc = (LambertConformalConic) proj;
        Assert.assertEquals( 0.0, lcc.getFalseEasting() );
        Assert.assertEquals( 0.0, lcc.getFalseNorthing() );
        Assert.assertEquals( ProjectionUtils.DTR * 48.666666666666664, lcc.getFirstParallelLatitude(), 1e-12 );
        Assert.assertEquals( ProjectionUtils.DTR * 53.66666666666665, lcc.getSecondParallelLatitude(), 1e-12 );
        Assert.assertEquals( ProjectionUtils.DTR * 10.5, lcc.getNaturalOrigin().getX() );
        Assert.assertEquals( ProjectionUtils.DTR * 51.0, lcc.getNaturalOrigin().getY() );
        Assert.assertEquals( 1.0, lcc.getScale() );
    }

    private String createProjectedCS() {
        String s = "PROJCS[";
        s += "\"Custom projected BBR crs\"";
        s += ",GEOGCS[";
        s += "\"WGS 84\"";
        s += ",DATUM[";
        s += "\"WGS_1984\"";
        s += ",SPHEROID[";
        s += "\"WGS84_Ellipsoid\"";
        s += ",6378137.0";
        s += ",298.257223563";
        s += ",AUTHORITY[\"WGS84_Ellipsoid\",\"epsg:7030\"]";
        s += "]";
        s += "]";
        s += ",PRIMEM[";
        s += "\"Greenwich\"";
        s += ",0.0";
        s += ",AUTHORITY[\"Greenwich\",\"urn:opengis:def:crs:epsg::8901\"]";
        s += "]";
        s += ",UNIT[\"Degree\",0.174532925199433]";
        s += ",AXIS[\"Longitude\",EAST]";
        s += ",AXIS[\"Latitude\",NORTH]";
        s += ",AUTHORITY[\"WGS 84\",\"epsg:4326\"]";
        s += "]";
        s += ",PROJECTION[\"lambert_conformal_conic\",AUTHORITY[\"projection_for_bbr:0001\",\"projection_for_bbr:0001\"]]";
        s += ",PARAMETER[\"Latitude_Of_Origin\",51.0]";
        s += ",PARAMETER[\"Central_Meridian\",10.5]";
        s += ",PARAMETER[\"Scale_Factor\",1.0]";
        s += ",PARAMETER[\"False_Easting\",0.0]";
        s += ",PARAMETER[\"False_Northing\",0.0]";
        s += ",PARAMETER[\"Standard_Parallel1\",48.666666666666664]";
        s += ",PARAMETER[\"Standard_Parallel2\",53.66666666666665]";
        s += ",UNIT[\"Meter\",1.0]";
        s += ",AXIS[\"x\",EAST]";
        s += ",AXIS[\"y\",NORTH]";
        s += ",AUTHORITY[\"Custom projected BBR crs\",\"bbr:0001\"]";
        s += "]";
        return s;
    }
}
