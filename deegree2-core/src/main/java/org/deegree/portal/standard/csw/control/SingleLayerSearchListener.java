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
import org.deegree.framework.util.ParameterList;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.csw.discovery.GetRecordByIdResultDocument;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.standard.csw.CatalogClientException;
import org.deegree.portal.standard.csw.MetadataTransformer;
import org.deegree.portal.standard.csw.configuration.CSWClientConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A <code>${type_name}</code> class.<br/> This class handles a CSW metadata search request for a single WMS layer.
 * The layer for which to search the metedata needs to contain a MetadataURL tag with the address of the corresponding
 * CSW.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @deprecated Shopping cart will not be supported at the moment. update: new changes in deegree1_fork will not be
 *             carried here, since this class is still not used. Remove when this status changes
 */
@Deprecated
public class SingleLayerSearchListener extends OverviewMetadataListener {
    // extends OverviewMetadataListener --> SimpleSearchListener --> AbstractListener.
    private static final ILogger LOG = LoggerFactory.getLogger( SingleLayerSearchListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        config = (CSWClientConfiguration) session.getAttribute( Constants.CSW_CLIENT_CONFIGURATION );

        if ( config == null ) {
            try {
                config = initConfiguration( session );
            } catch ( Exception e ) {
                gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_INIT_CSWCLIENT", e.getMessage() ) );
                LOG.logError( e.getMessage(), e );
                return;
            }
        }

        nsContext = CommonNamespaces.getNamespaceContext();
        RPCWebEvent rpcEvent = (RPCWebEvent) event;

        // get transformation file name
        String fileName = "metaContent2html.xsl"; // default value
        // FIXME replace format with current value
        String format = "Profiles.ISO19115";
        HashMap xslMap = config.getProfileXSL( format );
        if ( xslMap != null ) {
            if ( xslMap.get( "full" ) != null ) {
                fileName = (String) xslMap.get( "full" );
            }
        }
        String pathToXslFile = "file:" + getHomePath() + "WEB-INF/conf/igeoportal/" + fileName;

        try {
            validateRequest( rpcEvent );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_REQ", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        String rpcCatalog;
        RPCStruct rpcStruct;
        String metadataURL;

        try {
            rpcStruct = extractRPCStruct( rpcEvent, 0 );
            rpcCatalog = (String) extractRPCMember( rpcStruct, RPC_CATALOG );
            metadataURL = (String) extractRPCMember( rpcStruct, "METADATA_URL" );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_RPC_EVENT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        // "GetRecordById"-request
        HashMap<String, Document> result = new HashMap<String, Document>( 1 );
        try {
            // URL url = new URL( metadataURL );
            // Document doc = XMLTools.parse( url.openStream() );
            GetRecordByIdResultDocument doc = new GetRecordByIdResultDocument();
            doc.load( new URL( metadataURL ) );
            result.put( rpcCatalog, doc.getRootElement().getOwnerDocument() );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_SERVER_ERROR", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        // handle result: take result and transform it to produce html output
        try {
            handleResult( result, pathToXslFile, "overview" );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_HANDLE_RESULT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

    }

    /**
     * @param session
     * @return CSWClientConfiguration
     * @throws CatalogClientException
     */
    private CSWClientConfiguration initConfiguration( HttpSession session )
                            throws CatalogClientException {

        InitCSWModuleListener iml = new InitCSWModuleListener();
        ViewContext vc = (ViewContext) session.getAttribute( org.deegree.portal.Constants.CURRENTMAPCONTEXT );
        GeneralExtension gen = vc.getGeneral().getExtension();
        Module module = null;

        try {
            module = iml.findCswClientModule( gen );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_CLIENT_ERROR", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
        }
        CSWClientConfiguration config = new CSWClientConfiguration();

        ParameterList parList = module.getParameter();
        iml.initConfig( config, parList );

        String srs = "EPSG:4236";
        srs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem().getIdentifier();
        config.setSrs( srs );

        session.setAttribute( Constants.CSW_CLIENT_CONFIGURATION, config );

        return config;
    }

    @Override
    protected void validateRequest( RPCWebEvent rpcEvent )
                            throws CatalogClientException {

        RPCParameter[] params = extractRPCParameters( rpcEvent );
        if ( params.length != 1 ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_PARAMS_NUMBER", "1",
                                                                   params.length ) );
        }

        RPCStruct struct = extractRPCStruct( rpcEvent, 0 );

        extractRPCMember( struct, "METADATA_URL" );
        extractRPCMember( struct, "METADATA_TITLE" );

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
    @Override
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
            docString = DOMPrinter.nodeToString( doc, CharsetUtils.getSystemCharset() );
            Reader reader = new StringReader( docString );

            List nl = extractMetadata( doc );
            if ( nl.size() > 1 ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_TOO_MANY_NODES" ) );
            }

            String xPathToTitle = config.getXPathToDataTitle();
            String title = extractValue( (Node) nl.get( 0 ), xPathToTitle );
            String[] serviceCatalogs = null;
            Map catalogsMap = (HashMap) session.getAttribute( SESSION_AVAILABLESERVICECATALOGS );
            if ( catalogsMap != null ) {
                serviceCatalogs = extractServiceCatalogs( catalogsMap, title );
            }

            // transformation
            htmlFragment.append( mt.transformMetadata( reader, catalog, serviceCatalogs, 0, 0, metaVersion ) );
        }

        this.getRequest().setAttribute( HTML_FRAGMENT, htmlFragment.toString() );
        session.setAttribute( SESSION_METADATA, result );

        return;
    }

}
