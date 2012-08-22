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

package org.deegree.ogcwebservices.wfs.capabilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;

/**
 * This section defines the list of feature types (and the available operations on each feature
 * type) that are served by a web feature server. It's used in responses to
 * {@link WFSGetCapabilities} requests.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$ $Date$
 */
public class FeatureTypeList {

    private Operation[] globalOperations;

    private Map<QualifiedName, WFSFeatureType> featureTypes = new HashMap<QualifiedName, WFSFeatureType>();

    /**
     * Creates a new <code>FeatureTypeList</code> instance.
     *
     * @param globalOperations
     * @param featureTypes
     */
    public FeatureTypeList( Operation[] globalOperations, Collection<WFSFeatureType> featureTypes ) {
        this.globalOperations = globalOperations;
        for ( WFSFeatureType ft : featureTypes ) {
            this.featureTypes.put( ft.getName(), ft );
        }
    }

    /**
     * Returns all served feature types.
     *
     * @return all served feature types
     */
    public WFSFeatureType[] getFeatureTypes() {
        return this.featureTypes.values().toArray( new WFSFeatureType[this.featureTypes.size()] );
    }

    /**
     * Returns the feature type with the given name.
     *
     * @param name
     *            name of the feature type to look up
     * @return the feature type with the given name
     */
    public WFSFeatureType getFeatureType( QualifiedName name ) {
        return this.featureTypes.get( name );
    }

    /**
     * Adds the given feature type to the list of served feature types.
     *
     * @param featureType
     *            feature type to be added
     */
    public void addFeatureType( WFSFeatureType featureType ) {
        this.featureTypes.put( featureType.getName(), featureType );
    }

    /**
     * Removes the given feature type from the list of served feature types.
     *
     * @param featureType
     *            feature type to be removed
     */
    public void removeFeatureType( WFSFeatureType featureType ) {
        this.featureTypes.remove( featureType.getName() );
    }

    /**
     * Returns the {@link Operation}s that are available on all served feature types.
     *
     * @return the {@link Operation}s that are available on all served feature types
     */
    public Operation[] getGlobalOperations() {
        return globalOperations;
    }
}
