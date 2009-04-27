//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.commons.utils.math;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class VectorUtils {

    /**
     * Returns the minimum distance between a bounding box and a point.
     * 
     * @param bbox
     * @param p
     * @return minimum distance between <code>bbox</code> and
     *         <p>
     */
    public static float getDistance( float[][] bbox, float[] p ) {

        if ( p[0] >= bbox[0][0] && p[0] <= bbox[1][0] && p[1] >= bbox[0][1] && p[1] <= bbox[1][1] && p[2] >= bbox[0][2]
             && p[2] <= bbox[1][2] ) {
            return 0.0f;
        }

        float[] sideDistances = new float[6];
        sideDistances[0] = distanceToFront( bbox, p );
        sideDistances[1] = distanceToBack( bbox, p );
        sideDistances[2] = distanceToLeft( bbox, p );
        sideDistances[3] = distanceToRight( bbox, p );
        sideDistances[4] = distanceToTop( bbox, p );
        sideDistances[5] = distanceToBottom( bbox, p );

        float dist = sideDistances[0];
        for ( int i = 1; i < sideDistances.length; i++ ) {
            if ( sideDistances[i] < dist ) {
                dist = sideDistances[i];
            }
        }

        return dist;
    }

    private static float distanceToFront( float[][] bbox, float[] p ) {
        float qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        float qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        float qZ = bbox[1][2];
        return getDistance( qX, qY, qZ, p );
    }

    private static float distanceToBack( float[][] bbox, float[] p ) {
        float qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        float qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        float qZ = bbox[0][2];
        return getDistance( qX, qY, qZ, p );
    }

    private static float distanceToTop( float[][] bbox, float[] p ) {
        float qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        float qY = bbox[1][1];
        float qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static float distanceToBottom( float[][] bbox, float[] p ) {
        float qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        float qY = bbox[0][1];
        float qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static float distanceToLeft( float[][] bbox, float[] p ) {
        float qX = bbox[0][0];
        float qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        float qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static float distanceToRight( float[][] bbox, float[] p ) {
        float qX = bbox[1][0];
        float qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        float qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static float getDistance( float qX, float qY, float qZ, float[] p ) {
        float dX = qX - p[0];
        float dY = qY - p[1];
        float dZ = qZ - p[2];
        return (float) Math.sqrt( dX * dX + dY * dY + dZ * dZ );
    }

    private static float getClipped( float value, float min, float max ) {
        if ( value > max ) {
            return max;
        }
        if ( value < min ) {
            return min;
        }
        return value;
    }

    /**
     * An now for double
     */
    /**
     * Returns the minimum distance between a bounding box and a point.
     * 
     * @param bbox
     * @param p
     * @return minimum distance between <code>bbox</code> and
     *         <p>
     */
    public static double getDistance( double[][] bbox, double[] p ) {

        if ( p[0] >= bbox[0][0] && p[0] <= bbox[1][0] && p[1] >= bbox[0][1] && p[1] <= bbox[1][1] && p[2] >= bbox[0][2]
             && p[2] <= bbox[1][2] ) {
            return 0.0f;
        }

        double[] sideDistances = new double[6];
        sideDistances[0] = distanceToFront( bbox, p );
        sideDistances[1] = distanceToBack( bbox, p );
        sideDistances[2] = distanceToLeft( bbox, p );
        sideDistances[3] = distanceToRight( bbox, p );
        sideDistances[4] = distanceToTop( bbox, p );
        sideDistances[5] = distanceToBottom( bbox, p );

        double dist = sideDistances[0];
        for ( int i = 1; i < sideDistances.length; i++ ) {
            if ( sideDistances[i] < dist ) {
                dist = sideDistances[i];
            }
        }

        return dist;
    }

    private static double distanceToFront( double[][] bbox, double[] p ) {
        double qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        double qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        double qZ = bbox[1][2];
        return getDistance( qX, qY, qZ, p );
    }

    private static double distanceToBack( double[][] bbox, double[] p ) {
        double qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        double qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        double qZ = bbox[0][2];
        return getDistance( qX, qY, qZ, p );
    }

    private static double distanceToTop( double[][] bbox, double[] p ) {
        double qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        double qY = bbox[1][1];
        double qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static double distanceToBottom( double[][] bbox, double[] p ) {
        double qX = getClipped( p[0], bbox[0][0], bbox[1][0] );
        double qY = bbox[0][1];
        double qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static double distanceToLeft( double[][] bbox, double[] p ) {
        double qX = bbox[0][0];
        double qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        double qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static double distanceToRight( double[][] bbox, double[] p ) {
        double qX = bbox[1][0];
        double qY = getClipped( p[1], bbox[0][1], bbox[1][1] );
        double qZ = getClipped( p[2], bbox[0][2], bbox[1][2] );
        return getDistance( qX, qY, qZ, p );
    }

    private static double getDistance( double qX, double qY, double qZ, double[] p ) {
        double dX = qX - p[0];
        double dY = qY - p[1];
        double dZ = qZ - p[2];
        return Math.sqrt( dX * dX + dY * dY + dZ * dZ );
    }

    private static double getClipped( double value, double min, double max ) {
        if ( value > max ) {
            return max;
        }
        if ( value < min ) {
            return min;
        }
        return value;
    }
}
