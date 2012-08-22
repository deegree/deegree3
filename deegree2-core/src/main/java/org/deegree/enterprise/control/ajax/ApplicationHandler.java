//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.enterprise.control.ajax;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.deegree.datatypes.parameter.GeneralOperationParameterIm;
import org.deegree.datatypes.parameter.ParameterValueIm;
import org.deegree.enterprise.servlet.ServletRequestWrapper;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Handler for all web events.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * 
 * @version $Revision$, $Date$
 */
public class ApplicationHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( ApplicationHandler.class );

    private static final HashMap<String, Class<?>> handler = new HashMap<String, Class<?>>();

    private static final HashMap<String, XSLTDocument> xsl = new HashMap<String, XSLTDocument>();

    private static final HashMap<String, List<ParameterValueIm>> handlerParam = new HashMap<String, List<ParameterValueIm>>();

    private static final HashMap<String, String> nextPages = new HashMap<String, String>();

    private static final HashMap<String, String> beans = new HashMap<String, String>();

    private static final String EVENT = "event";

    private static final String NAME = "name";

    private static final String CLASS = "class";

    private static final String XSLT = "xslt";

    private static final String NEXT = "next";

    private static final String BEAN = "bean";

    /**
     * Creates a new ApplicationHandler object.
     * 
     * @param configFile
     * @throws Exception
     */
    public ApplicationHandler( String configFile ) throws Exception {
        ApplicationHandler.initHandler( configFile );
    }

    /**
     * Handles all web action events. Calls the specified listener using the mapping defined in control.xml file.
     * 
     * @param servletContext
     * @param request
     * @param responseHandler
     * @throws ServletException
     */
    public void actionPerformed( ServletContext servletContext, HttpServletRequest request,
                                 ResponseHandler responseHandler )
                            throws ServletException {

        WebEvent event = null;
        String actionName = request.getParameter( "action" );
        if ( actionName != null && actionName.trim().length() > 0 ) {
            event = new WebEvent( servletContext, new ServletRequestWrapper( request ), beans.get( actionName ) );
            // handle HTTP GET which is assumed to be KVP encoded
            LOG.logDebug( "KVP encoded Actionname: " + actionName );
        } else {
            // handle HTTP POST which is assumed to be JSON encoded
            event = new JSONEvent( servletContext, request );
            Map ps = event.getParameter();
            if ( ps != null ) {
                actionName = (String) ps.get( "className" );
                if ( actionName == null ) {
                    actionName = (String) ps.get( "action" );
                }
            }
            ( (JSONEvent) event ).setBean( beans.get( actionName ) );
            LOG.logDebug( "HTTP POST/JSON action/class name: " + actionName );
        }
        try {
            this.delegateToHelper( actionName, event, responseHandler );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new ServletException( "no handler available for action: " + actionName, e );
        }

    }

    /**
     * 
     * @param action
     * @param event
     * @param responseHandler
     * @throws Exception
     */
    protected void delegateToHelper( String action, WebEvent event, ResponseHandler responseHandler )
                            throws Exception {
        action = action.trim();
        Class<?> cls = ApplicationHandler.handler.get( action );
        AbstractListener helper = (AbstractListener) cls.newInstance();
        helper.setInitParameterList( handlerParam.get( action ) );
        helper.setNextPage( nextPages.get( action ) );
        helper.handle( event, responseHandler );
    }

    /**
     * 
     * 
     * @param configFile
     * 
     * @throws IOException
     * @throws MalformedURLException
     * @throws SAXException
     */
    private static void initHandler( String configFile )
                            throws ServletException {
        LOG.logInfo( "Reading event handler configuration file:" + configFile );
        // Read resource into Document...
        XMLFragment xml;
        try {
            xml = new XMLFragment( new File( configFile ).toURI().toURL() );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new ServletException( e );
        }

        List<Node> nodes;
        try {
            nodes = XMLTools.getNodes( xml.getRootElement(), EVENT, CommonNamespaces.getNamespaceContext() );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new ServletException( e );
        }
        for ( Node node : nodes ) {

            String name = XMLTools.getAttrValue( node, null, NAME, null );
            String cls = XMLTools.getAttrValue( node, null, CLASS, null );
            String nextPage = XMLTools.getAttrValue( node, null, NEXT, null );
            String bean = XMLTools.getAttrValue( node, null, BEAN, null );
            nextPages.put( name.trim(), nextPage );
            beans.put( name.trim(), bean );
            String xslt = XMLTools.getAttrValue( node, null, XSLT, null );
            if ( xslt != null ) {
                XSLTDocument xsltDoc;
                try {
                    xsltDoc = new XSLTDocument( xml.resolve( xslt ) );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new ServletException( e );
                }
                xsl.put( name.trim(), xsltDoc );
            }

            Class<?> clscls = null;
            try {
                clscls = Class.forName( cls );
                handler.put( name.trim(), clscls );
                List<ParameterValueIm> pvList = parseParameters( node );
                handlerParam.put( name.trim(), pvList );
                LOG.logInfo( "Handler '" + clscls + "' bound to event '" + name + "'" );
            } catch ( Exception ex ) {
                LOG.logError( "No handler '" + cls + "' specified for event '" + name + "'", ex );
                throw new ServletException( "No handler class specified for event:" + name + " " + cls, ex );
            }
        }
    }

    /**
     * several parameters can be passed to each Listener by adding
     * 
     * <pre>
     *   &lt;parameter&gt;
     *       &lt;name&gt;aName&lt;/name&gt;
     *       &lt;value&gt;aValue&lt;/value&gt;
     *   &lt;/parameter&gt;
     * </pre>
     * 
     * sections to the corresponding &lt;event&gt; element.
     * 
     * @param node
     * @return a List of ParameterValueIm
     * @throws XMLParsingException
     */
    private static List<ParameterValueIm> parseParameters( Node node )
                            throws XMLParsingException {

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        List<Node> nodes = XMLTools.getNodes( node, "parameter", nsc );
        List<ParameterValueIm> pvs = new ArrayList<ParameterValueIm>();
        for ( int i = 0; i < nodes.size(); i++ ) {
            Element element = (Element) nodes.get( i );
            String name = XMLTools.getRequiredNodeAsString( element, "name", nsc );
            String value = XMLTools.getRequiredNodeAsString( element, "value", nsc );
            GeneralOperationParameterIm descriptor = new GeneralOperationParameterIm( name, null, 1, 1 );
            pvs.add( new ParameterValueIm( descriptor, value ) );
        }

        return pvs;
    }

}
