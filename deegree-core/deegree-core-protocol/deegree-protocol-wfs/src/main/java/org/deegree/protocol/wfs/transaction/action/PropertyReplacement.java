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
package org.deegree.protocol.wfs.transaction.action;

import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.expression.ValueReference;

/**
 * Describes the change of a property as part of an {@link Update} action.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PropertyReplacement {

	private final ValueReference propertyName;

	private final XMLStreamReader xmlStream;

	private final UpdateAction updateAction;

	/**
	 * @param propertyName
	 * @param xmlStream
	 * @param updateAction
	 */
	public PropertyReplacement(ValueReference propertyName, XMLStreamReader xmlStream, UpdateAction updateAction) {
		this.propertyName = propertyName;
		this.xmlStream = xmlStream;
		this.updateAction = updateAction;
	}

	/**
	 * Returns the name of the property to be replaced.
	 * @return the name of the property to be replaced, never <code>null</code>
	 */
	public ValueReference getPropertyName() {
		return propertyName;
	}

	/**
	 * Returns an <code>XMLStreamReader</code> that provides access to the encoded
	 * replacement value (if such a value is specified).
	 * <p>
	 * <i>NOTE: The client <b>must</b> read this stream exactly once and exactly up to the
	 * next tag event after the <code>wfs:Value</code> END_ELEMENT event, i.e. the
	 * <code>wfs:Property</code> END_ELEMENT event.</i>
	 * </p>
	 * @return <code>XMLStreamReader</code> that provides access to the XML encoded
	 * replacement value, cursor points at the <code>wfs:Value</code>
	 * <code>START_ELEMENT</code> event, or <code>null</code>
	 */
	public XMLStreamReader getReplacementValue() {
		return xmlStream;
	}

	public UpdateAction getUpdateAction() {
		return updateAction;
	}

}
