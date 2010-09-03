//$HeadURL$
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

import junit.framework.Assert;

import org.deegree.cs.transformations.CRSDefines;
import org.junit.Test;

/**
 * Test the domain of validity for a known crs.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DomainOfValidityTest implements CRSDefines {

    /**
     * Test the area of use for a geographic crs
     */
    @Test
    public void wgs84GeoTest() {
        double[] areaOfUse = CRSDefines.geographic_4314.getAreaOfUseBBox();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 5.87, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 47.27, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 13.83, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 55.04, areaOfUse[3], 1E-8 );

        areaOfUse = CRSDefines.geographic_4314_lat_lon.getAreaOfUseBBox();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 5.87, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 47.27, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 13.83, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 55.04, areaOfUse[3], 1E-8 );

    }

    /**
     * Test the area of validity for a projected crs
     */
    @Test
    public void validGeoAreaTest() {
        double[] areaOfUse = CRSDefines.geographic_4314.getValidDomain();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 5.870573065378319, areaOfUse[0], 1E-10 );
        Assert.assertEquals( 47.27080493906228, areaOfUse[1], 1E-10 );
        Assert.assertEquals( 13.83188074193107, areaOfUse[2], 1E-10 );
        Assert.assertEquals( 55.04172947548665, areaOfUse[3], 1E-10 );

        areaOfUse = CRSDefines.geographic_4314_lat_lon.getValidDomain();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 47.27080493906228, areaOfUse[0], 1E-10 );
        Assert.assertEquals( 5.870573065378319, areaOfUse[1], 1E-10 );
        Assert.assertEquals( 55.04172947548665, areaOfUse[2], 1E-10 );
        Assert.assertEquals( 13.83188074193107, areaOfUse[3], 1E-10 );
    }

    /**
     * Test the area of use for a projected crs
     */
    @Test
    public void wgs84ProjectedTest() {
        double[] areaOfUse = CRSDefines.projected_25832.getAreaOfUseBBox();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 5.05, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 57.9, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 12.0, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 65.67, areaOfUse[3], 1E-8 );

        areaOfUse = CRSDefines.projected_25832_lat_lon.getAreaOfUseBBox();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 5.05, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 57.9, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 12.0, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 65.67, areaOfUse[3], 1E-8 );

        areaOfUse = CRSDefines.projected_25832_yx.getAreaOfUseBBox();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 5.05, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 57.9, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 12.0, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 65.67, areaOfUse[3], 1E-8 );

    }

    /**
     * Test the area of validity for a projected crs
     */
    @Test
    public void validProjectedAreaTest() {
        double[] areaOfUse = CRSDefines.projected_25832.getValidDomain();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 265948.819050026, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 6417675.8029399365, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 677786.3628639532, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 7288831.70135825, areaOfUse[3], 1E-8 );

        areaOfUse = CRSDefines.projected_25832_lat_lon.getValidDomain();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 265948.819050026, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 6417675.8029399365, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 677786.3628639532, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 7288831.70135825, areaOfUse[3], 1E-8 );

        areaOfUse = CRSDefines.projected_25832_yx.getValidDomain();
        Assert.assertNotNull( areaOfUse );
        Assert.assertEquals( 6417675.8029399365, areaOfUse[0], 1E-8 );
        Assert.assertEquals( 265948.819050026, areaOfUse[1], 1E-8 );
        Assert.assertEquals( 7288831.70135825, areaOfUse[2], 1E-8 );
        Assert.assertEquals( 677786.3628639532, areaOfUse[3], 1E-8 );
    }
}
