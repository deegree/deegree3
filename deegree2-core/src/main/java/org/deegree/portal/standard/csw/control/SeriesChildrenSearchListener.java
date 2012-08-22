//$$HeaderURL$$
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

import java.util.List;

import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.i18n.Messages;
import org.deegree.portal.standard.csw.CatalogClientException;

/**
 * This class is used in case a data series is found to display all its datasets So it receives a data series ID and
 * query all its children
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SeriesChildrenSearchListener extends SimpleSearchListener {

    /**
     *
     * @param rpcEvent
     * @throws CatalogClientException
     */
    @Override
    protected void validateRequest( RPCWebEvent rpcEvent )
                            throws CatalogClientException {

        RPCParameter[] params = extractRPCParameters( rpcEvent );

        // validity check for number of parameters in RPCMethodCall
        if ( params.length != 2 ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_WRONG_NUMB_PARAMS", "2",
                                                                   params.length ) );
        }

        RPCStruct rpcStruct = extractRPCStruct( rpcEvent, 1 );
        String rpcFormat = (String) extractRPCMember( rpcStruct, RPC_FORMAT );
        String rpcProtocol = (String) extractRPCMember( rpcStruct, Constants.RPC_PROTOCOL );
        if ( rpcFormat == null || rpcProtocol == null ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM", "Format, protocol" ) );
        }
        String rpcSeriesIdentifier = (String) extractRPCMember( rpcStruct, Constants.RPC_DATASERIES );
        if ( rpcSeriesIdentifier == null ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM",
                                                                   "series file identifier" ) );
        }

        // go through each catalog of the rpc and validate
        List<?> rpcCatalogs = extractRPCCatalogs( rpcEvent );
        String rpc_catalog = null;
        for ( int i = 0; i < rpcCatalogs.size(); i++ ) {
            rpc_catalog = (String) rpcCatalogs.get( i );

            // validity check for catalog
            String[] catalogs = config.getCatalogNames();
            boolean containsCatalog = false;
            for ( int j = 0; j < catalogs.length; j++ ) {
                if ( catalogs[j].equals( rpc_catalog ) ) {
                    containsCatalog = true;
                }
            }
            if ( !containsCatalog ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_CAT", rpc_catalog ) );
            }

            // validity check for format
            // is requested catalog capable to serve requested metadata format?
            List<?> formats = config.getCatalogFormats( rpc_catalog );
            if ( formats == null || !formats.contains( rpcFormat ) ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_FORMAT", rpc_catalog,
                                                                       rpcFormat ) );
            }

            // validity check for protocol
            // is requested catalog reachable through requested protocol?
            List<?> protocols = config.getCatalogProtocols( rpc_catalog );
            if ( !protocols.contains( rpcProtocol ) ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_WRONG_PROTOCOL", rpc_catalog,
                                                                       rpcProtocol ) );
            }
        }
    }

}
