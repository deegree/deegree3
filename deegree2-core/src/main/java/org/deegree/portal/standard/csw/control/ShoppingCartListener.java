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

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.portal.standard.csw.model.ShoppingCart;

/**
 * A <code>${type_name}</code> class.<br/> The Listener initializes the <code>ShoppingCart</code>
 * with the value taken from the users session or, if nonexistent, with a new, empty
 * <code>ShoppingCart</code>.
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
public class ShoppingCartListener extends AbstractListener {

    @Override
    public void actionPerformed( FormEvent e ) {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );

        ShoppingCart cart = (ShoppingCart) session.getAttribute( Constants.SESSION_SHOPPINGCART );
        if ( cart == null ) {
            cart = new ShoppingCart();
            session.setAttribute( Constants.SESSION_SHOPPINGCART, cart );
        }

        getRequest().setAttribute( Constants.SESSION_SHOPPINGCART, cart );
    }

}
