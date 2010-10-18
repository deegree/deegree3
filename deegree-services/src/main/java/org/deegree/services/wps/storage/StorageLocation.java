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

package org.deegree.services.wps.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wps.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a storage location and it's URL for storing a response document or an output parameter of the
 * {@link ProcessManager} as a web-accessible resource.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public abstract class StorageLocation {

    private static final Logger LOG = LoggerFactory.getLogger( StorageLocation.class );

    protected String id;

    protected File file;

    protected String mimeType;

    protected StorageLocation( File file, String id, String mimeType ) {
        this.file = file;
        this.id = id;
        this.mimeType = mimeType;
    }

    /**
     * Returns the sink for writing the resource contents.
     *
     * @return the sink for writing the resource contents
     * @throws FileNotFoundException
     */
    public OutputStream getOutputStream()
                            throws FileNotFoundException {
        return new FileOutputStream( file );
    }

    public String getId() {
        return id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public File getFile() {
        return file;
    }

    /**
     * Returns the URL that can be used to access the resource via the web.
     *
     * @return the URL that can be used to access the resource via the web
     */
    public abstract String getWebURL();

    public void sendResource ( HttpResponseBuffer response) {
        try {
            response.setContentType( getMimeType() );
            response.setContentLength( (int) getLength() );
            OutputStream os = response.getOutputStream();
            InputStream is = getInputStream();
            byte[] buffer = new byte[4096];
            int numBytes = -1;
            while ( ( numBytes = is.read( buffer ) ) != -1 ) {
                os.write( buffer, 0, numBytes );
            }
            os.flush();
        } catch ( IOException e ) {
            LOG.debug( "Error sending resource to client.", e );
        }
    }

    /**
     * @return
     */
    public long getLength() {
        return file.length();
    }

    public InputStream getInputStream()
                            throws FileNotFoundException {
        return new FileInputStream( file );
    }

    public int hashCode() {
        return file.getName().hashCode();
    }

    public boolean equals( Object o ) {
        if ( !( o instanceof StorageLocation ) ) {
            return false;
        }
        return ( (StorageLocation) o ).file.getName().equals( file.getName() );
    }
}
