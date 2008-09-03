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
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.model.feature.schema.FeatureCollectionType;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericFeatureCollection implements FeatureCollection {

    private String fid;

    private FeatureCollectionType ft;

    private List<Feature> memberFeatures;

    /**
     * Creates a new <code>GenericFeatureCollection</code> with type information and content.
     * 
     * @param ft
     * @param fid
     * @param props
     */
    public GenericFeatureCollection( FeatureCollectionType ft, String fid, List<Property<?>> props ) {
        this.ft = ft;
        this.fid = fid;
        // TODO initialize member features and non-feature properties
    }

    /**
     * Creates a new <code>GenericFeatureCollection</code> without type information that contains the given features.
     * 
     * @param fid
     * @param memberFeatures
     */
    public GenericFeatureCollection( String fid, List<Feature> memberFeatures ) {
        this.fid = fid;
        this.memberFeatures = new ArrayList<Feature> (memberFeatures);
    }

    @Override
    public List<Feature> getMemberFeatures() {
        return memberFeatures;
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
    public Property<?>[] getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureCollectionType getType() {
        return ft;
    }

    @Override
    public void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public Iterator<Feature> iterator() {
        return memberFeatures.iterator();
    }
}
