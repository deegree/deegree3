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
package org.deegree.ogcwebservices.wpvs.utils;

import javax.vecmath.Color3f;

/**
 * class for calculating sun light according to a specific tima, day of
 * the year (northern hemisper)
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class SunLight {

    private static final float BASE_LIGHT_INTENSITY = 0.95f;
    private SunPosition sunPosition;
    private double latitude;

    /**
     * @param latitude the position on the earth
     * @param sunPosition the Position of the sun
     */
    public SunLight( double latitude, SunPosition sunPosition ){
        this.latitude = latitude;
        this.sunPosition = sunPosition;
    }


    /**
     * @return a Color of the sunlight
     */
    public Color3f calculateSunlight( ) {

        double vDir = sunPosition.getVerticalSunposition( latitude );
        float c = 7.25f*((float)Math.sin( vDir ));

        float r = (float)(BASE_LIGHT_INTENSITY + (c/16.0) + 0.05)*0.6f;
        float g = (float)(BASE_LIGHT_INTENSITY + (c/18.5) + 0.05)*0.6f;
        float b = (float)(BASE_LIGHT_INTENSITY  +(c/17.0) + 0.05)*0.55f;
        if ( r > 1 ) r = 1;
        if ( g > 1 ) g = 1;
        if ( b > 1 ) b = 1;

        return new Color3f( r, g, b );
    }

    /**
     * @param cloudFactor describing howmuch clouds cover the sun
     * @return the intensity of the
     */
    public float calcSunlightIntensity( float cloudFactor) {
        if( cloudFactor < 0 || cloudFactor > 1.0 )
            cloudFactor = 1;
        Color3f vec = calculateSunlight( );
        return ((vec.x + vec.y + vec.z)* 0.33333f )* cloudFactor;
    }

}
