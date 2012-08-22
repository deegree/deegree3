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
package org.deegree.tools.wms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.xml.sax.SAXException;

/**
 * program for creating a Web Map Context document from a WMS capabilities file
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class WMS2WMC {

    private URL xsl = WMS2WMC.class.getResource( "wms2wmc.xsl" );

    private String inFile;

    private String outFile;

    private String wmsCapabilities;

    /**
     *
     * @param inFile
     * @param outFile
     */
    public WMS2WMC( String inFile, String outFile, String wmsCapabilities ) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.wmsCapabilities = wmsCapabilities;
    }

    /**
     * does all the work
     *
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */
    public void run()
                            throws MalformedURLException, IOException, SAXException, TransformerException {
        // TODO
        // consider already existing WMS capabilities

        File in = new File( inFile );
        XMLFragment xml = new XMLFragment( in.toURL() );
        XSLTDocument xslt = new XSLTDocument( xsl );
        xml = xslt.transform( xml );

        FileOutputStream fos = new FileOutputStream( new File( outFile ) );
        xml.write( fos );
        fos.close();
    }

    private static void validate( Properties map )
                            throws Exception {
        if ( map.get( "-inFile" ) == null ) {
            throw new Exception( "-inFile parameter must be set" );
        }
        if ( map.get( "-outFile" ) == null ) {
            throw new Exception( "-outFile parameter must be set" );
        }
        // TODO
        // validate -WMSCapabilities target if parameter is available
    }

    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            System.out.println( args[i + 1] );
            map.put( args[i], args[i + 1] );
        }
        try {
            validate( map );
        } catch ( Exception e ) {
            System.out.println( "!!! E R R O R !!!" );
            System.out.println( e.getMessage() );
            return;
        }

        WMS2WMC wms2wmc = new WMS2WMC( map.getProperty( "-inFile" ), map.getProperty( "-outFile" ),
                                       map.getProperty( "-WMSCapabilities" ) );
        wms2wmc.run();

    }

}
