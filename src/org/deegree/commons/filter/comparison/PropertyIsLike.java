//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.commons.filter.comparison;

import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.commons.filter.MatchableObject;
import org.deegree.commons.filter.expression.Literal;
import org.deegree.commons.filter.expression.PropertyName;

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

    public SubType getSubType() {
        return SubType.PROPERTY_IS_LIKE;
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
    
    public boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException {
        throw new FilterEvaluationException( "Evaluation of the '" + getSubType().name()
                                             + "' operator is not implemented yet." );
    }

    public String toString( String indent ) {
        String s = indent + "-PropertyIsLike\n";
        s += propName.toString (indent + "  ");
        s += literal.toString (indent + "  ");
        return s;
    }     
}
