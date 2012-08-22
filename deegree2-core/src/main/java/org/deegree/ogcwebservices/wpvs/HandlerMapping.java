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
package org.deegree.ogcwebservices.wpvs;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 *
 */
public class HandlerMapping {
    // private static final String BUNDLE_NAME = ;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( "org.deegree.ogcwebservices.wpvs.handler" );

    private static HashMap<String, String> configuredHandlers = null;

    private HandlerMapping() {
        // empty private constructor
    }

    /**
     * @param key
     * @return the class name
     */
    public static String getString( String key ) {
        try {
            return RESOURCE_BUNDLE.getString( key );
        } catch ( MissingResourceException e ) {
            return "!" + key + "!";
        }
    }

    /**
     * @return the configured GetViewHandlers in the bundle
     *         "org.deegree.ogcwebservices.wpvs.handler", but with the package name of the keys
     *         removed, e.g the bundle key: "WPVService.GETVIEW.BOX " will be transformed to "BOX"
     *         (for easy acces).
     */
    synchronized public static HashMap<String, String> getConfiguredGetViewHandlers() {
        if ( configuredHandlers != null )
            return configuredHandlers;
        configuredHandlers = new HashMap<String, String>();
        Enumeration<String> keys = RESOURCE_BUNDLE.getKeys();
        while ( keys.hasMoreElements() ) {
            String keyValue = keys.nextElement();
            String[] packageNames = keyValue.split( "[.]" );
            String pName = packageNames[0];
            if ( packageNames.length > 0 )
                pName = packageNames[packageNames.length - 1];
            configuredHandlers.put( pName, RESOURCE_BUNDLE.getString( keyValue ) );
        }
        return configuredHandlers;
    }
}
