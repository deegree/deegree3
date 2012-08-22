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

package org.deegree.portal.standard.csw.control;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.standard.csw.CatalogClientException;
import org.deegree.portal.standard.csw.configuration.CSWClientConfiguration;
import org.deegree.portal.standard.csw.model.DataSessionRecord;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TurnPageListener extends SimpleSearchListener {

    private static final ILogger LOG = LoggerFactory.getLogger( TurnPageListener.class );

    /* (non-Javadoc)
     * @see org.deegree.portal.standard.csw.control.SimpleSearchListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpcEvent = (RPCWebEvent) event;
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        config = (CSWClientConfiguration) session.getAttribute( Constants.CSW_CLIENT_CONFIGURATION );

        nsContext = CommonNamespaces.getNamespaceContext();

        try {
            validateRequest( rpcEvent );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_REQ", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );

            return;
        }

        RPCStruct struct = null;
        try {
            struct = extractRPCStruct( rpcEvent, 0 );
        } catch ( CatalogClientException e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_RPC_EVENT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );

            return;
        }

        // load request from session
        String req = (String) session.getAttribute( SESSION_REQUESTFORRESULTS );

        // exchange digits
        try {
            req = replaceIndexAttribs( req, struct );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_REQ", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );

            return;
        }

        // load request into session again for further use
        session.setAttribute( SESSION_REQUESTFORRESULTS, req );

        List<String> catalogs = new ArrayList<String>( 1 );
        catalogs.add( (String) struct.getMember( RPC_CATALOG ).getValue() );
        HashMap result = null;

        String protocol = (String) session.getAttribute( Constants.RPC_PROTOCOL );

        // performRequest of SimpleSearchListener
        try {
            // result = performRequest( protocol, req, catalogs, "TPL" );
            // For 2.0.2
            result = performRequest( protocol, req, catalogs );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_SERVER_ERROR", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );

            return;
        }

        // create data session records for results and add them to the List in the session.
        List<DataSessionRecord> dsrListSession = (ArrayList<DataSessionRecord>) session.getAttribute( SESSION_DATARECORDS );
        List<DataSessionRecord> dsrList = null;
        try {
            dsrList = createDataSessionRecords( result );
        } catch ( CatalogClientException e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_CREATE_DSRLIST", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );

            return;
        }
        for ( int i = 0; i < dsrList.size(); i++ ) {
            if ( !dsrListSession.contains( dsrList.get( i ) ) ) {
                dsrListSession.add( dsrList.get( i ) );
            }
        }
        session.setAttribute( SESSION_DATARECORDS, dsrListSession );

        Map availableServiceCatalogsMap = null;
        try { // TODO make usable for other formats, not only "ISO19119"
            availableServiceCatalogsMap = doServiceSearch( result, "ISO19119", "HITS" );
        } catch ( CatalogClientException e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_CREATE_SEARCH_RESULTS", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );

            return;
        }
        session.setAttribute( SESSION_AVAILABLESERVICECATALOGS, availableServiceCatalogsMap );

        // handleResult von SimpleSearchListener
        HashMap resultHits = (HashMap) session.getAttribute( SESSION_RESULTFORHITS );
        try {
            String pathToFile = StringTools.concat( 100, "file:", getHomePath(),
                                                    "WEB-INF/conf/igeoportal/csw/metaList2html.xsl" );
            handleResult( resultHits, result, pathToFile );

        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_HANDLE_RESULT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );

            return;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.portal.standard.csw.control.SimpleSearchListener#validateRequest(org.deegree.enterprise.control.RPCWebEvent)
     */
    @Override
    protected void validateRequest( RPCWebEvent rpcEvent )
                            throws CatalogClientException {

        RPCParameter[] params = extractRPCParameters( rpcEvent );

        // validity check for number of parameters in RPCMethodCall
        if ( params.length != 1 ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_PARAMS_NUMBER", "1",
                                                                   params.length ) );
        }

        RPCStruct struct = extractRPCStruct( rpcEvent, 0 );
        String catalog = (String) extractRPCMember( struct, RPC_CATALOG );
        String format = (String) extractRPCMember( struct, RPC_FORMAT );
        Integer matches = (Integer) extractRPCMember( struct, "matches" );
        Integer recReturned = (Integer) extractRPCMember( struct, "recReturned" );
        String direction = (String) extractRPCMember( struct, "direction" );
        if ( catalog == null || format == null || matches == null || recReturned == null || direction == null ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAMS" ) );
        }

        // validity check for format
        // is requested catalog capable to serve requested metadata format?
        List formats = config.getCatalogFormats( catalog );
        if ( !formats.contains( format ) ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_FORMAT", catalog, format ) );
        }

        return;
    }

    /**
     * @param request
     * @param struct
     * @return Returns the request after replacing the index.
     * @throws IOException
     * @throws SAXException
     * @throws CatalogClientException
     */
    private String replaceIndexAttribs( String request, RPCStruct struct )
                            throws IOException, SAXException, CatalogClientException {

        // read string as xml document
        StringReader sr = new StringReader( request );
        Document doc = XMLTools.parse( sr );
        //
        int oldStartPos = Integer.parseInt( doc.getDocumentElement().getAttribute( "startPosition" ) );
        int maxRec = Integer.parseInt( doc.getDocumentElement().getAttribute( "maxRecords" ) );

        int matches;
        int recRet;
        String direction;

        try {
            matches = ( (Integer) struct.getMember( "matches" ).getValue() ).intValue();
            recRet = ( (Integer) struct.getMember( "recReturned" ).getValue() ).intValue();
            direction = (String) struct.getMember( "direction" ).getValue();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_INVALID_STRUCT", e.getMessage() ) );
        }

        // validity check
        if ( recRet > maxRec ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_WRONG_NUMBER" ) );
        }

        int newStartPos = 1;
        if ( "previous".equals( direction ) ) {
            newStartPos = oldStartPos - maxRec < 1 ? 1 : oldStartPos - maxRec;
        } else if ( "next".equals( direction ) ) {
            newStartPos = oldStartPos + recRet;
        } else {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_WRONG_DIRECTION" ) );
        }

        // validity check: 1 <= startPosition <= matches
        if ( newStartPos > matches || newStartPos < 1 ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_WRONG_STARTPOS", newStartPos ) );
        }

        // replace value of startPosition
        doc.getDocumentElement().setAttribute( "startPosition", String.valueOf( newStartPos ) );

        // return doc as string
        return DOMPrinter.nodeToString( doc, CharsetUtils.getSystemCharset() );
    }

}
