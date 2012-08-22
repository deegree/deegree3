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
package org.deegree.io.datastore.schema;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree.framework.xml.schema.ElementDeclaration;
import org.deegree.framework.xml.schema.TypeReference;
import org.w3c.dom.Element;

/**
 * Represents an annotated XML complex type declaration in an {@link MappedGMLSchema}.
 * <p>
 * The following limitations apply:
 * <ul>
 * <li>the type may be defined using 'extension', but must not use 'restriction'</li>
 * <li>the content model must be a sequence</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedComplexTypeDeclaration extends ComplexTypeDeclaration {

    private Element annotationElement;

    /**
     * Creates a new <code>MappedComplexTypeDeclaration</code> instance from the given parameters.
     *
     * @param name
     * @param extensionBaseType
     * @param subElements
     * @param annotationElement
     */
    public MappedComplexTypeDeclaration( QualifiedName name, TypeReference extensionBaseType,
                                  ElementDeclaration[] subElements, Element annotationElement ) {
        super (name, extensionBaseType, subElements );
        this.annotationElement = annotationElement;
    }

    /**
     * Returns the "xs:annotation" element (which contains the mapping information).
     *
     * @return the "xs:annotation" element
     */
    public Element getAnnotation () {
        return this.annotationElement;
    }
}
