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
 * default implementation of the MultiPrimitive interface
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MultiPrimitiveImpl extends AggregateImpl implements MultiPrimitive, Serializable {

    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 7228377539686274411L;

    /**
     * Creates a new MultiPrimitiveImpl object.
     *
     * @param crs
     */
    protected MultiPrimitiveImpl( CoordinateSystem crs ) {
        super( crs );
    }

    /**
     * merges this aggregation with another one
     *
     * @exception GeometryException
     *                will be thrown if the submitted isn't the same type as the recieving one.
     */
    @Override
    public void merge( Aggregate aggregate )
                            throws GeometryException {
        if ( !( aggregate instanceof MultiPrimitive ) ) {
            throw new GeometryException( "The submitted aggregation isn't a MultiPrimitive" );
        }

        super.merge( aggregate );
    }

    /**
     * returns the Primitive at the submitted index.
     */
    public Primitive getPrimitiveAt( int index ) {
        return (Primitive) super.getObjectAt( index );
    }

    /**
     * returns all Primitives as array
     */
    public Primitive[] getAllPrimitives() {
        Primitive[] gmos = new Primitive[this.getSize()];

        return aggregate.toArray( gmos );
    }

    /**
     * @return -1
     */
    public int getCoordinateDimension() {
        return -1;
    }

    /**
     * @return dimension
     */
    public int getDimension() {
        return 2;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.spatialschema.GeometryImpl#calculateParam()
     */
    @Override
    protected void calculateParam() {
        throw new UnsupportedOperationException( "Not supported for multipritives" );

    }

}
