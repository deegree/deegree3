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
 * Represents an element reference. The reference may be resolved or not. If it is resolved, the
 * referenced <code>ElementDeclaration</code> is accessible, otherwise only the name of the
 * element is available.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @since 2.0
 */
public class ElementReference {

    private QualifiedName elementName;

    private ElementDeclaration declaration;

    private boolean isResolved;

    /**
     * Creates an unresolved <code>ElementReference</code>.
     *
     * @param elementName
     */
    public ElementReference( QualifiedName elementName ) {
        this.elementName = elementName;
    }

    /**
     * @return the full name of the element
     */
    public QualifiedName getName() {
        return this.elementName;
    }

    /**
     * @return true if the element is resolved
     */
    public boolean isResolved() {
        return this.isResolved;
    }

    /**
     * @return the declaration of the Element
     */
    public ElementDeclaration getElementDeclaration() {
        return this.declaration;
    }

    /**
     * Flag this elementreference as resolved, with the given declaration set.
     * @param declaration to set the element to.
     * @throws RuntimeException if the element is resolved already.
     */
    public void resolve( ElementDeclaration declaration ) {
        if ( isResolved() ) {
            throw new RuntimeException( "ElementDeclaration to element '"
                + elementName + "' has already been resolved." );
        }
        this.declaration = declaration;
        this.isResolved = true;
    }

    /**
     * set this element as resolved.
     * @throws RuntimeException if the element is resolved already
     */
    public void resolve() {
        if ( isResolved() ) {
            throw new RuntimeException( "ElementDeclaration to element '"
                + elementName + "' has already been resolved." );
        }
        this.isResolved = true;
    }
}
