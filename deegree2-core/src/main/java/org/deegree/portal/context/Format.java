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

/**
 * encapsulates the format description as described by the OGC Web Map Context specification
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class Format {
    private String name = null;

    private boolean current = false;

    /**
     * Creates a new Format object.
     *
     * @param name
     *            name of the format
     * @param current
     *            indicates if this is current format of this layer
     *
     * @throws ContextException
     */
    public Format( String name, boolean current ) throws ContextException {
        setName( name );
        setCurrent( current );
    }

    /**
     * @return the name of the format
     */
    public String getName() {
        return name;
    }

    /**
     * sets the the name of the format
     *
     * @param name
     *
     * @throws ContextException
     */
    public void setName( String name )
                            throws ContextException {
        if ( name == null ) {
            throw new ContextException( "name isn't allowed to be null" );
        }

        this.name = name;
    }

    /**
     * @return true if this is the current format of the layer
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * sets if this is the current format of the layer or not
     *
     * @param current
     */
    public void setCurrent( boolean current ) {
        this.current = current;
    }

}
