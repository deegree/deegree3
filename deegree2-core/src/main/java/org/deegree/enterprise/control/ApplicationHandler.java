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
package org.deegree.enterprise.control;

import static org.deegree.framework.util.CharsetUtils.getSystemCharset;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.deegree.datatypes.parameter.GeneralOperationParameterIm;
import org.deegree.datatypes.parameter.ParameterValueIm;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Handler for all web events.
 *
 * @author <a href="mailto:tfriebe@gmx.net">Torsten Friebe</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mays$
 *
 * @version $Revision$, $Date: 21.08.2008 19:19:59$
 */
public class ApplicationHandler implements WebListener {

    private static final ILogger LOG = LoggerFactory.getLogger( ApplicationHandler.class );

    private static final HashMap<String, Class<?>> handler = new HashMap<String, Class<?>>();

    private static final HashMap<String, String> handlerNext = new HashMap<String, String>();

    private static final HashMap<String, String> handlerANext = new HashMap<String, String>();

    private static final HashMap<String, List<ParameterValueIm>> handlerParam = new HashMap<String, List<ParameterValueIm>>();

    private static final String EVENT = "event";

    private static final String NAME = "name";

    private static final String CLASS = "class";

    private static final String NEXT = "next";

    private static final String ALTERNATIVENEXT = "alternativeNext";

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
     * @param e
     *            the action event generated out of the incoming http POST event.
     */
    public void actionPerformed( FormEvent e ) {
        Object source = e.getSource();

        if ( source instanceof HttpServletRequest ) {
            HttpServletRequest request = (HttpServletRequest) source;

            String actionName = request.getParameter( "action" );
            LOG.logDebug( "Actionname: " + actionName );
            if ( actionName != null ) {
                // handle simple KVP encoded request
                try {
                    if ( "version".equalsIgnoreCase( actionName ) ) {
                        this.showVersion( request );
                    } else {
                        try {
                            this.delegateToHelper( actionName, e );
                        } catch ( Exception ex ) {
                            ex.printStackTrace();
                            LOG.logError( "Action " + actionName + " is unknown!" );
                        }
                    }
                } catch ( Exception ex ) {
                    request.setAttribute( "next", "error.jsp" );
                    request.setAttribute( "javax.servlet.jsp.jspException", ex );
                }
            } else {
                // handle RPC encoded request
                try {
                    RPCMethodCall mc = getMethodCall( request );
                    e = new RPCWebEvent( e, mc );
                    this.delegateToHelper( mc.getMethodName(), e );
                } catch ( RPCException re ) {
                    re.printStackTrace();
                    request.setAttribute( "next", "error.jsp" );
                    request.setAttribute( "javax.servlet.jsp.jspException", re );
                } catch ( Exception ee ) {
                    ee.printStackTrace();
                    request.setAttribute( "next", "error.jsp" );
                    request.setAttribute( "javax.servlet.jsp.jspException", ee );
                }
            }
        }
    }

    /**
     * extracts the RPC method call from the
     *
     * @param request
     * @return the RPCMethodCall
     * @throws RPCException
     */
    private RPCMethodCall getMethodCall( ServletRequest request )
                            throws RPCException {

        String s = request.getParameter( "rpc" );

        try {
            if ( s == null ) {
                StringBuffer sb = new StringBuffer( 1000 );
                try {
                    BufferedReader br = request.getReader();
                    String line = null;
                    while ( ( line = br.readLine() ) != null ) {
                        sb.append( line );
                    }
                    br.close();
                } catch ( Exception e ) {
                    throw new RPCException( "Error reading stream from servlet\n" + e.toString() );
                }

                s = sb.toString();
                LOG.logDebug( "found first (perhaps double) encoded String: " + s );
                s = URLDecoder.decode( s, getSystemCharset() );
                String[] splitter = s.split( " \t<>" );
                if ( splitter.length == 1 ) {
                    s = URLDecoder.decode( s, getSystemCharset() );
                    LOG.logDebug( "Decoding a second time: " + s );
                }

                int pos1 = s.indexOf( "<methodCall>" );
                int pos2 = s.indexOf( "</methodCall>" );
                if ( pos1 < 0 ) {
                    throw new RPCException( "request doesn't contain a RPC methodCall" );
                }
                s = s.substring( pos1, pos2 + 13 );
            } else {
               s = URLDecoder.decode( s, Charset.defaultCharset().displayName() );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new RPCException( e.toString() );
        }
        LOG.logDebug( "RPC: " + s );

        return RPCFactory.createRPCMethodCall( new StringReader( s ) );

    }

    /**
     *
     *
     * @param action
     * @param e
     *
     * @throws Exception
     */
    protected void delegateToHelper( String action, FormEvent e )
                            throws Exception {
        action = action.trim();
        Class<?> cls = ApplicationHandler.handler.get( action );
        AbstractListener helper = (AbstractListener) cls.newInstance();
        helper.setNextPage( handlerNext.get( action ) );
        helper.setDefaultNextPage( handlerNext.get( action ) );
        helper.setAlternativeNextPage( handlerANext.get( action ) );
        helper.setInitParameterList( handlerParam.get( action ) );
        helper.handle( e );
    }

    /**
     *
     *
     * @param request
     */
    protected void showVersion( ServletRequest request ) {
        request.setAttribute( "next", "snoopy.jsp" );
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
                            throws IOException, MalformedURLException, SAXException {
        LOG.logInfo( "Reading event handler configuration file:" + configFile );
        /*
         * Read resource into Document...
         */
        URL url = new File( configFile ).toURL();
        Reader reader = new InputStreamReader( url.openStream() );
        Document doc = XMLTools.parse( reader );
        /*
         * Read and create page elements
         */
        NodeList nodes = doc.getElementsByTagName( EVENT );

        for ( int i = 0; i < nodes.getLength(); i++ ) {
            String name = XMLTools.getAttrValue( nodes.item( i ), null, NAME, null );
            String cls = XMLTools.getAttrValue( nodes.item( i ), null, CLASS, null );
            String nextPage = XMLTools.getAttrValue( nodes.item( i ), null, NEXT, null );
            String anextPage = XMLTools.getAttrValue( nodes.item( i ), null, ALTERNATIVENEXT, null );

            if ( anextPage == null ) {
                anextPage = nextPage;
            }

            Class<?> clscls = null;
            try {
                clscls = Class.forName( cls );
                handler.put( name.trim(), clscls );
                handlerNext.put( name.trim(), nextPage );
                handlerANext.put( name.trim(), anextPage );
                List<ParameterValueIm> pvList = parseParameters( nodes.item( i ) );
                handlerParam.put( name.trim(), pvList );
                LOG.logInfo( "Handler '" + clscls + "' bound to event '" + name + "'" );
            } catch ( Exception ex ) {
                ex.printStackTrace();
                LOG.logError( "No handler '" + cls + "' specified for event '" + name + "'", ex );
                throw new SAXException( "No handler class specified for event:" + name + " " + cls + "\n" + ex );
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
