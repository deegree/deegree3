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
package org.deegree.rendering.r2d.utils;

/**
 * Interface for converting images to numerical raw data matrices.
 *
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author: aaiordachioaie $
 *
 * @version $Revision: 18195 $, $Date: 2009-09-28 22:00:39 +0200 (28 Sep 2009) $
 */
public interface Image2RawData {

    /**
     * returns the raw data as a matrix (of ints, floats etc)
     *
     * @return the raw data as a matrix.
     */
    public Object[][] parse();

    /**
     * @param x
     *            index
     * @param y
     *            index
     * @return the appropriate value
     */
    public Object get( int x, int y );
}
