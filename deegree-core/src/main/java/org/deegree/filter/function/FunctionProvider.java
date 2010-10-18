//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.filter.function;

import java.util.List;

import org.deegree.filter.Expression;
import org.deegree.filter.expression.Function;

/**
 * Implementations of this class provide {@link Function} implementations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface FunctionProvider {

    /**
     * Returns the name of the provided function.
     * 
     * @return the name of the provided function, never <code>null</code>
     */
    public String getName();

    /**
     * Returns the number of arguments of the provided function.
     * 
     * @return the number of arguments
     */
    public int getArgCount();

    /**
     * Creates a new {@link Function} instance.
     * 
     * @param params
     *            params for the new function, may be empty, but never <code>null</code>
     * @return the new function instance, never <code>null</code>
     */
    public Function create( List<Expression> params );
}