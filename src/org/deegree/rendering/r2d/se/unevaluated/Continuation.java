//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.rendering.r2d.se.unevaluated;

import org.deegree.feature.Feature;

/**
 * <code>Continuation</code> is not a real continuation...
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 */
public abstract class Continuation<T> {

    private Continuation<T> next;

    /**
     * 
     */
    public Continuation() {
        // enable next to be null
    }

    /**
     * @param next
     */
    public Continuation( Continuation<T> next ) {
        this.next = next;
    }

    /**
     * @param base
     * @param f
     */
    public abstract void updateStep( T base, Feature f );

    /**
     * @param base
     * @param f
     */
    public void evaluate( T base, Feature f ) {
        updateStep( base, f );
        if ( next != null ) {
            evaluate( base, f );
        }
    }

    /**
     * <code>Updater</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * @param <T>
     */
    public static interface Updater<T> {
        /**
         * @param obj
         * @param val
         */
        void update( T obj, String val );
    }

}
