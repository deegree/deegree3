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

package org.deegree.gml.geometry.validation;

import java.util.List;

import org.deegree.geometry.validation.event.GeometryValidationEvent;

/**
 * A {@link GeometryValidationEvent} augmented with information on document positions and
 * names of affected GML elements.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class GmlGeometryValidationEvent {

	private final GeometryValidationEvent event;

	private final List<GmlElementIdentifier> affectedElements;

	/**
	 * Creates a new {@link GmlGeometryValidationEvent} instance.
	 * @param event validation event, never <code>null</code>
	 * @param affectedElements position and names of affected GML elements, never
	 * <code>null</code>
	 */
	public GmlGeometryValidationEvent(GeometryValidationEvent event, List<GmlElementIdentifier> affectedElements) {
		this.event = event;
		this.affectedElements = affectedElements;
	}

	/**
	 * Returns the validation event.
	 * @return validation event, never <code>null</code>
	 */
	public GeometryValidationEvent getEvent() {
		return event;
	}

	/**
	 * Returns the affected elements.
	 * @return affected elements, never <code>null</code>
	 */
	public List<GmlElementIdentifier> getAffectedElements() {
		return affectedElements;
	}

}
