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
package org.deegree.datatypes;

import java.io.Serializable;
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
public class Code implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String code = null;

    private URI codeSpace = null;

    private int ordinal = Integer.MIN_VALUE;

    /**
     * @param code
     */
    public Code( String code ) {
        this.code = code;
    }

    /**
     * @param code
     * @param codeSpace
     */
    public Code( String code, URI codeSpace ) {
        this.code = code;
        this.codeSpace = codeSpace;
    }

    /**
     * @param code
     * @param ordinal
     */
    public Code( String code, int ordinal ) {
        this.code = code;
        this.ordinal = ordinal;
    }

    /**
     * @param code
     * @param codeSpace
     * @param ordinal
     */
    public Code( String code, URI codeSpace, int ordinal ) {
        this.code = code;
        this.codeSpace = codeSpace;
        this.ordinal = ordinal;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     *            The code to set.
     * 
     */
    public void setCode( String code ) {
        this.code = code;
    }

    /**
     * @return Returns the codeSpace.
     * 
     */
    public URI getCodeSpace() {
        return codeSpace;
    }

    /**
     * @param codeSpace
     *            The codeSpace to set.
     * 
     */
    public void setCodeSpace( URI codeSpace ) {
        this.codeSpace = codeSpace;
    }

    /**
     * @return Returns the ordinal.
     * 
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * @param ordinal
     *            The ordinal to set.
     * 
     */
    public void setOrdinal( int ordinal ) {
        this.ordinal = ordinal;
    }

    /**
     * Tests this Code for equality with another object.
     * 
     * @param other
     *            object to compare
     */
    @Override
    public boolean equals( Object other ) {
        if ( other == null || !( other instanceof Code ) ) {
            return false;
        }
        Code oc = (Code) other;
        if ( ordinal != oc.ordinal ) {
            return false;
        }
        if ( !code.equals( oc.getCode() ) ) {
            return false;
        }
        if ( !codeSpace.equals( oc.getCodeSpace() ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ( ( codeSpace != null ) ? ( codeSpace.toASCIIString() + '/' ) : " " ) + code;
    }

}
