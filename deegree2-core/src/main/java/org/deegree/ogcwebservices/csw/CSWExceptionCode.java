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

package org.deegree.ogcwebservices.csw;

import org.deegree.ogcbase.ExceptionCode;

/**
 * The <code>CSWExceptionCode</code> class is a simple extension to supply csw/wrs exception
 * codes.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class CSWExceptionCode extends ExceptionCode {
    /**
     * Intended for cases in which the message sender seems to have erred in some manner (this
     * corresponds to an HTTP status code of 4xx).
     */
    public static final CSWExceptionCode WRS_SENDER = new CSWExceptionCode( "wrs:Sender" );

    /**
     * Intended for cases in which an unexpected condition prevented the service from fulfilling the
     * request (this corresponds to an HTTP status code of 5xx).
     */
    public static final CSWExceptionCode WRS_RECEIVER = new CSWExceptionCode( "wrs:Receiver" );

    /**
     * The request message is either invalid or is not well-formed.
     */
    public static final CSWExceptionCode WRS_INVALIDREQUEST = new CSWExceptionCode( "wrs:InvalidRequest" );

    /**
     * The requested transaction could not be completed.
     */
    public static final CSWExceptionCode WRS_TRANSACTIONFAILED = new CSWExceptionCode( "wrs:TransactionFailed" );

    /**
     * The (abstract) operation has not been implemented.
     */
    public static final CSWExceptionCode WRS_NOTIMPLEMENTED = new CSWExceptionCode( "wrs:NotImplemented" );

    /**
     * The requested resource does not exist or could not be found.
     */
    public static final CSWExceptionCode WRS_NOTFOUND = new CSWExceptionCode( "wrs:NotFound" );

    /**
     * A service option, feature, or capability is not supported.
     */
    public static final CSWExceptionCode WRS_NOTSUPPORTED = new CSWExceptionCode( "wrs:NotSupported" );

    /**
     * @param value
     *            to set this ExceptionCode to
     */
    public CSWExceptionCode( String value ) {
        super( value );
    }

}
