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
package org.deegree.feature.types;

import java.util.List;

import org.deegree.feature.types.property.ArrayPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;

/**
 * {@link FeatureType} that defines a collection of features.
 * <p>
 * A {@link FeatureCollectionType} always has at least one member property declaration, i.e. a property used for
 * aggregating member features (e.g. <code>gml:featureMember</code> for a default GML feature collection). Additionally,
 * it may define feature array properties that allow for aggregating of multiple features in a single property element
 * (e.g. <code>gml:featureMembers</code> for a default GML feature collection).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface FeatureCollectionType extends FeatureType {

    /**
     * Returns the feature member property declarations.
     * 
     * @return feature member property declarations (in order), never <code>null</code> and contains a least one entry
     */
    public List<FeaturePropertyType> getMemberDeclarations();

    /**
     * Returns the feature member array property declarations.
     * 
     * @return feature member array property declarations (in order), may be empty, but never <code>null</code>
     */
    public List<ArrayPropertyType> getMemberArrayDeclarations();
}