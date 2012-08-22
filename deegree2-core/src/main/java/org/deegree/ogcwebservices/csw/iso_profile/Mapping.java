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
package org.deegree.ogcwebservices.csw.iso_profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Node;

/**
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
public class Mapping {

    private static Properties mapping = null;
    static {
        mapping = new Properties();
        try {
            InputStream is = Mapping.class.getResourceAsStream( "mapping.properties" );
            InputStreamReader isr = new InputStreamReader( is );
            BufferedReader br = new BufferedReader( isr );
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                if ( !line.trim().startsWith( "#" ) ) {
                    String[] tmp = StringTools.toArray( line.trim(), "=", false );
                    mapping.put( tmp[0], tmp[1] );
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    /**
     * maps a property name of GetRecords, Delete and Update request from the catalogue schema to
     * the underlying WFS schema
     *
     * @param node
     * @param hlevel
     * @return the value
     * @throws XMLParsingException
     */
    public static String mapPropertyValue( Node node, String hlevel )
                            throws XMLParsingException {

        String s = XMLTools.getNodeAsString( node, ".", nsc, null );

        if ( s.startsWith( "/" ) ) {
            s = '.' + s;
        } else if ( s.startsWith( "." ) ) {
            // is this intentionally left blank?
        } else {
            s = "./" + s;
        }

        if ( "service".equals( hlevel ) ) {
            s = mapping.getProperty( "service_" + s );
        } else {
            s = mapping.getProperty( s );
        }
        return s;

    }

    /**
     * maps property names for sorting from the catalogue schema to the underlying WFS schema
     *
     * @param node
     * @param hlevel
     * @return the property
     * @throws XMLParsingException
     */
    public static String mapSortProperty( Node node, String hlevel )
                            throws XMLParsingException {
        String s = XMLTools.getNodeAsString( node, ".", nsc, null );

        if ( s.startsWith( "/" ) ) {
            s = '.' + s;
        } else if ( s.startsWith( "." ) ) {
            // again, intentionally left blank?
        } else {
            s = "./" + s;
        }
        s = StringTools.replace( s, "./", "./sortby/", false );

        if ( "service".equals( hlevel ) ) {
            s = mapping.getProperty( "service_" + s );
        } else {
            s = mapping.getProperty( s );
        }

        return s;
    }

}
