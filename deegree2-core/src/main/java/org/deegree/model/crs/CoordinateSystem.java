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
package org.deegree.model.crs;

// OpenGIS dependencies
import org.deegree.crs.components.Unit;
import org.deegree.datatypes.QualifiedName;

/**
 * A coordinate system is a mathematical space, where the elements of the space are called positions. Each position is
 * described by a list of numbers. The length of the list corresponds to the dimension of the coordinate system. So in a
 * 2D coordinate system each position is described by a list containing 2 numbers. <br>
 * <br>
 * However, in a coordinate system, not all lists of numbers correspond to a position - some lists may be outside the
 * domain of the coordinate system. For example, in a 2D Lat/Lon coordinate system, the list (91,91) does not correspond
 * to a position. <br>
 * <br>
 * Some coordinate systems also have a mapping from the mathematical space into locations in the real world. So in a
 * Lat/Lon coordinate system, the mathematical position (lat, long) corresponds to a location on the surface of the
 * Earth. This mapping from the mathematical space into real-world locations is called a Datum.
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class CoordinateSystem extends QualifiedName {

    /**
     *
     */
    private static final long serialVersionUID = -170831086069691683L;

    private org.deegree.crs.coordinatesystems.CoordinateSystem realCRS;

    private final String requestedID;

    /**
     * Creates a CoordinateSystem as a wrapper to the real CRS.
     * 
     * @param realCRS
     */
    CoordinateSystem( org.deegree.crs.coordinatesystems.CoordinateSystem realCRS, String requestedID ) {
        super( realCRS.getIdentifier() );
        this.realCRS = realCRS;
        this.requestedID = requestedID;
    }

    /**
     * @param realCRS
     *            to wrap
     */
    public CoordinateSystem( org.deegree.crs.coordinatesystems.CoordinateSystem realCRS ) {
        this( realCRS, realCRS.getIdentifier() );
    }

    /**
     * This method returns the requested id (given in the constructor) and not the
     * {@link org.deegree.crs.coordinatesystems.CoordinateSystem#getIdentifier()} which only returns the first
     * configured id.
     * 
     * @return the requested id.
     */
    public String getIdentifier() {
        return requestedID;
    }

    /**
     * Since the crs uses a different namespace system as {@link QualifiedName} this method only returns the
     * {@link #getIdentifier()}.
     */
    @Override
    public String getPrefixedName() {
        return getIdentifier();
    }

    /**
     * @return the units of all axis.
     */
    public Unit[] getAxisUnits() {
        return realCRS.getUnits();
    }

    /**
     * @return the dimension of the encapsulated CRS
     */
    public int getDimension() {
        return realCRS.getDimension();
    }

    /**
     * @return the encapsulated CRS.
     */
    public org.deegree.crs.coordinatesystems.CoordinateSystem getCRS() {
        return realCRS;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof CoordinateSystem ) {
            final CoordinateSystem that = (CoordinateSystem) other;
            return this.realCRS.equals( that.realCRS );
        }
        return false;
    }
}
