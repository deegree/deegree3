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
package org.deegree.filter.expression;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.slf4j.Logger;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Function implements Expression {

    private static final Logger LOG = getLogger( Function.class );

    private String name;

    private List<Expression> params;

    public Function( String name, List<Expression> params ) {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getParameters() {
        return params;
    }

    @Override
    public Type getType() {
        return Type.FUNCTION;
    }

    @Override
    public TypedObjectNode[] evaluate( MatchableObject object )
                            throws FilterEvaluationException {
        LOG.warn( "The function with name '{}' is not implemented.", name );
        throw new FilterEvaluationException( "Evaluation of the '" + getType().name()
                                             + "' expression is not implemented yet." );
    }

    @Override
    public String toString( String indent ) {
        String s = indent + "-Function (" + name + ")\n";
        if ( params != null ) {
            for ( Expression param : params ) {
                s += param.toString( indent + "  " );
            }
        }
        return s;
    }

    @Override
    public Expression[] getParams() {
        return params.toArray( new Expression[params.size()] );
    }
}
