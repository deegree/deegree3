/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.featureinfo;

import java.util.Map;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;

/**
 * Collections information about feature info requests. Used to produce serialized output
 * in feature info manager.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class FeatureInfoParams {

	private Map<String, String> nsBindings;

	private FeatureCollection featureCollection;

	private String format;

	private boolean withGeometries;

	private String schemaLocation;

	private FeatureType featureType;

	private ICRS crs;

	private final ICRS infoCrs;

	public FeatureInfoParams(Map<String, String> nsBindings, FeatureCollection col, String format,
			boolean withGeometries, String schemaLocation, FeatureType type, ICRS crs, ICRS infoCrs) {
		this.nsBindings = nsBindings;
		this.featureCollection = col;
		this.format = format;
		this.withGeometries = withGeometries;
		this.schemaLocation = schemaLocation;
		this.featureType = type;
		this.crs = crs;
		this.infoCrs = infoCrs;
	}

	/**
	 * @return the nsBindings
	 */
	public Map<String, String> getNsBindings() {
		return nsBindings;
	}

	/**
	 * @return the featureCollection
	 */
	public FeatureCollection getFeatureCollection() {
		return featureCollection;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @return the withGeometries
	 */
	public boolean isWithGeometries() {
		return withGeometries;
	}

	/**
	 * @return the schemaLocation
	 */
	public String getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * @return the featureType
	 */
	public FeatureType getFeatureType() {
		return featureType;
	}

	/**
	 * @return the crs
	 */
	public ICRS getCrs() {
		return crs;
	}

	/**
	 * @return the CRS of the values to return
	 */
	public ICRS getInfoCrs() {
		return infoCrs;
	}

}
