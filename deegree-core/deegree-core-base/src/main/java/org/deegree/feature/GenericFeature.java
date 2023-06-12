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
package org.deegree.feature;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows the representation of arbitrary {@link Feature}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GenericFeature extends AbstractFeature {

	private static final Logger LOG = LoggerFactory.getLogger(GenericFeature.class);

	private List<Property> props;

	/**
	 * Creates a new {@link GenericFeature} instance.
	 * @param ft feature type, must not be <code>null</code>
	 * @param fid feature id or <code>null</code> if the feature is anonymous (discouraged
	 * for most use cases)
	 * @param props properties of the feature
	 * @param extraProps extra properties, may be <code>null</code>
	 */
	public GenericFeature(FeatureType ft, String fid, List<Property> props, ExtraProps extraProps) {
		super(fid, ft, extraProps);
		this.props = new ArrayList<Property>(props);
	}

	@Override
	public List<Property> getProperties() {
		return props;
	}

	@Override
	public void setProperties(List<Property> props) throws IllegalArgumentException {
		this.props = new ArrayList<Property>(props);
	}

	@Override
	public void setPropertyValue(QName propName, int occurrence, TypedObjectNode value) {

		LOG.debug("Setting property value for " + occurrence + ". " + propName + " property");

		// check if change would violate minOccurs/maxOccurs constraint
		int current = getProperties(propName).size();
		PropertyType pt = getType().getPropertyDeclaration(propName);
		if (value == null) {
			// null means remove
			if (current - 1 < pt.getMinOccurs()) {
				String msg = "Cannot remove property '" + propName + "' from feature '" + getName()
						+ ": property must be present at least " + pt.getMinOccurs() + " time(s).";
				throw new IllegalArgumentException(msg);
			}
		}
		else {
			// TODO checks about maxOccurs (and check occurence)
		}

		int num = 0;
		for (int i = 0; i < props.size(); i++) {
			Property prop = props.get(i);
			// TODO this is not sufficient (prop name must not be equal to prop type name)
			if (prop.getName().equals(propName)) {
				if (num++ == occurrence) {
					if (value != null) {
						props.set(i, new GenericProperty(pt, propName, value));
					}
					else {
						props.remove(i);
					}
					LOG.debug("Yep.");
					break;
				}
			}
		}
	}

	@Override
	public List<Property> getProperties(QName propName) {
		List<Property> namedProps = new ArrayList<Property>(props.size());
		for (Property property : props) {
			if (propName.equals(property.getName())) {
				namedProps.add(property);
			}
		}
		return namedProps;
	}

	@Override
	public List<Property> getGeometryProperties() {
		List<Property> geoProps = new ArrayList<Property>(props.size());
		for (Property property : props) {
			if (property.getValue() instanceof Geometry && !(property.getValue() instanceof Envelope)) {
				geoProps.add(property);
			}
		}
		return geoProps;
	}

}
