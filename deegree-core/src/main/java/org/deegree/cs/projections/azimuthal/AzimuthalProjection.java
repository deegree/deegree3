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

package org.deegree.cs.projections.azimuthal;

import static org.deegree.cs.utilities.ProjectionUtils.EPS10;
import static org.deegree.cs.utilities.ProjectionUtils.HALFPI;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.projections.Projection;

/**
 * The <code>AzimuthalProjection</code> class functions as a super class to all azimuthal projections.
 * <p>
 * (From wikipedia) Azimuthal projections have the property that directions from a central point are preserved (and
 * hence, great circles through the central point are represented by straight lines on the map). Usually these
 * projections also have radial symmetry in the scales and hence in the distortions: map distances from the central
 * point are computed by a function r(d) of the true distance d, independent of the angle; correspondingly, circles with
 * the central point as center are mapped into circles which have as center the central point on the map.
 * </p>
 *
 * <p>
 * The mapping of radial lines can be visualized by imagining a plane tangent to the Earth, with the central point as
 * tangent point.
 * </p>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public abstract class AzimuthalProjection extends Projection {

    /**
     * Defining that the center of this azimuthal projection is at the north pole
     */
    public final static int NORTH_POLE = 0;

    /**
     * Defining that the center of this azimuthal projection is at the south pole
     */
    public final static int SOUTH_POLE = 1;

    /**
     * Defining that the center of this azimuthal projection is at the equator
     */
    public final static int EQUATOR = 2;

    /**
     * Defining that the center of this azimuthal projection is oblique
     */
    public final static int OBLIQUE = 3;

    private int mode;

    /**
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
    public AzimuthalProjection( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                Point2d naturalOrigin, Unit units, double scale, boolean conformal, boolean equalArea,
                                CRSIdentifiable id ) {
        super( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, conformal, equalArea, id );
        if ( Math.abs( Math.abs( getProjectionLatitude() ) - HALFPI ) < EPS10 ) {
            mode = getProjectionLatitude() < 0. ? SOUTH_POLE : NORTH_POLE;
        } else if ( Math.abs( getProjectionLatitude() ) > EPS10 ) {
            mode = OBLIQUE;
        } else {
            mode = EQUATOR;
        }
    }

    /**
     * @return the mode.
     */
    public final int getMode() {
        return mode;
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
        code = code * 37 + getMode();
        return (int) ( code >>> 32 ) ^ (int) code;
    }
}
