//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/model/feature/GMLFeatureCollectionDocumentTest.java $
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
package org.deegree.model.feature;

import static java.io.File.createTempFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.xml.sax.SAXException;

import alltests.Configuration;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version. $Revision: 21354 $, $Date: 2009-12-10 09:02:09 +0100 (Do, 10 Dez 2009) $
 */
public class GMLFeatureCollectionDocumentTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( GMLFeatureCollectionDocumentTest.class );

    private static String FILE_OUT = "wfs/output/GMLFeatureCollectionTest_Output.xml";

    /**
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     * @throws FeatureException
     */
    public void testParsing()
                            throws IOException, SAXException, XMLParsingException, FeatureException {

        InputStream in = new URL(
                                  "http://www.iai.fzk.de:80/www-extern-kit/fileadmin/download/download-geoinf/BP2070/BP2070_2_0.zip" ).openStream();
        File tmp = createTempFile( "gmlziptest", null );
        tmp.deleteOnExit();
        OutputStream out = new FileOutputStream( tmp );

        int read;
        byte[] buf = new byte[16384];
        while ( ( read = in.read( buf ) ) != -1 ) {
            out.write( buf, 0, read );
        }
        in.close();
        out.close();

        ZipFile zip = new ZipFile( tmp );

        ZipEntry entry = zip.entries().nextElement();
        assertNotNull( entry );

        in = zip.getInputStream( entry );

        GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument( false );
        doc.load( in, "http://www.systemid.org" );
        FeatureCollection fc = doc.parse();
        URL outputURL = new URL( Configuration.getResourceDir(), FILE_OUT );
        new GMLFeatureAdapter().export( fc, new FileOutputStream( outputURL.getFile() ) );

        LOG.logInfo( "Wrote '" + outputURL.getFile() + "'." );
    }

}
