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
package org.deegree.model.spatialschema;

import java.io.Serializable;

import org.deegree.model.crs.CoordinateSystem;

/**
 * default implementation of the OrientablePrimitive interface from package deegree.model. the implementation is
 * abstract because it doesn't make sense to instantiate it.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class OrientablePrimitiveImpl extends PrimitiveImpl implements OrientablePrimitive, Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 5655221930434396483L;

    protected char orientation = '+';

    /**
     * the constructor sets the curves orientation
     *
     * @param crs
     *            spatial reference system of the geometry
     * @param orientation
     *            orientation of the curve ('+'|'-')
     *
     * @exception GeometryException
     *                will be thrown if orientation is invalid
     */
    protected OrientablePrimitiveImpl( CoordinateSystem crs, char orientation ) throws GeometryException {
        super( crs );
        setOrientation( orientation );
    }

    /**
     * returns the orientation of a curve
     *
     * @return curve orientation ('+'|'-')
     *
     */
    public char getOrientation() {
        return orientation;
    }

    /**
     * sets the curves orientation
     *
     * @param orientation
     *            orientation of the curve ('+'|'-')
     *
     * @exception GeometryException
     *                will be thrown if orientation is invalid
     *
     */
    public void setOrientation( char orientation )
                            throws GeometryException {
        if ( ( orientation != '+' ) && ( orientation != '-' ) ) {
            throw new GeometryException( orientation + " isn't a valid direction" );
        }

        this.orientation = orientation;
    }

}
