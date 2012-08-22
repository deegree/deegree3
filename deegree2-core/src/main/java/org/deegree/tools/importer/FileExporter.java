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
package org.deegree.tools.importer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 *
 * The <code>FileExporter</code> writes a new file.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class FileExporter implements Exporter {

    private static final ILogger LOG = LoggerFactory.getLogger( FileExporter.class );

    /**
     * @param objectToExport
     *            contains a ListArray of objects, where the first object contains an Array of bytes
     *            to export and the second object the information of the target to export
     * @return true, when expor is successful; otherwise false
     */
    public boolean export( Object objectToExport ) {
        Boolean result = false;
        byte[] byteArray = (byte[]) ( (ArrayList<Object>) objectToExport ).get( 0 );
        String target = (String) ( (ArrayList<Object>) objectToExport ).get( 1 );
        LOG.logInfo( Messages.getString( "FileExporter.EXPORT", target ) );
        File file = new File( target );
        if ( !file.exists() ) {
            try {
                FileOutputStream fileOS = new FileOutputStream( file );
                BufferedOutputStream bufferedOS = new BufferedOutputStream( fileOS );
                bufferedOS.write( byteArray, 0, byteArray.length );
                bufferedOS.close();
                fileOS.close();
                result = true;
            } catch ( IOException e ) {
                LOG.logError( Messages.getString( "FileExporter.ERROR_WRITE_FILE", target, e.getMessage() ) );
                e.printStackTrace();
            }
        } else {
            LOG.logInfo( Messages.getString( "FileExporter.ERROR_EXPORT_FILE", target ) );
        }
        return result;
    }

}
