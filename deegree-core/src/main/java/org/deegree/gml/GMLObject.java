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
package org.deegree.gml;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;
import org.deegree.gml.dictionary.Definition;
import org.deegree.gml.props.GMLStdProps;

/**
 * Basic interface for GML objects.
 * <p>
 * Currently, deegree has built-in support for the following types of GML objects:
 * <ul>
 * <li>{@link Feature}</li>
 * <li>{@link Geometry}</li>
 * <li>{@link Definition}</li>
 * <li>{@link CoordinateSystem} (TODO needs integration with the GML package)</li>
 * </ul>
 * 
 * @see Feature
 * @see Geometry
 * @see Definition
 * @see CoordinateSystem
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GMLObject extends TypedObjectNode {

    /**
     * Returns the id of the object.
     * 
     * @return the id of the object, or <code>null</code> if it doesn't have an id
     */
    public String getId();

    /**
     * Returns the standard GML properties (e.g. <code>gml:name</code>).
     * 
     * @return the standard GML properties, may be <code><null</code>
     */
    public GMLStdProps getGMLProperties();
}