//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-client/deegree-jsf-console/src/main/java/org/deegree/client/generic/EditBean.java $
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
package org.deegree.console.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.client.core.model.ConfigurationXML;
import org.deegree.client.core.utils.FacesUtils;
import org.deegree.client.core.utils.MessageUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 29926 $, $Date: 2011-03-08 11:47:59 +0100 (Di, 08. Mär 2011) $
 */
@ManagedBean
@SessionScoped
public class EditBean implements Serializable {

    private static final long serialVersionUID = -6241447818339781779L;

    private static final Logger LOG = getLogger( EditBean.class );

    private Map<String, ConfigurationXML> map = new HashMap<String, ConfigurationXML>();

    public void openGUI() {
        ExternalContext elContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = elContext.getRequestParameterMap();

        String type = requestParameterMap.get( "type" );
        String schemaURLS = null;
        String xml = null;
        String id = requestParameterMap.get( "id" );
        LOG.debug( "openGUI: " + type + " " + id );
        if ( "jdbc".equalsIgnoreCase( type ) ) {
            schemaURLS = "http://schemas.deegree.org/jdbc/0.5.0/jdbc.xsd";
            File workspace = OGCFrontController.getServiceWorkspace().getLocation();
            File xmlFile = new File( workspace, "jdbc/postgis.xml" );
            XMLAdapter adapter = new XMLAdapter( xmlFile );
            xml = adapter.toString();
        } else if ( "fs".equalsIgnoreCase( type ) ) {
            schemaURLS = "http://schemas.deegree.org/jdbc/0.5.0/jdbc.xsd";
            File xmlFile = new File( FacesUtils.getAbsolutePath( null, "inspire-postgis.xml" ) );
            XMLAdapter adapter = new XMLAdapter( xmlFile );
            xml = adapter.toString();
        }
        if ( !map.containsKey( id ) ) {
            map.put( id, new ConfigurationXML( xml, schemaURLS, type ) );
        }
    }

    public void closeGUI() {
        ExternalContext elContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = elContext.getRequestParameterMap();

        String id = requestParameterMap.get( "id" );
        LOG.debug( "closeGUI: " + id );
        map.remove( id );
    }

    public void saveXMLAndCloseGUI() {
        ExternalContext elContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = elContext.getRequestParameterMap();
        String id = requestParameterMap.get( "id" );

        LOG.debug( "saveAndCloseGUI: " + id );
        ConfigurationXML editor = map.get( id );
        if ( editor != null ) {
            String type = editor.getType();
            String xml = editor.getXml();
            File xmlFile = null;
            File workspace = OGCFrontController.getServiceWorkspace().getLocation();
            if ( "jdbc".equalsIgnoreCase( type ) ) {
                xmlFile = new File( workspace, "jdbc/postgis.xml" );
            } else if ( "fs".equalsIgnoreCase( type ) ) {
                xmlFile = new File( FacesUtils.getAbsolutePath( null, "inspire-postgis.xml" ) );
            }
            if ( xmlFile != null && xml != null ) {
                try {
                    FileWriter writer = new FileWriter( xmlFile );
                    writer.write( xml );
                    writer.close();
                } catch ( IOException e ) {
                    FacesMessage msg = MessageUtils.getFacesMessage( FacesMessage.SEVERITY_ERROR,
                                                                     "org.deegree.guiEditBean.WRITE_FILE_FAILED",
                                                                     xmlFile.getName(), e.getMessage() );
                    FacesContext.getCurrentInstance().addMessage( null, msg );
                }
                LOG.debug( "Created file " + xmlFile.toString() );
            }
            map.remove( id );
        }
    }

    public Map<String, ConfigurationXML> getMap() {
        return map;
    }
}
