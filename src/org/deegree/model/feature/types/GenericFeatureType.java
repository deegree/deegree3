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
package org.deegree.model.feature.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.model.feature.Feature;
import org.deegree.model.feature.GenericFeature;
import org.deegree.model.feature.Property;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericFeatureType implements FeatureType {
   
    private QName name;

    // maps property names to their declaration (LinkedHashMap respects the correct key order)
    private Map<QName, PropertyType> propNameToDecl = new LinkedHashMap<QName, PropertyType>();

    private boolean isAbstract;

    public GenericFeatureType( QName name, List<PropertyType> propDecls, boolean isAbstract ) {
        this.name = name;
        for ( PropertyType propDecl : propDecls ) {
            propNameToDecl.put( propDecl.getName(), propDecl );
        }
        this.isAbstract = isAbstract;
    }

    @Override
    public QName getName() {
        return name;
    }

    @Override
    public PropertyType getPropertyDeclaration( QName propName ) {
        return propNameToDecl.get( propName );
    }    
    
    @Override
    public List<PropertyType> getPropertyDeclarations() {
        List<PropertyType> propDecls = new ArrayList<PropertyType>( propNameToDecl.size() );
        for ( QName propName : propNameToDecl.keySet() ) {
            propDecls.add( propNameToDecl.get( propName ) );
        }
        return propDecls;
    }

    @Override
    public Feature newFeature( String fid, List<Property<?>> props ) {
        return new GenericFeature( this, fid, props );
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public ApplicationSchema getSchema() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String toString () {
        String s = "- Feature type '" + name + "', abstract: " + isAbstract;
        for ( QName ptName : propNameToDecl.keySet() ) {
            PropertyType pt = propNameToDecl.get( ptName );
            s += "\n" + pt;
        }
        return s;
    }
}
