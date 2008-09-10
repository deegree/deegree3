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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.model.feature.types.FeatureType;
import org.deegree.model.feature.types.GenericFeatureType;

/**
 * Allows the representation of arbitrary {@link Feature}s.
 * <p>
 * Please note that it is more efficient to use the {@link GenericSimpleFeature} class if the feature to be represented
 * does not contain multiple properties or nested features ("complex properties").
 * </p>
 * 
 * @see GenericSimpleFeature
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericFeature implements Feature {

    private String fid;

    private GenericFeatureType ft;

    private List<Property<?>> props;

    public GenericFeature( GenericFeatureType ft, String fid, List<Property<?>> props ) {
        this.ft = ft;
        this.fid = fid;
        this.props = new ArrayList<Property<?>>( props );
    }

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
        return props.toArray( new Property<?>[props.size()] );
    }

    @Override
    public void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException {
        this.props = new ArrayList<Property<?>>( props );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, Object value ) {
        // TODO Auto-generated method stub
    }
}
