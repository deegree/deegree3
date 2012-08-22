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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.standard.csw.CatalogClientException;
import org.deegree.portal.standard.csw.MetadataTransformer;
import org.deegree.portal.standard.csw.configuration.CSWClientConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OverviewMetadataListener extends SimpleSearchListener {

    private static final ILogger LOG = LoggerFactory.getLogger( OverviewMetadataListener.class );

    static final String SESSION_METADATA = "SESSION_METADATA";

    @Override
    public void actionPerformed( FormEvent event ) {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        config = (CSWClientConfiguration) session.getAttribute( Constants.CSW_CLIENT_CONFIGURATION );

        nsContext = CommonNamespaces.getNamespaceContext();

        RPCWebEvent rpcEvent = (RPCWebEvent) event;
        RPCParameter[] params;
        try {
            params = extractRPCParameters( rpcEvent );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_RPC_EVENT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        // get transformation file name
        String fileName = "csw/metaOverview2html.xsl"; // default value
        // FIXME replace format with current value
        String format = "Profiles.ISO19115";
        HashMap xslMap = config.getProfileXSL( format );
        if ( xslMap != null ) {
            if ( xslMap.get( "full" ) != null ) {
                fileName = (String) xslMap.get( "full" );
            }
        }
        String pathToXslFile = "file:" + getHomePath() + "WEB-INF/conf/igeoportal/" + fileName;
        LOG.logDebug( "path to xslFile: ", pathToXslFile );

        if ( params == null || params.length == 0 ) {
            // get Metadata from the users session
            session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
            Object o = session.getAttribute( SESSION_METADATA );

            if ( o != null ) {
                try {
                    handleResult( o, pathToXslFile, "overview" );
                } catch ( Exception e ) {
                    gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_HANDLE_RESULT", e.getMessage() ) );
                    LOG.logError( e.getMessage() );
                    return;
                }
            }
        } else {
            try {
                validateRequest( rpcEvent );
            } catch ( Exception e ) {
                gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_REQ", e.getMessage() ) );
                LOG.logError( e.getMessage() );
                return;
            }

            String rpcCatalog;
            RPCStruct rpcStruct;
            String rpcFormat;
            String rpcProtocol;

            try {
                rpcStruct = extractRPCStruct( rpcEvent, 0 );
                rpcCatalog = (String) extractRPCMember( rpcStruct, RPC_CATALOG );
                rpcFormat = (String) extractRPCMember( rpcStruct, RPC_FORMAT );
                rpcProtocol = (String) extractRPCMember( rpcStruct, Constants.RPC_PROTOCOL );
            } catch ( Exception e ) {
                gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_RPC_EVENT", e.getMessage() ) );
                LOG.logError( e.getMessage() );
                return;
            }

            // "GetRecordById"-request
            String req = null;
            HashMap result = null;
            try {
                req = createRequest( rpcStruct, rpcFormat, null );
            } catch ( Exception e ) {
                gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_REQ", e.getMessage() ) );
                LOG.logError( e.getMessage() );
                return;
            }
            try {
                List<String> dataCatalogs = new ArrayList<String>( 1 );
                dataCatalogs.add( rpcCatalog );

                // key = data catalog name; value = csw:GetRecordByIdResponse
                result = performRequest( rpcProtocol, req, dataCatalogs );
                // result is a HashMap that contains only one key-value-pair,
                // because dataCatalogs contains only one catalog.
            } catch ( Exception e ) {
                gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_SERVER_ERROR", e.getMessage() ) );
                LOG.logError( e.getMessage() );
                return;
            }

            // handle result: take result and transform it to produce html output
            try {
                handleResult( result, pathToXslFile, "overview" );
            } catch ( Exception e ) {
                gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_HANDLE_RESULT", e.getMessage() ) );
                LOG.logError( e.getMessage() );
                return;
            }

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
        if ( params.length != 1 ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_PARAMS_NUMBER", "1",
                                                                   params.length ) );
        }

        RPCStruct struct = extractRPCStruct( rpcEvent, 0 );

        // validity check for catalog
        String rpcCatalog = (String) extractRPCMember( struct, RPC_CATALOG );
        String[] catalogs = config.getCatalogNames();
        boolean containsCatalog = false;
        for ( int i = 0; i < catalogs.length; i++ ) {
            if ( catalogs[i].equals( rpcCatalog ) ) {
                containsCatalog = true;
                break;
            }
        }
        if ( !containsCatalog ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_CAT", rpcCatalog ) );
        }

        // validity check for format
        // is requested catalog capable to serve requested metadata format?
        List formats = config.getCatalogFormats( rpcCatalog );
        String rpcFormat = (String) extractRPCMember( struct, RPC_FORMAT );
        if ( !formats.contains( rpcFormat ) ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_FORMAT", rpcCatalog, rpcFormat ) );
        }

        // validity check for protocol
        // is requested catalog reachable through requested protocol?
        List protocols = config.getCatalogProtocols( rpcCatalog );
        String rpcProtocol = (String) extractRPCMember( struct, Constants.RPC_PROTOCOL );
        if ( !protocols.contains( rpcProtocol ) ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_PROTOCOL", rpcCatalog,
                                                                   rpcProtocol ) );
        }

        try {
            struct.getMember( Constants.RPC_IDENTIFIER ).getValue();
        } catch ( Exception e ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_ID" ) );
        }

        // validity check for bounding box values
        RPCStruct bBox;
        try {
            bBox = (RPCStruct) struct.getMember( Constants.RPC_BBOX ).getValue();
        } catch ( Exception e ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_BBOX" ) );
        }
        double minx, miny, maxx, maxy;
        try {
            minx = ( (Double) bBox.getMember( Constants.RPC_BBOXMINX ).getValue() ).doubleValue();
            miny = ( (Double) bBox.getMember( Constants.RPC_BBOXMINY ).getValue() ).doubleValue();
            maxx = ( (Double) bBox.getMember( Constants.RPC_BBOXMAXX ).getValue() ).doubleValue();
            maxy = ( (Double) bBox.getMember( Constants.RPC_BBOXMAXY ).getValue() ).doubleValue();
        } catch ( Exception e ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_INVALID_BBOX_NOT_DECIMAL" ) );
        }
        if ( minx > maxx || miny > maxy ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_INVALID_BBOX_VALUES" ) );
        }
    }

    // super.createRequest();

    // super.performeRequest();

    /**
     * @param result
     * @param pathToXslFile
     *            e.g. file://$iGeoPortal_home$/WEB-INF/conf/igeoportal/metaOverview2html.xsl
     * @param metaVersion
     *            e.g. overview, detailed
     * @throws XMLParsingException
     * @throws CatalogClientException
     * @throws TransformerException
     * @throws IOException
     */
    protected void handleResult( Object result, String pathToXslFile, String metaVersion )
                            throws XMLParsingException, CatalogClientException, TransformerException, IOException {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );

        // result is a very short hashmap with only one entry!
        Map map = (HashMap) result;
        // key = data catalog name; value = csw:GetRecordByIdResponse
        Iterator it = map.keySet().iterator();

        String catalog = null;
        Document doc = null;
        String docString = null;

        URL u = null;
        StringBuffer htmlFragment = new StringBuffer( 5000 );
        MetadataTransformer mt = null;
        try {
            u = new URL( pathToXslFile );
            mt = new MetadataTransformer( u.getFile() );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( FileNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
        }

        while ( it.hasNext() ) {
            catalog = (String) it.next();
            doc = (Document) map.get( catalog );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( "xml doc to transform:\n",
                              new XMLFragment( doc.getDocumentElement() ).getAsPrettyString() );
            }
            docString = DOMPrinter.nodeToString( doc, CharsetUtils.getSystemCharset() );
            Reader reader = new StringReader( docString );

            List nl = extractMetadata( doc );
            if ( nl.size() > 1 ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_TOO_MANY_NODES" ) );
            }

            String xPathToId = config.getXPathToDataIdentifier();
            String identifier = extractValue( (Node) nl.get( 0 ), xPathToId );
            String[] serviceCatalogs = null;
            Map catalogsMap = (HashMap) session.getAttribute( SESSION_AVAILABLESERVICECATALOGS );
            if ( catalogsMap != null ) {
                serviceCatalogs = extractServiceCatalogs( catalogsMap, identifier );
            }

            // transformation
            htmlFragment.append( mt.transformMetadata( reader, catalog, serviceCatalogs, 0, 0, metaVersion ) );
            LOG.logDebug( "Transformed html", htmlFragment.toString() );
        }

        this.getRequest().setAttribute( HTML_FRAGMENT, htmlFragment.toString() );
        session.setAttribute( SESSION_METADATA, result );
    }

    /**
     * Extracts all Metadata nodes from the passed csw:GetRecordByIdResponse Document.
     *
     * @param doc
     *            The csw:GetRecordByIdResponse Document from which to extract the Metadata nodes.
     * @return Returns a NodeList of Metadata Elements for the passed Document.
     * @throws CatalogClientException
     *             if metadata nodes could not be extracted from the passed Document.
     * @throws XMLParsingException
     */
    @Override
    protected List extractMetadata( Document doc )
                            throws CatalogClientException, XMLParsingException {

        List nl = null;

        String xPathToMetadata = "csw202:GetRecordByIdResponse/child::*";

        nl = XMLTools.getNodes( doc, xPathToMetadata, nsContext );

        if ( nl == null || nl.size() < 1 ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_EXTRACT_MD_NODES" ) );
        }

        return nl;
    }

    /**
     * Extracts a List of available service catalogues from the Map in the session and returns its contents as an Array
     * of Strings.
     *
     * @param catalogsMap
     *            The Map containing the data title (as key) and the List of available service catalogues names (as
     *            value)
     * @param title
     *            The key for the value in the passed Map.
     * @return Returns an Array of Strings for the available service catalogues. If no service catalogues are available,
     *         an Array of one empty String is returned.
     */
    protected String[] extractServiceCatalogs( Map catalogsMap, String title ) {

        String[] catalogs = null;

        List catalogsList = (ArrayList) catalogsMap.get( title );

        if ( catalogsList != null ) {
            catalogs = new String[catalogsList.size()];
            for ( int i = 0; i < catalogsList.size(); i++ ) {
                catalogs[i] = (String) catalogsList.get( i );
            }
        } else {
            catalogs = new String[] { "" };
        }

        return catalogs;
    }

}
