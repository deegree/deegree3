//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.console.observationstore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.deegree.console.XMLConfigManager;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.observation.persistence.ObservationStoreProvider;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ObservationStoreConfigManager extends XMLConfigManager<ObservationStoreConfig> {

    private final Map<String, ObservationStoreProvider> osTypeToProvider = new TreeMap<String, ObservationStoreProvider>();

    public ObservationStoreConfigManager() {
        for ( ObservationStoreProvider provider : ObservationStoreManager.getProviders().values() ) {
            String className = provider.getClass().getSimpleName();
            String name = className;
            if ( className.endsWith( "ObservationProvider" ) ) {
                name = className.substring( 0, className.length() - "ObservationProvider".length() );
            }
            osTypeToProvider.put( name, provider );
        }
    }

    @Override
    protected void add( String id, String namespace, boolean ignore ) {
        boolean active = ObservationStoreManager.get( id ) != null;
        ObservationStoreProvider provider = ObservationStoreManager.getProviders().get( namespace );
        ObservationStoreConfig config = new ObservationStoreConfig( id, active, ignore, this, provider );
        idToConfig.put( id, config );
    }

    public List<String> getOsTypes() {
        return new ArrayList<String>( osTypeToProvider.keySet() );
    }

    public ObservationStoreProvider getProvider( String osType ) {
        ObservationStoreProvider osp = osTypeToProvider.get( osType );
        System.out.println( "osp : " + osp );
        return osp;
    }

    @Override
    public String getBaseDir() {
        return "datasources/observation";
    }
}
