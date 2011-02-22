//$HeadURL$
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
package org.deegree.feature.persistence.osm;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.WorkspaceInitializationException;
import org.deegree.feature.persistence.FeatureStoreProvider;

/**
 * {@link FeatureStoreProvider} for the {@link OSMFeatureStore}.
 * 
 * @author <a href="mailto:goerke@lat-lon.de">Sebastian Goerke</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OSMFeatureStoreProvider implements FeatureStoreProvider<OSMFeatureStore> {

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/osm";

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return null;
    }

    @Override
    public Map<String, URL> getConfigTemplates() {
        return new HashMap<String, URL>();
    }

    @Override
    public OSMFeatureStore create( URL configURL )
                            throws WorkspaceInitializationException {
        try {
            return new OSMFeatureStore();
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void init( DeegreeWorkspace workspace ) {
        // this.workspace = workspace;
    }
}