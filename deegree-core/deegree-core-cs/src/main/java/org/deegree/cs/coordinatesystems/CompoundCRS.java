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

package org.deegree.cs.coordinatesystems;

import org.deegree.cs.CRSResource;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IUnit;

/**
 * A <code>CompoundCRS</code> is a {@link GeographicCRS} with a third axis (the height axis) attached. This axis denotes
 * a vertical axis, which defines a unit for a given height above or below the datums surface.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CompoundCRS extends CRS implements ICompoundCRS {

    private ICRS underlyingCRS;

    private final IAxis[] axis;

    private final double defaultHeight;

    /**
     * @param heightAxis
     *            defining the name and the units of the 'vertical' axis
     * @param underlyingCRS
     *            the crs to which the height axis is added, only geographic, geocentric or projected crs are valid.
     * @param defaultHeight
     *            of the any coordinate which does not have a height-axis-value.
     * @param identity
     *            containing the identifiable values.
     * @throws IllegalArgumentException
     *             if the underlying crs is not of type geographic, geocentric or projected or one of the other values
     *             is <code>null</code>.
     */
    public CompoundCRS( IAxis heightAxis, ICRS underlyingCRS, double defaultHeight, CRSResource identity )
                            throws IllegalArgumentException {
        super( underlyingCRS.getGeodeticDatum(), underlyingCRS.getAxis(), identity );
        CRSType tmp = underlyingCRS.getType();
        if ( tmp != CRSType.GEOCENTRIC && tmp != CRSType.PROJECTED && tmp != CRSType.GEOGRAPHIC ) {
            throw new IllegalArgumentException(
                                                "A compound crs can only have a geographic, projected or geocentric crs as underlying coordinate system." );
        }
        checkForNullObject( heightAxis, "CompoundCRS", "heightAxis" );
        this.underlyingCRS = underlyingCRS;
        axis = new Axis[3];
        axis[0] = underlyingCRS.getAxis()[0];
        axis[1] = underlyingCRS.getAxis()[1];
        axis[2] = heightAxis;
        this.defaultHeight = defaultHeight;
    }

    @Override
    public int getDimension() {
        return 3;
    }

    @Override
    public final CRSType getType() {
        return CRSType.COMPOUND;
    }

    /**
     * @return the heightAxis.
     */
    public final IAxis getHeightAxis() {
        return axis[2];
    }

    /**
     * @return the units of the heightAxis.
     */
    public final IUnit getHeightUnits() {
        return axis[2].getUnits();
    }

    /**
     * @return the geographic Axis and the heightAxis as the third component.
     */
    @Override
    public IAxis[] getAxis() {
        IAxis[] result = new IAxis[axis.length];
        System.arraycopy( axis, 0, result, 0, axis.length );
        return result;
    }

    /**
     * @return the underlyingCRS.
     */
    public final ICRS getUnderlyingCRS() {
        return underlyingCRS;
    }

    /**
     * @return the defaultHeight or 0 if it was not set.
     */
    public double getDefaultHeight() {
        return defaultHeight;
    }

    @Override
    public boolean equalsWithFlippedAxis( Object other ) {
        boolean result = super.equalsWithFlippedAxis( other );
        if ( !result )
            return false;
        CompoundCRS c = (CompoundCRS) other;
        return getUnderlyingCRS().equalsWithFlippedAxis( c.getUnderlyingCRS() )
               && getHeightAxis().equals( c.getHeightAxis() ) && getHeightUnits().equals( c.getHeightUnits() );
    }

}
