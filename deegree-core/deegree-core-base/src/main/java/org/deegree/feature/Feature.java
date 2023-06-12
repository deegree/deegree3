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

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;

/**
 * A feature is a structured object with named properties, an identifier and type
 * information. Properties may have geometric and non-geometric values and may be (nested)
 * features.
 * <p>
 * The {@link Feature} interface and related types are designed to be compatible with the
 * following specifications:
 * <p>
 * <ul>
 * <li><a href="http://www.opengeospatial.org/standards/as">Abstract Feature
 * specification</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/sfa">Simple Features Interface
 * Standard (SFS)</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/gml">GML features: XML encoding
 * for features</a></li>
 * <li>ISO 19109</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface Feature extends GMLObject {

	/**
	 * Sets the id of the feature.
	 * <p>
	 * In a GML encoding of the feature, this corresponds to the <code>gml:id</code> (GML
	 * 3 and later) or <code>fid</code> (GML 2) attribute of the feature element.
	 * </p>
	 * @param id the id of the feature instance, may be <code>null</code>
	 */
	public void setId(String id);

	/**
	 * Returns the name of the feature.
	 * <p>
	 * In a GML encoding of the feature, this corresponds to the feature's element name.
	 * </p>
	 * @return the name of the feature instance, never <code>null</code>
	 */
	public QName getName();

	/**
	 * Returns the type information for this feature.
	 * @return the type information, never <code>null</code>
	 */
	public FeatureType getType();

	/**
	 * Returns all {@link Geometry}-valued properties in order.
	 * <p>
	 * NOTE: This excludes {@link Envelope}-valued properties, such as
	 * <code>gml:boundedBy</code>.
	 * </p>
	 * @return all geometry properties
	 */
	public List<Property> getGeometryProperties();

	/**
	 * Returns the envelope associated with this feature.
	 * @return the envelope of the feature, or <code>null</code> if the feature has no
	 * envelope information / geometry properties
	 */
	public Envelope getEnvelope();

	/**
	 * Sets the envelope associated with this feature.
	 * @param env the envelope for the feature, can be <code>null</code> (no envelope)
	 */
	public void setEnvelope(Envelope env);

	/**
	 * Returns the envelope aggregated from all geometry/envelope properties of the
	 * feature.
	 * @return envelope of all geometry properties of the feature, can be
	 * <code>null</code> (no envelope)
	 */
	public Envelope calcEnvelope();

	/**
	 * Sets the value of a specific occurrence of a property with a given name (or removes
	 * the property).
	 * @param propName property name
	 * @param occurence index of the property, starting with zero. If the property is not
	 * a multi-property (i.e. maxOccurs=1), this is always zero.
	 * @param value new value of the property or <code>null</code> (removes the property)
	 * @throws IllegalArgumentException if the property names or values are not compatible
	 * with the feature type
	 */
	public void setPropertyValue(QName propName, int occurence, TypedObjectNode value);

	/**
	 * Called during construction to initialize the properties of the feature.
	 * @param props
	 * @throws IllegalArgumentException if the property names or values are not compatible
	 * with the feature type
	 */
	public void setProperties(List<Property> props) throws IllegalArgumentException;

	/**
	 * Returns the extra properties associated with the feature.
	 * <p>
	 * These properties are not defined by the {@link FeatureType}, but provide a generic
	 * way to attach information to the {@link Feature} (e.g. rendering hints).
	 * </p>
	 * @return extra properties, may be <code>null</code>
	 */
	public ExtraProps getExtraProperties();

	public void setExtraProperties(ExtraProps extraProps);

}
