//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.feature.persistence;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;

/**
 * A {@link FeatureType} which is associated with a {@link FeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class PersistentFeatureType implements FeatureType {

    private final FeatureType ft;

    private final FeatureStore store;

    private ApplicationSchema schema;
    
    /**
     * @param ft
     * @param store 
     */
    public PersistentFeatureType (FeatureType ft, FeatureStore store) {
        this.ft = ft;
        this.store = store;
    }

    /**
     * Returns the associated {@link FeatureStore}.
     * 
     * @return the associated <code>FeatureStore</code>
     */
    public FeatureStore getStore () {
        return store;
    }
    
    @Override
    public QName getName() {
        return ft.getName();
    }

    @Override
    public PropertyType getPropertyDeclaration( QName propName ) {
        return ft.getPropertyDeclaration( propName );
    }

    @Override
    public List<PropertyType> getPropertyDeclarations() {
        return ft.getPropertyDeclarations();
    }

    @Override
    public boolean isAbstract() {
        return ft.isAbstract();
    }

    @Override
    public Feature newFeature( String fid, List<Property<?>> props ) {
        return ft.newFeature( fid, props );
    }
    
    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public void setSchema( ApplicationSchema schema ) {
       this.schema = schema;
    }     
}
