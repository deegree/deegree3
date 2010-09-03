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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StorageLocation} for process outputs.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class OutputStorage extends StorageLocation {

    private static final Logger LOG = LoggerFactory.getLogger( OutputStorage.class );

    OutputStorage( File file, String id, String mimeType ) throws IOException {
        super( file, id, mimeType );
        storeMimeType();
    }

    OutputStorage( File file, String id ) throws IOException {
        super( file, id, null );
        this.mimeType = retrieveMimeType();
    }

    public String getWebURL() {
        String url = OGCFrontController.getHttpGetURL() + "service=WPS&version=1.0.0&request=GetOutput&identifier="
                     + id;
        return url;
    }

    private void storeMimeType()
                            throws IOException {
        File mimeInfoFile = new File( file.getPath() + ".mimeinfo" );
        LOG.debug( "Storing output mime type ('" + mimeType + "') in file '" + mimeInfoFile + "'" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( mimeInfoFile ) );
        if ( mimeType == null ) {
            LOG.warn( "No mimetype specified!? defaulting to text/xml..." );
            mimeType = "text/xml";
        }
        writer.write( mimeType );
        writer.close();
    }

    private String retrieveMimeType() {
        String mimeType = null;
        File mimeInfoFile = new File( file.getPath() + ".mimeinfo" );
        LOG.debug( "Retrieving output mime type from file '" + mimeInfoFile + "'" );
        BufferedReader reader;
        try {
            reader = new BufferedReader( new FileReader( mimeInfoFile ) );
            mimeType = reader.readLine();
            reader.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        LOG.debug( "mimeType: " + mimeType );
        return mimeType;
    }
}
