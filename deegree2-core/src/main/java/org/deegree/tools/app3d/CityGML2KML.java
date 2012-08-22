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
package org.deegree.tools.app3d;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;

/**
 * Utility program for converting a CityGML documents in KML. At the moment just Buildings are supported.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class CityGML2KML {

    private static URL xslt = CityGML2KML.class.getResource( "citygml2kml.xsl" );

    /**
     * @param args
     *            may be -sourceFile, -outFile, -sourceCRS
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }

        if ( map.getProperty( "-sourceFile" ) == null ) {
            System.out.println( "parameter -sourceFile must be set" );
            return;
        }
        if ( map.getProperty( "-outFile" ) == null ) {
            System.out.println( "parameter -outFile must be set" );
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        if ( map.get( "-sourceCRS" ) != null ) {
            params.put( "SRCCRS", map.getProperty( "-sourceCRS" ) );
        } else {
            System.out.println( "use default source CRS (-sourceCRS EPSG:31467)" );
        }

        XSLTDocument outXSLSheet = new XSLTDocument();
        outXSLSheet.load( xslt );

        XMLFragment doc = new XMLFragment();
        doc.load( new File( map.getProperty( "-sourceFile" ) ).toURL() );

        XMLFragment resultDocument = outXSLSheet.transform( doc, XMLFragment.DEFAULT_URL, null, params );

        FileOutputStream fos = new FileOutputStream( map.getProperty( "-outFile" ) );
        resultDocument.write( fos );
        fos.close();
    }

}
