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
package org.deegree.processing.raster.filter;

/**
 * This class offeres some convenience methods for convolving a data matrix. <br>
 * Convolution filtering is often used to reduce the effects of noise in images or to sharpen the
 * detail in rasters. Convolution filtering is a form of spatial filtering that computes each output
 * sample by multiplying elements of a kernel with the samples surrounding a particular source
 * sample. <br>
 * Convolution filtering operates on a group of input values surrounding a center pixel. The
 * adjoining values provide important information about values trends in the area of the values
 * being processed. <br>
 * Convolution filtering moves across the source raster, cell by cell, placing resulting values into
 * the destination raster. The resulting value of each source cell depends on the group of values
 * surrounding the source cell. Using the cell values of the source cell's neighbors, the
 * convolution process calculates the spatial frequency activity in the area, making it possible to
 * filter the values based on the spatial frequency of the area. <br>
 * Convolution filtering uses a convolve kernel, containing an array of convolution coefficient
 * values, called key elements. The array is not restricted to any particular size, and does not
 * even have to be square. The kernel can be 1 x 1, 3 x 3, 5 x 5, M x N, and so on. A larger kernel
 * size affords a more precise filtering operation by increasing the number of neighboring cells
 * used in the calculation. However, the kernel cannot be bigger in any dimension than the source
 * data. Also, the larger the kernel, the more computations that are required to be performed. For
 * example, given a 640 x 480 raster and a 3 x 3 kernel, the convolve operation requires over five
 * million total multiplications and additions. <br>
 * The convolution filtering operation computes each output sample by multiplying the key elements
 * of the kernel with the samples surrounding a particular source cell. For each destination cell,
 * the kernel is rotated 180 degrees and its key element is placed over the source pixel
 * corresponding with the destination pixel. The key elements are multiplied with the source cell
 * value under them, and the resulting products are summed together to produce the destination
 * sample value. <br>
 * The selection of the weights for the key elements determines the nature of the filtering action,
 * such as high-pass or low-pass. If the values of the key elements are the reciprocal of the number
 * of key elements in the kernel (for example, 1/9 for a 3 x 3 kernel), the result is a conventional
 * low-pass averaging process. If the weights are altered, certain cells in the kernel will have an
 * increased or decreased influence in the average. <br>
 * (modified from JAI documentation)
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class Convolve {

    /**
     * convolves the passed float matrix by applying the passed kernel. A kernel for lowpass
     * filtering may looks like:
     *
     * <pre>
     *  float[][] kernel = new float[5][5];
     *  for ( int i = 0; i &lt; kernel.length; i++ ) {
     *       for ( int j = 0; j &lt; kernel[i].length; j++ ) {
     *           kernel[i][j] = 1;
     *       }
     *   }
     * &lt;pre&gt;
     * which results in a strong smoothing of the raster.
     * <BR>
     * A kernel for highpass filtering may looks like:
     * &lt;pre&gt;
     *  float[][] kernel = new float[3][3];
     *  for ( int i = 0; i &lt; kernel.length; i++ ) {
     *       for ( int j = 0; j &lt; kernel[i].length; j++ ) {
     *           kernel[i][j] = -1;
     *       }
     *   }
     *   kernel[kernel.length/2][kernel[0].length/2] = 2;
     * &lt;/pre&gt;
     * Notice that the sum of all kernel values does not must be
     * == 1! Correct weighting will be done by this method.
     *
     * @param source to apply the kernel to.
     * @param kernel to apply
     * @return the raster after applying the given kernel.
     * @throws RasterFilterException if the kernel does not have an odd number of cells.
     *
     */
    public static float[][] perform( float[][] source, float[][] kernel )
                            throws RasterFilterException {

        if ( kernel.length % 2 == 0 || kernel[0].length % 2 == 0 ) {
            throw new RasterFilterException( "A Kernel must have an odd number of cells in x- and y-direction" );
        }

        kernel = rotateKernel( kernel );

        float[][] dest = new float[source.length][source[0].length];

        int ww = kernel.length;
        int hh = kernel[0].length;
        int xOrigin = ww / 2;
        int yOrigin = hh / 2;
        for ( int y = 0; y < source.length; y++ ) {
            for ( int x = 0; x < source[y].length; x++ ) {
                float g = 0;
                float v = 0;
                for ( int i = -xOrigin; i < -xOrigin + ww; i++ ) {
                    for ( int j = -yOrigin; j < -yOrigin + ww; j++ ) {
                        if ( y + i >= 0 && y + i < source.length && x + j >= 0 && x + j < source[y].length ) {
                            v += source[y + i][x + j] * kernel[xOrigin + i][yOrigin + j];
                            g += kernel[xOrigin + i][yOrigin + j];
                        }
                    }
                }
                dest[y][x] = v / g;
            }
        }

        return dest;
    }

    /**
     * rotates a convolution kernel by 180°
     *
     * @param kernel
     * @return the rotated kernel.
     */
    private static float[][] rotateKernel( float[][] kernel ) {
        for ( int i = 0; i < kernel.length / 2; i++ ) {
            for ( int j = 0; j < kernel[i].length / 2; j++ ) {
                float v = kernel[i][j];
                kernel[i][j] = kernel[kernel.length - i - 1][kernel[i].length - j - 1];
                kernel[kernel.length - i - 1][kernel[i].length - j - 1] = v;
            }
        }
        return kernel;
    }

}
