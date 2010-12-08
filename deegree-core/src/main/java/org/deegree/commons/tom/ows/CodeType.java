//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.tom.ows;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;

/**
 * Name or code with an (optional) authority. If the codeSpace attribute is present, its value shall reference a
 * dictionary, thesaurus, or authority for the name or code, such as the organisation who assigned the value, or the
 * dictionary from which it is taken.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class CodeType implements TypedObjectNode {

    private String code;

    private String codeSpace;

    /**
     * Returns a new {@link CodeType} instance without authority.
     *
     * @param code
     *            code value, not null
     * @throws InvalidParameterValueException
     *             if code is null
     */
    public CodeType( String code ) throws InvalidParameterValueException {
        if ( code == null ) {
            throw new InvalidParameterValueException( "code cannot be null" );
        }
        this.code = code;
    }

    /**
     * Returns a new {@link CodeType} instance with optional authority.
     *
     * @param code
     *            code value, not null
     * @param codeSpace
     *            authority, may be null
     * @throws InvalidParameterValueException
     *             if code is null
     */
    public CodeType( String code, String codeSpace ) throws InvalidParameterValueException {
        if ( code == null ) {
            throw new InvalidParameterValueException( "code cannot be null" );
        }
        this.code = code;
        this.codeSpace = codeSpace;
    }

    /**
     * Returns the code value.
     *
     * @return the code value, never null
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the authority of the code.
     *
     * @return the authority of the code or null if unspecified
     */
    public String getCodeSpace() {
        return codeSpace;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof CodeType ) ) {
            return false;
        }
        CodeType that = (CodeType) o;
        if ( !code.equals( that.code ) ) {
            return false;
        }
        if ( codeSpace != null ) {
            return codeSpace.equals( that.codeSpace );
        }
        return that.codeSpace == null;
    }
}
