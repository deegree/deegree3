//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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

import java.util.Collection;

import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;

/**
 * A feature collection is a collection of {@link Feature} instances.
 * <p>
 * Note that a {@link FeatureCollection} is a {@link Feature} itself, which complies to GML feature collection
 * definitions.
 * </p>
 * 
 * @see Feature
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 13814 $, $Date: 2008-09-02 20:28:13 +0200 (Di, 02 Sep 2008) $
 */
public interface FeatureCollection extends Feature, Collection<Feature> {

    /**
     * Returns the type information for this feature collection.
     * 
     * @return the type information, never <code>null</code>
     */
    @Override
    public FeatureCollectionType getType();

    /**
     * Returns the contained features that match the given {@link Filter}.
     * 
     * @param filter
     *            <code>Filter</code> to be applied, must not be <code>null</code>
     * @return matching feature instances as a new <code>FeatureCollection</code>
     * @throws FilterEvaluationException
     *             if an exception occurs during the evaluation of the <code>Filter</code>
     */
    public FeatureCollection getMembers( Filter filter )
                            throws FilterEvaluationException;
}
