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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.portal.standard.csw.CatalogClientException;
import org.deegree.portal.standard.csw.model.SessionRecord;
import org.deegree.portal.standard.csw.model.ShoppingCart;

/**
 * A <code>${type_name}</code> class.<br/>
 *
 * The Listener deletes an entry from the selection/shopping cart of a user.
 * The required information is contained in the RPCWebEvent passed to the actionPerformed method.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$ $Date$
 *
 * @deprecated Shopping cart will not be supported at the moment. update: new changes in deegree1_fork will not be
 *             carried here, since this class is still not used. Remove when this status changes
 */
@Deprecated
public class DeleteFromShoppingCartListener extends AddToShoppingCartListener {
    //  extends AddToShoppingCartListener --> SimpleSearchListener --> AbstractListener.

    private static final ILogger LOG = LoggerFactory.getLogger( DeleteFromShoppingCartListener.class );

    /* (non-Javadoc)
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpcEvent = (RPCWebEvent) event;
        try {
            validateRequest( rpcEvent );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_INVALID_RPC_EVENT", e.getMessage() ) );
            LOG.logError( e.getMessage(), e );
            return;
        }

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ShoppingCart cart = (ShoppingCart) session.getAttribute( Constants.SESSION_SHOPPINGCART );
        if ( cart != null ) {

            RPCParameter[] params = rpcEvent.getRPCMethodCall().getParameters();

            for ( int i = 0; i < params.length; i++ ) {
                RPCStruct struct = (RPCStruct) params[i].getValue();

                String identifier = (String) struct.getMember( Constants.RPC_IDENTIFIER ).getValue();
                String catalog = (String) struct.getMember( RPC_CATALOG ).getValue();
                String title = (String) struct.getMember( RPC_TITLE ).getValue();

                cart.remove( new SessionRecord( identifier, catalog, title ) );
            }

            // write the shopping cart back to the users session and to the request
            session.setAttribute( Constants.SESSION_SHOPPINGCART, cart );
            getRequest().setAttribute( Constants.SESSION_SHOPPINGCART, cart );
        }

        return;
    }

    /* (non-Javadoc)
     * @see org.deegree.portal.standard.csw.control.SimpleSearchListener#validateRequest(org.deegree.enterprise.control.RPCWebEvent)
     */
    @Override
    protected void validateRequest( RPCWebEvent rpcEvent )
                            throws CatalogClientException {

        RPCParameter[] params = extractRPCParameters( rpcEvent );

        // there are a varying number of params in this rpcEvent. Check them all !
        for ( int i = 0; i < params.length; i++ ) {

            RPCStruct struct = extractRPCStruct( rpcEvent, i );
            Object member = extractRPCMember( struct, Constants.RPC_IDENTIFIER );
            if ( member == null ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM", Constants.RPC_IDENTIFIER ) );
            }
            member = extractRPCMember( struct, RPC_CATALOG );
            if ( member == null ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM", RPC_CATALOG ) );
            }
            member = extractRPCMember( struct, RPC_TITLE );
            if ( member == null ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PARAM", RPC_TITLE ) );
            }
        }

        return;
    }

}
