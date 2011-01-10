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
package org.deegree.geometry;

import org.deegree.geometry.primitive.Point;

/**
 * Axis-parallel bounding box.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Envelope extends Geometry {

    /**
     * Must always return {@link Geometry.GeometryType#ENVELOPE}.
     * 
     * @return {@link Geometry.GeometryType#ENVELOPE}.
     */
    public GeometryType getGeometryType();

    /**
     * Returns the envelope's minimum coordinate.
     * 
     * @return minimum coordinate
     */
    public Point getMin();

    /**
     * Returns the envelope's maximum coordinate
     * 
     * @return maximum coordinate
     */
    public Point getMax();

    /**
     * Merges this envelope with another envelope into a new one.
     * 
     * @param other
     * @return merged envelope
     */
    public Envelope merge( Envelope other );

    /**
     * Returns the envelope's span of the first dimension (in units of the associated coordinate system).
     * 
     * @return span of the first dimension
     */
    public double getSpan0();

    /**
     * Returns the envelope's span of the second dimension (in units of the associated coordinate system).
     * 
     * @return span of the second dimension
     */
    public double getSpan1();

    /**
     * Returns the envelope's span of the second dimension (in units of the associated coordinate system).
     * 
     * @param dim
     *            index of the span to be returned
     * @return span of the specified dimension
     */
    public double getSpan( int dim );
}
