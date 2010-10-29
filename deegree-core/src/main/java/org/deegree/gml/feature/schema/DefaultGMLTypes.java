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
package org.deegree.gml.feature.schema;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.property.ArrayPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;

/**
 * <code>DefaultGMLTypes</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultGMLTypes {

    /**
     * 
     */
    public static final FeatureCollectionType GML311_FEATURECOLLECTION;

    /**
     * 
     */
    public static final FeatureCollectionType GML321_FEATURECOLLECTION;

    static {
        QName name = new QName( GMLNS, "FeatureCollection", "gml" );
        List<PropertyType> props = new ArrayList<PropertyType>();
        props.add( new FeaturePropertyType( new QName( GMLNS, "featureMember", "gml" ), 0, -1, false, false, null, null,
                                            BOTH ) );
        props.add( new ArrayPropertyType( new QName( GMLNS, "featureMembers", "gml" ), 0, -1, false, false, null ) );
        GML311_FEATURECOLLECTION = new GenericFeatureCollectionType( name, props, false );

        name = new QName( GML3_2_NS, "FeatureCollection", "gml" );
        props = new ArrayList<PropertyType>();
        props.add( new FeaturePropertyType( new QName( GML3_2_NS, "featureMember", "gml" ), 0, -1, false, false, null,
                                            null, BOTH ) );
        props.add( new ArrayPropertyType( new QName( GML3_2_NS, "featureMembers", "gml" ), 0, -1, false, false, null ) );
        GML321_FEATURECOLLECTION = new GenericFeatureCollectionType( name, props, false );
    }

}