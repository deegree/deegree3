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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.standard.csw.CatalogClientException;
import org.deegree.portal.standard.csw.configuration.CSWClientConfiguration;
import org.deegree.portal.standard.csw.model.DataSessionRecord;
import org.deegree.portal.standard.csw.model.ServiceSessionRecord;
import org.deegree.portal.standard.csw.model.ShoppingCart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 * @deprecated Shopping cart will not be supported at the moment. update: new changes in deegree1_fork will not be
 *             carried here, since this class is still not used. Remove when this status changes
 */
@Deprecated
public class AddToShoppingCartListener extends SimpleSearchListener {

    private static final ILogger LOG = LoggerFactory.getLogger( AddToShoppingCartListener.class );

    static final String RPC_TITLE = "RPC_TITLE";

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
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
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_RPC_EVENT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        String dataId = null;
        String dataCatalog = null;
        String dataTitle = null;
        try {
            RPCStruct struct = extractRPCStruct( rpcEvent, 0 );
            dataId = (String) extractRPCMember( struct, Constants.RPC_IDENTIFIER );
            dataCatalog = (String) extractRPCMember( struct, RPC_CATALOG );
            dataTitle = (String) extractRPCMember( struct, RPC_TITLE );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_RPC_EVENT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        String serviceReq = null;
        try {
            serviceReq = createServiceRequest( dataTitle );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_SERVICE_REQ", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        HashMap serviceResult = null;
        try {
            serviceResult = performServiceRequest( dataTitle, serviceReq );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_SERVER_ERROR", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        // create ssr's ...
        ServiceSessionRecord[] ssrArray = null;
        try {
            ssrArray = createServiceSessionRecords( serviceResult );
        } catch ( CatalogClientException e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_FAIL_CREATE_SSR", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        // ... add the ssr's to the corresponding dsr in the session ...
        List dsrList = (ArrayList) session.getAttribute( SESSION_DATARECORDS );
        dsrList = addServiceSessionRecords( dsrList, dataId, dataCatalog, dataTitle, ssrArray );
        session.setAttribute( SESSION_DATARECORDS, dsrList );

        // ... and add this dsr to the shopping cart.
        ShoppingCart cart = (ShoppingCart) session.getAttribute( Constants.SESSION_SHOPPINGCART );
        if ( cart == null ) {
            cart = new ShoppingCart();
        }
        DataSessionRecord dsr = getDataSessionRecordFromList( dsrList, dataId, dataCatalog, dataTitle );
        cart.add( dsr );
        session.setAttribute( Constants.SESSION_SHOPPINGCART, cart );

        getRequest().setAttribute( Constants.MESSAGE, Messages.getMessage( "IGEO_STD_CSW_SUCCESS_ADD_DATASET" ) );

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
        Object member = extractRPCMember( struct, RPC_CATALOG );
        if ( member == null ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM", RPC_CATALOG ) );
        }
        member = extractRPCMember( struct, Constants.RPC_IDENTIFIER );
        if ( member == null ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM",
                                                                   Constants.RPC_IDENTIFIER ) );
        }
        member = extractRPCMember( struct, RPC_TITLE );
        if ( member == null ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM", RPC_TITLE ) );
        }
    }

    /**
     * This method creates a csw:GetRecords request for RESULTS for service metadata, using the paramter values
     * contained in the struct of the passed rpcEvent.
     *
     * @param title
     * @return Returns the xml encoded request as <code>String</code>.
     * @throws CatalogClientException
     * @throws RPCException
     */
    private String createServiceRequest( String title )
                            throws CatalogClientException, RPCException {
        String format = "ISO19119"; // TODO make usable for other formats, not only "ISO19119"
        String template = "CSWServiceSearchRPCMethodCallTemplate.xml";
        RPCStruct serviceStruct = createRpcStructForServiceSearch( template, title );

        return createRequest( serviceStruct, format, "RESULTS" );
    }

    /**
     * Performs the given service request and returns its result
     *
     * @param title
     * @param serviceReq
     * @return Returns a <code>HashMap</code>, which contains one key-value-pair for each service catalogue, that has
     *         been searched. The key is the name of the service catalogue. The value is the xml Document, that contains
     *         1 to n service metadata entries.
     * @throws CatalogClientException
     */
    private HashMap performServiceRequest( String title, String serviceReq )
                            throws CatalogClientException {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        Map availableServiceCatalogs = (HashMap) session.getAttribute( SESSION_AVAILABLESERVICECATALOGS );
        List serviceCatalogs = extractAvailableServiceCatalogs( availableServiceCatalogs, title );

        return performRequest( null, serviceReq, serviceCatalogs );
    }

    /**
     * This method creates a ServiceSessionRecord for each service metadata element in the passed serviceResults and
     * returns them as Array.
     *
     * @param serviceResults
     *            Map containing service metadata catalogs as keys and metadata-Documents as values.
     * @return Returns an Array of ServiceSessionRecords.
     * @throws CatalogClientException
     */
    private ServiceSessionRecord[] createServiceSessionRecords( HashMap serviceResults )
                            throws CatalogClientException {

        List<ServiceSessionRecord> ssrList = new ArrayList<ServiceSessionRecord>( 10 );

        Iterator it = serviceResults.keySet().iterator();
        while ( it.hasNext() ) {
            // one loop for every available service metadata catalog
            String catalog = (String) it.next();
            Document doc = (Document) serviceResults.get( catalog );

            List mdList;
            try {
                mdList = extractMetadata( doc );
            } catch ( Exception e ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_EXTRACT_METADATA",
                                                                       e.getMessage() ) );
            }

            for ( int j = 0; j < mdList.size(); j++ ) {
                // one loop for every service metadata element for the current catalog
                Node mdNode = (Node) mdList.get( j );
                if ( !"service".equals( getMetadataType( (Element) mdNode ) ) ) {
                    // Ignore the node if it is a data metadata type(i.e. dataset or dataseries)
                    // Or use it if its a service metadata
                    // TODO handle service metadata properly, also in iGeoPortal xslts
                    continue;
                }
                String id = null;
                String title = null;
                String address = null;
                String serviceType = null;
                String serviceTypeVersion = null;

                try {
                    id = extractValue( mdNode, config.getXPathToServiceIdentifier() );
                    title = extractValue( mdNode, config.getXPathToServiceTitle() );
                    address = extractValue( mdNode, config.getXPathToServiceAddress() );
                    serviceType = extractValue( mdNode, config.getXPathToServiceType() );
                    serviceTypeVersion = extractValue( mdNode, config.getXPathToServiceTypeVersion() );
                } catch ( Exception e ) {
                    throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_EXTRACT_VAL",
                                                                           e.getMessage() ) );
                }

                ServiceSessionRecord ssr = new ServiceSessionRecord( id, catalog, title, address, serviceType,
                                                                     serviceTypeVersion );
                ssrList.add( ssr );
            }
        }

        ServiceSessionRecord[] serviceSessionRecords = ssrList.toArray( new ServiceSessionRecord[ssrList.size()] );

        return serviceSessionRecords;
    }

    /**
     * This method searches the passed List of DataSessionRecords for the dataSessionRecord that corresponds to the
     * passed data parameters and adds to it the passed serviceSessionRecords. It returns the List with one
     * DataSessionRecord changed.
     *
     * @param dataSessionRecList
     *            The List containing the DataSessionRecord to which to add the passed ServiceSessionRecords.
     * @param dataId
     *            One of three parameters to identify the DataSessionRecord.
     * @param dataCatalog
     *            One of three parameters to identify the DataSessionRecord.
     * @param dataTitle
     *            One of three parameters to identify the DataSessionRecord.
     * @param serviceSessionRecs
     *            The ServiceSessionRecords to add to the DataSessionRecord.
     * @return Returns the changed dataSessionRecList.
     */
    private List addServiceSessionRecords( List dataSessionRecList, String dataId, String dataCatalog,
                                           String dataTitle, ServiceSessionRecord[] serviceSessionRecs ) {

        for ( int i = 0; i < dataSessionRecList.size(); i++ ) {

            DataSessionRecord dsr = (DataSessionRecord) dataSessionRecList.get( i );

            if ( dataId.equals( dsr.getIdentifier() ) && dataCatalog.equals( dsr.getCatalogName() )
                 && dataTitle.equals( dsr.getTitle() ) ) {

                ( (DataSessionRecord) dataSessionRecList.get( i ) ).setServices( serviceSessionRecs );
            }
        }

        return dataSessionRecList;
    }

    /**
     * @param dataSessionRecList
     *            The List containing the DataSessionRecord to return.
     * @param dataId
     *            One of three parameters to identify the DataSessionRecord.
     * @param dataCatalog
     *            One of three parameters to identify the DataSessionRecord.
     * @param dataTitle
     *            One of three parameters to identify the DataSessionRecord.
     * @return Returns the DataSessionRecord from the passed List, that matches the passed data parameters OR null, if
     *         no match was found in the passed List.
     */
    protected DataSessionRecord getDataSessionRecordFromList( List dataSessionRecList, String dataId,
                                                              String dataCatalog, String dataTitle ) {

        for ( int i = 0; i < dataSessionRecList.size(); i++ ) {

            DataSessionRecord dsr = (DataSessionRecord) dataSessionRecList.get( i );
            if ( dataId.equals( dsr.getIdentifier() ) && dataCatalog.equals( dsr.getCatalogName() )
                 && dataTitle.equals( dsr.getTitle() ) ) {

                return dsr;
            }
        }

        return null;
    }

    /**
     * Extracts the List of available service catalogs names for the passed title from the passed Map.
     *
     * @param availableServiceCatalogs
     *            A Map containing the title of a data-metadata object (as key) and a List of names of all available
     *            service catalogs for this tilte (as value)
     * @param title
     *            The title of a data-metadata object.
     * @return Returns the List of available service catalogs names for the passed title. May be null.
     */
    private List extractAvailableServiceCatalogs( Map availableServiceCatalogs, String title ) {

        List serviceCatalogs = null;

        if ( availableServiceCatalogs != null && title != null ) {
            serviceCatalogs = (List) availableServiceCatalogs.get( title );
        }

        return serviceCatalogs;
    }

}
