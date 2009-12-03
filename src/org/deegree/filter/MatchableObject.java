//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/geometry/Geometry.java $
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
import org.deegree.filter.expression.PropertyName;
import org.deegree.gml.GMLVersion;
import org.jaxen.JaxenException;

/**
 * Interface for objects that can be filtered, i.e. {@link Filter} expressions may be evaluated against them.
 * <p>
 * Therefore the objects must provide access to their id and their property values using XPath-expressions.
 * </p>
 * 
 * @see Filter
 * @see Feature
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface MatchableObject {

    /**
     * Returns the identifier of the object.
     * 
     * @return the identifier of the object or null if it is an anonymous object
     */
    public String getId();

    /**
     * Returns the values that are selected by the given {@link PropertyName} expression.
     * 
     * @param propName
     *            XPath expression that usually selects a property
     * @param version 
     * @return the selected values
     * @throws JaxenException
     *             if an exception occurs during the evaluation of the XPath expression
     */
    public Object[] getPropertyValues( PropertyName propName, GMLVersion version )
                            throws JaxenException;
}
