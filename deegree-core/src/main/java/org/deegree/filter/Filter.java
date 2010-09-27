// $HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/framework/xml/XMLFragment.java $
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

package org.deegree.filter;

import org.deegree.feature.Feature;

/**
 * A <code>Filter</code> is a boolean expression (often containing spatial predicates) that can be tested against
 * objects, such as {@link Feature}s.
 * <p>
 * The filter subsystem is designed to be compatible with the <a
 * href="http://www.opengeospatial.org/standards/filter">OpenGIS Filter Encoding Implementation Specification</a>.
 * </p>
 * 
 * @see IdFilter
 * @see OperatorFilter
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface Filter {

    /**
     * Convenience enum type for discriminating the different filter types.
     */
    public enum Type {
        /** Filter that matches objects with certain ids. The object is an instance of {@link IdFilter}. */
        ID_FILTER,
        /**
         * Filter that matches objects that match a certain expression. The object is an instance of
         * {@link OperatorFilter}.
         */
        OPERATOR_FILTER
    }

    /**
     * Returns the type of filter. Use this to safely determine the subtype of {@link Filter}.
     * 
     * @return type of filter (id or expression based)
     */
    public Type getType();

    /**
     * Determines if the given object matches this <code>Filter</code>.
     * 
     * @param <T>
     *            type of the context object
     * @param obj
     *            object that the operator is evaluated upon, must not be <code>null</code>
     * @param xpathEvaluator
     *            used for evaluation of XPath expressions, must not be <code>null</code>
     * @return true, if the operator evaluates to true, false otherwise
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public <T> boolean evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                            throws FilterEvaluationException;
}