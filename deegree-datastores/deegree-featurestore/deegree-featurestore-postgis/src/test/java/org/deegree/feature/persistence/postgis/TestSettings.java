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
package org.deegree.feature.persistence.postgis;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.deegree.CoreTstProperties;
import org.deegree.commons.config.DeegreeWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSettings {

    private static Logger LOG = LoggerFactory.getLogger( CoreTstProperties.class );

    private static final Properties props = new Properties();

    static {
        File file = new File( DeegreeWorkspace.getWorkspaceRoot(), ".testsettings" + File.separatorChar
                                                                   + "deegree-featurestore-postgis.properties" );
        if ( file.exists() ) {
            LOG.info( "Using test properties from file {}.", file );
            try {
                props.load( new FileReader( file ) );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        } else {
            LOG.info( "File {} does not exist. Some tests will be skipped.", file );
        }
    }

    /**
     * Returns the property with the given name.
     * 
     * @param key
     *            name of the property
     * @return the property with the given name, or <code>null</code> if it is not available
     */
    public static String getProperty( String key ) {
        return props.getProperty( key );
    }
}