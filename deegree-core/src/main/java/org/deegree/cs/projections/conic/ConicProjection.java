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

package org.deegree.cs.projections.conic;

import static org.deegree.cs.utilities.ProjectionUtils.EPS11;
import static org.deegree.cs.utilities.ProjectionUtils.WORLD_BOUNDS_RAD;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.projections.Projection;

/**
 * The <code>ConicProjection</code> is a super class for all conic projections.
 * <p>
 * <q>(From Snyder p.97)</q>
 * </p>
 * <p>
 * To show a region for which the greatest extent is from east to west in the temperate zones, conic projections are
 * usually preferable to cylindrical projections.
 * </p>
 * <p>
 * Normal conic projections are distinguished by the use of arcs of concentric circles for parallesl of latitude and
 * equally spaced straight radii of these circles for meridians. The angles between the meridians on the map are smaller
 * than the actual differences in longitude. The circular arcs may or may not be equally spaced, depending on the
 * projections. The polyconic projections and the oblique conic projections have characteristcs different from these.
 * </p>
 * <p>
 * There are three important classes of conic projections:
 * <ul>
 * <li>The equidistant</li>
 * <li>the conformal</li>
 * <li>the equal area</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public abstract class ConicProjection extends Projection {
    private double firstParallelLatitude;

    private double secondParallelLatitude;

    /**
     * @param firstParallelLatitude
     *            the latitude (in radians) of the first parallel. (Snyder phi_1).
     * @param secondParallelLatitude
     *            the latitude (in radians) of the second parallel. (Snyder phi_2).
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param conformal
     * @param equalArea
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public ConicProjection( double firstParallelLatitude, double secondParallelLatitude, GeographicCRS geographicCRS,
                            double falseNorthing, double falseEasting, Point2d naturalOrigin, Unit units, double scale,
                            boolean conformal, boolean equalArea, CRSIdentifiable id ) {
        super( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, conformal, equalArea, id );

        if ( Double.isNaN( firstParallelLatitude ) || firstParallelLatitude == 0
             || Math.abs( firstParallelLatitude ) < EPS11 || firstParallelLatitude < WORLD_BOUNDS_RAD.getMinY()
             || firstParallelLatitude > WORLD_BOUNDS_RAD.getMaxY() ) {
            this.firstParallelLatitude = getProjectionLatitude();
            this.secondParallelLatitude = getProjectionLatitude();
        } else {
            this.firstParallelLatitude = firstParallelLatitude;
            this.secondParallelLatitude = secondParallelLatitude;
            if ( this.secondParallelLatitude < WORLD_BOUNDS_RAD.getMinY()
                 || this.secondParallelLatitude > WORLD_BOUNDS_RAD.getMaxY() ) {
                this.secondParallelLatitude = Double.NaN;
            }
        }
    }

    /**
     * @return the latitude of the first parallel which is the intersection of the earth with the cone or the
     *         projectionLatitude if the cone is tangential with earth (e.g. one standard parallel).
     */
    public final double getFirstParallelLatitude() {
        return firstParallelLatitude;
    }

    /**
     * @return the latitude of the first parallel which is the intersection of the earth with the cone or the
     *         projectionLatitude if the cone is tangential with earth (e.g. one standard parallel).
     */
    public final double getSecondParallelLatitude() {
        return secondParallelLatitude;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof ConicProjection ) {
            final ConicProjection that = (ConicProjection) other;
            return super.equals( other )
                   && ( Double.isNaN( this.firstParallelLatitude ) ? Double.isNaN( that.firstParallelLatitude )
                                                                  : Math.abs( this.firstParallelLatitude
                                                                              - that.firstParallelLatitude ) < EPS11 )
                   && ( Double.isNaN( this.secondParallelLatitude ) ? Double.isNaN( that.secondParallelLatitude )
                                                                   : Math.abs( this.secondParallelLatitude
                                                                               - that.secondParallelLatitude ) < EPS11 );
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     *
     * @return (int) ( result >>> 32 ) ^ (int) result;
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long code = 32452843;
        code = code * 37 + super.hashCode();

        long tmp = Double.doubleToLongBits( firstParallelLatitude );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( secondParallelLatitude );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        return (int) ( code >>> 32 ) ^ (int) code;
    }
}
