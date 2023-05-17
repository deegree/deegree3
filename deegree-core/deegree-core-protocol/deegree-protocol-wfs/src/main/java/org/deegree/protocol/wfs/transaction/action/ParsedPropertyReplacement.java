/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.filter.expression.ValueReference;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class ParsedPropertyReplacement {

	private final Property newProp;

	private final UpdateAction action;

	private final ValueReference path;

	private final int index;

	/**
	 * @param newProp never <code>null</code>
	 * @param action can be <code>null</code>
	 * @param path never <code>null</code>
	 * @param index the index of the matched property
	 */
	public ParsedPropertyReplacement(Property newProp, UpdateAction action, ValueReference path, int index) {
		this.newProp = newProp;
		this.action = action;
		this.path = path;
		this.index = index;
	}

	/**
	 * @return never <code>null</code>
	 */
	public Property getNewValue() {
		return newProp;
	}

	/**
	 * @return can be <code>null</code>
	 */
	public UpdateAction getUpdateAction() {
		return action;
	}

	/**
	 * @return never <code>null</code>
	 */
	public ValueReference getValueReference() {
		return path;
	}

	/**
	 * @return the index of the matched property
	 */
	public int getIndex() {
		return index;
	}

}
