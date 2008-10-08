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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.model.feature.types.FeatureCollectionType;
import org.deegree.model.feature.types.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericFeatureCollection extends AbstractFeatureCollection {

    private static final Logger LOG = LoggerFactory.getLogger( GenericFeatureCollection.class );       
    
    private String fid;

    private FeatureCollectionType ft;

    private List<Feature> memberFeatures = new ArrayList<Feature>();

    private List<Property<?>> nonMemberProps = new ArrayList<Property<?>>();

    private PropertyType featureMemberDecl;

    private static QName FEATURE_MEMBER = new QName( "http://www.opengis.net/gml", "featureMember" );

    private static QName FEATURE_MEMBERS = new QName( "http://www.opengis.net/gml", "featureMembers" );

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
        for ( Property<?> prop : props ) {
            if ( FEATURE_MEMBER.equals( prop.getName() ) ) {
                // TODO do this a better way                
                if (prop.getValue() instanceof Feature) {
                    memberFeatures.add(  (Feature) prop.getValue() );                    
                } else {
                    memberFeatures.add(  null );
                }
            } else if ( FEATURE_MEMBERS.equals( prop.getName() ) ) {
                for (Feature feature : (Feature []) prop.getValue()) {
                    memberFeatures.add( feature );
                }
            } else {
                nonMemberProps.add( prop );
            }
        }

        for ( PropertyType propertyDecl : ft.getPropertyDeclarations()) {
            if (FEATURE_MEMBER.equals(propertyDecl.getName())) {
                featureMemberDecl = propertyDecl;
            }
        }
    }

    /**
     * Creates a new <code>GenericFeatureCollection</code> without type information that contains the given features.
     * 
     * @param fid
     * @param memberFeatures
     */
    public GenericFeatureCollection( String fid, List<Feature> memberFeatures ) {
        this.fid = fid;
        this.memberFeatures = new ArrayList<Feature>( memberFeatures );
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
        Property<?>[] props = new Property<?>[nonMemberProps.size() + memberFeatures.size()];
        int i = 0;
        for ( Property<?> property : nonMemberProps ) {
            props [i++] = property;
        }
        for ( Feature feature : memberFeatures ) {
            props [i++] = new GenericProperty<Feature>(featureMemberDecl, feature);
        }
        return props;
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

    // -----------------------------------------------------------------------
    // implementation of List<Feature>
    // -----------------------------------------------------------------------

    @Override
    public Iterator<Feature> iterator() {
        return memberFeatures.iterator();
    }

    @Override
    public boolean add( Feature e ) {
        return false;
    }

    @Override
    public boolean addAll( Collection<? extends Feature> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        memberFeatures.clear();
    }

    @Override
    public boolean contains( Object o ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        return memberFeatures.isEmpty();
    }

    @Override
    public boolean remove( Object o ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int size() {
        return memberFeatures.size();
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPropertyValue( QName propName, int occurrence, Object value ) {
        LOG.debug ("Setting property value for " + occurrence + ". " + propName + " property");  
        if (!propName.equals( FEATURE_MEMBER )) {
            throw new RuntimeException ("Only property '" + FEATURE_MEMBER + " may be set.");
        }
        int featureNum = occurrence - nonMemberProps.size();
        memberFeatures.set( featureNum, (Feature) value );
    }
}
