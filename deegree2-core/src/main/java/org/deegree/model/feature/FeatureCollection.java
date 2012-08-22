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
package org.deegree.model.feature;

import java.util.Iterator;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface FeatureCollection extends Feature {

    /**
     * removes all features from a collection
     *
     */
    public void clear();

    /**
     * @param index
     *            of the feature.
     * @return the feature at the submitted index
     */
    public Feature getFeature( int index );

    /**
     * @param id
     *            of the feature
     * @return the feature that is assigned to the submitted id. If no valid feature could be found <code>null</code>
     *         will be returned.
     */
    public Feature getFeature( String id );

    /**
     * @return an array of all features
     */
    public Feature[] toArray();

    /**
     * returns an <tt>Iterator</tt> on the feature contained in a collection
     *
     * @return an <tt>Iterator</tt> on the feature contained in a collection
     */
    public Iterator<Feature> iterator();

    /**
     * adds a feature to the collection
     *
     * @param feature
     *            to add.
     */
    public void add( Feature feature );

   

    /**
     * adds a list of features to the collection
     *
     * @param features
     *            to add.
     */
    public void addAllUncontained( Feature[] features );

  

    /**
     * Adds the features to the collection, if they're not already contained.
     *
     * @param featureCollection
     */
    public void addAllUncontained( FeatureCollection featureCollection );

    /**
     * removes the submitted feature from the collection
     *
     * @param feature
     *            to remove
     *
     * @return removed feature
     */
    public Feature remove( Feature feature );

    /**
     * removes the feature at the submitted index from the collection
     *
     * @param index
     *            of the feature to remove.
     *
     * @return removed feature
     */
    public Feature remove( int index );

    /**
     * removes the feature that is assigned to the submitted id. The removed feature will be returned. If no valid
     * feature could be found null will be returned
     *
     * @param id
     *            of the feature to remove.
     *
     * @return removed feature
     */
    public Feature remove( String id );

    /**
     * @return the number of features within the collection
     */
    public int size();
}
