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

import java.util.LinkedHashSet;

import org.deegree.datatypes.QualifiedName;

/**
 * Represents an XML complex type declaration in an {@link XMLSchema}.
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
public class ComplexTypeDeclaration implements TypeDeclaration {

    private QualifiedName name;

    private TypeReference extensionBaseType;

    private ElementDeclaration[] subElements;

    /**
     * Creates a new <code>ComplexTypeDeclaration</code> instance from the given parameters.
     *
     * @param name
     * @param extensionBaseType
     * @param subElements
     */
    public ComplexTypeDeclaration( QualifiedName name, TypeReference extensionBaseType,
                                  ElementDeclaration[] subElements ) {
        this.name = name;
        this.extensionBaseType = extensionBaseType;
        this.subElements = subElements;
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
     * Returns a {@link TypeReference} to the XML type that this complex type extends.
     *
     * @return a TypeReference to the XML type that this complex type extends
     */
    public TypeReference getExtensionBaseType() {
        return this.extensionBaseType;
    }

    /**
     * Returns the {@link ElementDeclaration}s that this {@link ComplexTypeDeclaration}
     * contains, but not the ones that are inherited (from the extended type).
     *
     * @return the explicit ElementDeclarations in this ComplexTypeDeclaration
     */
    public ElementDeclaration[] getExplicitElements() {
        return this.subElements;
    }

    /**
     * Returns the {@link ElementDeclaration}s in this {@link ComplexTypeDeclaration}
     * contains, this includes the ones that are inherited (from the extended type).
     *
     * @return the explicit+implicit ElementDeclarations in this ComplexTypeDeclaration
     */
    public ElementDeclaration[] getElements() {
        LinkedHashSet<ElementDeclaration> allElementSet = new LinkedHashSet<ElementDeclaration>();
        addElements( allElementSet );
        return allElementSet.toArray( new ElementDeclaration[allElementSet.size()] );
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return toString( "" );
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
        sb.append( "- complexType" );
        if ( name != null ) {
            sb.append( " name=\"" );
            sb.append( this.name );
            sb.append( "\"" );
        }
        if ( this.extensionBaseType != null ) {
            sb.append( ", extension base=\"" );
            sb.append( this.extensionBaseType.getName() );
            sb.append( "\"" );
        }
        sb.append( "\n" );
        for (int i = 0; i < subElements.length; i++) {
            sb.append( subElements[i].toString( indent
                + "  " ) );
        }
        return sb.toString();
    }

    /**
     * Recursively collects all <code>ElementDeclaration</code>s that this
     * <code>ComplexType</code> has.
     * <p>
     * Respects order and scope (overwriting) of <code>ElementDeclaration</code>s.
     *
     * @param elementSet
     *            the inherited (and own) elements are added to this LinkedHashSet
     */
    private void addElements( LinkedHashSet<ElementDeclaration> elementSet ) {
        if ( this.extensionBaseType != null
            && this.extensionBaseType.getTypeDeclaration() != null ) {
            ( (ComplexTypeDeclaration) this.extensionBaseType.getTypeDeclaration() )
                .addElements( elementSet );
        }
        for (int i = 0; i < subElements.length; i++) {
            elementSet.add( this.subElements[i] );
        }
    }
}
