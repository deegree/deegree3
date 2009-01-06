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
package org.deegree.model.filter.expression;

import org.deegree.model.filter.Expression;
import org.deegree.model.filter.FilterEvaluationException;
import org.deegree.model.filter.MatchableObject;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class PropertyName implements Expression {

    private String propName;

    private NamespaceContext nsContext;

    public PropertyName( String propName, NamespaceContext nsContext ) {
        this.propName = propName;
        this.nsContext = nsContext;
    }

    public Type getType() {
        return Type.PROPERTY_NAME;
    }

    public String getPropertyName() {
        return propName;
    }

    public NamespaceContext getNsContext() {
        return nsContext;
    }

    @Override
    public Object evaluate( MatchableObject obj )
                            throws FilterEvaluationException {
        Object value;
        try {
            value = obj.getPropertyValue( this );
        } catch ( JaxenException e ) {
            throw new FilterEvaluationException( e.getMessage() );
        }
        if ( value instanceof String ) {
            // try to parse the value as a double value
            try {
                return new Double( (String) value );
            } catch ( NumberFormatException e ) {
                // not a double -> eat the exception
            }
        }
        return value;
    }

    public String toString( String indent ) {
        String s = indent + "-PropertyName ('" + propName + "')\n";
        return s;
    }
}
