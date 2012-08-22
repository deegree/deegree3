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

/**
 * A <code>${type_name}</code> class.<br/> Constants class for the CSW client based on iGeoPortal
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Constants implements org.deegree.portal.Constants {

    // please insert new constants in alphabetical order. thanks.

    static final String CONF_DATASERIES = "CONF_DATASERIES"; // used in SeriesChildrenSearch

    static final String CONF_DATE = "CONF_DATE";

    static final String CONF_GEOGRAPHICBOX = "CONF_GEOGRAPHICBOX";

    static final String CONF_IDENTIFIER = "CONF_IDENTIFIER";

    static final String CONF_KEYWORDS = "CONF_KEYWORDS";

    static final String CONF_MDTYPE = "CONF_MDTYPE"; // added for CSW 2.0.2

    static final String CONF_SERVICESEARCH = "CONF_SERVICESEARCH";

    static final String CONF_SIMPLESEARCH = "CONF_SIMPLESEARCH";

    static final String CONF_TOPICCATEGORY = "CONF_TOPICCATEGORY";

    static final String CSW_CLIENT_CONFIGURATION = "CSW_CLIENT_CONFIGURATION";

    static final String RPC_DATASERIES = "RPC_DATASERIES"; // used in SeriesChildrenSearch

    static final String RPC_DATEFROM = "RPC_DATEFROM";

    static final String RPC_DATETO = "RPC_DATETO";

    static final String RPC_DAY = "RPC_DAY";

    static final String RPC_IDENTIFIER = "RPC_IDENTIFIER";

    static final String RPC_MONTH = "RPC_MONTH";

    static final String RPC_PROTOCOL = "RPC_PROTOCOL"; // SOAP, POST

    static final String RPC_SERVICESEARCH = "RPC_SERVICESEARCH";

    static final String RPC_YEAR = "RPC_YEAR";

    /**
     * needed in jsp pages
     */
    public static final String SESSION_SHOPPINGCART = "SESSION_SHOPPINGCART";

}
