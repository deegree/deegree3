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
package org.deegree.feature.persistence.sql.rules;

import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;

/**
 * Various static methods for performing standard tasks on {@link Mapping} objects.
 *
 * @see Mapping
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class Mappings {

	public static GeometryMapping getGeometryMapping(Mapping mapping) {
		if (mapping instanceof GeometryMapping) {
			return (GeometryMapping) mapping;
		}
		if (mapping instanceof CompoundMapping) {
			CompoundMapping cm = (CompoundMapping) mapping;
			for (Mapping child : cm.getParticles()) {
				GeometryMapping gm = getGeometryMapping(child);
				if (gm != null) {
					return gm;
				}
			}
		}
		return null;
	}

	public static DBField getDBField(Mapping mapping) throws UnsupportedOperationException, IllegalArgumentException {
		MappingExpression me = getMappingExpression(mapping);
		if (!(me instanceof DBField)) {
			throw new IllegalArgumentException("Mapping '" + mapping + "' does not ");
		}
		return (DBField) me;
	}

	public static MappingExpression getMappingExpression(Mapping mapping) throws UnsupportedOperationException {
		MappingExpression me = null;
		if (mapping instanceof PrimitiveMapping) {
			me = ((PrimitiveMapping) mapping).getMapping();
		}
		else if (mapping instanceof GeometryMapping) {
			me = ((GeometryMapping) mapping).getMapping();
		}
		else {
			throw new UnsupportedOperationException(
					"Mappings of type '" + mapping.getClass() + "' are not handled yet.");
		}
		return me;
	}

}