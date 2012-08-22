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

package org.deegree.model.filterencoding;

import java.util.List;

import org.apache.commons.codec.language.Soundex;
import org.deegree.model.feature.Feature;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FunctionSoundex extends Function {

    /**
     *
     */
    FunctionSoundex() {
        super();
    }

    /**
     * @param name
     * @param args
     */
    FunctionSoundex( String name, List<Expression> args ) {
        super( name, args );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.filterencoding.Function#evaluate(org.deegree.model.feature.Feature)
     */
    @Override
    public Object evaluate( Feature feature )
                            throws FilterEvaluationException {
        Literal literal = (Literal) args.get( 0 );
        String s = literal.getValue();

        return new Soundex().soundex( s );

    }

}
