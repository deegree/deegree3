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
package org.deegree.feature;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows the representation of arbitrary {@link FeatureCollection}s, also those that use {@link FeatureCollectionType}s
 * with their own properties.
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

    /** * */
    public static QName FEATURE_MEMBER = new QName( "http://www.opengis.net/gml", "featureMember" );

    private static QName FEATURE_MEMBERS = new QName( "http://www.opengis.net/gml", "featureMembers" );

    /**
     * Creates a new {@link GenericFeatureCollection} instance with type information and content specified using
     * properties.
     * 
     * @param ft
     * @param fid
     * @param props
     */
    public GenericFeatureCollection( FeatureCollectionType ft, String fid, List<Property<?>> props, GMLVersion version ) {
        this.ft = ft;
        this.fid = fid;
        for ( Property<?> prop : props ) {
            // TODO do this a better way
            Object propValue = prop.getValue();
            if ( propValue instanceof Feature ) {
                memberFeatures.add( (Feature) prop.getValue() );
            } else if ( propValue instanceof Feature[] ) {
                for ( Feature feature : (Feature[]) propValue ) {
                    memberFeatures.add( feature );
                }
            } else {
                nonMemberProps.add( prop );
            }
        }

        for ( PropertyType propertyDecl : ft.getPropertyDeclarations() ) {
            if ( FEATURE_MEMBER.equals( propertyDecl.getName() ) ) {
                featureMemberDecl = propertyDecl;
            }
        }

        if ( featureMemberDecl == null ) {
            featureMemberDecl = new FeaturePropertyType( FEATURE_MEMBER, 0, -1, null, false, null, BOTH );
        }
    }

    /**
     * Creates a new {@link GenericFeatureCollection} instance without type information that contains the given
     * features.
     * 
     * @param fid
     * @param memberFeatures
     */
    public GenericFeatureCollection( String fid, Collection<Feature> memberFeatures ) {
        this.fid = fid;
        this.memberFeatures = new ArrayList<Feature>( memberFeatures );
        featureMemberDecl = new FeaturePropertyType( FEATURE_MEMBER, 0, -1, null, false, null, BOTH );
        this.ft = new GenericFeatureCollectionType( new QName( GMLNS, "FeatureCollection" ),
                                                    Collections.singletonList( featureMemberDecl ), false );
    }

    /**
     * Creates a new empty {@link GenericFeatureCollection} instance without type information.
     */
    public GenericFeatureCollection() {
        featureMemberDecl = new FeaturePropertyType( FEATURE_MEMBER, 0, -1, null, false, null, BOTH );
        this.ft = new GenericFeatureCollectionType( new QName( GMLNS, "FeatureCollection" ),
                                                    Collections.singletonList( featureMemberDecl ), false );
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
            props[i++] = property;
        }
        for ( Feature feature : memberFeatures ) {
            props[i++] = new GenericProperty<Feature>( featureMemberDecl, null, feature );
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
        return memberFeatures.add( e );
    }

    @Override
    public boolean addAll( Collection<? extends Feature> c ) {
        // TODO Auto-generated method stub
        return memberFeatures.addAll( c );
    }

    @Override
    public void clear() {
        memberFeatures.clear();
    }

    @Override
    public boolean contains( Object o ) {
        return memberFeatures.contains( o );
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        return memberFeatures.containsAll( c );
    }

    @Override
    public boolean isEmpty() {
        return memberFeatures.isEmpty();
    }

    @Override
    public boolean remove( Object o ) {
        return memberFeatures.remove( o );
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        return memberFeatures.removeAll( c );
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
        return memberFeatures.toArray();
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        return memberFeatures.toArray( a );
    }

    @Override
    public void setPropertyValue( QName propName, int occurrence, Object value ) {
        LOG.debug( "Setting property value for " + occurrence + ". " + propName + " property" );
        if ( !propName.equals( FEATURE_MEMBER ) ) {
            throw new RuntimeException( "Only property '" + FEATURE_MEMBER + " may be set." );
        }
        int featureNum = occurrence - nonMemberProps.size();
        memberFeatures.set( featureNum, (Feature) value );
    }

    // TODO also allow the retrieval of featureMember properties in the methods below

    @Override
    public Property<?>[] getProperties( QName propName ) {
        List<Property<?>> namedProps = new ArrayList<Property<?>>( nonMemberProps.size() );
        for ( Property<?> property : nonMemberProps ) {
            if ( propName.equals( property.getName() ) ) {
                namedProps.add( property );
            }
        }
        return namedProps.toArray( new Property<?>[namedProps.size()] );
    }

    @Override
    public Property<?> getProperty( QName propName ) {
        Property<?> prop = null;
        for ( Property<?> property : nonMemberProps ) {
            if ( propName.equals( property.getName() ) ) {
                if ( prop != null ) {
                    String msg = "Feature has more than one property with name '" + propName + "'.";
                    throw new IllegalArgumentException( msg );
                }
                prop = property;
            }
        }
        return prop;
    }

    @Override
    public Object getPropertyValue( QName propName ) {
        Object value = null;
        for ( Property<?> property : nonMemberProps ) {
            if ( propName.equals( property.getName() ) ) {
                if ( value != null ) {
                    String msg = "Feature has more than one property with name '" + propName + "'.";
                    throw new IllegalArgumentException( msg );
                }
                value = property.getValue();
            }
        }
        return value;
    }

    @Override
    public Object[] getPropertyValues( QName propName ) {
        List<Object> propValues = new ArrayList<Object>( nonMemberProps.size() );
        for ( Property<?> property : nonMemberProps ) {
            if ( propName.equals( property.getName() ) ) {
                propValues.add( property.getValue() );
            }
        }
        return propValues.toArray( new Object[propValues.size()] );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Property<Geometry>[] getGeometryProperties() {
        List<Property<Geometry>> geoProps = new ArrayList<Property<Geometry>>( nonMemberProps.size() );
        for ( Property<?> property : nonMemberProps ) {
            if ( property.getValue() instanceof Geometry ) {
                geoProps.add( (Property<Geometry>) property );
            }
        }
        return geoProps.toArray( new Property[geoProps.size()] );
    }
}
