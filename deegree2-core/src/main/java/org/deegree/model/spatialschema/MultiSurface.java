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

/**
 *
 * This Interface defines the Aggregation of Surfaces. The implementing class should encapsulate a java.util.Vector or a
 * comparative data structure.
 *
 * <p>
 * -----------------------------------------------------
 * </p>
 *
 * @author Andreas Poth
 * @version $Revision$ $Date$
 *          <p>
 */

public interface MultiSurface extends MultiPrimitive {

    /**
     * adds an Surface to the aggregation
     *
     * @param surface
     */
    public void addSurface( Surface surface );

    /**
     * inserts a Surface in the aggregation. all elements with an index equal or larger index will be moved. if index is
     * larger then getSize() - 1 or smaller then 0 or surface equals null an exception will be thrown.
     *
     * @param surface
     *            Surface to insert.
     * @param index
     *            position where to insert the new Surface
     * @throws GeometryException
     */
    public void insertSurfaceAt( Surface surface, int index )
                            throws GeometryException;

    /**
     * sets the submitted Surface at the submitted index. the element at the position <code>index</code> will be
     * removed. if index is larger then getSize() - 1 or smaller then 0 or surface equals null an exception will be
     * thrown.
     *
     * @param surface
     *            Surface to set.
     * @param index
     *            position where to set the new Surface
     * @throws GeometryException
     */
    public void setSurfaceAt( Surface surface, int index )
                            throws GeometryException;

    /**
     * removes the submitted Surface from the aggregation
     *
     * @param surface
     *
     * @return the removed Surface
     */
    public Surface removeSurface( Surface surface );

    /**
     * removes the Surface at the submitted index from the aggregation. if index is larger then getSize() - 1 or smaller
     * then 0 an exception will be thrown.
     *
     * @param index
     *
     * @return the removed Surface
     * @throws GeometryException
     */
    public Surface removeSurfaceAt( int index )
                            throws GeometryException;

    /**
     * returns the Surface at the submitted index.
     *
     * @param index
     * @return the Surface at the submitted index.
     */
    public Surface getSurfaceAt( int index );

    /**
     * returns all Surfaces as an array
     *
     * @return all Surfaces as an array
     */
    public Surface[] getAllSurfaces();

    /**
     * returns the area of a MultiSurface
     *
     * @return the area of a MultiSurface
     */
    public double getArea();

}
