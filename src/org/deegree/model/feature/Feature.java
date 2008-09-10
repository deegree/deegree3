//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.feature;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.model.feature.types.FeatureType;

/**
 * A feature is a structured object with named properties. Properties may have geometric and non-geometric values.
 * <p>
 * The feature interface is designed to be compatible with the concepts from the following standards:
 * <p>
 * <ul>
 * <li><a href="http://www.opengeospatial.org/standards/as">Abstract Feature specification</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/sfa">Simple Features Interface Standard (SFS)</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/gml">GML features: XML encoding for features</a></li>
 * <li>ISO 19109</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Feature {

    /**
     * Returns the id of the feature.
     * <p>
     * In the GML representation of the feature, this corresponds to the <code>gml:id</code> (GML 3) or <code>fid</code>
     * (GML 2) attribute of the feature element.
     * </p>
     * 
     * @return the id of the feature instance
     */
    public String getId();

    /**
     * Sets the id of the feature.
     * <p>
     * In the GML representation of the feature, this corresponds to the <code>gml:id</code> (GML 3) or <code>fid</code>
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
     * In the GML representation of the feature, this corresponds to the feature element's name.
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
     * Returns all properties of this feature in order.
     * 
     * @return all properties of this feature
     */
    public Property<?>[] getProperties();

    /**
     * Sets the value of a specific occurence of a property with a given name.
     * 
     * @param propName
     *            property name
     * @param occurence
     *            index of the property, starting with zero. If the property is not a multi-property (i.e. maxOccurs=1), this
     *            is always zero.
     * @param value
     *            new value of the property
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    public void setPropertyValue( QName propName, int occurence, Object value );

    /**
     * Called by the {@link FeatureBuilder} during construction to initialize the properties of the feature.
     * 
     * @param props
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException;
}
