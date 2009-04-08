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

import org.deegree.commons.filter.Expression;
import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.commons.filter.MatchableObject;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class PropertyIsLessThan extends BinaryComparisonOperator {

    public PropertyIsLessThan( Expression parameter1, Expression parameter2, boolean matchCase ) {
       super (parameter1, parameter2, matchCase);
    }

    public SubType getSubType() {
        return SubType.PROPERTY_IS_LESS_THAN;
    }

    public boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException {
        Comparable<Object> parameter1Value = checkComparableOrNull( param1.evaluate( object ));
        Comparable<Object> parameter2Value = checkComparableOrNull( param2.evaluate( object ));
        return parameter1Value.compareTo( parameter2Value ) < 0;
    }

    public String toString( String indent ) {
        String s = indent + "-PropertyIsLessThan\n";
        s += param1.toString (indent + "  ");
        s += param2.toString (indent + "  ");
        return s;
    }    
}
