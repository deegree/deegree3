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
package org.deegree.io;

import java.net.MalformedURLException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Element;

/**
 * This class provides method for reading IO configuration elements that are common to several
 * services/applications.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class IODocument extends XMLFragment {

    private static ILogger LOG = LoggerFactory.getLogger( IODocument.class );

    private static final long serialVersionUID = -8166238044553562735L;

    /**
     * @param element
     */
    public IODocument( Element element ) {
        super( element );
    }

    /**
     * parses a JDBCConnection element and returns the object representation
     *
     * @return a jdbc connection object
     * @throws XMLParsingException
     */
    public JDBCConnection parseJDBCConnection()
                            throws XMLParsingException {
        JDBCConnection connection = null;

        Element connectionElement = this.getRootElement();
        if ( connectionElement != null ) {
            String driver = XMLTools.getRequiredNodeAsString( connectionElement, "dgjdbc:Driver/text()", nsContext );
            String logon = XMLTools.getRequiredNodeAsString( connectionElement, "dgjdbc:Url/text()", nsContext );
            // following code handles the relative paths in the configuration of a hsql datatore,
            // which was configured with the semi variable ${docRoot}, which actually is a
            // stringconstant :-)
            if ( logon.contains( "hsqldb" ) && logon.contains( "${docRoot}" ) ) {
                String filename = logon.substring( logon.lastIndexOf( "${docRoot}" ) + "${docRoot}".length() );
                int firstSlash = filename.indexOf( '/' );
                if ( firstSlash != -1 && firstSlash == 0 ) {
                    filename = filename.substring( 1 );
                }
                LOG.logDebug( "found filename: " + filename );
                String extension = ".script";
                try {
                    filename = resolve( filename + extension ).toExternalForm();
                } catch ( MalformedURLException e ) {
                    e.printStackTrace();
                }
                filename = filename.substring( 0, filename.indexOf( extension ) );
                LOG.logDebug( "resolved filename: " + filename );
                logon = "jdbc:hsqldb:" + filename;
            }

            String user = XMLTools.getNodeAsString( connectionElement, "dgjdbc:User/text()", nsContext, null );
            String password = XMLTools.getNodeAsString( connectionElement, "dgjdbc:Password/text()", nsContext, null );
            String securityConstraints = XMLTools.getNodeAsString( connectionElement,
                                                                   "dgjdbc:SecurityConstraints/text()", nsContext, null );
            String encoding = XMLTools.getNodeAsString( connectionElement, "dgjdbc:Encoding/text()", nsContext,
                                                        CharsetUtils.getSystemCharset() );
            String aliasPrefix = XMLTools.getNodeAsString( connectionElement, "dgjdbc:AliasPrefix/text()", nsContext,
                                                           null );
            String sdeDatabase = XMLTools.getNodeAsString( connectionElement, "dgjdbc:SDEDatabase/text()", nsContext,
                                                           null );
            String sdeVersion = XMLTools.getNodeAsString( connectionElement, "dgjdbc:SDEVersion/text()", nsContext,
                                                          null );
            connection = new JDBCConnection( driver, logon, user, password, securityConstraints, encoding, aliasPrefix,
                                             sdeDatabase, sdeVersion );
        }
        return connection;
    }

}
