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

public class ValueRange implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private TypedLiteral min = null;

    private TypedLiteral max = null;

    private TypedLiteral spacing = null;

    private URI type = null;

    private URI semantic = null;

    private boolean atomic = false;

    private Closure closure = new Closure();

    /**
     * default: atomic = false closure = closed
     *
     * @param min
     * @param max
     * @param type
     * @param semantic
     */
    public ValueRange( TypedLiteral min, TypedLiteral max, URI type, URI semantic ) {
        this.min = min;
        this.max = max;
        this.type = type;
        this.semantic = semantic;
    }

    /**
     *
     * @param min
     * @param max
     * @param spacing
     */
    public ValueRange( TypedLiteral min, TypedLiteral max, TypedLiteral spacing ) {
        this.min = min;
        this.max = max;
        this.spacing = spacing;
    }

    /**
     * @param min
     * @param max
     * @param type
     * @param semantic
     * @param atomic
     * @param closure
     */
    public ValueRange( TypedLiteral min, TypedLiteral max, URI type, URI semantic, boolean atomic, Closure closure ) {
        this.min = min;
        this.max = max;
        this.type = type;
        this.semantic = semantic;
        this.atomic = atomic;
        this.closure = closure;
    }

    /**
     * @param min
     * @param max
     * @param spacing
     * @param type
     * @param semantic
     * @param atomic
     * @param closure
     */
    public ValueRange( TypedLiteral min, TypedLiteral max, TypedLiteral spacing, URI type, URI semantic,
                       boolean atomic, Closure closure ) {
        this.min = min;
        this.max = max;
        this.type = type;
        this.semantic = semantic;
        this.atomic = atomic;
        this.closure = closure;
        this.spacing = spacing;
    }

    /**
     * @return Returns the atomic.
     *
     */
    public boolean isAtomic() {
        return atomic;
    }

    /**
     * @param atomic
     *            The atomic to set.
     *
     */
    public void setAtomic( boolean atomic ) {
        this.atomic = atomic;
    }

    /**
     * @return Returns the closure.
     *
     */
    public Closure getClosure() {
        return closure;
    }

    /**
     * @param closure
     *            The closure to set.
     *
     */
    public void setClosure( Closure closure ) {
        this.closure = closure;
    }

    /**
     * @return Returns the max.
     *
     */
    public TypedLiteral getMax() {
        return max;
    }

    /**
     * @param max
     *            The max to set.
     *
     */
    public void setMax( TypedLiteral max ) {
        this.max = max;
    }

    /**
     * @return Returns the min.
     *
     */
    public TypedLiteral getMin() {
        return min;
    }

    /**
     * @param min
     *            The min to set.
     *
     */
    public void setMin( TypedLiteral min ) {
        this.min = min;
    }

    /**
     * @return Returns the spacing.
     */
    public TypedLiteral getSpacing() {
        return spacing;
    }

    /**
     * @param spacing
     *            The spacing to set.
     */
    public void setSpacing( TypedLiteral spacing ) {
        this.spacing = spacing;
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
        TypedLiteral min_ = (TypedLiteral) min.clone();
        TypedLiteral max_ = (TypedLiteral) max.clone();
        TypedLiteral space_ = (TypedLiteral) spacing.clone();
        Closure closure_ = new Closure( closure.value );
        return new ValueRange( min_, max_, space_, type, semantic, atomic, closure_ );
    }

}
