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
package org.deegree.commons.utils;

import static java.lang.System.getProperty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>ConfigManager</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ConfigManager {

    private static File HOMEDIR;

    static {
        HOMEDIR = new File( getProperty( "user.home" ), ".deegree" );
        if ( !HOMEDIR.exists() ) {
            HOMEDIR.mkdirs();
        }
    }

    /**
     * @param path
     * @return an input stream reading from the file
     * @throws FileNotFoundException
     */
    public static InputStream getInputResource( String path )
                            throws FileNotFoundException {
        return new BufferedInputStream( new FileInputStream( new File( HOMEDIR, path ) ) );
    }

    /**
     * @param path
     * @param append
     * @return an output stream writing to the file
     * @throws FileNotFoundException
     */
    public static OutputStream getOutputResource( String path, boolean append )
                            throws FileNotFoundException {
        return new BufferedOutputStream( new FileOutputStream( new File( HOMEDIR, path ), append ) );
    }

}
