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
package org.deegree.datatypes.values;

import java.net.URI;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class ValueEnum extends ValueEnumBase implements Cloneable {

    private static final long serialVersionUID = 1L;

    private URI type = null;

    private URI semantic = null;

    /**
     * @param interval
     * @param singleValue
     */
    public ValueEnum( Interval[] interval, TypedLiteral[] singleValue ) {
        super( interval, singleValue );
    }

    /**
     * @param interval
     * @param singleValue
     * @param type
     * @param semantic
     */
    public ValueEnum( Interval[] interval, TypedLiteral[] singleValue, URI type, URI semantic ) {
        super( interval, singleValue );
        this.type = type;
        this.semantic = semantic;
    }

    /**
     * @return Returns the semantic.
     *
     */
    public URI getSemantic() {
        return semantic;
    }

    /**
     * @param semantic
     *            The semantic to set.
     *
     */
    public void setSemantic( URI semantic ) {
        this.semantic = semantic;
    }

    /**
     * @return Returns the type.
     *
     */
    public URI getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     *
     */
    public void setType( URI type ) {
        this.type = type;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        TypedLiteral[] singleValue = getSingleValue();
        TypedLiteral[] singleValue_ = new TypedLiteral[singleValue.length];
        for ( int i = 0; i < singleValue_.length; i++ ) {
            singleValue_[i] = (TypedLiteral) singleValue[i].clone();
        }

        Interval[] interval = getInterval();
        Interval[] interval_ = new Interval[interval.length];
        for ( int i = 0; i < interval_.length; i++ ) {
            interval_[i] = (Interval) interval[i].clone();
        }

        return new ValueEnum( interval_, singleValue_, type, semantic );

    }

}
