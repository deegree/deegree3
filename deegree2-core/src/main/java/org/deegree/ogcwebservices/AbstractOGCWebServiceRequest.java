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

package org.deegree.ogcwebservices;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the abstract base class for all requests to OGC Web Services (OWS).
 * <p>
 * Contains utility methods to ease the extraction of values from KVP parameter maps.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class AbstractOGCWebServiceRequest implements OGCWebServiceRequest, Serializable {


    private static final long serialVersionUID = -8818680896982952739L;

    private Map<String, String> vendorSpecificParameter;

    private String id;

    private String version;

    /**
     * returns the ID of a request
     */
    public String getId() {
        return id;
    }

    /**
     * returns the requested service version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Creates a new instance of <code>AbstractOGCWebServiceRequest</code>.
     *
     * @param version
     * @param id
     * @param vendorSpecificParameter
     */
    protected AbstractOGCWebServiceRequest( String version, String id, Map<String, String> vendorSpecificParameter ) {
        this.id = id;
        if ( vendorSpecificParameter != null ) {
            this.vendorSpecificParameter = vendorSpecificParameter;
        } else {
            this.vendorSpecificParameter = new HashMap<String, String>();
        }
        this.version = version;
    }

    /**
     * Finally, the requests allow for optional vendor-specific parameters (VSPs) that will enhance
     * the results of a request. Typically, these are used for private testing of non-standard
     * functionality prior to possible standardization. A generic client is not required or expected
     * to make use of these VSPs.
     */
    public Map<String, String> getVendorSpecificParameters() {
        return vendorSpecificParameter;
    }

    /**
     * Finally, the requests allow for optional vendor-specific parameters (VSPs) that will enhance
     * the results of a request. Typically, these are used for private testing of non-standard
     * functionality prior to possible standardization. A generic client is not required or expected
     * to make use of these VSPs.
     */
    public String getVendorSpecificParameter( String name ) {
        return vendorSpecificParameter.get( name );
    }

    /**
     * returns the URI of a HTTP GET request. If the request doesn't support HTTP GET a
     * <tt>WebServiceException</tt> will be thrown
     *
     */
    public String getRequestParameter()
                            throws OGCWebServiceException {
        throw new OGCWebServiceException( "HTTP GET isn't supported" );
    }

    /**
     * Extracts a <code>String</code> parameter value from the given parameter map. If the given
     * parameter does not exist, the also submitted default value is returned.
     *
     * @param name
     *            name of the parameter to be looked up
     * @param kvp
     *            must contain Strings as keys and Strings as values
     * @param defaultValue
     *            default value to be used if parameter is missing
     * @return parameter value
     */
    protected static String getParam( String name, Map<String, String> kvp, String defaultValue ) {
        String value = kvp.remove( name );
        if ( value == null ) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Extracts a <code>String</code> list from the given parameter map. The single values are
     * separated by commas. If the given parameter does not exist, the also submitted default value
     * is returned.
     *
     * @param name
     *            name of the parameter to be looked up
     * @param kvp
     *            must contain Strings as keys and Strings as values
     * @param defaultValue
     *            default value to be used if parameter is missing
     * @return parameter value
     */
    protected static String[] getParamValues( String name, Map<String, String> kvp, String defaultValue ) {
        String value = kvp.get( name );
        if ( value == null ) {
            value = defaultValue;
        }
        return value.split( "," );
    }

    /**
     * Extracts a <code>String</code> parameter value from the given parameter map. Generates
     * exceptions with descriptive messages, if the parameter does not exist in the <code>Map</code>.
     *
     * @param name
     *            name of the parameter to be looked up
     * @param kvp
     *            must contain Strings as keys and Strings as values
     * @return parameter value
     * @throws MissingParameterValueException
     */
    protected static String getRequiredParam( String name, Map<String, String> kvp )
                            throws MissingParameterValueException {
        String value = kvp.remove( name );
        if ( value == null ) {
            throw new MissingParameterValueException( "Cannot create OGC web service request. Required parameter '"
                                                      + name + "' is missing.", name );
        }
        return value;
    }

    /**
     * Extracts an <code>int</code> parameter value from the given parameter map. If the given
     * parameter does not exist, the also submitted default value is returned.
     *
     * @param name
     *            name of the parameter to be looked up
     * @param kvp
     *            must contain Strings as keys and Strings as values
     * @param defaultValue
     *            default value to be used if parameter is missing
     * @return parameter value
     * @throws InvalidParameterValueException
     */
    protected static int getParamAsInt( String name, Map<String, String> kvp, int defaultValue )
                            throws InvalidParameterValueException {
        int value = defaultValue;
        String paramValue = kvp.get( name );
        if ( paramValue != null ) {
            try {
                value = Integer.parseInt( paramValue );
            } catch ( NumberFormatException e ) {
                throw new InvalidParameterValueException( "Value '" + paramValue + "' for parameter '" + name
                                                          + "' is invalid. Must be of type integer." );
            }
        }
        return value;
    }

    @Override
    public String toString() {
        String ret = "vendorSpecificParameter = " + vendorSpecificParameter + "\n";
        ret += ( "id = " + id + "\n" );
        ret += ( "version = " + version );
        return ret;
    }
}
