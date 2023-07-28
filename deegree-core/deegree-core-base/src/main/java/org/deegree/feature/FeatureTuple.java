/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.feature;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;

/**
 * Encapsulates a tuple of features as described in WFS 2.0 as result set for joins.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class FeatureTuple implements Feature {

	private final List<Feature> features;

	private String id;

	private Envelope envelope;

	private boolean envelopeCalculated = false;

	/**
	 * @param features list of features part of this tuple, never <code>null</code>
	 */
	public FeatureTuple(List<Feature> features) {
		this.features = features;
		this.id = createId(features);
	}

	/**
	 * @return all features part of this tuple, never <code>null</code>
	 */
	public List<Feature> getTupleFeatures() {
		return features;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public List<Property> getProperties() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public List<Property> getProperties(QName propName) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public QName getName() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public FeatureType getType() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public List<Property> getGeometryProperties() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Envelope getEnvelope() {
		if (!envelopeCalculated) {
			envelope = calcEnvelope();
		}
		return envelope;
	}

	@Override
	public void setEnvelope(Envelope env) {
		this.envelope = env;
		envelopeCalculated = true;
	}

	@Override
	public Envelope calcEnvelope() {
		Envelope fcBBox = null;
		for (Feature feature : this.features) {
			Envelope memberBBox = feature.getEnvelope();
			if (memberBBox != null) {
				if (fcBBox != null) {
					fcBBox = fcBBox.merge(memberBBox);
				}
				else {
					fcBBox = memberBBox;
				}
			}
		}
		return fcBBox;
	}

	@Override
	public void setPropertyValue(QName propName, int occurence, TypedObjectNode value) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void setProperties(List<Property> props) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public ExtraProps getExtraProperties() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void setExtraProperties(ExtraProps extraProps) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	private String createId(List<Feature> features) {
		String id = "tupel_";
		for (Feature feature : features)
			id += feature.getId();
		return id;
	}

}