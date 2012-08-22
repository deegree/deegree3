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

package org.deegree.graphics.sld;

/**
 * <code>InterpolationPoint</code> is used by Interpolate.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InterpolationPoint {

    /**
     * The location of this interpolation point.
     */
    public double data;

    /**
     * The red value of this interpolation point.
     */
    public int redValue;

    /**
     * The green value of this interpolation point.
     */
    public int greenValue;

    /**
     * The blue value of this interpolation point.
     */
    public int blueValue;

    /**
     * The opacity value of this interpolation point.
     */
    public int opacity;

    /**
     * Whether opacity was explicitly set.
     */
    public boolean opacitySet;

    /**
     * @param data
     * @param str
     */
    public InterpolationPoint( double data, String str ) {
        opacitySet = str.length() > 6;

        long val = Long.parseLong( str, 16 );
        this.data = data;
        opacity = (int) ( ( 0xff000000 & val ) >> 24 );
        redValue = (int) ( ( 0x00ff0000 & val ) >> 16 );
        greenValue = (int) ( ( 0x0000ff00 & val ) >> 8 );
        blueValue = (int) ( 0x000000ff & val );
    }

}
