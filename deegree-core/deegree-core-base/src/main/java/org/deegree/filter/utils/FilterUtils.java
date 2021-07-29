//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.filter.utils;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.i18n.Messages;
import org.deegree.geometry.Geometry;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FilterUtils {

    /**
     * @param node
     * @return null, if the node cannot be interpreted as geometry
     */
    public static Geometry getGeometryValue( TypedObjectNode node ) {
        Geometry geom = null;
        if ( node instanceof Geometry ) {
            geom = (Geometry) node;
        } else if ( node instanceof Property && ( (Property) node ).getValue() instanceof Geometry ) {
            geom = (Geometry) ( (Property) node ).getValue();
        } else if ( node instanceof GenericXMLElement ) {
            GenericXMLElement xml = (GenericXMLElement) node;
            if ( xml.getChildren() != null && xml.getChildren().size() == 1 ) {
                TypedObjectNode maybeGeom = xml.getChildren().get( 0 );
                if ( maybeGeom instanceof Geometry ) {
                    geom = (Geometry) maybeGeom;
                }
            }
        }
        return geom;
    }

}
