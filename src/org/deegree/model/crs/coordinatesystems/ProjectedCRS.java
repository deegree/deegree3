//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.model.crs.coordinatesystems;

import java.util.List;

import org.deegree.model.crs.CRSIdentifiable;
import org.deegree.model.crs.components.Axis;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.crs.transformations.polynomial.PolynomialTransformation;

/**
 * A <code>ProjectedCRS</code> is a coordinatesystem defined with a projection and a geographic crs. It allows for
 * transformation between projected coordinates (mostly in meters) and the lat/lon coordinates of the geographic crs and
 * vice versa.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class ProjectedCRS extends CoordinateSystem {

    private final GeographicCRS underlyingCRS;

    private Projection projection;

    /**
     * @param projection
     *            the projection which converts coordinates from this ProjectedCRS into the underlying GeographicCRS and
     *            vice versa.
     * @param axisOrder
     *            of this projection.
     * @param identity
     */
    public ProjectedCRS( Projection projection, Axis[] axisOrder, CRSIdentifiable identity ) {
        this( null, projection, axisOrder, identity );
    }

    /**
     * @param projection
     *            the projection which converts coordinates from this ProjectedCRS into the underlying GeographicCRS and
     *            vice versa.
     * @param axisOrder
     *            of this projection.
     * @param identifiers
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public ProjectedCRS( Projection projection, Axis[] axisOrder, String[] identifiers, String[] names,
                         String[] versions, String[] descriptions, String[] areasOfUse ) {
        super( projection.getGeographicCRS().getGeodeticDatum(), axisOrder, identifiers, names, versions, descriptions,
               areasOfUse );
        this.underlyingCRS = projection.getGeographicCRS();
        this.projection = projection;
    }

    /**
     * 
     * @param projection
     *            the projection which converts coordinates from this ProjectedCRS into the underlying GeographicCRS and
     *            vice versa.
     * @param axisOrder
     *            of this projection.
     * @param identifiers
     */
    public ProjectedCRS( Projection projection, Axis[] axisOrder, String[] identifiers ) {
        this( projection, axisOrder, identifiers, null, null, null, null );
    }

    /**
     * @param projection
     *            the projection which converts coordinates from this ProjectedCRS into the underlying GeographicCRS and
     *            vice versa.
     * @param axisOrder
     *            of this projection.
     * @param identifier
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public ProjectedCRS( Projection projection, Axis[] axisOrder, String identifier, String name, String version,
                         String description, String areaOfUse ) {
        this( projection, axisOrder, new String[] { identifier }, new String[] { name }, new String[] { version },
              new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * 
     * @param projection
     *            the projection which converts coordinates from this ProjectedCRS into the underlying GeographicCRS and
     *            vice versa.
     * @param axisOrder
     *            of this projection.
     * @param identifier
     */
    public ProjectedCRS( Projection projection, Axis[] axisOrder, String identifier ) {
        this( projection, axisOrder, identifier, null, null, null, null );
    }

    /**
     * @param transformations
     *            to use instead of the helmert transformation.
     * @param projection
     *            the projection which converts coordinates from this ProjectedCRS into the underlying GeographicCRS and
     *            vice versa.
     * @param axisOrder
     *            of this projection.
     * @param identity 
     */
    public ProjectedCRS( List<PolynomialTransformation> transformations, Projection projection, Axis[] axisOrder, CRSIdentifiable identity ) {
        super( transformations, projection.getGeographicCRS().getGeodeticDatum(), axisOrder, identity );
        this.underlyingCRS = projection.getGeographicCRS();
        this.projection = projection;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.crs.coordinatesystems.CoordinateSystem#getDimension()
     */
    @Override
    public int getDimension() {
        return getAxis().length;
    }

    /**
     * @return the underlyingCRS.
     */
    public final GeographicCRS getGeographicCRS() {
        return underlyingCRS;
    }

    @Override
    public final int getType() {
        return PROJECTED_CRS;
    }

    /**
     * @return the projection.
     */
    public final Projection getProjection() {
        return projection;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof ProjectedCRS ) {
            final ProjectedCRS that = (ProjectedCRS) other;
            return super.equals( that ) && this.projection.equals( that.projection );
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\n - Projection: " ).append( projection );
        return sb.toString();
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f </li>
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
        if ( projection != null ) {
            code = code * 37 + projection.hashCode();
        }

        return (int) ( code >>> 32 ) ^ (int) code;
    }
}
