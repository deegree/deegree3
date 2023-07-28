/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.observation.persistence.binary;

import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.observation.persistence.ObservationDatastore;
import org.deegree.observation.persistence.ObservationStoreProvider;

/**
 * The <code>BinaryObservationProvider</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * 
 * 
 */
public class BinaryObservationProvider implements ObservationStoreProvider {

    @Override
    public String getConfigNamespace() {
        // TODO Auto-generated method stub
        throw new RuntimeException(
                                    "The support for Binary Observation Stores is not yet finished. Please check back soon!" );
    }

    @Override
    public URL getConfigSchema() {
        // TODO
        throw new RuntimeException(
                                    "The support for Binary Observation Stores is not yet finished. Please check back soon!" );
    }

    @Override
    public ObservationDatastore create( URL configURL )
                            throws ResourceInitException {
        // TODO
        throw new ResourceInitException(
                                         "The support for Binary Observation Stores is not yet finished. Please check back soon!" );
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        // nothing to do
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[0];
    }
}