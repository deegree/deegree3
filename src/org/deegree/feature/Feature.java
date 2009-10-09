//$HeadURL$
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

import org.deegree.commons.types.gml.StandardObjectProps;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.MatchableObject;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;

/**
 * A feature is a structured object with named properties and an identifier. Properties may have geometric and
 * non-geometric values and may be (nested) features.
 * <p>
 * The {@link Feature} interface and related types are designed to be compatible with the following specifications:
 * <p>
 * <ul>
 * <li><a href="http://www.opengeospatial.org/standards/as">Abstract Feature specification</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/sfa">Simple Features Interface Standard (SFS)</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/gml">GML features: XML encoding for features</a></li>
 * <li>ISO 19109</li>
 * </ul>
 * </p>
 * <p>
 * <h4>Notes on the representation of GML features</h4>
 * 
 * The "StandardObjectProperties" defined by GML (e.g. multiple <code>gml:name</code> elements or
 * <code>gml:description</code>) which are inherited by any GML feature type definition are treated in a specific way.
 * They are modelled using the {@link StandardObjectProps} class and not as standard properties of the feature.
 * This design decision has been driven by the goal to make the implementation less GML (and GML-version) specific and
 * to allow for example to export a {@link Feature} instance as either GML 3.2.1 or GML 3.1.1 (different namespaces for
 * the standard properties).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Feature extends MatchableObject {

    /**
     * Returns the id of the feature.
     * <p>
     * In an GML representation of the feature, this corresponds to the <code>gml:id</code> (GML 3 and later) or
     * <code>fid</code> (GML 2) attribute of the feature element.
     * </p>
     * 
     * @return the id of the feature
     */
    public String getId();

    /**
     * Sets the id of the feature.
     * <p>
     * In an GML representation of the feature, this corresponds to the <code>gml:id</code> (GML 3) or <code>fid</code>
     * (GML 2) attribute of the feature element.
     * </p>
     * 
     * @param id
     *            the id of the feature instance
     */
    public void setId( String id );

    /**
     * Returns the name of the feature.
     * <p>
     * In an GML representation of the feature, this corresponds to the feature element's name.
     * </p>
     * 
     * @return the name of the feature instance
     */
    public QName getName();

    /**
     * Returns the type information for this feature.
     * 
     * @return the type information
     */
    public FeatureType getType();

    /**
     * Returns all properties in order.
     * 
     * @return all properties
     */
    public Property<?>[] getProperties();

    /**
     * Returns the values of the properties with the given name, in order.
     * 
     * @param propName
     *            name of the requested property
     * @return the values of the properties with the given name, in order
     */
    public Object[] getPropertyValues( QName propName );

    /**
     * Returns the values of the property with the given name.
     * 
     * @param propName
     *            name of the requested property
     * @return the values of the properties with the given name
     * @throws IllegalArgumentException
     *             if the feature has more than one property with the given name
     */
    public Object getPropertyValue( QName propName );

    /**
     * Returns the properties with the given name, in order.
     * 
     * @param propName
     *            name of the requested properties
     * @return the properties with the given name, in order
     */
    public Property<?>[] getProperties( QName propName );

    /**
     * Returns the property with the given name.
     * 
     * @param propName
     *            name of the requested property
     * @return the property with the given name
     * @throws IllegalArgumentException
     *             if the feature has more than one property with the given name
     */
    public Property<?> getProperty( QName propName );

    /**
     * Returns all geometry-valued properties in order.
     * 
     * @return all geometry properties
     */
    public Property<Geometry>[] getGeometryProperties();

    /**
     * Returns the envelope of the feature.
     * 
     * @return the envelope of the feature, or null if the feature has no geometry properties
     */
    public Envelope getEnvelope();

    /**
     * Sets the value of a specific occurence of a property with a given name.
     * 
     * @param propName
     *            property name
     * @param occurence
     *            index of the property, starting with zero. If the property is not a multi-property (i.e. maxOccurs=1),
     *            this is always zero.
     * @param value
     *            new value of the property
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    public void setPropertyValue( QName propName, int occurence, Object value );

    /**
     * Called during construction to initialize the properties of the feature.
     * 
     * @param props
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException;

    /**
     * Returns a representation of the standard GML properties (e.g. <code>gml:name</code> or
     * <code>gml:description</code).
     * 
     * @return a representation of the standard GML properties, may be null
     */
    public StandardObjectProps getStandardGMLProperties();

    /**
     * Sets the standard GML properties (e.g. <code>gml:name</code> or <code>gml:description</code).
     * 
     * @param standardProps
     *            representation of the standard GML properties
     */
    public void setStandardGMLProperties( StandardObjectProps standardProps );
}
