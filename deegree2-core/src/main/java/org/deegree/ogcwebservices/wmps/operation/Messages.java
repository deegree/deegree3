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
package org.deegree.ogcwebservices.wmps.operation;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Messages class provides the functionality to read response, exception messages from a properties
 * file.
 *
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class Messages {

    private static final String BUNDLE_NAME = "org.deegree.ogcwebservices.wmps.operation.messages";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

    /**
     * Create a new Messages instance.
     */
    private Messages() {
    }

    /**
     * Get Message
     *
     * @param key
     * @return String
     */
    public static String getString( String key ) {
        try {
            return RESOURCE_BUNDLE.getString( key );
        } catch ( MissingResourceException e ) {
            return '!' + key + '!';
        }
    }

    /**
     * Get Message along with the appropriate replacement for the object.
     *
     * @param key
     * @param arg0
     * @return String
     */
    public static String format( String key, Object arg0 ) {
        return format( key, new Object[] { arg0 } );
    }

    /**
     * Get Message along with the appropriate replacement for the objects.
     *
     * @param key
     * @param arg0
     * @param arg1
     * @return String
     */
    public static String format( String key, Object arg0, Object arg1 ) {
        return format( key, new Object[] { arg0, arg1 } );
    }

    /**
     * Get Message along with the appropriate replacement for the objects.
     *
     * @param key
     * @param arg0
     * @param arg1
     * @param arg2
     * @return String
     */
    public static String format( String key, Object arg0, Object arg1, Object arg2 ) {
        return format( key, new Object[] { arg0, arg1, arg2 } );
    }

    /**
     * Get Message along with the appropriate replacement for the object(s).
     *
     * @param key
     * @param arguments
     * @return String
     */
    public static String format( String key, Object[] arguments ) {
        return MessageFormat.format( getString( key ), arguments );
    }
}
