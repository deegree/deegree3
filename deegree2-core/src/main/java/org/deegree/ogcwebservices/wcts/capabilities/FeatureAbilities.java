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

package org.deegree.ogcwebservices.wcts.capabilities;

import java.util.List;

import org.deegree.framework.util.Pair;

/**
 * <code>FeatureAbilities</code> encapsulates the elements of a featureAbilties element in the contents element of the
 * wcts capabilities 0.4.0.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class FeatureAbilities {

    private final List<Pair<String, String>> geometryTypes;

    private final List<InputOutputFormat> featureFormats;

    private final boolean remoteProperties;


    /**
     * @param geometryTypes
     *            Unordered list of one or more GML 3 geometric primitive types supported by this WCTS server. It is
     *            assumed that a WCTS server can also transform the corresponding geometric complexes and aggregates. It
     *            is also assumed that this WCTS server can transform at least one geometric primitive type.
     * @param featureFormats
     *            Unordered list of one or more identifiers of well-known feature formats in which the transform
     *            operation can accept input features and/or produce output features.
     * @param remoteProperties
     *            Specifies if this server supports remote properties in features transformed.
     */
    public FeatureAbilities( List<Pair<String, String>> geometryTypes, List<InputOutputFormat> featureFormats,
                             boolean remoteProperties ) {
        this.geometryTypes = geometryTypes;
        this.featureFormats = featureFormats;
        this.remoteProperties = remoteProperties;
    }

    /**
     * @return the geometryTypes as a list of &lt;value,codetype &gt; pairs.
     */
    public final List<Pair<String, String>> getGeometryTypes() {
        return geometryTypes;
    }

    /**
     * @return the featureFormats.
     */
    public final List<InputOutputFormat> getFeatureFormats() {
        return featureFormats;
    }

    /**
     * @return the remoteProperties.
     */
    public final boolean getRemoteProperties() {
        return remoteProperties;
    }
}
