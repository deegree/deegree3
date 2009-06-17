//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.gml.schema;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;

public class DefaultGMLTypes {

    public static FeatureCollectionType GML311_FEATURECOLLECTION; 
    
    static {
        QName name = new QName (GMLNS, "FeatureCollection");
        List<PropertyType> props = new ArrayList<PropertyType>();
        props.add( new FeaturePropertyType (new QName (GMLNS, "featureMember"), 0, -1, null) );
        GML311_FEATURECOLLECTION = new GenericFeatureCollectionType (name, props, false);
    }
    
}
