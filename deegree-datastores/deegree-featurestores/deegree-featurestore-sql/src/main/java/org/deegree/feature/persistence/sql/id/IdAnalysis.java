/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.persistence.sql.id;

import org.deegree.commons.utils.StringUtils;
import org.deegree.feature.types.FeatureType;

/**
 * Analysis of an incoming feature / geometry id.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class IdAnalysis {

	private final FeatureType ft;

	// TODO: geometries
	private final boolean isFid = true;

	private String[] idKernels;

	IdAnalysis(FeatureType ft, String idRemainder, FIDMapping fidMapping) throws IllegalArgumentException {
		this.ft = ft;
		if (fidMapping.getColumns().size() == 1) {
			idKernels = new String[] { idRemainder };
		}
		else {
			idKernels = StringUtils.split(idRemainder, fidMapping.getDelimiter());
		}
	}

	/**
	 * @return
	 */
	public FeatureType getFeatureType() {
		return ft;
	}

	/**
	 * Returns the values for the feature id columns from the {@link FIDMapping}.
	 * @return values for feature id columns, never <code>null</code>
	 */
	public String[] getIdKernels() {
		return idKernels;
	}

	/**
	 * @return
	 */
	public boolean isFid() {
		return isFid;
	}

	@Override
	public String toString() {
		return "ft=" + ft.getName() + ",idKernels=" + idKernels;
	}

}