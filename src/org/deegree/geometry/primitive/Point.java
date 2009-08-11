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
package org.deegree.geometry.primitive;

/**
 * 0-dimensional primitive.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Point extends GeometricPrimitive {

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Point}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Point}
     */
    public PrimitiveType getPrimitiveType();

    /**
     * Returns the value of the first ordinate.
     * 
     * @return value of the first ordinate
     */
    public double get0();

    /**
     * Returns the value of the second ordinate.
     * 
     * @return value of the second ordinate, or <code>Double.NAN</code> if the point only has one dimension
     */
    public double get1();

    /**
     * Returns the value of the third ordinate.
     * 
     * @return value of the third ordinate, or <code>Double.NAN</code> if the point only has one or two dimensions
     */
    public double get2();

    /**
     * Returns the value of the specified ordinate.
     * 
     * @param dimension
     *            ordinate to be returned (first dimension=0)
     * @return ordinate value of the passed dimension, or <code>Double.NAN</code> if <code>dimension</code> is greater
     *         than the number of actual dimensions
     */
    public double get( int dimension );

    /**
     * Returns all ordinates.
     * 
     * @return all ordinates, the length of the array is equal to the number of dimensions
     */
    public double[] getAsArray();
}
