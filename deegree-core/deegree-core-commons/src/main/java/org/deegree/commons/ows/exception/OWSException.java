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
package org.deegree.commons.ows.exception;

import java.util.Collections;
import java.util.List;

import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;

/**
 * OGC Web Service Exception for all OGC service related errors.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWSException extends Exception {

    /** */
    private static final long serialVersionUID = 4274635657976045225L;

    // OWS GetCapabilities exceptions
    /**
     * a required parameter is missing
     */
    public static final String MISSING_PARAMETER_VALUE = "MissingParameterValue";

    /**
     * Server processing failed
     */
    public static final String OPERATION_PROCESSING_FAILED = "OperationProcessingFailed";

    /**
     * the parameter value is invalid
     */
    public static final String INVALID_PARAMETER_VALUE = "InvalidParameterValue";

    /**
     * A Transaction (see Clause 15) has attempted to insert or change the value of a data component in a way that
     * violates the schema of the feature. (WFS 2.0.0, Table 3 - WFS exception codes)
     */
    public static final String INVALID_VALUE = "InvalidValue";

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

    /**
     * what the text says
     */
    public static final String LOCK_HAS_EXPIRED = "LockHasExpired";

    /**
     * the layer is not queryable by get feature info
     */
    public static final String LAYER_NOT_QUERYABLE = "LayerNotQueryable";

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

    /**
     * exception code for all not known exceptions
     */
    public static final String NO_APPLICABLE_CODE = "NoApplicableCode";

    /**
     * exception code indicating an HTTP 404 error
     */
    public static final String NOT_FOUND = "NotFound";

    /**
     * the identifier specified for a stored query expression is a duplicate.
     */
    public static final String DUPLICATE_STORED_QUERY_ID_VALUE =  "DuplicateStoredQueryIdValue";

    private final String exceptionCode;

    private final String locator;

    private final List<String> messages;

    /**
     * Create a new OWSException.
     * 
     * @param message
     *            the exception text
     * @param exceptionCode
     *            defining a machine readable code.
     */
    public OWSException( String message, String exceptionCode ) {
        super( message );
        this.exceptionCode = exceptionCode;
        this.locator = "";
        messages = Collections.singletonList( message );
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
        super( message, cause );
        this.exceptionCode = exceptionCode;
        this.locator = "";
        messages = Collections.singletonList( message );
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
        super( message );
        this.exceptionCode = exceptionCode;
        this.locator = locator;
        messages = Collections.singletonList( message );
    }

    public OWSException( List<String> messages, String exceptionCode, String locator ) {
        super( StringUtils.concat( messages, ";" ) );
        this.messages = messages;
        this.exceptionCode = exceptionCode;
        this.locator = locator;
    }

    /**
     * Creates a new {@link OWSException} from an {@link InvalidParameterValueException}.
     * 
     * @param cause
     *            causing exception
     */
    public OWSException( InvalidParameterValueException cause ) {
        super( cause.getMessage() );
        this.exceptionCode = INVALID_PARAMETER_VALUE;
        locator = cause.getName();
        messages = Collections.singletonList( cause.getMessage() );
    }

    /**
     * Creates a new {@link OWSException} from an {@link MissingParameterException}.
     * 
     * @param cause
     *            causing exception
     */
    public OWSException( MissingParameterException cause ) {
        super( cause.getMessage() );
        this.exceptionCode = MISSING_PARAMETER_VALUE;
        locator = cause.getName();
        messages = Collections.singletonList( cause.getMessage() );
    }

    /**
     * @return the locator
     */
    public final String getLocator() {
        return locator;
    }

    public final String getExceptionCode() {
        return exceptionCode;
    }

    public final List<String> getMessages() {
        return messages;
    }
}
