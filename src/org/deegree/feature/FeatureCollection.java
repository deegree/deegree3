//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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

import java.util.Collection;

import org.deegree.commons.filter.Filter;
import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.feature.types.FeatureCollectionType;

/**
 * A feature collection is a collection of {@link Feature} instances.
 * <p>
 * Note that a {@link FeatureCollection} is a {@link Feature} itself.
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
     * @return the type information
     */
    @Override
    public FeatureCollectionType getType();

    /**
     * Returns the contained features that match the given {@link Filter}.
     * 
     * @param filter
     *            <code>Filter</code> to be applied
     * @return matching feature instances as a new <code>FeatureCollection</code>
     * @throws FilterEvaluationException
     *             if an exception occurs during the evaluation of the <code>Filter</code>
     */
    public FeatureCollection getMembers( Filter filter )
                            throws FilterEvaluationException;
}
