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
package org.deegree.filter.comparison;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class PropertyIsLike extends ComparisonOperator {

    private final String wildCard;

    private final String singleChar;

    private final String escapeChar;

    private final PropertyName propName;

    private final Literal literal;

    /**
     * @param propName
     * @param literal
     * @param wildCard
     * @param singleChar
     * @param escapeChar
     */
    public PropertyIsLike( PropertyName propName, Literal literal, String wildCard, String singleChar,
                              String escapeChar ) {
        this.propName = propName;
        this.literal = literal;
        this.wildCard = wildCard;
        this.singleChar = singleChar;
        this.escapeChar = escapeChar;
    }

    public PropertyName getPropertyName () {
        return propName;
    }

    public Literal getLiteral () {
        return literal;
    }

    public String getWildCard () {
        return wildCard;
    }

    public String getSingleChar () {
        return singleChar;
    }

    public String getEscapeChar () {
        return escapeChar;
    }

    @Override
    public SubType getSubType() {
        return SubType.PROPERTY_IS_LIKE;
    }    
    
    @Override        
    public boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException {
        throw new FilterEvaluationException( "Evaluation of the '" + getSubType().name()
                                             + "' operator is not implemented yet." );
    }

    @Override
    public String toString( String indent ) {
        String s = indent + "-PropertyIsLike\n";
        s += propName.toString (indent + "  ");
        s += literal.toString (indent + "  ");
        return s;
    }
}
