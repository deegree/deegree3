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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConfigurationManager {

    private static String DEFAULT_CONFIG = "/WEB-INF/conf/mdeditor/MDEDITOR_configuration.xml";

    private static Map<String, Configuration> config = new HashMap<String, Configuration>();

    /**
     * @return the configuration
     * @throws ConfigurationException
     */
    public static Configuration getConfiguration()
                            throws ConfigurationException {
        if ( FacesContext.getCurrentInstance() == null ) {
            URL resource = ConfigurationManager.class.getResource( "MDEDITOR_configuration.xml" );
            if ( resource == null ) {
                throw new ConfigurationException( "Could not create configuration" );
            }
            return ConfigurationParser.parseConfiguration( new File( resource.getPath() ) );
        }

        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();

        HttpSession session = (HttpSession) ctx.getSession( true );
        String sessionId = session.getId();

        if ( !config.containsKey( sessionId ) ) {
            // TODO: load user specific configurations
            String path = ctx.getRealPath( DEFAULT_CONFIG );
            File f = new File( path );
            if ( f.exists() && f.isFile() ) {
                config.put( sessionId, ConfigurationParser.parseConfiguration( f ) );
            } else {
                throw new ConfigurationException( "Could not create configuration" );
            }
        }
        return config.get( sessionId );
    }
}
