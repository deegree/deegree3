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
package org.deegree.portal.context;

import java.util.HashMap;

/**
 * This class encapsulates a collection of references to Web Map Context documents as defined by the
 * OGC Web Map Context specification
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class ViewContextCollection {
    private HashMap<String, ViewContextReference> viewContextReferences = new HashMap<String, ViewContextReference>();

    /**
     * Creates a new ViewContextCollection object.
     *
     * @param viewContextReferences
     *
     * @throws ContextException
     */
    public ViewContextCollection( ViewContextReference[] viewContextReferences ) throws ContextException {
        setViewContextReferences( viewContextReferences );
    }

    /**
     *
     *
     * @return all ViewContextReference
     */
    public ViewContextReference[] getViewContextReferences() {
        ViewContextReference[] fr = new ViewContextReference[viewContextReferences.size()];
        return viewContextReferences.values().toArray( fr );
    }

    /**
     *
     *
     * @param viewContextReferences
     *
     * @throws ContextException
     */
    public void setViewContextReferences( ViewContextReference[] viewContextReferences )
                            throws ContextException {
        if ( ( viewContextReferences == null ) || ( viewContextReferences.length == 0 ) ) {
            throw new ContextException( "at least one viewContextReference must be defined for a layer" );
        }

        this.viewContextReferences.clear();

        for ( int i = 0; i < viewContextReferences.length; i++ ) {
            this.viewContextReferences.put( viewContextReferences[i].getTitle(), viewContextReferences[i] );
        }
    }

    /**
     *
     *
     * @param name
     *
     * @return named ViewContextReference
     */
    public ViewContextReference getViewContextReference( String name ) {
        return viewContextReferences.get( name );
    }

    /**
     *
     *
     * @param viewContextReference
     */
    public void addViewContextReference( ViewContextReference viewContextReference ) {
        viewContextReferences.put( viewContextReference.getTitle(), viewContextReference );
    }

    /**
     *
     *
     * @param name
     *
     * @return removed ViewContextReference
     */
    public ViewContextReference removeViewContextReference( String name ) {
        return viewContextReferences.remove( name );
    }

    /**
     *
     */
    public void clear() {
        viewContextReferences.clear();
    }

}
