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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class CodeList implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private URI codeSpace = null;

    private String name = null;

    private List<String> codes = new ArrayList<String>();

    /**
     * @param name
     * @param codes
     */
    public CodeList( String name, String[] codes ) {
        this( name, codes, null );
    }

    /**
     * @param name
     * @param codes
     * @param codeSpace
     */
    public CodeList( String name, String[] codes, URI codeSpace ) {
        setName( name );
        setCodes( codes );
        setCodeSpace( codeSpace );
    }

    /**
     * @return Returns the name.
     *
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     *
     */
    public void setName( String name ) {
        this.name = name;
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
     * @return Returns the codes.
     *
     */
    public String[] getCodes() {
        return codes.toArray( new String[codes.size()] );
    }

    /**
     * @param codes
     *            The codes to set.
     */
    public void setCodes( String[] codes ) {
        this.codes = Arrays.asList( codes );
    }

    /**
     * @param code
     *            The code to add
     */
    public void addCode( String code ) {
        codes.add( code );
    }

    /**
     * @param code
     *            The code to remove
     */
    public void removeCode( String code ) {
        codes.remove( code );
    }

    /**
     * returns true if a CodeList contains the passed codeSpace-value combination. Otherwise false
     * will be returned
     *
     * @param codeSpace
     * @param value
     * @return true if a CodeList contains the passed codeSpace-value combination. Otherwise false
     *         will be returned
     */
    public boolean validate( String codeSpace, String value ) {
        String[] codes = getCodes();
        URI space = getCodeSpace();
        String csp = null;
        if ( space != null ) {
            csp = space.toString();
        }
        for ( int j = 0; j < codes.length; j++ ) {
            if ( ( csp != null && csp.equals( codeSpace ) ) || ( csp == null && codeSpace == null )
                 && codes[j].equals( value ) ) {
                return true;
            }
        }
        return false;
    }

}
