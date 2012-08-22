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
 * Represents a type reference. The reference may be resolved or not. If it is resolved, the
 * referenced <code>TypeDeclaration</code> is accessible, otherwise only the name of the type is
 * available.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TypeReference {

    private QualifiedName typeName;

    private TypeDeclaration declaration;

    private boolean isResolved;

    /**
     * Creates an unresolved <code>TypeReference</code>.
     *
     * @param typeName
     */
    public TypeReference( QualifiedName typeName ) {
        this.typeName = typeName;
    }

    /**
     * Creates an anonymous (inline) <code>TypeReference</code>.
     *
     * @param declaration
     */
    public TypeReference( TypeDeclaration declaration ) {
        this.declaration = declaration;
    }

    /**
     * @return Name
     */
    public QualifiedName getName() {
        return this.typeName;
    }

    /**
     * @return true if is resolved
     */
    public boolean isResolved () {
        return this.isResolved;
    }

    /**
     * @return declaration
     */
    public TypeDeclaration getTypeDeclaration() {
        return this.declaration;
    }

    /**
     * Returns whether this <code>TypeReference</code> is anynoums (unnamed) or not.
     *
     * @return true, if this reference is anonymous, false otherwise
     */
    public boolean isAnonymous() {
        return this.typeName == null;
    }

    /**
     * @param declaration
     */
    public void resolve( TypeDeclaration declaration ) {
        if ( isResolved() ) {
            throw new RuntimeException( "TypeReference to type '"
                + typeName + "' has already been resolved." );
        }
        this.declaration = declaration;
        this.isResolved = true;
    }

    /**
     *
     */
    public void resolve() {
        if ( isResolved() ) {
            throw new RuntimeException( "TypeReference to type '"
                + typeName + "' has already been resolved." );
        }
        this.isResolved = true;
    }
}
