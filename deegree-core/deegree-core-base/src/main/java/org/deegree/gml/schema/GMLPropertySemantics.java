/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.gml.schema;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.feature.types.property.ValueRepresentation;

/**
 * An {@link XSElementDeclaration} with complex GML property semantics (encapsulates or
 * references another element).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class GMLPropertySemantics {

	private final XSElementDeclaration propertyElDecl;

	private final XSElementDeclaration valueElDecl;

	private final ValueRepresentation valueRepresentations;

	private final GMLObjectCategory category;

	/**
	 * Creates a new {@link GMLPropertySemantics} instance.
	 * @param propertyElDecl declaration of the property element, must not be
	 * <code>null</code>
	 * @param valueElDecl declaration of the value element, must not be <code>null</code>
	 * @param valueRepresentations allowed representations for the value, must not be
	 * <code>null</code>
	 * @param category GML object category, can be <code>null</code>
	 */
	public GMLPropertySemantics(XSElementDeclaration propertyElDecl, XSElementDeclaration valueElDecl,
			ValueRepresentation valueRepresentations, final GMLObjectCategory category) {
		this.propertyElDecl = propertyElDecl;
		this.valueElDecl = valueElDecl;
		this.valueRepresentations = valueRepresentations;
		this.category = category;
	}

	/**
	 * Returns the declaration of the property element.
	 * @return declaration of the property element, never <code>null</code>
	 */
	public XSElementDeclaration getPropertyElDecl() {
		return propertyElDecl;
	}

	/**
	 * Returns the declaration of the value element.
	 * @return declaration of the value element, never <code>null</code>
	 */
	public XSElementDeclaration getValueElDecl() {
		return valueElDecl;
	}

	/**
	 * Returns the allowed representations for the value element (by reference, inline or
	 * both).
	 * @return allowed representations, never <code>null</code>
	 */
	public ValueRepresentation getRepresentations() {
		return valueRepresentations;
	}

	/**
	 * @return can be <code>null</code>
	 */
	public GMLObjectCategory getValueCategory() {
		return category;
	}

}
