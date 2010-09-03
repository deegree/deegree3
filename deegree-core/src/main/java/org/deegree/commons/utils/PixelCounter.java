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
package org.deegree.commons.utils;

import static java.math.BigInteger.ZERO;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.math.BigInteger;

/**
 * <code>PixelCounter</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PixelCounter {

    /**
     * @param img
     * @param vals
     * @return a value between 0 and 1: for each color (ARGB) add (1 - (abs(origVal - newVal)) / (origVal + newVal)) /
     *         4, values are determined by summing x * y * valueAtXY(x, y)
     */
    public static double similarityLevel( BufferedImage img, BigInteger[] vals ) {
        BigInteger[] cnts = countPixels( img );
        double res = 0;
        if ( vals[0].equals( ZERO ) && cnts[0].equals( ZERO ) ) {
            res += 1;
        } else {
            res += ( 1 - cnts[0].subtract( vals[0] ).abs().doubleValue() / vals[0].add( cnts[0] ).doubleValue() );
        }
        if ( vals[1].equals( ZERO ) && cnts[1].equals( ZERO ) ) {
            res += 1;
        } else {
            res += ( 1 - cnts[1].subtract( vals[1] ).abs().doubleValue() / vals[1].add( cnts[1] ).doubleValue() );
        }
        if ( vals[2].equals( ZERO ) && cnts[2].equals( ZERO ) ) {
            res += 1;
        } else {
            res += ( 1 - cnts[2].subtract( vals[2] ).abs().doubleValue() / vals[2].add( cnts[2] ).doubleValue() );
        }
        if ( vals[3].equals( ZERO ) && cnts[3].equals( ZERO ) ) {
            res += 1;
        } else {
            res += ( 1 - cnts[3].subtract( vals[3] ).abs().doubleValue() / vals[3].add( cnts[3] ).doubleValue() );
        }
        return res / 4;
    }

    /**
     * @param img
     * @return the ARGB pixel values: sum over x * y * valueAtXY(x, y)
     */
    public static BigInteger[] countPixels( BufferedImage img ) {
        BigInteger alpha = BigInteger.valueOf( 0 );
        BigInteger red = BigInteger.valueOf( 0 );
        BigInteger green = BigInteger.valueOf( 0 );
        BigInteger blue = BigInteger.valueOf( 0 );
        for ( int x = 0; x < img.getWidth(); ++x ) {
            for ( int y = 0; y < img.getHeight(); ++y ) {
                Color c = new Color( img.getRGB( x, y ) );
                alpha = alpha.add( BigInteger.valueOf( x * y * c.getAlpha() ) );
                red = red.add( BigInteger.valueOf( x * y * c.getRed() ) );
                green = green.add( BigInteger.valueOf( x * y * c.getGreen() ) );
                blue = blue.add( BigInteger.valueOf( x * y * c.getBlue() ) );
            }
        }
        return new BigInteger[] { alpha, red, green, blue };
    }

}
