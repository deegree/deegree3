//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/filter/function/FunctionProvider.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.filter.sql.function;

import java.util.List;
import java.util.Set;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.filter.expression.Function;
import org.deegree.filter.sql.expression.SQLExpression;

/**
 * Implementations map {@link Function}s to SQL functions.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface SQLFunctionProvider {

    /**
     * Returns the name of the provided function.
     * 
     * @return name of the provided function, never <code>null</code>
     */
    public String getName();

    /**
     * Returns the supported SQL dialects.
     * 
     * @return supported SQL dialects, never <code>null</code>
     */
    public Set<Type> getDialects();

    /**
     * Translates the given arguments into an an SQL function call.
     * 
     * @param args
     *            SQL arguments, can be empty, but never <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     */
    public SQLExpression toProtoSQL( List<SQLExpression> args );
}
