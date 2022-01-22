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
package org.deegree.commons.utils.io;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
public class Utils {

    /**
     * Output stream that loses all data.
     */
    public static final OutputStream DEV_NULL = new OutputStream() {
        @Override
        public void write( int b )
                        throws IOException {
            // lost
        }
    };

    /**
     * Makes pixel by pixel comparison and determines the percentage of equal pixel.
     *
     * @param in1
     * @param in2
     * @return the percentage (0..1)
     * @throws IOException
     */
    public static double determineSimilarity( RenderedImage in1, RenderedImage in2 ) {
        Raster data1 = in1.getData();
        Raster data2 = in2.getData();
        long equal = 0;
        for ( int b = 0; b < data1.getNumBands(); b++ ) {
            for ( int x = 0; x < data1.getWidth(); x++ ) {
                for ( int y = 0; y < data1.getHeight(); y++ ) {
                    if ( b < data2.getNumBands() && x < data2.getWidth() && y < data2.getHeight() ) {
                        if ( data1.getSample( x, y, b ) == data2.getSample( x, y, b ) ) {
                            ++equal;
                        }
                    }
                }
            }
        }
        int comparedPixels = data1.getNumBands() * data1.getWidth() * data1.getHeight();
        return equal / (double) comparedPixels;
    }

}
