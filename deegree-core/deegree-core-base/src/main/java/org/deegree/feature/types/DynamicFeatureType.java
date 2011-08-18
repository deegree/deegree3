//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.types;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.gml.GMLVersion;

/**
 * {@link FeatureType} that allows to add property declarations after construction.
 * 
 * @author <a href="schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DynamicFeatureType implements FeatureType {

    private final QName ftName;

    private final DynamicAppSchema appSchema;

    private final List<PropertyType> props = new LinkedList<PropertyType>();

    private final Map<QName, PropertyType> propNameToDecl = new HashMap<QName, PropertyType>();

    public DynamicFeatureType( QName ftName, DynamicAppSchema appSchema ) {
        this.ftName = ftName;
        this.appSchema = appSchema;
    }

    @Override
    public QName getName() {
        return ftName;
    }

    @Override
    public PropertyType getPropertyDeclaration( QName propName ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertyType getPropertyDeclaration( QName propName, GMLVersion version ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PropertyType> getPropertyDeclarations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PropertyType> getPropertyDeclarations( GMLVersion version ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GeometryPropertyType getDefaultGeometryPropertyDeclaration() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public Feature newFeature( String fid, List<Property> props, ExtraProps extraProps, GMLVersion version ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AppSchema getSchema() {
        return appSchema;
    }
}
