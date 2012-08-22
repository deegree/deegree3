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

import java.io.Serializable;
import java.net.URI;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class Interval extends ValueRange implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private TypedLiteral res = null;

    /**
     * default: atomic = false closure = closed
     *
     * @param min
     * @param max
     * @param type
     * @param semantic
     * @param res
     */
    public Interval( TypedLiteral min, TypedLiteral max, URI type, URI semantic, TypedLiteral res ) {
        super( min, max, type, semantic );
        this.res = res;
    }

    /**
     * @param min
     * @param max
     * @param type
     * @param semantic
     * @param atomic
     * @param closure
     * @param res
     */
    public Interval( TypedLiteral min, TypedLiteral max, URI type, URI semantic, boolean atomic, Closure closure,
                     TypedLiteral res ) {
        super( min, max, type, semantic, atomic, closure );
        this.res = res;
    }

    /**
     * @return Returns the res.
     *
     */
    public TypedLiteral getRes() {
        return res;
    }

    /**
     * @param res
     *            The res to set.
     *
     */
    public void setRes( TypedLiteral res ) {
        this.res = res;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        ValueRange vr = (ValueRange) super.clone();
        TypedLiteral res_ = (TypedLiteral) res.clone();
        return new Interval( vr.getMin(), vr.getMax(), vr.getType(), vr.getSemantic(), vr.isAtomic(), vr.getClosure(),
                             res_ );
    }

}
