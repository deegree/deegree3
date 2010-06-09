//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.mdeditor.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class Parser {

    private static final Logger LOG = getLogger( Parser.class );

    protected static final String NS = "http://www.deegree.org/igeoportal";

    private List<String> idList = new ArrayList<String>();

    protected String getId( XMLStreamReader xmlStream )
                            throws ConfigurationException {
        String id = xmlStream.getAttributeValue( null, "id" );
        if ( id == null || id.length() == 0 ) {
            throw new ConfigurationException( "missing id" );
        }
        if ( idList.contains( id ) ) {
            throw new ConfigurationException( "An element with id " + id
                                              + " exists! Ids must be unique in the complete configuration." );
        } else {
            idList.add( id );
        }
        return id;
    }

    protected static String getElementText( XMLStreamReader xmlStream, String name, String defaultValue )
                            throws XMLStreamException {
        String s = defaultValue;
        if ( name != null && name.equals( xmlStream.getLocalName() ) ) {
            s = xmlStream.getElementText();
            xmlStream.nextTag();
        }
        return s;
    }

    public static URL resolve( String url, XMLStreamReader in )
                            throws MalformedURLException {
        String systemId = in.getLocation().getSystemId();
        if ( systemId == null ) {
            LOG.warn( "SystemID was null, cannot resolve '{}', trying to use it as absolute URL.", url );
            return new URL( url );
        }

        LOG.debug( "Resolving URL '" + url + "' against SystemID '" + systemId + "'." );

        // check if url is an absolute path
        File file = new File( url );
        if ( file.isAbsolute() ) {
            return file.toURI().toURL();
        }

        URL resolvedURL = new URL( new URL( systemId ), url );
        LOG.debug( "-> resolvedURL: '" + resolvedURL + "'" );
        return resolvedURL;
    }

}
