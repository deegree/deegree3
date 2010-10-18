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
package org.deegree.services.wcs.model;

import static java.lang.Math.min;

import org.deegree.geometry.Envelope;

/**
 * This class defines an output grid for a WCS request.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Grid {

    private Envelope targetEnvelope;

    private final int width, height, depth;

    private Grid( int width, int height, int depth, Envelope targetEnvelope ) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.targetEnvelope = targetEnvelope;
    }

    /**
     * Create a Gird for the given grid size and output envelope
     * 
     * @param width
     *            the width of the grid in pixel
     * @param height
     *            the height of the grid in pixel
     * @param depth
     *            the depth of the grid in pixel
     * @param targetEnvelope
     *            the envelope of the output grid
     * @return a new grid
     */
    public static Grid fromSize( int width, int height, int depth, Envelope targetEnvelope ) {
        return new Grid( width, height, depth, targetEnvelope );
    }

    /**
     * Create a Gird for the given pixel resolution and output envelope
     * 
     * @param resx
     *            resolution of a pixel (ie. the width of each grid cell in CRS units)
     * @param resy
     *            resolution of a pixel (ie. the height of each grid cell in CRS units)
     * @param resz
     *            resolution of a pixel (ie. the depth of each grid cell in CRS units)
     * @param targetEnvelope
     *            the envelope of the output grid
     * @return a new grid
     */
    public static Grid fromRes( double resx, double resy, double resz, Envelope targetEnvelope ) {
        int width = (int) Math.round( targetEnvelope.getSpan0() / resx );
        int height = (int) Math.round( targetEnvelope.getSpan1() / resy );
        int depth = Integer.MIN_VALUE;
        if ( !Double.isNaN( resz ) ) {
            if ( targetEnvelope.getCoordinateDimension() == 3 ) {
                double envDepth = getEnvDepth( targetEnvelope );
                depth = (int) Math.round( envDepth / resz );
            }
        }

        return new Grid( width, height, depth, targetEnvelope );
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the envelope
     */
    public Envelope getEnvelope() {
        return targetEnvelope;
    }

    /**
     * @return the lowest resolution of the grid
     */
    public double getResolution() {
        double xRes = targetEnvelope.getSpan0() / width;
        double yRes = targetEnvelope.getSpan1() / height;
        double zRes = Double.MAX_VALUE;
        if ( depth != Integer.MIN_VALUE ) {
            double envDepth = getEnvDepth( targetEnvelope );
            if ( !Double.isNaN( envDepth ) ) {
                zRes = envDepth / depth;
            }

        }
        return min( xRes, min( yRes, zRes ) );
    }

    private static double getEnvDepth( Envelope env ) {
        double result = Double.NaN;
        if ( env.getCoordinateDimension() == 3 ) {
            result = env.getMax().get2() - env.getMin().get2();
        }
        return result;
    }

    /**
     * @return the depth
     */
    public final int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "[width: " + width + ", height: " + height + ", depth: " + depth + ", target envelope: "
               + targetEnvelope + "]";
    }

}
