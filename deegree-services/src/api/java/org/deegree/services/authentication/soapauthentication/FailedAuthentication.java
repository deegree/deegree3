//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.authentication.soapauthentication;

import javax.xml.namespace.QName;

import org.deegree.services.authentication.SecurityException;

/**
 * SOAP-Fault if the authentication fails regarding to the credentials provided in the SOAP security header.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FailedAuthentication extends SecurityException {

    private final static String faultString = "The security token could not be authenticated or authorized";

    private final static String param = "FailedAuthentication";

    private final static String SOAP_10 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    private final static String SOAP_10_PREFIX = "wsse";

    private final static QName faultCode11 = new QName( SOAP_10, param, SOAP_10_PREFIX );

    @Override
    public String getMessage() {
        return faultString;
    }

    /**
     * @return
     */
    public String getName() {

        return param;
    }

    /**
     * 
     * @return
     */
    public QName getFaultCode11() {
        return faultCode11;
    }

}
