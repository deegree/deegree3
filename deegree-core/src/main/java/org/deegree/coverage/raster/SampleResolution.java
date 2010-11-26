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
package org.deegree.coverage.raster;

import java.io.Serializable;
import java.util.Arrays;

import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.geometry.Envelope;

/**
 * Defines the resolution(s) of a sample.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SampleResolution implements Serializable {

    private static final long serialVersionUID = 8741054911757029539L;

    private final double[] resolutions;

    /**
     * @param resolutions
     */
    public SampleResolution( double[] resolutions ) {
        if ( resolutions == null ) {
            this.resolutions = new double[0];
        } else {
            this.resolutions = Arrays.copyOf( resolutions, resolutions.length );
        }
    }

    /**
     * Returns the resolution (in coordinate system units) of the sample domain for the given dimension (crs-axis).
     * 
     * @param dim
     *            the 'crs-axis' to get the dimension for
     * @return the resolution of the sample domain of the given dimension.
     */
    public double getResolution( int dim ) {
        return resolutions[dim];
    }

    /**
     * Create a raster reference for the given envelope from this resolution.
     * 
     * @param location
     *            of the origin of the raster reference, if <code>null</code> center will be assumed.
     * @param envelope
     *            the envelope to create a raster reference for.
     * 
     * @return the raster reference with sample resolutions and the origin fitting the given envelope, if no resolutions
     *         were known, <code>null</code> will be returned;
     */
    public RasterGeoReference createGeoReference( OriginLocation location, Envelope envelope ) {
        if ( resolutions == null ) {
            return null;
        }
        return RasterGeoReference.create( location == null ? OriginLocation.CENTER : location, envelope,
                                          resolutions[0], resolutions[1] );
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof SampleResolution ) {
            final SampleResolution that = (SampleResolution) other;
            return resMatch( that.resolutions );
        }
        return false;
    }

    /**
     * 
     * @param otherRes
     * @return true if the given resolutions are equal within an epsilon of 1E-8
     */
    private boolean resMatch( double[] otherRes ) {
        if ( this.resolutions.length != otherRes.length ) {
            return false;
        }
        // length checked.
        for ( int i = 0; i < resolutions.length; ++i ) {
            if ( Math.abs( resolutions[i] - otherRes[i] ) > 1E-8 ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode( resolutions );
    }

    @Override
    public String toString() {
        return Arrays.toString( resolutions );
    }

}
