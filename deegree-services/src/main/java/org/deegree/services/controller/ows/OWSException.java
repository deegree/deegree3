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
package org.deegree.services.controller.ows;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.services.controller.exception.ControllerException;

/**
 * OGC Web Service Exception for all OGC related errors.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class OWSException extends ControllerException {

    /** */
    private static final long serialVersionUID = 4274635657976045225L;

    // OWS GetCapabilities exceptions
    /**
     * a required parameter is missing
     */
    public static final String MISSING_PARAMETER_VALUE = "MissingParameterValue";

    /**
     * the parameter value is invalid
     */
    public static final String INVALID_PARAMETER_VALUE = "InvalidParameterValue";

    /**
     * the parameter value of the format parameter is invalid
     */
    public static final String INVALID_FORMAT = "InvalidFormat";

    /**
     * the layer is not defined
     */
    public static final String LAYER_NOT_DEFINED = "LayerNotDefined";

    /**
     * what the text says
     */
    public static final String STYLE_NOT_DEFINED = "StyleNotDefined";

    /**
     * what the text says
     */
    public static final String INVALID_SRS = "InvalidSRS";

    /***/
    public static final String INVALID_CRS = "InvalidCRS";

    /***/
    public static final String INVALID_POINT = "InvalidPoint";

    /**
     * server doesn't support any of the requested AcceptVersions
     */
    public static final String VERSION_NEGOTIATION_FAILED = "VersionNegotiationFailed";

    /**
     * updateSequence value is greater than the current value
     */
    public static final String INVALID_UPDATE_SEQUENCE = "InvalidUpdateSequence";

    /**
     * what the text says
     */
    public static final String CURRENT_UPDATE_SEQUENCE = "CurrentUpdateSequence";

    // OSW standard exceptions (+GetCapabilities exceptions)
    /**
     * the requested operation is not supported
     */
    public static final String OPERATION_NOT_SUPPORTED = "OperationNotSupported";

    /**
     * the requested option is not supported
     */
    public static final String OPTION_NOT_SUPPORTED = "OptionNotSupported";

    /**
     * the date is not parsable
     */
    public static final String INVALID_DATE = "InvalidDate";

    private final String locator;

    /**
     * Create a new OWSException.
     * 
     * @param message
     *            the exception text
     * @param exceptionCode
     *            defining a machine readable code.
     */
    public OWSException( String message, String exceptionCode ) {
        super( message, exceptionCode );
        this.locator = "";
    }

    /**
     * Create a new OWSException.
     * 
     * @param message
     *            the exception text
     * @param cause
     *            of this exception to happen.
     * @param exceptionCode
     *            defining a machine readable code.
     */
    public OWSException( String message, Throwable cause, String exceptionCode ) {
        super( message, cause, exceptionCode );
        this.locator = "";
    }

    /**
     * Create a new OWSException.
     * 
     * @param message
     *            the exception text
     * @param exceptionCode
     *            defining a machine readable code.
     * @param locator
     *            the exception location
     */
    public OWSException( String message, String exceptionCode, String locator ) {
        super( message, exceptionCode );
        this.locator = locator;
    }

    /**
     * Creates a new {@link OWSException} from an {@link InvalidParameterValueException}.
     * 
     * @param cause
     *            causing exception
     */
    public OWSException( InvalidParameterValueException cause ) {
        super( cause.getMessage(), INVALID_PARAMETER_VALUE );
        locator = cause.getName();
    }

    /**
     * Creates a new {@link OWSException} from an {@link MissingParameterException}.
     * 
     * @param cause
     *            causing exception
     */
    public OWSException( MissingParameterException cause ) {
        super( cause.getMessage(), MISSING_PARAMETER_VALUE );
        locator = cause.getName();
    }

    /**
     * @return the locator
     */
    public final String getLocator() {
        return locator;
    }

}
