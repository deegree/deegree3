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

package org.deegree.services.controller.wps.storage;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides storage locations for response documents and outputs of processes that can be published as web-accessible
 * resources.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class StorageManager {

    private static final Logger LOG = LoggerFactory.getLogger( StorageManager.class );

    private File baseDir;

    private long LAST_OUTPUT_ID = System.currentTimeMillis();

    private long LAST_RESPONSE_ID = System.currentTimeMillis();

    private static final String OUTPUT_PREFIX = "wps_output_";

    private static final String RESPONSE_PREFIX = "wps_response_";

    /**
     * Creates a new {@link StorageManager} instance.
     * 
     * @param baseDir
     *            base directory where the resources are stored on the filesystem
     */
    public StorageManager( File baseDir ) {
        LOG.info( "Using directory '" + baseDir + "' for publishing complex outputs and response documents." );
        if ( !baseDir.exists() ) {
            LOG.error( "Configured WPS storage directory name '" + baseDir
                       + "' does not exist. Please create this directory or adapt the WPS configuration." );
        }
        if ( !baseDir.isDirectory() ) {
            LOG.error( "Configured WPS resource directory name '" + baseDir
                       + "' is not a directory. Please create this directory or adapt the WPS configuration." );
        }
        this.baseDir = baseDir;
    }

    public synchronized OutputStorage allocateOutputStorage( String mimeType )
                            throws IOException {
        LOG.debug( "Allocating new storage location for publishing output parameter." );
        String outputId = generateOutputId();
        String resourceName = OUTPUT_PREFIX + outputId;
        File resourceFile = new File( baseDir, resourceName );
        if ( resourceFile.exists() ) {
            LOG.debug( "File '" + resourceFile + "' already exists. Deleting it." );
            resourceFile.delete();
        }
        return new OutputStorage( resourceFile, outputId, mimeType );
    }

    public ResponseDocumentStorage allocateResponseDocumentStorage()
                            throws IOException {
        LOG.debug( "Allocating new storage location for publishing response document." );
        String responseId = generateResponseId();
        String resourceName = RESPONSE_PREFIX + responseId;
        File resourceFile = new File( baseDir, resourceName );
        if ( resourceFile.exists() ) {
            LOG.debug( "File '" + resourceFile + "' already exists. Deleting it." );
            resourceFile.delete();
        }
        return new ResponseDocumentStorage( resourceFile, responseId );
    }

    public OutputStorage lookupOutputStorage( String outputId ) {
        OutputStorage output = null;
        File resourceFile = new File( baseDir, OUTPUT_PREFIX + outputId );
        try {
            if ( resourceFile.exists() ) {
                output = new OutputStorage( resourceFile, outputId );
            }
        } catch ( IOException e ) {
            LOG.debug( "Cannot access stored output (file='" + resourceFile + "')" );
        }
        return output;
    }

    public ResponseDocumentStorage getResponseDocumentStorage( String responseId ) {
        File resourceFile = new File( baseDir, RESPONSE_PREFIX + responseId );
        return new ResponseDocumentStorage( resourceFile, responseId );
    }

    private synchronized String generateOutputId() {
        return Long.toHexString( LAST_OUTPUT_ID++ );
    }

    private synchronized String generateResponseId() {
        return Long.toHexString( LAST_RESPONSE_ID++ );
    }
}
