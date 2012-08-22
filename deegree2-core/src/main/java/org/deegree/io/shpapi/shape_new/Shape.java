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
package org.deegree.io.shpapi.shape_new;

import org.deegree.model.spatialschema.Geometry;

/**
 * <code>Shape</code> defines methods to read, write and use objects read from/written to a
 * shapefile (as well as some basic information).
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface Shape {

    /**
     * Reads the object from a byte array.
     *
     * @param bytes
     * @param offset
     *            where to start reading
     * @return the new offset or -1, if the type was wrong.
     */
    public int read( byte[] bytes, int offset );

    /**
     * Writes the object to a byte array.
     *
     * @param bytes
     * @param offset
     * @return the new offset.
     */
    public int write( byte[] bytes, int offset );

    /**
     * @return the number of bytes necessary to write this instance.
     */
    public int getByteLength();

    /**
     * @return the type of the shape
     */
    public int getType();

    /**
     * @return the shapes' envelope, or null, if it has none
     */
    public ShapeEnvelope getEnvelope();

    /**
     * @return the shape as deegree Geometry
     * @throws ShapeGeometryException
     *             if the deegree geometry could not be constructed
     */
    public Geometry getGeometry()
                            throws ShapeGeometryException;

}
