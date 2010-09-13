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

import static org.deegree.cs.projections.SupportedProjectionParameters.FALSE_EASTING;
import static org.deegree.cs.projections.SupportedProjectionParameters.FALSE_NORTHING;
import static org.deegree.cs.projections.SupportedProjectionParameters.FIRST_PARALLEL_LATITUDE;
import static org.deegree.cs.projections.SupportedProjectionParameters.LATITUDE_OF_NATURAL_ORIGIN;
import static org.deegree.cs.projections.SupportedProjectionParameters.LONGITUDE_OF_NATURAL_ORIGIN;
import static org.deegree.cs.projections.SupportedProjectionParameters.SCALE_AT_NATURAL_ORIGIN;
import static org.deegree.cs.projections.SupportedProjectionParameters.SECOND_PARALLEL_LATITUDE;
import static org.deegree.cs.utilities.ProjectionUtils.DTR;

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
 * The <code>WKTParserTest</code> class provides a detailed check for the {@link WKTParser} class.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WKTParserTest {

    final String projCRSName = "Custom projected BBR crs";

    final String projCRSCode = "bbr:0001";

    final String geogCRSName = "WGS 84";

    final String geogCRSCode = "epsg:4326";

    final String datumName = "WGS_1984";

    final String ellipsoidName = "WGS84_Ellipsoid";

    final String ellipsoidCode = "epsg:7030";

    final double semiMajorAxis = 6378137.0;

    final double inverseFlattening = 298.257223563;

    final String pmName = "Greenwich";

    final String pmCode = "urn:opengis:def:crs:epsg::8901";

    final double pmLongitude = 0.0;

    /** Projection parameters **/
    final double falseEasting = 0.0;

    final double falseNorthing = 0.0;

    final double scaleFactor = 1.0;

    final double lonNatOrigin = 10.5;

    final double latNatOrigin = 51.0;

    final double stdParallel1 = 48.666666666666664;

    final double stdParallel2 = 53.66666666666665;

    @Test
    public void testProjectedCS()
                            throws IOException {
        String s = buildPROJCS( projCRSName, projCRSCode, "Meter", 1.0 );

        CoordinateSystem cs = WKTParser.parse( s );
        Assert.assertTrue( cs instanceof ProjectedCRS );
        ProjectedCRS projCRS = (ProjectedCRS) cs;
        Assert.assertEquals( projCRSName, projCRS.getName() );
        Assert.assertEquals( projCRSName + ":" + projCRSCode, projCRS.getCode().getOriginal() );
        Assert.assertEquals( "x", projCRS.getAxis()[0].getName() );
        Assert.assertEquals( "east", projCRS.getAxis()[0].getOrientationAsString() );
        Assert.assertEquals( "y", projCRS.getAxis()[1].getName() );
        Assert.assertEquals( "north", projCRS.getAxis()[1].getOrientationAsString() );

        GeographicCRS geographicCRS = projCRS.getGeographicCRS();
        Assert.assertEquals( geogCRSName, geographicCRS.getName() );
        Assert.assertEquals( geogCRSName + ":" + geogCRSCode, geographicCRS.getCode().getOriginal() );
        Assert.assertEquals( "Longitude", geographicCRS.getAxis()[0].getName() );
        Assert.assertEquals( "east", geographicCRS.getAxis()[0].getOrientationAsString() );
        Assert.assertEquals( "Latitude", geographicCRS.getAxis()[1].getName() );
        Assert.assertEquals( "north", geographicCRS.getAxis()[1].getOrientationAsString() );

        Datum datum = geographicCRS.getDatum();
        Assert.assertEquals( datumName, datum.getName() );
        Ellipsoid ellipsoid = ( (GeodeticDatum) datum ).getEllipsoid();
        Assert.assertEquals( ellipsoidName, ellipsoid.getName() );
        Assert.assertEquals( ellipsoidName + ":" + ellipsoidCode, ellipsoid.getCode().getOriginal() );
        Assert.assertEquals( semiMajorAxis, ellipsoid.getSemiMajorAxis() );
        Assert.assertEquals( inverseFlattening, ellipsoid.getInverseFlattening() );

        PrimeMeridian pm = ( (GeodeticDatum) datum ).getPrimeMeridian();
        Assert.assertEquals( pmName, pm.getName() );
        Assert.assertEquals( pmName + ":" + pmCode, pm.getCode().getOriginal() );
        Assert.assertEquals( Unit.DEGREE, pm.getAngularUnit() );
        Assert.assertEquals( pmLongitude, pm.getLongitude() );
        Projection proj = projCRS.getProjection();
        Assert.assertTrue( proj instanceof LambertConformalConic );

        LambertConformalConic lcc = (LambertConformalConic) proj;
        Assert.assertEquals( falseEasting, lcc.getFalseEasting() );
        Assert.assertEquals( falseNorthing, lcc.getFalseNorthing() );
        Assert.assertEquals( DTR * stdParallel1, lcc.getFirstParallelLatitude(), 1e-12 );
        Assert.assertEquals( DTR * stdParallel2, lcc.getSecondParallelLatitude(), 1e-12 );
        Assert.assertEquals( DTR * lonNatOrigin, lcc.getNaturalOrigin().x );
        Assert.assertEquals( DTR * latNatOrigin, lcc.getNaturalOrigin().y );
        Assert.assertEquals( scaleFactor, lcc.getScale() );
    }

    private String buildPROJCS( String name, String code, String unitName, double unitConversion ) {
        String s = "PROJCS[";
        s += "\"" + name + "\"";
        s += "," + buildGEOGCS( geogCRSName, geogCRSCode, "Degree", ProjectionUtils.DTR );
        s += ","
             + buildProjAndParams( "lambert_conformal_conic", "projection_for_bbr:0001", "projection_for_bbr:0001",
                                   latNatOrigin, lonNatOrigin, scaleFactor, falseEasting, falseNorthing, stdParallel1,
                                   stdParallel2 );
        s += ",UNIT[\"" + unitName + "\"," + unitConversion + "]";
        s += ",AXIS[\"x\",EAST]";
        s += ",AXIS[\"y\",NORTH]";
        s += ",AUTHORITY[\"" + name + "\",\"" + code + "\"]";
        s += "]";
        return s;
    }

    private String buildProjAndParams( String type, String name, String code, double latNatOrigP, double lonNatOrigP,
                                       double scaleFactorP, double falseEastingP, double falseNorthingP,
                                       double stdParallel1P, double stdParallel2P ) {
        String s = "PROJECTION[\"" + type + "\",AUTHORITY[\"" + name + "\",\"" + code + "\"]]";
        s += ",PARAMETER[\"" + LATITUDE_OF_NATURAL_ORIGIN + "\"," + latNatOrigP + "]";
        s += ",PARAMETER[\"" + LONGITUDE_OF_NATURAL_ORIGIN + "\"," + lonNatOrigP + "]";
        s += ",PARAMETER[\"" + SCALE_AT_NATURAL_ORIGIN + "\"," + scaleFactorP + "]";
        s += ",PARAMETER[\"" + FALSE_EASTING + "\"," + falseEastingP + "]";
        s += ",PARAMETER[\"" + FALSE_NORTHING + "\"," + falseNorthingP + "]";
        s += ",PARAMETER[\"" + FIRST_PARALLEL_LATITUDE + "\"," + stdParallel1P + "]";
        s += ",PARAMETER[\"" + SECOND_PARALLEL_LATITUDE + "\"," + stdParallel2P + "]";
        return s;
    }

    private String buildGEOGCS( String name, String code, String unitName, double unitConversion ) {
        String s = "GEOGCS[";
        s += "\"" + name + "\"";
        s += "," + buildDatum( datumName );
        s += "," + buildPrimeMeridian( pmName, pmCode, pmLongitude );
        s += ",UNIT[\"" + unitName + "\"," + unitConversion + "]";
        s += ",AXIS[\"Longitude\",EAST]";
        s += ",AXIS[\"Latitude\",NORTH]";
        s += ",AUTHORITY[\"" + name + "\",\"" + code + "\"]";
        s += "]";
        return s;
    }

    private String buildPrimeMeridian( String name, String code, double longitude ) {
        String s = "PRIMEM[";
        s += "\"" + name + "\"";
        s += "," + longitude;
        s += ",AUTHORITY[\"" + name + "\",\"" + code + "\"]";
        s += "]";
        return s;
    }

    private String buildDatum( String name ) {
        String s = "DATUM[";
        s += "\"" + name + "\"";
        s += "," + buildEllipsoid( ellipsoidName, ellipsoidCode, semiMajorAxis, inverseFlattening );
        s += "]";
        return s;
    }

    private String buildEllipsoid( String name, String code, double semiMajorAxisP, double inverseFlatteningP ) {
        String s = "SPHEROID[";
        s += "\"" + name + "\"";
        s += "," + semiMajorAxisP;
        s += "," + inverseFlatteningP;
        s += ",AUTHORITY[\"" + name + "\",\"" + code + "\"]";
        s += "]";
        return s;
    }
}
