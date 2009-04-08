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
package org.deegree.feature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;

/**
 * {@link Feature} implementation that allows the representation of arbitrary {@link SimpleFeature}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericSimpleFeature extends AbstractFeature implements SimpleFeature {

    private String fid;

    private FeatureType ft;

    // stores the property names and their values (respects the insertion order)
    private Map<QName, Object> propertyNamesToValues = new LinkedHashMap<QName, Object>();

    @Override
    public String getId() {
        return fid;
    }

    @Override
    public void setId( String fid ) {
        this.fid = fid;
    }

    @Override
    public QName getName() {
        return ft.getName();
    }

    @Override
    public FeatureType getType() {
        return ft;
    }

    @Override
    public Property<?>[] getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getPropertyValue( QName propName ) {
        return propertyNamesToValues.get( propName );
    }

    @Override
    public Geometry getGeometryPropertyValue( QName propName ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, Object value ) {
        if ( occurence != 0 ) {
            String msg = "Cannot set value of the " + occurence + ". occurence in '" + ft.getName()
                         + "' feature: multi properties are not allowed in simple features.";
            throw new IllegalArgumentException( msg );
        }
        setPropertyValue( propName, value );
    }

    @Override
    public void setPropertyValue( QName propName, Object value ) {
        propertyNamesToValues.put( propName, value );
    }

    @Override
    public void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public Property<?>[] getProperties( QName propName ) {
        return null;
    }

    @Override
    public Property<?> getProperty( QName propName ) {
        return null;
    }

    @Override
    public Object[] getPropertyValues( QName propName ) {
        return null;
    }

    @Override
    public Envelope getEnvelope() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Property<Geometry>[] getGeometryProperties() {
        return null;
    }
}
