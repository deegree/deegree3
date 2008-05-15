//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
package org.deegree.model.filter.comparison;

import org.deegree.model.filter.Expression;
import org.deegree.model.filter.FilterEvaluationException;
import org.deegree.model.generic.DeegreeObject;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class PropertyIsNotEqualTo extends BinaryComparisonOperator {

    public PropertyIsNotEqualTo( Expression param1, Expression param2, boolean matchCase ) {
        super (param1, param2, matchCase);
    }

    public SubType getSubType() {
        return SubType.PROPERTY_IS_NOT_EQUAL_TO;
    }     
    
    public boolean evaluate( DeegreeObject object )
                            throws FilterEvaluationException {
        Comparable parameter1Value = param1.evaluate( object );
        Comparable parameter2Value = param2.evaluate( object );
        return !parameter1Value.equals( parameter2Value );
    }

    public String toString( String indent ) {
        String s = indent + "-PropertyIsNotEqualTo\n";
        s += param1.toString (indent + "  ");
        s += param2.toString (indent + "  ");
        return s;
    }    
}
