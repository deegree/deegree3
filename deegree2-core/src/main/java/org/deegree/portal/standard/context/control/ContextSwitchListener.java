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

package org.deegree.portal.standard.context.control;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCUtils;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.i18n.Messages;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.Constants;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.portal.context.XMLFactory;
import org.w3c.dom.Node;

/**
 * This class handles switch of map contexts. The basic logic is (1) receiving an rpc request with a context name (xml)
 * and, possibly, a bounding box, (2) transforming this xml using a provided xsl, and (3) forwarding the result back to
 * the browser. <br/>
 * Most of the action takes place in <code>doTransformContext</code>, and is delegated to the
 * <code>ContextTransformer</code>.<br/>
 * In order to perform the transformation from a context xml to a html, a xslt is provided. This is per default called
 * <code>context2HTML.xsl</code> (see the class member <code>DEFAULT_CTXT2HTML</code>) and should be put under
 * <code>${context-home}/WEB-INF/xml/</code>. <br/>
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ContextSwitchListener extends AbstractContextListener {

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private static final ILogger LOG = LoggerFactory.getLogger( ContextSwitchListener.class );

    /**
     * A <code>String</code> used as a key value for the new html (of the client). This key is used in the JSP which
     * output the new(-ly transformed html.
     */
    public static final String NEW_CONTEXT_HTML = "NEW_CONTEXT_HTML";

    /**
     * A <code>String</code> defining the name of the xsl file that defines the transformation from a context to html.
     * This must be placed, together with the map context xml and helper xsl files, under
     * <code>${context-home}/WEB-INF/conf/igeoportal/</code>.
     */
    protected URL ctxt2html;

    /**
     * script to transform a standard WMC document into a deegree WMC
     */
    protected static final String WEBMAPCTXT2HTML = "WEB-INF/conf/igeoportal/defaultcontext.xsl";

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCMethodCall mc = ( (RPCWebEvent) event ).getRPCMethodCall();
        RPCParameter[] pars = mc.getParameters();
        RPCStruct struct = (RPCStruct) pars[0].getValue();

        // get map context value
        String curContxt = RPCUtils.getRpcPropertyAsString( struct, "mapContext" );

        // now get bbox
        Envelope bbox = null;
        RPCMember rpcStruct = struct.getMember( Constants.RPC_BBOX );
        if ( rpcStruct != null ) {
            RPCStruct bboxStruct = (RPCStruct) rpcStruct.getValue();
            bbox = extractBBox( bboxStruct, null );
        }

        // get the servlet path using the session
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession();

        // path to context dir
        String path2Dir = getHomePath();

        // context and xsl files
        String mapContext = "file://" + path2Dir + "WEB-INF/conf/igeoportal/" + curContxt;

        String newHtml = null;
        String sid = null;
        try {
            // read session ID trying different possible sources
            sid = readSessionID( struct );

            // ContextSwitchLister.actionPerformed is the first action that
            // will be performed if a user enter iGeoPortal. So store the
            // users sessionID into his HTTP session for futher usage
            session.setAttribute( "SESSIONID", sid );
            // if no sessionID is available the context will be read as
            // anonymous user which will cause that layers assigned to a
            // authorized user may will not be parsed correctly
            XMLFragment xml = getContext( mapContext, bbox, sid );
            newHtml = doTransformContext( xml );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( StringTools.stackTraceToString( e ) );
            return;
        }

        session.setAttribute( NEW_CONTEXT_HTML, newHtml );

        // need to keep a reference to the last context...
        // often used when changing/saving the shown context
        try {
            writeContextToSession( mapContext, sid );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( StringTools.stackTraceToString( e ) );
        }

        getRequest().setAttribute( "SESSIONID", sid );
    }

    /**
     * writes the context the user choosed to the users session. It can be accessed using
     * <tt>Constants.CURRENTMAPCONTEXT</tt> key value
     * 
     * @param context
     * @param sessionID
     * @throws Exception
     */
    protected void writeContextToSession( String context, String sessionID )
                            throws Exception {
        URL ctxtUrl = new URL( context );
        ViewContext vc = WebMapContextFactory.createViewContext( ctxtUrl, null, sessionID );
        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
        session.setAttribute( Constants.CURRENTMAPCONTEXT, vc );
    }

    /**
     * returns the context to be used as a String
     * 
     * @param context
     * @param bbox
     * @param sessionID
     * @return the context as String
     * @throws Exception
     */
    protected XMLFragment getContext( String context, Envelope bbox, String sessionID )
                            throws Exception {

        XMLFragment xml = new XMLFragment();
        try {
            LOG.logInfo( "reading context: " + context );
            URL ctxtUrl = new URL( context );

            ViewContext vc = WebMapContextFactory.createViewContext( ctxtUrl, null, sessionID );
            if ( bbox != null ) {
                changeBBox( vc, bbox );
            }

            ctxt2html = vc.getGeneral().getExtension().getXslt();

            xml = XMLFactory.export( vc );

            // if at least one element is present we have a deegree Web Map Context
            // document; otherwise we have a standard Web Map Context document that
            // must be transformed into a deegree WMC document
            List<Node> nl = XMLTools.getNodes( xml.getRootElement().getOwnerDocument(),
                                               "cntxt:ViewContext/cntxt:General/cntxt:Extension/dgcntxt:IOSettings",
                                               nsContext );
            if ( nl.size() == 0 ) {
                xml = transformToDeegreeContext( xml );
            }
        } catch ( Exception e ) {
            throw e;
        }

        return xml;
    }

    /**
     * transforms a standard Web Map Context document to a deegree Web Map Context document
     * 
     * @param xml
     * @return the transformed xml
     * @throws Exception
     */
    private XMLFragment transformToDeegreeContext( XMLFragment xml )
                            throws Exception {

        String xslFilename = getHomePath() + WEBMAPCTXT2HTML;
        File file = new File( xslFilename );
        XSLTDocument xslt = new XSLTDocument();
        xslt.load( file.toURL() );
        xml = xslt.transform( xml );

        return xml;
    }

    /**
     * Transforms the context pointed to by <code>xml</code> into html using <code>xsl</code>
     * 
     * @param xml
     *            the context xml
     * @return the transformed context
     */
    protected String doTransformContext( XMLFragment xml ) {

        StringWriter sw = new StringWriter( 60000 );
        try {
            XSLTDocument xslt = new XSLTDocument();
            xslt.load( ctxt2html );
            xml = xslt.transform( xml );
            xml.write( sw );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_CREATE_URL", e.getMessage() ) );

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_TRANSFORM_CNTXT", e.getMessage() ) );
        }
        return sw.toString();
    }

}
