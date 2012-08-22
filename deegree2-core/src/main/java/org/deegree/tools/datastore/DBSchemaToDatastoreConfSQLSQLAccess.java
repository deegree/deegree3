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
package org.deegree.tools.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.deegree.framework.util.BootLogger;
import org.deegree.i18n.Messages;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DBSchemaToDatastoreConfSQLSQLAccess {

    private static Properties props = new Properties();

    /**
     * Initialization done at class loading time.
     */
    static {
        try {
            String fileName = "DBSchemaToDatastoreConfSQL.properties";
            InputStream is = DBSchemaToDatastoreConfSQLSQLAccess.class.getResourceAsStream( fileName );
            props.load( is );
            is.close();
        } catch ( IOException e ) {
            BootLogger.logError( "Error while initializing " + Messages.class.getName() + " : " + e.getMessage(), e );
        }
    }

    /**
     * Returns the sql statement assigned to the passed key. If no sql statement is assigned, an
     * error message will be returned that indicates the missing key.
     *
     * @see MessageFormat for conventions on string formatting and escape characters.
     *
     * @param key
     * @param arguments
     * @return the sql statement assigned to the passed key
     */
    public static String getSQLStatement( String key, Object... arguments ) {
        String s = props.getProperty( key );
        if ( s != null ) {
            return MessageFormat.format( s, arguments );
        }

        // to avoid NPEs
        return "$SQLStatement with key: " + key + " not found$";
    }
}
