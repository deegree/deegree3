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
package org.deegree.security.drm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deegree.model.feature.Feature;
import org.deegree.security.GeneralSecurityException;

/**
 * A <code>RightSet</code> encapsulates a number of <code>Right</code> objects. This are grouped by the
 * <code>SecurableObject</code> for which they apply to support an efficient implementation of the merge()-operation.
 * The merge()-operation results in a <code>RightSet</code> that contains the logical rights of boths sets, but only one
 * <code>Right</code> object of each <code>RightType</code> (and <code>SecurableObject</code>). This is accomplished by
 * merging the constraints of the <code>Rights</code> of the same type (and object).
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */
public class RightSet {

    // keys are SecurableObjects (for which the rights are defined), values
    // are Maps (keys are RightTypes, values are Rights)
    private Map<SecurableObject, Map<RightType, Right>> secObjects = new HashMap<SecurableObject, Map<RightType, Right>>();

    private boolean isEmpty;

    RightSet() {
    }

    /**
     * @param rights
     */
    public RightSet( Right[] rights ) {
        this.isEmpty = rights == null || rights.length == 0;
        for ( int i = 0; i < rights.length; i++ ) {
            Map<RightType, Right> rightMap = secObjects.get( rights[i].getSecurableObject() );
            if ( rightMap == null ) {
                rightMap = new HashMap<RightType, Right>();
            }
            rightMap.put( rights[i].getType(), rights[i] );
            secObjects.put( rights[i].getSecurableObject(), rightMap );
        }
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Checks if the <code>RightSet</code> contains the permissions for a <code>SecurableObject</code> and a concrete
     * situation (the situation is represented by the given <code>Feature</code>).
     * 
     * @param object
     * @param type
     * @param situation
     * @return true, if the right applies
     * @throws GeneralSecurityException
     */
    public boolean applies( SecurableObject object, RightType type, Feature situation )
                            throws GeneralSecurityException {
        boolean applies = false;
        Map<RightType, Right> rightMap = secObjects.get( object );
        if ( rightMap != null ) {
            Right right = rightMap.get( type );
            if ( right != null ) {
                applies = right.applies( object, situation );
            }
        }
        return applies;
    }

    /**
     * Checks if the <code>RightSet</code> contains the (unrestricted) permissions for a <code>SecurableObject</code>
     * and a certain type of right.
     * 
     * @param object
     * @param type
     * @return true, if the right applies
     */
    public boolean applies( SecurableObject object, RightType type ) {
        boolean applies = false;
        Map<RightType, Right> rightMap = secObjects.get( object );
        if ( rightMap != null ) {
            Right right = rightMap.get( type );
            if ( right != null ) {
                applies = right.applies( object );
            }

        }
        return applies;
    }

    /**
     * @param secObject
     * @param type
     * @return the <code>Right</code> of the specified <code>RightType</code> that this <code>RightSet</code> defines on
     *         the specified <code>SecurableObject</code>.
     */
    public Right getRight( SecurableObject secObject, RightType type ) {
        Right right = null;
        if ( secObjects.get( secObject ) != null ) {
            right = secObjects.get( secObject ).get( type );
        }
        return right;
    }

    /**
     * @param secObject
     * @return the encapulated <code>Rights</code> (for one <code>SecurableObject</code>) as an one-dimensional array.
     */
    public Right[] toArray( SecurableObject secObject ) {
        Right[] rights = new Right[0];
        Map<RightType, Right> rightMap = secObjects.get( secObject );
        if ( rightMap != null ) {
            rights = rightMap.values().toArray( new Right[rightMap.size()] );
        }
        return rights;
    }

    /**
     * @return the encapulated <code>Rights</code> as a two-dimensional array:
     *         <ul>
     *         <li>first index: runs the different <code>SecurableObjects</code>
     *         <li>second index: runs the different <code>Rights</code>
     *         </ul>
     */
    public Right[][] toArray2() {
        ArrayList<Right[]> secObjectList = new ArrayList<Right[]>();
        Iterator<Map<RightType, Right>> it = secObjects.values().iterator();
        while ( it.hasNext() ) {
            Map<RightType, Right> rightMap = it.next();
            Right[] rights = rightMap.values().toArray( new Right[rightMap.size()] );
            secObjectList.add( rights );
        }
        return secObjectList.toArray( new Right[secObjectList.size()][] );
    }

    /**
     * Produces the logical disjunction of two <code>RightSets</code>.
     * 
     * @param that
     * @return the new right set
     */
    public RightSet merge( RightSet that ) {

        ArrayList<Right> mergedRights = new ArrayList<Right>( 20 );
        Iterator<SecurableObject> secObjectsIt = this.secObjects.keySet().iterator();

        // add all rights from 'this' (and merge them with corresponding right
        // from 'that')
        while ( secObjectsIt.hasNext() ) {
            SecurableObject secObject = secObjectsIt.next();
            Map<RightType, Right> thisRightMap = this.secObjects.get( secObject );
            Map<RightType, Right> thatRightMap = that.secObjects.get( secObject );
            Iterator<RightType> rightIt = ( thisRightMap ).keySet().iterator();
            while ( rightIt.hasNext() ) {
                RightType type = rightIt.next();
                Right mergedRight = thisRightMap.get( type );

                // find corresponding Right (if any) in the other RightSet
                if ( thatRightMap != null && thatRightMap.get( type ) != null ) {
                    try {
                        mergedRight = mergedRight.merge( thatRightMap.get( type ) );
                    } catch ( GeneralSecurityException e ) {
                        e.printStackTrace();
                    }
                }
                mergedRights.add( mergedRight );
            }
        }

        // add role rights from 'that'
        secObjectsIt = that.secObjects.keySet().iterator();
        while ( secObjectsIt.hasNext() ) {
            SecurableObject secObject = secObjectsIt.next();
            Map<RightType, Right> thisRightMap = this.secObjects.get( secObject );
            Map<RightType, Right> thatRightMap = that.secObjects.get( secObject );

            Iterator<RightType> it = thatRightMap.keySet().iterator();
            while ( it.hasNext() ) {
                Object o = it.next();
                RightType type = (RightType) o;
                // find corresponding Right (if none, add)
                if ( thisRightMap == null || thisRightMap.get( type ) == null ) {
                    mergedRights.add( thatRightMap.get( type ) );
                }
            }
        }
        return new RightSet( mergedRights.toArray( new Right[mergedRights.size()] ) );
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( "RightSet:" );
        Iterator<SecurableObject> it = secObjects.keySet().iterator();
        while ( it.hasNext() ) {
            SecurableObject secObject = it.next();
            sb.append( "on SecurableObject " ).append( secObject ).append( "\n" );
            Map<RightType, Right> rightMap = secObjects.get( secObject );
            Iterator<RightType> rights = rightMap.keySet().iterator();
            while ( rights.hasNext() ) {
                RightType rightType = rights.next();
                sb.append( "- Right " ).append( rightMap.get( rightType ) ).append( "\n" );
            }
        }
        return sb.toString();
    }

}
