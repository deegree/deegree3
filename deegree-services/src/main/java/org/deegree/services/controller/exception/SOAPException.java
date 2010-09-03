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

package org.deegree.services.controller.exception;

import org.deegree.services.controller.ows.OWSException;

/**
 * The <code>SoapException</code> class wraps the soap specific fault parameters.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class SOAPException extends ControllerException {

    /**
     *
     */
    private static final long serialVersionUID = -8628066105740690101L;

    /**
     * SOAP error code, the receiver expects a different SOAP version.
     */
    public static final String VERSION_MISMATCH = "VersionMismatch";

    /**
     * SOAP error code, the receiver does not understand the required functionality
     */
    public static final String MUST_UNDERSTAND = "MustUnderstand";

    /**
     * SOAP error code, the receiver does not support given character encoding
     */
    public static final String DATA_ENCODING = "DataEncodingUnknown";

    /**
     * SOAP error code, the receiver is not able to handle the request given in the body.
     */
    public static final String SENDER = "Sender";

    /**
     * SOAP error code, the receiver is not able to handle the request without knowing the contents of the body.
     */
    public static final String RECEIVER = "Receiver";

    private OWSException detail;

    private final String[] subcodes;

    /**
     * @param reason
     *            of this error
     * @param code
     *            of this exception, one of the above
     */
    public SOAPException( String reason, String code ) {
        super( reason, code );
        subcodes = null;
    }

    /**
     * @param reason
     *            of this error
     * @param code
     *            of this exception, one of the above
     * @param subcodes
     *            an array of strings which will be added in the subcodes of the Fault/Code node.
     */
    public SOAPException( String reason, String code, String[] subcodes ) {
        super( reason, code );
        this.subcodes = subcodes;
    }

    /**
     * @param reason
     *            of this error
     * @param code
     *            of this exception, one of the above
     * @param detail
     *            an ows exeption which will be put in the detail information
     */
    public SOAPException( String reason, String code, OWSException detail ) {
        super( reason, code );
        this.detail = detail;
        subcodes = null;
    }

    /**
     * @return the reason of this exception, which is the same as calling getMessage
     */
    public final String getReason() {
        return getLocalizedMessage();
    }

    /**
     * @return the detail
     */
    public final OWSException getDetail() {
        return detail;
    }

    /**
     * @return the subcodes
     */
    public final String[] getSubcodes() {
        return subcodes;
    }

}
