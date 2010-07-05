//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.version;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides runtime access to information about the available deegree modules in the JVM.
 * <p>
 * Module information includes:
 * <ul>
 * <li>Name</li>
 * <li>Version/build information</li>
 * </ul>
 * </p>
 * <p>
 * Registration of modules is based on the {@link ServiceLoader} mechanism, i.e. it is required that each deegree module
 * is packaged in a jar and with a corresponding <code>META-INF/services/org.deegree.CoreModuleInfo</code> file.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public abstract class DeegreeModuleInfo {

    private static final Logger LOG = LoggerFactory.getLogger( DeegreeModuleInfo.class );

    private static ServiceLoader<DeegreeModuleInfo> moduleInfoLoader = ServiceLoader.load( DeegreeModuleInfo.class );

    private ModuleVersion version;

    /**
     * Returns the canonical name of the module (corresponds to the SVN module name).
     *
     * @return the canonical name
     */
    public abstract String getName();

    /**
     * Returns the version and build information of the module.
     *
     * @return the version and build information
     */
    public ModuleVersion getVersion() {
        if ( version == null ) {
            String propertiesName = "/" + getName() + "_moduleinfo.properties";
            URL buildInfoURL = this.getClass().getResource( propertiesName );
            if ( buildInfoURL == null ) {
                LOG.error( "Version/build info properties file '" + propertiesName + "' not found on classpath." );
                return null;
            }
            version = new ModuleVersion( buildInfoURL );
        }
        return version;
    }

    /**
     * Returns info about all registered modules.
     *
     * @return info about all registered modules
     */
    public static List<DeegreeModuleInfo> getRegisteredModules() {
        List<DeegreeModuleInfo> modulesInfo = new ArrayList<DeegreeModuleInfo>();
        try {
            for ( DeegreeModuleInfo moduleInfo : moduleInfoLoader ) {
                modulesInfo.add( moduleInfo );
            }
        } catch ( Exception e ) {
            LOG.error( e.getMessage(), e );
        }
        return modulesInfo;
    }

    @Override
    public String toString() {
        return getName() + ", " + getVersion();
    }
}
