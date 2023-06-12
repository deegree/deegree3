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

import static org.deegree.gml.schema.WellKnownGMLTypes.GML311_FEATURECOLLECTION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.array.TypedObjectNodeArray;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;

/**
 * Allows the representation of arbitrary {@link FeatureCollection}s, including those that
 * use {@link FeatureCollectionType}s with additional properties.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GenericFeatureCollection extends AbstractFeatureCollection {

	// private static final Logger LOG = LoggerFactory.getLogger(
	// GenericFeatureCollection.class );

	private final List<Feature> memberFeatures = new ArrayList<Feature>();

	private final List<Property> props;

	/**
	 * Creates a new {@link GenericFeatureCollection} instance with type information and
	 * content specified using properties.
	 * @param ft feature collection type, must not be <code>null</code>
	 * @param fid feature id, may be <code>null</code>
	 * @param props properties (including feature member/feature member array properties),
	 * must not be <code>null</code>
	 */
	public GenericFeatureCollection(FeatureCollectionType ft, String fid, List<Property> props, ExtraProps extraProps) {

		super(fid, ft, extraProps);
		this.props = props;

		// extract member features
		for (Property prop : props) {
			Object propValue = prop.getValue();
			if (propValue instanceof Feature) {
				memberFeatures.add((Feature) prop.getValue());
			}
			else if (propValue instanceof TypedObjectNodeArray<?>) {
				for (TypedObjectNode member : ((TypedObjectNodeArray<?>) propValue).getElements()) {
					if (member instanceof Feature) {
						memberFeatures.add((Feature) member);
					}
				}
			}
		}
	}

	/**
	 * Creates a new {@link GenericFeatureCollection} instance without type information
	 * that contains the given features.
	 * @param fid
	 * @param memberFeatures
	 */
	public GenericFeatureCollection(String fid, Collection<Feature> memberFeatures) {
		super(fid, GML311_FEATURECOLLECTION, null);
		this.memberFeatures.addAll(memberFeatures);
		this.props = null;
	}

	/**
	 * Creates a new empty {@link GenericFeatureCollection} instance without type
	 * information.
	 */
	public GenericFeatureCollection() {
		super(null, GML311_FEATURECOLLECTION, null);
		this.props = null;
	}

	@Override
	public QName getName() {
		return ft.getName();
	}

	@Override
	public List<Property> getProperties() {
		if (props == null) {
			List<Property> props = new ArrayList<Property>(memberFeatures.size());
			for (Feature feature : memberFeatures) {
				props.add(new GenericProperty(getType().getMemberDeclarations().get(0), null, feature));
			}
			return props;
		}
		return props;
	}

	@Override
	public void setProperties(List<Property> props) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	// -----------------------------------------------------------------------
	// implementation of List<Feature>
	// -----------------------------------------------------------------------

	@Override
	public Iterator<Feature> iterator() {
		return memberFeatures.iterator();
	}

	@Override
	public boolean add(Feature e) {
		return memberFeatures.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends Feature> c) {
		return memberFeatures.addAll(c);
	}

	@Override
	public void clear() {
		memberFeatures.clear();
	}

	@Override
	public boolean contains(Object o) {
		return memberFeatures.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return memberFeatures.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return memberFeatures.isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		return memberFeatures.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return memberFeatures.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		return memberFeatures.size();
	}

	@Override
	public Object[] toArray() {
		return memberFeatures.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return memberFeatures.toArray(a);
	}

	@Override
	public void setPropertyValue(QName propName, int occurrence, TypedObjectNode value) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public List<Property> getProperties(QName propName) {
		List<Property> namedProps = new ArrayList<Property>();
		if (props != null) {
			for (Property property : props) {
				if (propName.equals(property.getName())) {
					namedProps.add(property);
				}
			}
		}
		else if (propName.equals(getType().getMemberDeclarations().get(0))) {
			for (Feature feature : memberFeatures) {
				namedProps.add(new GenericProperty(getType().getMemberDeclarations().get(0), null, feature));
			}
		}
		return namedProps;
	}

	@Override
	public List<Property> getGeometryProperties() {
		List<Property> geoProps = new ArrayList<Property>();
		if (props != null) {
			for (Property property : props) {
				if (property.getValue() instanceof Geometry && !(property.getValue() instanceof Envelope)) {
					geoProps.add(property);
				}
			}
		}
		return geoProps;
	}

}
