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
package org.deegree.commons.filter.expression;

import org.deegree.commons.filter.Expression;
import org.deegree.commons.filter.MatchableObject;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class Literal implements Expression {

    private String value;

    public Literal( String value ) {
        this.value = value;
    }

    public Type getType() {
        return Type.LITERAL;
    }

    public String getValue() {
        return value;
    }

    /**
     * Returns the <code>Literal</code>'s value (to be used in the evaluation of a complexer <code>Expression</code>).
     * <p>
     * If the value appears to be numerical, a <code>Double</code> object is returned, else a <code>String</code>.
     */
    @Override
    public Object evaluate( MatchableObject obj ) {

        // try to parse the literal as a double value
        try {
            return new Double( value );
        } catch ( NumberFormatException e ) {
            // not a double -> eat the exception
        }
        return value;
    }

    public String toString( String indent ) {
        String s = indent + "-Literal ('" + value + "')\n";
        return s;
    }
}
