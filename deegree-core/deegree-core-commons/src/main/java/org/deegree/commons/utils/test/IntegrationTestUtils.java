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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.commons.utils.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;

/**
 * <code>IntegrationTestUtils</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class IntegrationTestUtils {

    private static void collect( List<Object[]> list, File dir, String prefix ) {
        File[] fs = dir.listFiles();
        if ( fs == null ) {
            return;
        }
        for ( File f : fs ) {
            String name = f.getName();
            if ( name.endsWith( ".kvp" ) || name.endsWith( ".xml" ) ) {
                File respFile = new File( f.getParentFile(), name.substring( 0, name.length() - 4 ) + ".response" );
                Object[] o;
                try {
                    List<byte[]> responses = new ArrayList<byte[]>();
                    int idx = 1;
                    while ( respFile.exists() ) {
                        responses.add( IOUtils.toByteArray( new FileInputStream( respFile ) ) );
                        respFile = new File( f.getParentFile(), name.substring( 0, name.length() - 4 ) + ".response"
                                                                + ++idx );
                    }

                    o = new Object[] { name.endsWith( ".xml" ), IOUtils.toString( new FileInputStream( f ) ), responses, prefix + name };
                    list.add( o );
                } catch ( FileNotFoundException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if ( f.isDirectory() ) {
                collect( list, f, prefix  + f.getName() + "/" );
            }
        }
    }

    /**
     * Scans the System.getProperty("requestdir") directories' contents for .kvp/.xml request files. Responses end in
     * .response, alternative responses end in .response2 etc.
     * 
     * @return the .kvp/.xml and .response contents as triples (boolean wasXml, String and List<byte[]>)
     */
    public static Collection<Object[]> getTestRequests() {
        File dir = new File( System.getProperty( "requestdir" ) );
        List<Object[]> list = new ArrayList<Object[]>();
        collect( list, dir, "" );
        return list;
    }

    /**
     * Create Base64 encoded text of a ZIP-Archive containing the passed binary data/file
     */
    public static String toBase64Zip( byte[] data, String filename ) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ZipOutputStream zip = new ZipOutputStream( baos )) {
            zip.putNextEntry( new ZipEntry( filename ) );
            IOUtils.write( data, zip );
            zip.closeEntry();
            zip.flush();
            baos.flush();
            byte[] gzipped = baos.toByteArray();
            return StringUtils.newStringUtf8( Base64.encodeBase64Chunked( gzipped ) );
        } catch ( IOException ex ) {
            ex.printStackTrace();
            return "Encoding failed with: " + ex.getMessage();
        }
    }
}
