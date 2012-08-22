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
package org.deegree.framework.xml.schema;

import org.deegree.datatypes.QualifiedName;

/**
 * Represents an XML simple type declaration in an {@link XMLSchema}.
 * <p>
 * The following limitations apply:
 * <ul>
 * <li>the type must be defined using 'restriction' (of a basic xsd type)</li>
 * <li>the content model (enumeration, ...) is not evaluated</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SimpleTypeDeclaration implements TypeDeclaration {

    private QualifiedName name;

    private TypeReference restrictionBaseType;

    /**
     * Creates a new <code>SimpleTypeDeclaration</code> instance from the given parameters.
     *
     * @param name
     * @param restrictionBaseType
     */
    public SimpleTypeDeclaration( QualifiedName name, TypeReference restrictionBaseType ) {
        this.name = name;
        this.restrictionBaseType = restrictionBaseType;
    }

    /**
     * Returns the qualified name of the declared XML type.
     *
     * @return the qualified name of the declared XML type
     */
    public QualifiedName getName() {
        return this.name;
    }

    /**
     * Returns a {@link TypeReference} to the XML type that this simple type restricts.
     *
     * @return a TypeReference to the XML type that this simple type restricts
     */
    public TypeReference getRestrictionBaseType() {
        return this.restrictionBaseType;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString () {
        return toString ("");
    }

    /**
     * Returns a string representation of the object (indented for better readablity,
     * as this is a hierarchical structure).
     *
     * @param indent
     *             current indentation (as a whitespace string)
     * @return an indented string representation of the object
     */
    public String toString( String indent ) {
        StringBuffer sb = new StringBuffer();
        sb.append( indent );
        sb.append( "- simpleType" );
        if ( name != null ) {
            sb.append( " name=\"" );
            sb.append( this.name );
            sb.append( "\"" );
        }

        sb.append( " restriction base=\"" );
        sb.append( this.restrictionBaseType.getName() );
        sb.append( "\"\n" );
        return sb.toString();
    }
}
