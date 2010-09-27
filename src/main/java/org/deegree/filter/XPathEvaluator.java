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

package org.deegree.filter;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.feature.Feature;
import org.deegree.filter.expression.PropertyName;

/**
 * Implementations enable the evaluation of XPath expressions (given as {@link PropertyName}s) on a specific class of
 * objects, e.g. {@link Feature} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @param <T>
 *            type that this evaluator works on
 */
public interface XPathEvaluator<T> {

    /**
     * Returns the values that are selected by evaluating the given XPath 1.0 expression using the context object.
     * 
     * @param context
     *            object that the expression is evaluated upon, must not be <code>null</code>
     * @param propName
     *            XPath expression (usually selects a property, hence the name)
     * @return the selected values, never <code>null</code> and contains at least one entry
     * @throws FilterEvaluationException
     *             if an exception occurs during the evaluation of the XPath expression
     */
    public TypedObjectNode[] eval( T context, PropertyName propName )
                            throws FilterEvaluationException;

    /**
     * Returns the identifier of the given context object.
     * 
     * @param context
     *            context object, never <code>null</code>
     * @return the identifier, can be <code>null</code>
     */
    public String getId( T context );
}