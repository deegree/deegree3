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

import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.generic.Attribute;
import org.deegree.model.generic.Node;
import org.deegree.model.geometry.Geometry;

/**
 * {@link Feature} implementation that allows the representation of arbitrary {@link SimpleFeature}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericSimpleFeature implements SimpleFeature {

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.feature.Feature#getProperties()
     */
    @Override
    public Property<?>[] getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.feature.Feature#getPropertyValue(javax.xml.namespace.QName)
     */
    @Override
    public Object getPropertyValue( QName propName ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.feature.Feature#getType()
     */
    @Override
    public FeatureType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.generic.DeegreeObject#getAttributes()
     */
    @Override
    public List<Attribute> getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.generic.DeegreeObject#getContents()
     */
    @Override
    public List<Node> getContents() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.generic.DeegreeObject#getName()
     */
    @Override
    public QName getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Geometry getGeometryPropertyValue( QName propName ) {
        // TODO Auto-generated method stub
        return null;
    }
}
